package net.ddns.peder.jaktlaget.AsyncTasks;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.JsonReader;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import net.ddns.peder.jaktlaget.Constants;
import net.ddns.peder.jaktlaget.MainActivity;
import net.ddns.peder.jaktlaget.database.PositionsDbHelper;
import net.ddns.peder.jaktlaget.database.TeamLandmarksDbHelper;
import net.ddns.peder.jaktlaget.interfaces.OnSyncComplete;
import net.ddns.peder.jaktlaget.utils.JsonUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class JaktlagetAPISynchronizer {

    public static int SUCCESS = 0;
    public static int FAILED_USER = 1;
    public static int FAILED_TEAM = 2;
    public static int FAILED_TRANSFER = 3;
    public static int FAILED_CODE = 4;
    public static int FAILED_TIMEOUT = 5;

    private String userId;
    private SQLiteDatabase posdb;
    private SQLiteDatabase lmdb;
    private String teamId;
    private String code;
    private final static String tag = "JaktlagetAPISyncronizer";
    private SharedPreferences sharedPrefs;
    private URL jaktlagetEndpoint;

    private Context mContext;

    private OnSyncComplete onSyncComplete;
    static final String url = Constants.API_URL;

    public JaktlagetAPISynchronizer(Context context, OnSyncComplete onSyncComplete) {
        this.mContext = context;
        this.onSyncComplete = onSyncComplete;

        try {
            jaktlagetEndpoint = new URL(Constants.API_URL);
        } catch (Exception e) {
            Log.d(tag, "Malformed URL?");
        }

        // Get last location
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        userId = sharedPrefs.getString(Constants.SHARED_PREF_USER_ID, Constants.DEFAULT_USER_ID);
        teamId = sharedPrefs.getString(Constants.SHARED_PREF_TEAM_ID, Constants.DEFAULT_TEAM_ID);
        code = sharedPrefs.getString(Constants.SHARED_PREF_TEAM_CODE, "");

        // Initialize databases
        PositionsDbHelper positionsDbHelper = new PositionsDbHelper(mContext);
        posdb = positionsDbHelper.getWritableDatabase();
        TeamLandmarksDbHelper teamLandmarksDbHelper = new TeamLandmarksDbHelper(mContext);
        lmdb = teamLandmarksDbHelper.getWritableDatabase();

    }

    public void execute() {

        if (userId.equals(Constants.DEFAULT_USER_ID)) {
            onSyncComplete.onSyncComplete(FAILED_USER);
            return;
        }
        if (teamId.equals(Constants.DEFAULT_TEAM_ID)) {
            onSyncComplete.onSyncComplete(FAILED_USER);
            return;
        }

        // Add yourself to database
        final String[] PROJECTION = {
            PositionsDbHelper.COLUMN_NAME_ID,
            PositionsDbHelper.COLUMN_NAME_USER,
        };
        ContentValues values = new ContentValues();
        values.put(PositionsDbHelper.COLUMN_NAME_LATITUDE, 0);
        values.put(PositionsDbHelper.COLUMN_NAME_LONGITUDE, 0);
        values.put(PositionsDbHelper.COLUMN_NAME_TIME, "-1");
        // Query database to see if user exists
        String selection = PositionsDbHelper.COLUMN_NAME_USER + " = ?";
        String[] selectionArgs = {userId};
        Cursor cursor = posdb.query(PositionsDbHelper.TABLE_NAME,
                         PROJECTION,
                         selection,
                         selectionArgs,
                         null,
                         null,
                         null);
        if (cursor.getCount() > 0) {
            // If exists, update
            posdb.update(PositionsDbHelper.TABLE_NAME, values, selection, selectionArgs);
        } else {
            // If new user, push new row to database
            values.put(PositionsDbHelper.COLUMN_NAME_SHOWED, true);
            values.put(PositionsDbHelper.COLUMN_NAME_USER, userId);
            posdb.insert(PositionsDbHelper.TABLE_NAME, null, values);
        }
        cursor.close();

        // Construct POST data object
        JSONObject postObject = JsonUtil.exportDataToJson(mContext);
        // Add team and code
        try {
            postObject.put("Team", teamId);
            postObject.put("Code", code);
        } catch (JSONException e) {
            Log.e(tag, "Error adding team and code to POST Json payload");
        }
        Log.i(tag, ""+postObject.toString());

        try {
            HttpsURLConnection myConnection =
                    (HttpsURLConnection) jaktlagetEndpoint.openConnection();
            myConnection.setRequestMethod("POST");
            InputStream responseBody = myConnection.getInputStream();
            InputStreamReader responseBodyReader = new InputStreamReader(responseBody,
                    "UTF-8");
            JsonReader jsonReader = new JsonReader(responseBodyReader);
        } catch (IOException e) {
            Log.e(tag, e.toString());
            Log.e(tag, "Error in HTTPS connection");
        }

    }
}