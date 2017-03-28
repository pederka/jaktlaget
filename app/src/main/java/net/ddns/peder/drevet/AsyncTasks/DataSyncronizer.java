package net.ddns.peder.drevet.AsyncTasks;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.auth.CognitoCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import net.ddns.peder.drevet.Constants;
import net.ddns.peder.drevet.database.PositionsDbHelper;
import net.ddns.peder.drevet.dynamoDB.Position;
import net.ddns.peder.drevet.utils.JsonUtil;

import java.util.List;

public class DataSyncronizer extends AsyncTask<Void, Void, Integer>{
    private final int SUCCESS = 0;
    private final int FAILED_USER = 1;
    private final int FAILED_TEAM = 2;

    private CognitoCredentialsProvider credentialsProvider;
    private Context mContext;
    private String userId;
    private String teamId;
    private float lat;
    private float lon;
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
        lat = sharedPrefs.getFloat(Constants.SHARED_PREF_LAT, 0.0f);
        lon = sharedPrefs.getFloat(Constants.SHARED_PREF_LON, 0.0f);
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
                "jaktlag/"+teamId+"/"+userId+".json",    /* The key for the uploaded object */
                JsonUtil.exportDataToFile(mContext, "tmp")
        );

        ObjectListing list = s3.listObjects(Constants.MY_BUCKET, "jaktlag/"+teamId);
        List<S3ObjectSummary> summaries = list.getObjectSummaries();

        Log.i(tag, "Found data from "+summaries.size()+" users:");
        for (int i=0; i<summaries.size(); i++) {
            Log.i(tag, summaries.get(i).getKey());
        }

        return SUCCESS;
    }

    @Override
    protected void onPostExecute(Integer result) {
    }
}


