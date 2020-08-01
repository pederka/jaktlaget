package net.ddns.peder.jaktlaget.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.ddns.peder.jaktlaget.Constants;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        String json = sharedPrefs.getString(Constants.SHARED_PREF_LOCATION_HISTORY, "");
        Type type = new TypeToken<ArrayList<LatLng>>() {}.getType();
        List<LatLng> myLocationHistory = gson.fromJson(json,type);
        if (myLocationHistory != null) {
            Log.d(tag, "Loaded " + myLocationHistory.size() + " locations from history");
        }
        return myLocationHistory;
    }

    static public void clearLocationHistory(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(Constants.SHARED_PREF_LOCATION_HISTORY, "");
        editor.apply();
    }

    static public void saveTeamLocationHistoryToPreferences(Context context,
                                                   Map<String, List<LatLng>> teamLocationHistory) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(teamLocationHistory);
        editor.putString(Constants.SHARED_PREF_TEAM_LOCATION_HISTORY, json);
        editor.apply();
        if (teamLocationHistory != null) {
            Log.d(tag, "Saved " + teamLocationHistory.size() + " team member's location histories");
        }
    }

    static public Map<String, List<LatLng>> loadTeamLocationHistoryFromPreferences(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = sharedPrefs.getString(Constants.SHARED_PREF_TEAM_LOCATION_HISTORY, "");
        Type type = new TypeToken<Map<String, List<LatLng>>>() {}.getType();
        Map<String, List<LatLng>> teamLocationHistory = gson.fromJson(json,type);
        if (teamLocationHistory != null) {
            Log.d(tag, "Loaded " + teamLocationHistory.size() + " team member's locations from history");
        }
        return teamLocationHistory;
    }

    static public void clearTeamLocationHistory(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(Constants.SHARED_PREF_TEAM_LOCATION_HISTORY, "");
        editor.apply();
    }
}
