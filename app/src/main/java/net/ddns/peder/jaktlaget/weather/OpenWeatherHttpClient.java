package net.ddns.peder.jaktlaget.weather;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by peder on 5/26/17.
 */

public class OpenWeatherHttpClient extends WeatherHttpClient {

    private static final String BASE_URL =
                          "https://api.openweathermap.org/data/2.5/weather?lat=%1$s&lon=%2$s&appid=";
    private final static String tag = "OpenWeatherHttpClient";
    private static final String API_KEY = "c15b30390af4e93d81ba16a7dc110b01";

    public OpenWeatherHttpClient(Context context) {
        super(context);
    }

    @Override
    public WindResult getWindData(LatLng position) {
        HttpURLConnection con = null ;
        InputStream is = null;
        String FULL_URL = String.format(BASE_URL, position.latitude,
                                                    position.longitude)+API_KEY;
        try {
            con = (HttpURLConnection) ( new URL(FULL_URL)).openConnection();
            con.setRequestMethod("GET");
            con.setDoInput(true);
            con.setDoOutput(true);
            con.connect();

            // Let's read the response
            StringBuffer buffer = new StringBuffer();
            is = con.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ( (line = br.readLine()) != null )
                buffer.append(line + "rn");

            is.close();
            con.disconnect();
            return parseJson(buffer.toString());
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        finally {
            try { is.close(); } catch(Throwable t) {}
            try { con.disconnect(); } catch(Throwable t) {}
        }
    }

    private static WindResult parseJson(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            // We get weather info (This is an array)
            JSONObject jWind = jsonObject.getJSONObject("wind");
            float speed = (float)jWind.getDouble("speed");
            float heading = (float)jWind.getDouble("deg");
            return new WindResult(heading, speed);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
