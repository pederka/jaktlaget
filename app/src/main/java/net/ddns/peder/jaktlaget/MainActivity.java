package net.ddns.peder.jaktlaget;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import net.ddns.peder.jaktlaget.AsyncTasks.DataSynchronizer;
import net.ddns.peder.jaktlaget.fragments.AboutFragment;
import net.ddns.peder.jaktlaget.fragments.AllLandmarksFragment;
import net.ddns.peder.jaktlaget.fragments.AllTeamFragment;
import net.ddns.peder.jaktlaget.fragments.LandmarksFragment;
import net.ddns.peder.jaktlaget.fragments.MapFragment;
import net.ddns.peder.jaktlaget.fragments.SettingsFragment;
import net.ddns.peder.jaktlaget.fragments.TeamFragment;
import net.ddns.peder.jaktlaget.fragments.TeamInfoFragment;
import net.ddns.peder.jaktlaget.fragments.TeamLandmarksFragment;
import net.ddns.peder.jaktlaget.fragments.TeamManagementFragment;
import net.ddns.peder.jaktlaget.services.LocationService;
import net.ddns.peder.jaktlaget.utils.LocationHistoryUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.ddns.peder.jaktlaget.Constants.SYNC_DELAY_ACTIVITY;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, MapFragment.OnFragmentInteractionListener,
        TeamFragment.OnFragmentInteractionListener, SettingsFragment.OnFragmentInteractionListener,
        LandmarksFragment.OnFragmentInteractionListener, TeamLandmarksFragment.OnFragmentInteractionListener,
        TeamManagementFragment.OnFragmentInteractionListener, AllLandmarksFragment.OnFragmentInteractionListener,
        AllTeamFragment.OnFragmentInteractionListener, TeamInfoFragment.OnFragmentInteractionListener,
        AboutFragment.OnFragmentInteractionListener {

    private final static int MY_PERMISSIONS_REQUEST = 1654;
    private final static int ACTIVATE_PERMISSION_REQUEST = 1655;
    private LocationListener locationListener;
    private LocationManager locationManager;
    private Handler mHandler;
    public CameraPosition cameraPosition;
    private boolean runningService;
    private Context mContext;
    private TextView activeText;
    private TextView action_bar_title;
    private static final String tag = "MainActivity";
    private SwitchCompat runSwitch;

    private List<LatLng> myLocationHistory;
    private Map<String, List<LatLng>> teamLocationHistory = new HashMap<>();

    private BroadcastReceiver br;
    public static final String ACTION_SERVICE = "net.ddns.peder.jaktlaget.ACTION_SERVICE";

    private class ServiceBroadcastReceiver extends BroadcastReceiver {
        private final String brtag = "BroadcastReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            String extra = intent.getStringExtra("ACTION");
            if (extra.equals("STOP")) {
                Log.d(brtag, "Action stop received");
                stopService(new Intent(getApplicationContext(), LocationService.class));
                goInactive();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        br = new ServiceBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_SERVICE);
        this.registerReceiver(br, intentFilter);


        mContext = this;

        mHandler = new Handler();

        if (teamLocationHistory == null) {
            teamLocationHistory = new HashMap<>();
        }

        if (myLocationHistory == null) {
            myLocationHistory = new ArrayList<>();
        }

        action_bar_title = (TextView) findViewById(R.id.action_bar_title);

        // Any running service should be stopped when the app is opened
        stopService(new Intent(getApplicationContext(), LocationService.class));

        // If position sharing is active, start periodic sync
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(
                getApplicationContext());
        runningService = prefs.getBoolean(Constants.SHARED_PREF_RUNNING, false);
        mHandler.removeCallbacks(syncData);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        activeText = (TextView)findViewById(R.id.active_text);

        // Run switch
        runSwitch = (SwitchCompat) findViewById(R.id.run_switch);
        runSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    goInactive();
                } else {
                    goActive();
                }
            }
        });
        if (runningService) {
            activeText.setText(getString(R.string.actionbar_active));
            runSwitch.setChecked(true);
            mHandler.postDelayed(syncData, SYNC_DELAY_ACTIVITY);
        } else {
            activeText.setText(getString(R.string.actionbar_inactive));
            runSwitch.setChecked(false);
        }

        // Request permissions
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST);
        }

        startPositionUpdates();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String userId = sharedPreferences.getString(Constants.SHARED_PREF_USER_ID,
                                                                        Constants.DEFAULT_USER_ID);
        if (userId.equals(Constants.DEFAULT_USER_ID)) {
            displaySelectedScreen(R.id.nav_team_manage);
        } else {
            displaySelectedScreen(R.id.nav_map);
        }


        //PreferenceManager.setDefaultValues(this, R.xml.fragment_settings, false);
    }

    private void startPositionUpdates() {
         if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            locationListener = new MyLocationListener(mContext);
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, Constants.ACTIVITY_GPS_UPDATE_TIME,
                                                Constants.ACTIVITY_GPS_DISTANCE, locationListener);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startPositionUpdates();
                }
                break;
            }
            case ACTIVATE_PERMISSION_REQUEST: {
                 if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                      final SwitchCompat runSwitch = (SwitchCompat) findViewById(R.id.run_switch);
                      runSwitch.setChecked(true);
                      clearMyLocationHistory();
                      activeText.setText(getString(R.string.actionbar_active));
                      mHandler.postDelayed(syncData, SYNC_DELAY_ACTIVITY);
                      runningService = true;
                      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                      prefs.edit().putBoolean(Constants.SHARED_PREF_RUNNING, true).apply();
                      Toast.makeText(getApplicationContext(), R.string.run_stop,
                      Toast.LENGTH_SHORT).show();
                 }
                 break;
            }
        }
    }

    public boolean isActive() {
        return runningService;
    }

    public void goInactive() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        activeText.setText(getString(R.string.actionbar_inactive));
        mHandler.removeCallbacks(syncData);
        runningService = false;
        runSwitch.setChecked(false);

        prefs.edit().putBoolean(Constants.SHARED_PREF_RUNNING, false).apply();
        Toast.makeText(getApplicationContext(), R.string.run_start,
        Toast.LENGTH_SHORT).show();
    }

    public void goActive() {
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
         String userid = prefs.getString(Constants.SHARED_PREF_USER_ID,
                                                             Constants.DEFAULT_USER_ID);
         String teamid = prefs.getString(Constants.SHARED_PREF_TEAM_ID,
                                                             Constants.DEFAULT_TEAM_ID);
         String code = prefs.getString(Constants.SHARED_PREF_TEAM_CODE, "");
         if (userid.equals(Constants.DEFAULT_USER_ID)
                                         || teamid.equals(Constants.DEFAULT_TEAM_ID)
                             || code.equals("")) {
                                      Toast.makeText(getApplicationContext(), R.string.cant_start,
                     Toast.LENGTH_SHORT).show();
              runSwitch.setChecked(false);
              // Switch to team management fragment
              Fragment fragment = new TeamManagementFragment();
              FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
              ft.replace(R.id.content_frame, fragment);
              ft.commit();
         }
         else if (ContextCompat.checkSelfPermission((Activity) mContext,
                     Manifest.permission.ACCESS_FINE_LOCATION)
                     == PackageManager.PERMISSION_GRANTED) {

             clearMyLocationHistory();
             activeText.setText(getString(R.string.actionbar_active));
             mHandler.postDelayed(syncData, SYNC_DELAY_ACTIVITY);
             runningService = true;
             runSwitch.setChecked(true);
             prefs.edit().putBoolean(Constants.SHARED_PREF_RUNNING, true).apply();
             Toast.makeText(getApplicationContext(), R.string.run_stop,
                     Toast.LENGTH_SHORT).show();
         }
         else {
             // Request permission
             ActivityCompat.requestPermissions((Activity) mContext,
                     new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                     ACTIVATE_PERMISSION_REQUEST);
             runSwitch.setChecked(false);
         }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }

    public Map<String, List<LatLng>> getTeamLocationHistory() {
        return teamLocationHistory;
    }

    public void addToTeamLocationHistory(String user, LatLng latLng) {
        if (teamLocationHistory == null) {
            teamLocationHistory = new HashMap<>();
        }
        if (teamLocationHistory.containsKey(user)) {
            List<LatLng> userHistory = teamLocationHistory.get(user);
            LatLng last = userHistory.get(userHistory.size() - 1);
            // Only add to history of location has changed
            if (last.latitude != latLng.latitude || last.longitude != latLng.longitude) {
                userHistory.add(latLng);
                teamLocationHistory.put(user, userHistory);
            }
        } else {
            List<LatLng> userHistory = new ArrayList<>();
            userHistory.add(latLng);
            teamLocationHistory.put(user, userHistory);
        }
    }

    public void clearTeamLocationHistory() {
        teamLocationHistory.clear();
    }

    public List<LatLng> getMyLocationHistory() {
        return myLocationHistory;
    }

    public void addToMyLocationHistory(LatLng latLng) {
        myLocationHistory.add(latLng);
    }

    public void clearMyLocationHistory() {
        myLocationHistory.clear();
    }

    private Runnable syncData = new Runnable() {
        @Override
        public void run() {
            DataSynchronizer dataSynchronizer = new DataSynchronizer(getApplicationContext(), null,
                                                                                        false);
            dataSynchronizer.execute();
            mHandler.postDelayed(this, SYNC_DELAY_ACTIVITY);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        stopService(new Intent(getApplicationContext(), LocationService.class));
        mHandler.removeCallbacks(syncData);
        myLocationHistory = LocationHistoryUtil.loadLocationHistoryFromPreferences(this);
        teamLocationHistory = LocationHistoryUtil.loadTeamLocationHistoryFromPreferences(this);
        if (myLocationHistory == null) {
            myLocationHistory = new ArrayList<>();
        }
        if (mHandler != null && runningService) {
            mHandler.postDelayed(syncData, SYNC_DELAY_ACTIVITY);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED && locationManager != null) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, Constants.ACTIVITY_GPS_UPDATE_TIME,
                                                Constants.ACTIVITY_GPS_DISTANCE, locationListener);

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(br);
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(syncData);
        LocationHistoryUtil.saveLocationHistoryToPreferences(this, myLocationHistory);
        LocationHistoryUtil.saveTeamLocationHistoryToPreferences(this, teamLocationHistory);
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
        if (runningService) {
            startService(new Intent(getApplicationContext(), LocationService.class));
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        displaySelectedScreen(item.getItemId());
        return true;
    }

    private void displaySelectedScreen(int itemId) {

        //creating fragment object
        Fragment fragment = null;

        //initializing the fragment object which is selected
        switch (itemId) {
            case R.id.nav_map:
                fragment = new MapFragment();
                break;
            case R.id.nav_team_manage:
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                String code = sharedPreferences.getString(Constants.SHARED_PREF_TEAM_CODE, "");
                if (code.equals("")) {
                    fragment = new TeamManagementFragment();
                } else {
                    fragment = new AllTeamFragment();
                }
                break;
            case R.id.nav_landmarks:
                fragment = new AllLandmarksFragment();
                break;
            case R.id.nav_settings:
                fragment = new SettingsFragment();
                break;
            case R.id.nav_about:
                fragment = new AboutFragment();
                break;
        }

        //replacing the fragment
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    @Override
    public void onFragmentInteraction(Uri uri){
        //you can leave it empty
    }

    private class MyLocationListener implements LocationListener {

        SharedPreferences preferences;

        public MyLocationListener(Context context) {
            preferences = PreferenceManager.getDefaultSharedPreferences(
                    context);

        }

        @Override
        public void onLocationChanged(Location location) {
            preferences.edit().putFloat(Constants.SHARED_PREF_LAT,
                                                            (float)location.getLatitude()).apply();
            preferences.edit().putFloat(Constants.SHARED_PREF_LON,
                                                            (float)location.getLongitude()).apply();
            preferences.edit().putLong(Constants.SHARED_PREF_TIME,
                                                            System.currentTimeMillis()).apply();
            if (runningService) {
                addToMyLocationHistory(new LatLng(location.getLatitude(), location.getLongitude()));
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

    }
    public void setActionBarTitle(String title){
            action_bar_title.setText(title);
    }


}
