package net.ddns.peder.jaktlaget.AsyncTasks;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.JsonReader;
import android.util.Log;

import net.ddns.peder.jaktlaget.Constants;
import net.ddns.peder.jaktlaget.R;
import net.ddns.peder.jaktlaget.database.PositionsDbHelper;
import net.ddns.peder.jaktlaget.database.TeamLandmarksDbHelper;
import net.ddns.peder.jaktlaget.interfaces.OnSyncComplete;
import net.ddns.peder.jaktlaget.utils.JsonUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.net.SocketFactory;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

public class HttpsDataSynchronizer extends AsyncTask<Void, Void, Integer>{
    public static int SUCCESS = 0;
    public static int FAILED_USER = 1;
    public static int FAILED_TEAM = 2;
    public static int FAILED_TRANSFER = 3;
    public static int FAILED_CODE = 4;
    public static int FAILED_TIMEOUT = 5;
    private Context mContext;
    private SocketFactory socketFactory;
    private String userId;
    private SQLiteDatabase posdb;
    private SQLiteDatabase lmdb;
    private OnSyncComplete onSyncComplete;
    private String teamId;
    private String code;
    private boolean verbose;
    private final static String tag = "HttpsDataSyncronizer";
    private ProgressDialog dialog;
    private SharedPreferences sharedPrefs;
    private URL jaktlagetEndpoint;

    public HttpsDataSynchronizer(Context context, OnSyncComplete onSyncComplete) {
        mContext = context;

        this.onSyncComplete = onSyncComplete;

        dialog = new ProgressDialog(context);

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

        // Clear team landmarks database
        teamLandmarksDbHelper.clearTable(lmdb);
    }

    @Override
    protected void onPreExecute() {
        if (this.verbose) {
            this.dialog.setMessage("Kommuniserer med server..");
            this.dialog.show();
        }
    }

    @Override
    protected Integer doInBackground(Void... params) {

        if (userId.equals(Constants.DEFAULT_USER_ID)) {
            return FAILED_USER;
        }
        if (teamId.equals(Constants.DEFAULT_TEAM_ID)) {
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
            myConnection.setDoInput(true);
            myConnection.setDoOutput(true);
            myConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            myConnection.setRequestProperty("Accept", "application/json");
            myConnection.setRequestMethod("POST");
            OutputStreamWriter wr = new OutputStreamWriter(myConnection.getOutputStream());
            wr.write(postObject.toString());
            wr.flush();
            wr.close();
            StringBuffer response = new StringBuffer();
            String inputLine;
            int res = myConnection.getResponseCode();
            if (res == HttpsURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(myConnection.getInputStream()));
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Start parsing response
                // Get code
                JSONObject responseJsonObject = new JSONObject(response.toString());
                String codeString = responseJsonObject.getString("code");
                Log.d(tag, "Code is: "+codeString);
                sharedPrefs.edit().putString(Constants.SHARED_PREF_TEAM_CODE, codeString).apply();
                // Get team info
                JSONArray team_members = responseJsonObject.getJSONArray("team_members");
                for (int i=0; i<team_members.length(); i++) {
                    JsonUtil.importUserInformationFromJson(mContext, posdb, lmdb,
                            team_members.getJSONObject(i));
                }
                return SUCCESS;
            } else if (res == HttpsURLConnection.HTTP_UNAUTHORIZED) {
                return FAILED_CODE;
            }
            else {
                Log.d(tag, "Https connection failed with response code: "+res);
                return FAILED_TRANSFER;
            }

        } catch (IOException e) {
            Log.e(tag, e.toString());
            Log.e(tag, "Error in HTTPS connection");
            return FAILED_TRANSFER;
        } catch (JSONException e) {
            Log.e(tag, "Failed to parse JSON");
            return FAILED_TRANSFER;
        }
    }

    @Override
    protected void onPostExecute(Integer result) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        if (onSyncComplete != null) {
                onSyncComplete.onSyncComplete(result);
        }
    }

    private static int byteArrayToInt(byte[] b)
    {
        return   b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }
}


