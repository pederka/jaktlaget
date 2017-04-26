package net.ddns.peder.drevet.AsyncTasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import net.ddns.peder.drevet.Constants;
import net.ddns.peder.drevet.R;

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

public class SslSynchronizer extends AsyncTask<Void, Void, Integer>{
    private final int SUCCESS = 0;
    private final int FAILED_USER = 1;
    private final int FAILED_TEAM = 2;
    private Context mContext;
    private SocketFactory socketFactory;
    private List<String> jsonStrings;
    private String userId;
    private String teamId;
    private final static String tag = "SslSyncronizer";

    public SslSynchronizer(Context context) {
        mContext = context;

        // Get last location
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        userId = sharedPrefs.getString(Constants.SHARED_PREF_USER_ID, Constants.DEFAULT_USER_ID);
        teamId = sharedPrefs.getString(Constants.SHARED_PREF_TEAM_ID, Constants.DEFAULT_TEAM_ID);

        // (could be from a resource or ByteArrayInputStream or ...)
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            // From https://www.washington.edu/itconnect/security/ca/load-der.crt
            InputStream is = context.getResources().openRawResource(R.raw.pederddnsnet);
            InputStream caInput = new BufferedInputStream(is);
            Certificate ca;
            try {
                ca = cf.generateCertificate(caInput);
                // System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
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
    protected Integer doInBackground(Void... params) {

        Log.i(tag, "Synchronizing");
        try {
            Socket socket = (SSLSocket) socketFactory.createSocket(Constants.SOCKET_ADDR,
                                    Constants.SOCKET_PORT);
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

            byte[] outdata = JsonUtil.exportDataToJson(mContext).toString().getBytes();
            int size = outdata.length;
            dataOutputStream.writeInt(size);
            dataOutputStream.write(outdata);
            byte[] teamIdData = teamId.getBytes();
            size = teamIdData.length;
            dataOutputStream.writeInt(size);
            dataOutputStream.write(teamIdData);
            dataOutputStream.flush();

            byte[] inputSizeBytes = new byte[4];
            dataInputStream.read(inputSizeBytes, 0, 4);
            int inputSize = byteArrayToInt(inputSizeBytes);
            Log.d(tag, "Received int "+inputSize);

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return SUCCESS;
    }

    @Override
    protected void onPostExecute(Integer result) {
        if (result.equals(SUCCESS)) {
            Log.i(tag, "Sync successful!");
        }
    }

    public static int byteArrayToInt(byte[] b)
    {
        return   b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }
}


