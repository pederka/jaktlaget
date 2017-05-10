package net.ddns.peder.jaktlaget.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import net.ddns.peder.jaktlaget.AsyncTasks.DataSynchronizer;
import net.ddns.peder.jaktlaget.Constants;
import net.ddns.peder.jaktlaget.MainActivity;
import net.ddns.peder.jaktlaget.R;
import net.ddns.peder.jaktlaget.database.PositionsDbHelper;
import net.ddns.peder.jaktlaget.interfaces.OnSyncComplete;
import net.ddns.peder.jaktlaget.utils.LocationHistoryUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by peder on 3/23/17.
 */

public class LocationService extends Service {
    private LocationManager mLocationManager = null;
    private int LOCATION_INTERVAL;
    private static final float LOCATION_DISTANCE = 10f;
    private static final String tag = "LocationService";
    private String userId;
    private String teamId;
    private List<LatLng> myLocationHistory;
    private Map<String, List<LatLng>> teamLocationHistory = new HashMap<>();

    private class LocationListener implements android.location.LocationListener, OnSyncComplete {
        private Location mLastLocation;

        @Override
        public void onSyncComplete(int result) {
            if (result == DataSynchronizer.SUCCESS) {
                PositionsDbHelper positionsDbHelper = new PositionsDbHelper(getApplicationContext());
                SQLiteDatabase posdb = positionsDbHelper.getReadableDatabase();
                final String[] PROJECTION = {
                        PositionsDbHelper.COLUMN_NAME_ID,
                        PositionsDbHelper.COLUMN_NAME_SHOWED,
                        PositionsDbHelper.COLUMN_NAME_USER,
                        PositionsDbHelper.COLUMN_NAME_LATITUDE,
                        PositionsDbHelper.COLUMN_NAME_LONGITUDE
                };

                String selection = PositionsDbHelper.COLUMN_NAME_SHOWED + " = ?";
                String[] selectionArgs = {"1"};
                final Cursor cursor = posdb.query(PositionsDbHelper.TABLE_NAME,
                        PROJECTION,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        null);
                while (cursor.moveToNext()) {
                    Float latitude = cursor.getFloat(cursor.getColumnIndexOrThrow(
                            PositionsDbHelper.COLUMN_NAME_LATITUDE));
                    Float longitude = cursor.getFloat(cursor.getColumnIndexOrThrow(
                            PositionsDbHelper.COLUMN_NAME_LONGITUDE));
                    LatLng pos = new LatLng(latitude, longitude);
                    String user = cursor.getString(cursor.getColumnIndexOrThrow(
                            PositionsDbHelper.COLUMN_NAME_USER));
                    addToTeamLocationHistory(user, pos);
                }
                LocationHistoryUtil.saveTeamLocationHistoryToPreferences(getApplicationContext(),
                                            teamLocationHistory);
                cursor.close();
            }
        }

        private void addToTeamLocationHistory(String user, LatLng latLng) {
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
            DataSynchronizer dataSynchronizer = new DataSynchronizer(getApplicationContext(),
                                                                            this,
                                                                            false);
            myLocationHistory.add(new LatLng(location.getLatitude(), location.getLongitude()));
            Log.i(tag, "Syncing after location changed");
            LocationHistoryUtil.saveLocationHistoryToPreferences(getApplicationContext(),
                                                                            myLocationHistory);
            dataSynchronizer.execute();
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

        myLocationHistory = LocationHistoryUtil.loadLocationHistoryFromPreferences(
                                                                getApplicationContext());
        teamLocationHistory = LocationHistoryUtil.loadTeamLocationHistoryFromPreferences(
                                                    getApplicationContext());
        if (myLocationHistory == null) {
            myLocationHistory = new ArrayList<>();
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                                                                        this);
        userId = sharedPreferences.getString(Constants.SHARED_PREF_USER_ID, Constants.DEFAULT_USER_ID);
        teamId = sharedPreferences.getString(Constants.SHARED_PREF_TEAM_ID, Constants.DEFAULT_TEAM_ID);
        LOCATION_INTERVAL = 60000*Integer.parseInt(sharedPreferences.getString("pref_syncInterval",
                Long.toString(Constants.DEFAULT_UPDATE_INTERVAL)));

        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new Notification.Builder(this)
                .setContentTitle(getText(R.string.notification_title))
                .setContentText(getText(R.string.notification_message))
                .setSmallIcon(R.mipmap.status_bar)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
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