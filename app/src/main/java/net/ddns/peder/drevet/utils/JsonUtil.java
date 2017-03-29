package net.ddns.peder.drevet.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import net.ddns.peder.drevet.Constants;
import net.ddns.peder.drevet.database.LandmarksDbHelper;
import net.ddns.peder.drevet.database.PositionsDbHelper;
import net.ddns.peder.drevet.database.TeamLandmarksDbHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by peder on 26.03.2017.
 */

public class JsonUtil {
    private static final String JSON_USER = "User";
    private static final String JSON_TIME = "Last_update";
    private static final String JSON_LAT = "Latitude";
    private static final String JSON_LON = "Longitude";
    private static final String JSON_DESC = "Description";
    private static final String JSON_LMARRAY = "Landmarks";
    private static final String tag = "JsonUtil";

    public static File exportDataToFile(Context context, String outFile) {
        File file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS), outFile);
        try {
            file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS), outFile);
            OutputStream out = new FileOutputStream(file);
            JSONObject exportData = new JSONObject();
            writeUserPositionToJson(context, exportData);
            exportData.put(JSON_LMARRAY, writeSharedLandmarksToJsonArray(context));
            out.write(exportData.toString().getBytes());
        } catch(Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    private static void writeUserPositionToJson(Context context, JSONObject object) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            object.put(JSON_USER,
                    sharedPrefs.getString(Constants.SHARED_PREF_USER_ID, Constants.DEFAULT_USER_ID));
            object.put(JSON_LAT, sharedPrefs.getFloat(Constants.SHARED_PREF_LAT, 0.0f));
            object.put(JSON_LON, sharedPrefs.getFloat(Constants.SHARED_PREF_LON, 0.0f));
            object.put(JSON_TIME, sharedPrefs.getLong(Constants.SHARED_PREF_TIME, 0));
        } catch(Exception e) {
            e.printStackTrace();
        }
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

    public static void importUserInformationFromJsonString(Context context, SQLiteDatabase posdb,
                                                        SQLiteDatabase lmdb, String jsonString) {
        try {
            Log.i(tag, jsonString);
            JSONObject json = new JSONObject(jsonString);
            // Get user position and update local database
            String userId = json.getString(JSON_USER);
            Log.i(tag, "New position from user: "+userId);
            Float latitude = (float)json.getDouble(JSON_LAT);
            Float longitude = (float)json.getDouble(JSON_LON);
            long time = json.getLong(JSON_TIME);
            updateUserPosition(posdb, userId, latitude, longitude, time);
            // Add user shared landmarks to local database
            JSONArray landmarksArray = json.getJSONArray(JSON_LMARRAY);
            readLandmarksFromJson(lmdb, userId, landmarksArray);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void updateUserPosition(SQLiteDatabase db, String userId, float lat, float lon,
                                             long time) {
        final String[] PROJECTION = {
            PositionsDbHelper.COLUMN_NAME_ID,
            PositionsDbHelper.COLUMN_NAME_USER,
        };
        ContentValues values = new ContentValues();
        values.put(PositionsDbHelper.COLUMN_NAME_LATITUDE, lat);
        values.put(PositionsDbHelper.COLUMN_NAME_LONGITUDE, lon);
        values.put(PositionsDbHelper.COLUMN_NAME_TIME, time);
        // Query database to see if user exists
        String selection = PositionsDbHelper.COLUMN_NAME_USER + " = ?";
        String[] selectionArgs = {userId};
        Cursor cursor = db.query(PositionsDbHelper.TABLE_NAME,
                         PROJECTION,
                         selection,
                         selectionArgs,
                         null,
                         null,
                         null);
        if (cursor.getCount() > 0) {
            // If exists, update
            String whereClause = PositionsDbHelper.COLUMN_NAME_USER + "= ?";
            String[] whereArgs = new String[]{userId};
            db.update(PositionsDbHelper.TABLE_NAME, values, whereClause, whereArgs);
        } else {
            // If new user, push new row to database
            values.put(PositionsDbHelper.COLUMN_NAME_USER, userId);
            db.insert(PositionsDbHelper.TABLE_NAME, null, values);
        }
        cursor.close();
    }

    private static void readLandmarksFromJson(SQLiteDatabase lmdb, String userID,
                                              JSONArray landmarksArray) {
        try {
            Log.i(tag, "Importing "+landmarksArray.length()+" landmarks");
            for (int i=0; i<landmarksArray.length(); i++) {
                JSONObject landmark = landmarksArray.getJSONObject(i);
                ContentValues values = new ContentValues();
                values.put(TeamLandmarksDbHelper.COLUMN_NAME_USER, userID);
                values.put(TeamLandmarksDbHelper.COLUMN_NAME_DESCRIPTION,
                                                                landmark.getString(JSON_DESC));
                values.put(TeamLandmarksDbHelper.COLUMN_NAME_LATITUDE,
                                                                landmark.getDouble(JSON_LAT));
                values.put(TeamLandmarksDbHelper.COLUMN_NAME_LONGITUDE,
                                                                landmark.getDouble(JSON_LON));
                values.put(TeamLandmarksDbHelper.COLUMN_NAME_SHOWED, 1);
                lmdb.insert(TeamLandmarksDbHelper.TABLE_NAME, null, values);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
