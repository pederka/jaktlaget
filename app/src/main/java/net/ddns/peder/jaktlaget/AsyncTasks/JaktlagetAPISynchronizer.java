package net.ddns.peder.jaktlaget.AsyncTasks;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
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

import org.json.JSONObject;

public class JaktlagetAPISynchronizer {

    public static int SUCCESS = 0;
    public static int FAILED_USER = 1;
    public static int FAILED_TEAM = 2;
    public static int FAILED_TRANSFER = 3
    public static int FAILED_CODE = 4;
    public static int FAILED_TIMEOUT = 5;

    private String userId;
    private SQLiteDatabase posdb;
    private SQLiteDatabase lmdb;
    private String teamId;
    private String code;
    private final static String tag = "JaktlagetAPISyncronizer";
    private SharedPreferences sharedPrefs;

    private Context mContext;

    private OnSyncComplete onSyncComplete;
    static final String url = Constants.API_URL;

    public JaktlagetAPISynchronizer(Context context, OnSyncComplete onSyncComplete) {
        this.mContext = context;
        this.onSyncComplete = onSyncComplete;

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

    public int execute() {

        if (userId.equals(Constants.DEFAULT_USER_ID)) {
            onSyncComplete.onSyncComplete(FAILED_USER);
            return FAILED_USER;
        }
        if (teamId.equals(Constants.DEFAULT_TEAM_ID)) {
            onSyncComplete.onSyncComplete(FAILED_USER);
            return FAILED_TEAM;
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

        // prepare the Request
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.POST, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        onSyncComplete.onSyncComplete(SUCCESS);
                        Log.d("Response", response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                    }
                }) {
            @Override
            public byte[] getBody() {
                String your_string_json = "";
                return your_string_json.getBytes();
            }
        };

        // add it to the global Activity RequestQueue
        ((MainActivity)mContext).queue.add(getRequest);
    }
}