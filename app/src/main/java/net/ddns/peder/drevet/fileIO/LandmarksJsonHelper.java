package net.ddns.peder.drevet.fileIO;

import android.content.Context;

import net.ddns.peder.drevet.Constants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * Created by peder on 26.03.2017.
 */

public class LandmarksJsonHelper {
    private Context mContext;
    private JSONArray landmarks;
    private static final String JSON_ID = "ID";
    private static final String JSON_USER = "User";
    private static final String JSON_TEAM = "Team";
    private static final String JSON_LAT = "Latitude";
    private static final String JSON_LON = "Longitude";
    private static final String JSON_DESC = "Description";
    private static final String JSON_SHOWED = "Showed";
    private static final String JSON_SHARED = "Shared";

    public LandmarksJsonHelper(Context context) {
        mContext = context;
        try {
            JSONObject object = new JSONObject(readFromFile(Constants.LOCAL_LANDMARKS_FILE));
            landmarks = object.getJSONArray("landmarks");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public ArrayList<Landmark> getLandmarks() {
        ArrayList<Landmark> landmarkList = new ArrayList<>();
        try {
            for (int i = 0; i < landmarks.length(); i++) {
                JSONObject lm = landmarks.getJSONObject(i);
                landmarkList.add(parseJSONLandmark(lm));
            }
            return landmarkList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Landmark parseJSONLandmark(JSONObject JSONLandmark) {
        Landmark landmark = new Landmark();
        try {
            landmark.setId(JSONLandmark.getLong(JSON_ID));
            landmark.setUser(JSONLandmark.getString(JSON_USER));
            landmark.setUser(JSONLandmark.getString(JSON_TEAM));
            landmark.setLatitude(JSONLandmark.getDouble(JSON_LAT));
            landmark.setLongitude(JSONLandmark.getLong(JSON_LON));
            landmark.setDescription(JSONLandmark.getString(JSON_DESC));
            landmark.setShowed(JSONLandmark.getInt(JSON_SHOWED));
            landmark.setShared(JSONLandmark.getInt(JSON_SHARED));
            return landmark;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void addLandmark(Landmark landmark) {


    }

    private String readFromFile(String filename) {
        FileInputStream inputStream;
        try {
            inputStream = mContext.openFileInput(filename);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            return new String(buffer, "UTF-8");

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void writeToFile(String filename, String string) {
        FileOutputStream outputStream;
        try {
            outputStream = mContext.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(string.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
