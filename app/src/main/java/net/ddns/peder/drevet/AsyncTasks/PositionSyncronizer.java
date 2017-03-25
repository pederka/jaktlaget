package net.ddns.peder.drevet.AsyncTasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.auth.CognitoCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;

import net.ddns.peder.drevet.Constants;
import net.ddns.peder.drevet.dynamoDB.Position;

public class PositionSyncronizer extends AsyncTask<Void, Void, Integer>{
    private final int SUCCESS = 0;
    private final int FAILED_USER = 1;
    private final int FAILED_TEAM = 2;

    private CognitoCredentialsProvider credentialsProvider;
    private Context mContext;
    private String userId;
    private String teamId;
    private float lat;
    private float lon;
    private long position_time;
    private final static String tag = "PositionSyncronizer";

    public PositionSyncronizer(Context context) {
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
        position_time = sharedPrefs.getLong(Constants.SHARED_PREF_TIME, 0);
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
        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
        DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

        Position position = new Position();
        position.setUser(userId);
        position.setTeam(teamId);
        position.setLatitude(lat);
        position.setLongitude(lon);
        position.setTime(Long.toString(position_time));

        mapper.save(position);

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        PaginatedScanList<Position> result = mapper.scan(Position.class, scanExpression);

        for (int i=0; i < result.size(); i++) {
            Log.i(tag, "Found position for "+result.get(i).getUser());
        }

        return SUCCESS;
    }

    @Override
    protected void onPostExecute(Integer result) {
    }
}


