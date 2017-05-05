package net.ddns.peder.drevet.AsyncTasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import net.ddns.peder.drevet.Constants;
import net.ddns.peder.drevet.MainActivity;
import net.ddns.peder.drevet.R;

import net.ddns.peder.drevet.database.PositionsDbHelper;
import net.ddns.peder.drevet.database.TeamLandmarksDbHelper;
import net.ddns.peder.drevet.interfaces.OnSyncComplete;
import net.ddns.peder.drevet.utils.JsonUtil;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.Socket;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.List;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

public class DataSynchronizer extends AsyncTask<Void, Void, Integer>{
    public static int SUCCESS = 0;
    public static int FAILED_USER = 1;
    public static int FAILED_TEAM = 2;
    public static int FAILED_TRANSFER = 3;
    public static int FAILED_CODE = 4;
    private Context mContext;
    private SocketFactory socketFactory;
    private String userId;
    private SQLiteDatabase posdb;
    private SQLiteDatabase lmdb;
    private OnSyncComplete onSyncComplete;
    private String teamId;
    private boolean removeOutdated;
    private int expirationTime;
    private boolean verbose;
    private final static String tag = "SslSyncronizer";
    private ProgressDialog dialog;


    public DataSynchronizer(Context context, OnSyncComplete onSyncComplete, boolean verbose) {
        mContext = context;

        this.verbose = verbose;
        this.onSyncComplete = onSyncComplete;

        dialog = new ProgressDialog(context);

        // Get last location
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        userId = sharedPrefs.getString(Constants.SHARED_PREF_USER_ID, Constants.DEFAULT_USER_ID);
        teamId = sharedPrefs.getString(Constants.SHARED_PREF_TEAM_ID, Constants.DEFAULT_TEAM_ID);

        // Get preferences relating to outdated positions
        removeOutdated = sharedPrefs.getBoolean("pref_hideoldteam", true);
        expirationTime = Integer.parseInt(sharedPrefs.getString("pref_hideteamlimit", "30"));

        // Initialize databases
        PositionsDbHelper positionsDbHelper = new PositionsDbHelper(mContext);
        posdb = positionsDbHelper.getWritableDatabase();
        TeamLandmarksDbHelper teamLandmarksDbHelper = new TeamLandmarksDbHelper(mContext);
        lmdb = teamLandmarksDbHelper.getWritableDatabase();

        // Clear team landmarks database
        teamLandmarksDbHelper.clearTable(lmdb);

        // (could be from a resource or ByteArrayInputStream or ...)
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            // From https://www.washington.edu/itconnect/security/ca/load-der.crt
            InputStream is = context.getResources().openRawResource(R.raw.pederddnsnet);
            InputStream caInput = new BufferedInputStream(is);
            Certificate ca;
            try {
                ca = cf.generateCertificate(caInput);
            } finally {
                caInput.close();
            }

            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);


            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // Create an SSLContext that uses our TrustManager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
            socketFactory = sslContext.getSocketFactory();

        } catch (Exception e) {
            e.printStackTrace();
        }

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

        Log.i(tag, "Synchronizing");
        try {
            Socket socket = (SSLSocket) socketFactory.createSocket(Constants.SOCKET_ADDR,
                                    Constants.SOCKET_PORT);
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

            // Send user name
            byte[] nameBytes = userId.getBytes();
            int nameSize = nameBytes.length;
            dataOutputStream.writeInt(nameSize);
            dataOutputStream.write(nameBytes);

            // Send team name
            byte[] teamBytes = teamId.getBytes();
            int teamSize = teamBytes.length;
            dataOutputStream.writeInt(teamSize);
            dataOutputStream.write(teamBytes);

            // Send data
            byte[] outdata = JsonUtil.exportDataToJson(mContext).toString().getBytes();
            int size = outdata.length;
            Log.d(tag, "Sending "+size+" bytes of data");
            dataOutputStream.writeInt(size);
            dataOutputStream.write(outdata);
            dataOutputStream.flush();

            // Read incoming data size
            byte[] inputSizeBytes = new byte[4];
            dataInputStream.read(inputSizeBytes, 0, 4);
            int incomingSize = byteArrayToInt(inputSizeBytes);
            Log.d(tag, "Received "+incomingSize+" bytes of data");

            if (incomingSize == -1) {
                return FAILED_CODE;
            }
            else if (incomingSize > 0) {
                // Read incoming data
                byte[] incomingData = new byte[incomingSize];
                dataInputStream.read(incomingData, 0, incomingSize);
                String incomingString = new String(incomingData, "UTF-8");
                Log.d(tag, "Received string " + incomingString);

                String[] userData = incomingString.split("%");
                for (int i = 0; i < userData.length; i++) {
                    JsonUtil.importUserInformationFromJsonString(mContext, posdb, lmdb, userData[i]);
                }
            }

            // Remove any teammates with outdated positions
            if (removeOutdated) {
                final String[] PROJECTION = {
                        PositionsDbHelper.COLUMN_NAME_ID,
                        PositionsDbHelper.COLUMN_NAME_TIME,
                };
                Cursor cursor = posdb.query(PositionsDbHelper.TABLE_NAME,
                         PROJECTION,
                         null,
                         null,
                         null,
                         null,
                         null);
                long currenttime = System.currentTimeMillis();
                long exptime = expirationTime*60000;
                String whereClause = PositionsDbHelper.COLUMN_NAME_ID + "= ?";
                while (cursor.moveToNext()) {
                    long time = Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(
                            PositionsDbHelper.COLUMN_NAME_TIME)));
                    if (currenttime-time > exptime) {
                        posdb.delete(PositionsDbHelper.TABLE_NAME, whereClause,
                                        new String[] {cursor.getString(cursor.getColumnIndexOrThrow(
                                                    PositionsDbHelper.COLUMN_NAME_ID))});
                    }
                }
                cursor.close();
            }

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
            return FAILED_TRANSFER;
        }
        return SUCCESS;
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


