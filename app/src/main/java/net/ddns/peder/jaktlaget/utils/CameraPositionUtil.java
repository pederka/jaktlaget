package net.ddns.peder.jaktlaget.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.ddns.peder.jaktlaget.Constants;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CameraPositionUtil {

    private static String tag = "CameraPositionUtil";

    static public void saveCameraPositionToPreferences(Context context, CameraPosition cameraPosition) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(cameraPosition);
        editor.putString(Constants.SHARED_PREF_CAMERA_POSITION, json);
        editor.apply();
        Log.d(tag, "Saved camera position to shared preferences");
    }

    static public CameraPosition loadCameraPositionFromPreferences(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = sharedPrefs.getString(Constants.SHARED_PREF_CAMERA_POSITION, null);
        Type type = new TypeToken<CameraPosition>() {}.getType();
        CameraPosition cameraPosition = gson.fromJson(json,type);
        if (cameraPosition != null) {
            Log.d(tag, "Loaded camera position from history");
        }
        return cameraPosition;
    }

}
