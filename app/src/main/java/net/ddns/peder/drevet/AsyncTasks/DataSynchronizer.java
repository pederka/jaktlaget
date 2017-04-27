package net.ddns.peder.drevet.AsyncTasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.auth.CognitoCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import net.ddns.peder.drevet.Constants;
import net.ddns.peder.drevet.database.PositionsDbHelper;
import net.ddns.peder.drevet.database.TeamLandmarksDbHelper;
import net.ddns.peder.drevet.interfaces.OnSyncComplete;
import net.ddns.peder.drevet.utils.JsonUtil;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

public class DataSynchronizer extends AsyncTask<Void, Void, Integer>{
    private final int SUCCESS = 0;
    private final int FAILED_USER = 1;
    private final int FAILED_TEAM = 2;
    private CognitoCredentialsProvider credentialsProvider;
    private Context mContext;
    private String userId;
    private String teamId;
    private OnSyncComplete onSyncComplete;
    private final static String tag = "PositionSyncronizer";

    public DataSynchronizer(Context context, OnSyncComplete onSyncComplete) {
        this.onSyncComplete = onSyncComplete;
        // Initialize the Amazon Cognito credentials provider
        credentialsProvider = new CognitoCachingCredentialsProvider(
                context,
                "us-east-1:a9e2f85b-792f-4b92-801b-6b170909ea42", // Identity Pool ID
                Regions.US_EAST_1 // Region
        );
        mContext = context;
        // Get last location
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        userId = sharedPrefs.getString(Constants.SHARED_PREF_USER_ID, Constants.DEFAULT_USER_ID);
        teamId = sharedPrefs.getString(Constants.SHARED_PREF_TEAM_ID, Constants.DEFAULT_TEAM_ID);
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected Integer doInBackground(Void... params) {

        Log.i(tag, "Syncronizing position");
        if (userId.equals(Constants.DEFAULT_USER_ID)) {
            return FAILED_USER;
        }
        if (teamId.equals(Constants.DEFAULT_TEAM_ID)) {
            return FAILED_TEAM;
        }

        AmazonS3 s3 = new AmazonS3Client(credentialsProvider);

        TransferUtility transferUtility = new TransferUtility(s3, mContext);

        Log.i(tag, "Uploading data to S3");

        TransferObserver observer = transferUtility.upload(
                Constants.MY_BUCKET,     /* The bucket to upload to */
                Constants.OBJECT_KEY_BASE + teamId + "/" + userId + ".json",    /* The key */
                JsonUtil.exportDataToFile(mContext, "tmp")
        );

        ObjectListing list = s3.listObjects(Constants.MY_BUCKET, Constants.OBJECT_KEY_BASE + teamId);
        List<S3ObjectSummary> summaries = list.getObjectSummaries();

        // Get writable database for positions
        PositionsDbHelper positionsDbHelper = new PositionsDbHelper(mContext);
        final SQLiteDatabase posdb = positionsDbHelper.getWritableDatabase();
        // Get writable database for team landmarks
        TeamLandmarksDbHelper teamLandmarksDbHelper = new TeamLandmarksDbHelper(mContext);
        final SQLiteDatabase lmdb = teamLandmarksDbHelper.getWritableDatabase();
        // Clear landmarks database
        teamLandmarksDbHelper.clearTable(lmdb);
        // Clear positions database
        positionsDbHelper.clearTable(posdb);

        try {
            Log.i(tag, "Found data from " + summaries.size() + " users:");
            for (int i = 0; i < summaries.size(); i++) {
                // Skip own landmarks
                String filename = summaries.get(i).getKey();
                if (filename.substring(filename.lastIndexOf("/")+1,
                                                    filename.lastIndexOf(".")).equals(userId)) {
                    continue;
                }
                File file = File.createTempFile(Constants.TMP_FILE_NAME,
                                    null, mContext.getCacheDir());
                file.createNewFile();
                Log.i(tag, "Getting file: " + filename);
                final TransferObserver transferObserver = transferUtility.download(Constants.MY_BUCKET,
                                                                filename, file);
                transferObserver.setTransferListener(new TransferListener() {
                    @Override
                    public void onStateChanged(int id, TransferState state) {
                        if (state == TransferState.COMPLETED) {
                            Log.i(tag, "Download number "+id+" complete.");
                            File file = new File(transferObserver.getAbsoluteFilePath());
                            try {
                                FileInputStream is = new FileInputStream(file);
                                int size = is.available();
                                byte[] buffer = new byte[size];
                                is.read(buffer);
                                is.close();
                                JsonUtil.importUserInformationFromJsonString(mContext, posdb, lmdb,
                                                            new String(buffer, "UTF-8"));
                                file.delete();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                    }

                    @Override
                    public void onError(int id, Exception ex) {
                        Log.i(tag, "Download of "+transferObserver.getAbsoluteFilePath()+" failed");
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return SUCCESS;
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        if (result.equals(SUCCESS)) {
            if (onSyncComplete != null) {
                onSyncComplete.onSyncComplete();
            }
            Log.i(tag, "Sync successful!");
        }
        else if (result.equals(FAILED_USER)) {
            Log.i(tag, "Sync failed due to missing user");
        }
        else if (result.equals(FAILED_TEAM)) {
            Log.i(tag, "Sync failed due to missing team");
        }
    }
}


