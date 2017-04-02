package net.ddns.peder.drevet;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.model.CameraPosition;

import net.ddns.peder.drevet.AsyncTasks.DataSynchronizer;
import net.ddns.peder.drevet.fragments.AllLandmarksFragment;
import net.ddns.peder.drevet.fragments.LandmarksFragment;
import net.ddns.peder.drevet.fragments.MapFragment;
import net.ddns.peder.drevet.fragments.SettingsFragment;
import net.ddns.peder.drevet.fragments.TeamFragment;
import net.ddns.peder.drevet.fragments.TeamLandmarksFragment;
import net.ddns.peder.drevet.fragments.TeamManagementFragment;
import net.ddns.peder.drevet.listeners.MyLocationListener;
import net.ddns.peder.drevet.services.LocationService;

import static net.ddns.peder.drevet.Constants.SYNC_DELAY_ACTIVITY;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, MapFragment.OnFragmentInteractionListener,
        TeamFragment.OnFragmentInteractionListener, SettingsFragment.OnFragmentInteractionListener,
        LandmarksFragment.OnFragmentInteractionListener, TeamLandmarksFragment.OnFragmentInteractionListener,
        TeamManagementFragment.OnFragmentInteractionListener, AllLandmarksFragment.OnFragmentInteractionListener {

    private int MY_PERMISSIONS_REQUEST;
    private LocationListener locationListener;
    private LocationManager locationManager;

    private Handler mHandler;

    public CameraPosition cameraPosition;

    private boolean runningService;
    private Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mContext = this;

        // Any running service should be stopped when the app is opened
        stopService(new Intent(getApplicationContext(), LocationService.class));

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Run switch
        final SwitchCompat runSwitch = (SwitchCompat) findViewById(R.id.run_switch);
        runSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    runningService = false;
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(
                            getApplicationContext());
                    prefs.edit().putBoolean(Constants.SHARED_PREF_RUNNING, false).apply();
                    Toast.makeText(getApplicationContext(), R.string.run_start,
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Request permission
                    if (ContextCompat.checkSelfPermission((Activity) mContext,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions((Activity) mContext,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                MY_PERMISSIONS_REQUEST);

                    }
                    runningService = true;
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(
                            getApplicationContext());
                    prefs.edit().putBoolean(Constants.SHARED_PREF_RUNNING, true).apply();
                    Toast.makeText(getApplicationContext(), R.string.run_stop,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Sync switch
        final ImageButton syncButton = (ImageButton) findViewById(R.id.sync_button);
        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataSynchronizer dataSynchronizer = new DataSynchronizer(MainActivity.this);
                dataSynchronizer.execute();
            }
        });

        // Request permissions
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST);

        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST);

        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            locationListener = new MyLocationListener(mContext);
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, Constants.ACTIVITY_GPS_UPDATE_TIME,
                                                Constants.ACTIVITY_GPS_DISTANCE, locationListener);

        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String userId = sharedPreferences.getString(Constants.SHARED_PREF_USER_ID,
                                                                        Constants.DEFAULT_USER_ID);
        if (userId.equals(Constants.DEFAULT_USER_ID)) {
            displaySelectedScreen(R.id.nav_team_manage);
        } else {
            displaySelectedScreen(R.id.nav_map);
        }

        mHandler = new Handler();
        mHandler.postDelayed(syncData, SYNC_DELAY_ACTIVITY);

        //PreferenceManager.setDefaultValues(this, R.xml.fragment_settings, false);
    }

    private Runnable syncData = new Runnable() {
        @Override
        public void run() {
            DataSynchronizer dataSynchronizer = new DataSynchronizer(mContext);
            dataSynchronizer.execute();
            mHandler.postDelayed(this, SYNC_DELAY_ACTIVITY);
        }
    };


    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(syncData);
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
        if (runningService) {
            startService(new Intent(getApplicationContext(), LocationService.class));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        stopService(new Intent(getApplicationContext(), LocationService.class));
        if (mHandler != null) {
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
    public void onStart() {
        super.onStart();
        stopService(new Intent(getApplicationContext(), LocationService.class));
        if (mHandler != null) {
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
    public void onStop() {
        super.onStop();
        mHandler.removeCallbacks(syncData);
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
        if (runningService) {
            startService(new Intent(getApplicationContext(), LocationService.class));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(syncData);
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
            case R.id.nav_team:
                fragment = new TeamFragment();
                break;
            case R.id.nav_team_manage:
                fragment = new TeamManagementFragment();
                break;
            case R.id.nav_landmarks:
                fragment = new AllLandmarksFragment();
                break;
            case R.id.nav_settings:
                fragment = new SettingsFragment();
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
}
