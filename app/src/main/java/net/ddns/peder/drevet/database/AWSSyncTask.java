package net.ddns.peder.drevet.database;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.auth.CognitoCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import java.util.Random;


public class AWSSyncTask extends AsyncTask<String, Void, String>{
    private CognitoCredentialsProvider credentialsProvider;
    private Context mContext;
    private final String tag = "Sync";

    public AWSSyncTask(Context context) {
        // Initialize the Amazon Cognito credentials provider
        credentialsProvider = new CognitoCachingCredentialsProvider(
                context,
                "us-east-1:a9e2f85b-792f-4b92-801b-6b170909ea42", // Identity Pool ID
                Regions.US_EAST_1 // Region
        );
        mContext = context;
    }

    @Override
    protected String doInBackground(String... params) {
        final String[] PROJECTION = {
                LandmarksDbHelper.COLUMN_NAME_ID,
                LandmarksDbHelper.COLUMN_NAME_SHOWED,
                LandmarksDbHelper.COLUMN_NAME_SHARED,
                LandmarksDbHelper.COLUMN_NAME_DESCRIPTION,
                LandmarksDbHelper.COLUMN_NAME_LATITUDE,
                LandmarksDbHelper.COLUMN_NAME_LONGDITUDE,
        };

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
                Random r = new Random();
                r.setSeed(System.currentTimeMillis());
                String id = Integer.toString(r.nextInt(1000000));
                landmark.setLandmarkId(id);
                String desc = cursor.getString(cursor.getColumnIndex(
                        LandmarksDbHelper.COLUMN_NAME_DESCRIPTION));
                landmark.setDescription(desc);
                Log.i(tag, "Logging landmark "+desc);
                landmark.setUser("Peder");
                landmark.setLatitude(cursor.getFloat(cursor.getColumnIndex(
                                                        LandmarksDbHelper.COLUMN_NAME_LATITUDE)));
                landmark.setLongitude(cursor.getFloat(cursor.getColumnIndex(
                                                        LandmarksDbHelper.COLUMN_NAME_LONGDITUDE)));
                mapper.save(landmark);
            }
        }
        cursor.close();
        return "yes";
    }
}


