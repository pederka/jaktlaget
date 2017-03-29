package net.ddns.peder.drevet.AsyncTasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.auth.CognitoCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import net.ddns.peder.drevet.Constants;
import net.ddns.peder.drevet.R;
import net.ddns.peder.drevet.utils.JsonUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DataSyncronizer extends AsyncTask<Void, Void, Integer>{
    private final int SUCCESS = 0;
    private final int FAILED_USER = 1;
    private final int FAILED_TEAM = 2;
    private CognitoCredentialsProvider credentialsProvider;
    private Context mContext;
    private String userId;
    private String teamId;
    private final static String tag = "PositionSyncronizer";

    public DataSyncronizer(Context context) {
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

        try {
            List<File> files = new ArrayList<>();
            Log.i(tag, "Found data from " + summaries.size() + " users:");
            for (int i = 0; i < summaries.size(); i++) {
                File file = File.createTempFile(Constants.TMP_FILE_NAME, null, mContext.getCacheDir());
                transferUtility.download(Constants.MY_BUCKET, summaries.get(i).getKey(), file);
                files.add(file);
                Log.i(tag, "Getting file: " + summaries.get(i).getKey());
            }
            JsonUtil.importUserInformationFromFiles(mContext, files);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return SUCCESS;
    }

    @Override
    protected void onPostExecute(Integer result) {
        if (result.equals(SUCCESS)) {
            Toast.makeText(mContext, R.string.toast_sync_success, Toast.LENGTH_SHORT).show();
        }
        else if (result.equals(FAILED_USER)) {
            Toast.makeText(mContext, R.string.toast_no_user_no_sync, Toast.LENGTH_SHORT).show();
        }
        else if (result.equals(FAILED_TEAM)) {
            Toast.makeText(mContext, R.string.toast_no_team_no_sync, Toast.LENGTH_SHORT).show();
        }
    }
}


