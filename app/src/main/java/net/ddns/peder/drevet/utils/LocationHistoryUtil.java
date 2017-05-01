package net.ddns.peder.drevet.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.ddns.peder.drevet.Constants;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class LocationHistoryUtil {

    private static String tag = "LocationHistoryUtil";

    static public void saveLocationHistoryToPreferences(Context context, List<LatLng> myLocationHistory) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(myLocationHistory);
        editor.putString(Constants.SHARED_PREF_LOCATION_HISTORY, json);
        editor.apply();
        Log.d(tag, "Saved "+myLocationHistory.size()+" locations to history");
    }

    static public List<LatLng> loadLocationHistoryFromPreferences(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = sharedPrefs.getString(Constants.SHARED_PREF_LOCATION_HISTORY, null);
        Type type = new TypeToken<ArrayList<LatLng>>() {}.getType();
        List<LatLng> myLocationHistory = gson.fromJson(json,type);
        Log.d(tag, "Loaded "+myLocationHistory.size()+" locations from history");
        return myLocationHistory;
    }
}
