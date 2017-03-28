package net.ddns.peder.drevet.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.preference.PreferenceManager;

import net.ddns.peder.drevet.Constants;
import net.ddns.peder.drevet.database.LandmarksDbHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by peder on 26.03.2017.
 */

public class JsonUtil {
    private static final String JSON_USER = "User";
    private static final String JSON_TIME = "Last_update";
    private static final String JSON_LAT = "Latitude";
    private static final String JSON_LON = "Longitude";
    private static final String JSON_DESC = "Description";


    public static File exportDataToFile(Context context, String outFile) {
        File file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS), outFile);
        try {
            file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS), outFile);
            OutputStream out = new FileOutputStream(file);
            JSONArray exportData = new JSONArray();
            exportData.put(writeUserPositionToJson(context));
            JSONObject landmarks = new JSONObject();
            landmarks.put("Landmarks", writeSharedLandmarksToJsonArray(context));
            exportData.put(landmarks);
            out.write(exportData.toString().getBytes());
        } catch(Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    private static JSONObject writeUserPositionToJson(Context context) {
        JSONObject userPosition = new JSONObject();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            userPosition.put(JSON_USER,
                    sharedPrefs.getString(Constants.SHARED_PREF_USER_ID, Constants.DEFAULT_USER_ID));
            userPosition.put(JSON_LAT, sharedPrefs.getFloat(Constants.SHARED_PREF_LAT, 0.0f));
            userPosition.put(JSON_LON, sharedPrefs.getFloat(Constants.SHARED_PREF_LON, 0.0f));
            userPosition.put(JSON_TIME, sharedPrefs.getLong(Constants.SHARED_PREF_TIME, 0));
        } catch(Exception e) {
            e.printStackTrace();
        }
        return userPosition;
    }

    private static JSONArray writeSharedLandmarksToJsonArray(Context context) {
         final String[] PROJECTION = {
            LandmarksDbHelper.COLUMN_NAME_ID,
            LandmarksDbHelper.COLUMN_NAME_SHARED,
            LandmarksDbHelper.COLUMN_NAME_DESCRIPTION,
            LandmarksDbHelper.COLUMN_NAME_LATITUDE,
            LandmarksDbHelper.COLUMN_NAME_LONGITUDE,
        };
        LandmarksDbHelper landmarksDbHelper = new LandmarksDbHelper(context);
        SQLiteDatabase db = landmarksDbHelper.getReadableDatabase();
                String selection = LandmarksDbHelper.COLUMN_NAME_SHARED + " = ?";
        String[] selectionArgs = { "1" };
        final Cursor cursor = db.query(LandmarksDbHelper.TABLE_NAME,
                         PROJECTION,
                         selection,
                         selectionArgs,
                         null,
                         null,
                         null);

        JSONArray landmarks = new JSONArray();

        try {

            while (cursor.moveToNext()) {
                JSONObject landmark = new JSONObject();
                landmark.put(JSON_DESC, cursor.getString(cursor.getColumnIndexOrThrow(
                        LandmarksDbHelper.COLUMN_NAME_DESCRIPTION)));
                landmark.put(JSON_LAT, cursor.getFloat(cursor.getColumnIndexOrThrow(
                        LandmarksDbHelper.COLUMN_NAME_LATITUDE)));
                landmark.put(JSON_LON, cursor.getFloat(cursor.getColumnIndexOrThrow(
                        LandmarksDbHelper.COLUMN_NAME_LONGITUDE)));
                landmarks.put(landmark);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        cursor.close();

        return landmarks;
    }

    public static void readLandmarksFromFile(Context context, String inFile) {

    }

}
