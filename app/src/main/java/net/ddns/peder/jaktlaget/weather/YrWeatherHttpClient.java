package net.ddns.peder.jaktlaget.weather;

import android.content.Context;
import android.util.Log;
import com.google.android.gms.maps.model.LatLng;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by peder on 5/26/17.
 */

public class YrWeatherHttpClient extends WeatherHttpClient {

    private static final String BASE_URL =
            "https://api.met.no/weatherapi/locationforecast/2.0/compact?lat=%1$s;lon=%2$s";
    private final static String tag = "YrWeatherClient";

    public YrWeatherHttpClient(Context context) {
        super(context);
    }

    @Override
    public WindResult getWindData(LatLng position) {
        HttpURLConnection con = null ;
        InputStream is = null;
        String FULL_URL = String.format(BASE_URL, String.format("%.4f", position.latitude),
                                                    String.format("%.4f", position.longitude));
        int count = 0;
        int maxTries = 4;
        while(true) {
            try {
                Log.d(tag, "Using URL: " + FULL_URL);
                con = (HttpsURLConnection) (new URL(FULL_URL)).openConnection();
                con.setRequestMethod("GET");
                con.setDoInput(true);
                con.setDoOutput(true);
                con.setRequestProperty("User-Agent", "JaktlagetApp peder.aursand@gmail.com");
                con.connect();

                // Let's read the response
                is = con.getInputStream();
                WindResult result = parseJson(is);
                is.close();
                con.disconnect();
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                if (++count > maxTries) {
                    return null;
                }
                else {
                    Log.d(tag, "Failed to get weather data, retrying ("+count+"/"+maxTries+")");
                }
            } finally {
                try {
                    is.close();
                } catch (Throwable t) {
                }
                try {
                    con.disconnect();
                } catch (Throwable t) {
                }
            }
        }
    }

    private static WindResult parseJson(InputStream is) {
        float windDirection;
        float windSpeed;
        try {
            String jsonString = convertInputStreamToString(is);
            Log.d("json received", jsonString);
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONObject propertiesObject = jsonObject.getJSONObject("properties");
            JSONArray tsArray = propertiesObject.getJSONArray("timeseries");
            JSONObject firstTsObject = (JSONObject)tsArray.get(0);
            JSONObject dataObject = firstTsObject.getJSONObject("data");
            JSONObject instantObject = dataObject.getJSONObject("instant");
            JSONObject detailsObject = instantObject.getJSONObject("details");
            windDirection = (float)detailsObject.getDouble("wind_from_direction");
            windSpeed = (float)detailsObject.getDouble("wind_speed");

            return new WindResult(windDirection, windSpeed);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Plain Java
    private static String convertInputStreamToString(InputStream is) throws IOException, IOException {

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int length;
        while ((length = is.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }

        // Java 1.1
        return result.toString(StandardCharsets.UTF_8.name());

        // Java 10
        // return result.toString(StandardCharsets.UTF_8);

    }
}
