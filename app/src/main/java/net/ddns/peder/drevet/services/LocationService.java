package net.ddns.peder.drevet.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import net.ddns.peder.drevet.AsyncTasks.PositionSyncronizer;
import net.ddns.peder.drevet.Constants;
import net.ddns.peder.drevet.MainActivity;
import net.ddns.peder.drevet.R;

/**
 * Created by peder on 3/23/17.
 */

public class LocationService extends Service {
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    private static final String tag = "LocationService";
    private String userId;
    private String teamId;

    private class LocationListener implements android.location.LocationListener {
        private Location mLastLocation;

        public LocationListener(String provider)
        {
            Log.e(tag, "LocationListener " + provider);
            mLastLocation = new Location(provider);

        }

        @Override
        public void onLocationChanged(Location location)
        {
            Log.e(tag, "onLocationChanged: " + location);
            mLastLocation.set(location);
            // Save location to shared preferences
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(
                                                                      getApplicationContext());
            preferences.edit().putFloat(Constants.SHARED_PREF_LAT,
                                                            (float)location.getLatitude()).apply();
            preferences.edit().putFloat(Constants.SHARED_PREF_LON,
                                                            (float)location.getLongitude()).apply();
            preferences.edit().putLong(Constants.SHARED_PREF_TIME,
                                                            System.currentTimeMillis()).apply();
            PositionSyncronizer posSync = new PositionSyncronizer(getApplicationContext());
            posSync.execute();
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e(tag, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.e(tag, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.e(tag, "onStatusChanged: " + provider);
        }
    }

    LocationListener mLocationListener = new LocationListener(LocationManager.GPS_PROVIDER);

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.e(tag, "onStartCommand");
        super.onStartCommand(intent, flags, startId);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                                                                        this);
        userId = sharedPreferences.getString(Constants.SHARED_PREF_USER_ID, Constants.DEFAULT_USER_ID);
        teamId = sharedPreferences.getString(Constants.SHARED_PREF_TEAM_ID, Constants.DEFAULT_TEAM_ID);

        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new Notification.Builder(this)
                .setContentTitle(getText(R.string.notification_title))
                .setContentText(getText(R.string.notification_message))
                .setSmallIcon(R.drawable.ic_map_black_24dp)
                .setContentIntent(pendingIntent)
                .setTicker(getText(R.string.ticker_text))
                .build();

        startForeground(Constants.NOTIFICATION_ID, notification);

        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        Log.e(tag, "onCreate");
        initializeLocationManager();

        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListener);
        } catch (java.lang.SecurityException ex) {
            Log.i(tag, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(tag, "gps provider does not exist " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy()
    {
        Log.e(tag, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            try {
                mLocationManager.removeUpdates(mLocationListener);
            } catch (Exception ex) {
                Log.i(tag, "fail to remove location listner, ignore", ex);
            }
        }
    }

    private void initializeLocationManager() {
        Log.e(tag, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

}
