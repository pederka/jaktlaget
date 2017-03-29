package net.ddns.peder.drevet.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.preference.PreferenceManager;

import net.ddns.peder.drevet.Constants;
import net.ddns.peder.drevet.database.LandmarksDbHelper;
import net.ddns.peder.drevet.database.PositionsDbHelper;
import net.ddns.peder.drevet.database.TeamLandmarksDbHelper;

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
    private static final String JSON_LMARRAY = "Landmarks";


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
            landmarks.put(JSON_LMARRAY, writeSharedLandmarksToJsonArray(context));
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
                landmark.put(JSON_TIME, cursor.getString(cursor.getColumnIndexOrThrow(
                        LandmarksDbHelper.COLUMN_NAME_TIME)));
                landmarks.put(landmark);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        cursor.close();

        return landmarks;
    }

    private static void readUserInformationFromJson(Context context, String userId, File userFile) {
        PositionsDbHelper positionsDbHelper = new PositionsDbHelper(context);
        SQLiteDatabase db = positionsDbHelper.getWritableDatabase();
        try {
            JSONObject json = new JSONObject(userFile.toString());
            // Get user position and update local database
            Float latitude = (float)json.getDouble(JSON_LAT);
            Float longitude = (float)json.getDouble(JSON_LON);
            long time = Long.getLong(json.getString(JSON_TIME));
            updateUserPosition(db, userId, latitude, longitude, time);
            // Add user shared landmarks to local database
            JSONArray landmarksArray = json.getJSONArray(JSON_LMARRAY);
            readLandmarksFromJson(context, userId, landmarksArray);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void updateUserPosition(SQLiteDatabase db, String userId, float lat, float lon,
                                             long time) {
        ContentValues values = new ContentValues();
        values.put(PositionsDbHelper.COLUMN_NAME_LATITUDE, lat);
        values.put(PositionsDbHelper.COLUMN_NAME_LONGITUDE, lon);
        values.put(PositionsDbHelper.COLUMN_NAME_TIME, time);
        // Query databare to see if user exists

        // If exists, update
        String whereClause = PositionsDbHelper.COLUMN_NAME_USER+"= ?";
        String[] whereArgs = new String[] {userId};
        db.update(PositionsDbHelper.TABLE_NAME, values, whereClause, whereArgs);
        // If new user, push new row to database
        values.put(PositionsDbHelper.COLUMN_NAME_USER, userId);
    }

    private static void readLandmarksFromJson(Context context, String userID,
                                                                        JSONArray landmarksArray) {
        // Get writable database
        TeamLandmarksDbHelper teamLandmarksDbHelper = new TeamLandmarksDbHelper(context);
        SQLiteDatabase db = teamLandmarksDbHelper.getWritableDatabase();
        try {
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
                values.put(TeamLandmarksDbHelper.COLUMN_NAME_TIME, landmark.getString(JSON_TIME));
                db.insert(TeamLandmarksDbHelper.TABLE_NAME, null, values);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.close();
    }

}
