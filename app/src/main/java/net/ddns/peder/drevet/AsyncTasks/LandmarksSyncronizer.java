package net.ddns.peder.drevet.AsyncTasks;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.auth.CognitoCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import net.ddns.peder.drevet.Constants;
import net.ddns.peder.drevet.R;
import net.ddns.peder.drevet.database.Landmark;
import net.ddns.peder.drevet.database.LandmarksDbHelper;

public class LandmarksSyncronizer extends AsyncTask<Void, Void, Integer>{
    public final int SUCCESS = 0;
    public final int FAILED_USER = 1;
    public final int FAILED_TEAM = 1;

    private CognitoCredentialsProvider credentialsProvider;
    private Context mContext;
    private String userId;
    private String teamId;
    private final String tag = "Sync";
    private final String[] PROJECTION = {
                LandmarksDbHelper.COLUMN_NAME_ID,
                LandmarksDbHelper.COLUMN_NAME_SHOWED,
                LandmarksDbHelper.COLUMN_NAME_SHARED,
                LandmarksDbHelper.COLUMN_NAME_DESCRIPTION,
                LandmarksDbHelper.COLUMN_NAME_LATITUDE,
                LandmarksDbHelper.COLUMN_NAME_LONGITUDE,
    };

    public LandmarksSyncronizer(Context context) {
        // Initialize the Amazon Cognito credentials provider
        credentialsProvider = new CognitoCachingCredentialsProvider(
                context,
                "us-east-1:a9e2f85b-792f-4b92-801b-6b170909ea42", // Identity Pool ID
                Regions.US_EAST_1 // Region
        );
        mContext = context;
        // Read userid from preferences
        SharedPreferences prefs = ((Activity)mContext).getPreferences(Context.MODE_PRIVATE);
        userId = prefs.getString(Constants.PREF_USER_ID, Constants.DEFAULT_USER_ID);
        // Read userid from preferences
        teamId = prefs.getString(Constants.PREF_TEAM_ID, Constants.DEFAULT_TEAM_ID);
    }

    @Override
    protected Integer doInBackground(Void... params) {
        if (userId.equals(Constants.DEFAULT_USER_ID)) {
            return FAILED_USER;
        }
        if (teamId.equals(Constants.DEFAULT_TEAM_ID)) {
            return FAILED_TEAM;
        }

        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
        DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);


        LandmarksDbHelper landmarksDbHelper = new LandmarksDbHelper(mContext);
        SQLiteDatabase db = landmarksDbHelper.getReadableDatabase();

        Cursor cursor = db.query(LandmarksDbHelper.TABLE_NAME,
                                 PROJECTION,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null);

        while (cursor.moveToNext()) {
            final int shared = cursor.getInt(cursor.getColumnIndex(
                                            LandmarksDbHelper.COLUMN_NAME_SHARED));
            if (shared > 0) {
                Landmark landmark = new Landmark();
                String id = cursor.getString(cursor.getColumnIndex(
                        LandmarksDbHelper.COLUMN_NAME_LANDMARKID));
                landmark.setLandmarkId(id);
                String desc = cursor.getString(cursor.getColumnIndex(
                        LandmarksDbHelper.COLUMN_NAME_DESCRIPTION));
                landmark.setDescription(desc);
                Log.i(tag, "Logging landmark "+desc);
                landmark.setUser(userId);
                landmark.setLatitude(cursor.getFloat(cursor.getColumnIndex(
                                                        LandmarksDbHelper.COLUMN_NAME_LATITUDE)));
                landmark.setLongitude(cursor.getFloat(cursor.getColumnIndex(
                                                        LandmarksDbHelper.COLUMN_NAME_LONGITUDE)));
                mapper.save(landmark);
            }
        }
        cursor.close();
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


