package net.ddns.peder.drevet;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.CompoundButton;
import android.widget.Toast;

import net.ddns.peder.drevet.database.AWSSyncTask;
import net.ddns.peder.drevet.fragments.LandmarksFragment;
import net.ddns.peder.drevet.fragments.MapFragment;
import net.ddns.peder.drevet.fragments.SettingsFragment;
import net.ddns.peder.drevet.fragments.TeamFragment;
import net.ddns.peder.drevet.fragments.TeamLandmarksFragment;
import net.ddns.peder.drevet.fragments.TeamManagementFragment;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, MapFragment.OnFragmentInteractionListener,
        TeamFragment.OnFragmentInteractionListener, SettingsFragment.OnFragmentInteractionListener,
        LandmarksFragment.OnFragmentInteractionListener, TeamLandmarksFragment.OnFragmentInteractionListener,
        TeamManagementFragment.OnFragmentInteractionListener {

    private int MY_PERMISSIONS_REQUEST;
    private AWSSyncTask syncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        syncTask = new AWSSyncTask(getApplicationContext());

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Run switch
        final SwitchCompat runSwitch = (SwitchCompat) findViewById(R.id.run_switch);
        runSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(getApplicationContext(), R.string.run_start,
                                                            Toast.LENGTH_SHORT).show();
                    syncTask.execute();
                }
                else {
                    Toast.makeText(getApplicationContext(), R.string.run_stop,
                                                                        Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Request permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST);

            }

        displaySelectedScreen(R.id.nav_map);
        //PreferenceManager.setDefaultValues(this, R.xml.fragment_settings, false);
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
            case R.id.nav_team_landmarks:
                fragment = new TeamLandmarksFragment();
                break;
            case R.id.nav_team_manage:
                fragment = new TeamManagementFragment();
                break;
            case R.id.nav_landmarks:
                fragment = new LandmarksFragment();
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
