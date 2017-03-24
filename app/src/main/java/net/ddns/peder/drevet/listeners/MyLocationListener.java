package net.ddns.peder.drevet.listeners;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.preference.PreferenceManager;

import net.ddns.peder.drevet.Constants;


/**
 * Created by peder on 3/24/17.
 */

public class MyLocationListener implements LocationListener {

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
