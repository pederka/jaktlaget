package net.ddns.peder.jaktlaget.weather;

import android.content.Context;
import android.util.Log;
import android.util.Xml;
import com.google.android.gms.maps.model.LatLng;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * Created by peder on 5/26/17.
 */

public class YrWeatherHttpClient extends WeatherHttpClient {

    private static final String BASE_URL =
            "http://api.met.no/weatherapi/locationforecastlts/1.3/?lat=%2$s;lon=%2$s";
    private final static String tag = "YrWeatherClient";

    public YrWeatherHttpClient(Context context) {
        super(context);
    }

    @Override
    public WindResult getWindData(LatLng position) {
        HttpURLConnection con = null ;
        InputStream is = null;
        String FULL_URL = String.format(BASE_URL, position.latitude,
                                                    position.longitude);
        try {
            Log.d(tag, "Using URL: "+FULL_URL);
            con = (HttpURLConnection) ( new URL(FULL_URL)).openConnection();
            con.setRequestMethod("GET");
            con.setDoInput(true);
            con.setDoOutput(true);
            con.connect();

            // Let's read the response
            is = con.getInputStream();
            WindResult result = parseXml(is);
            is.close();
            con.disconnect();
            return result;
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

    private static WindResult parseXml(InputStream is) {
        try {

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();

            parser.setInput(is, null);
            String windDirection = null;
            String windSpeed = null;

            String currentTag = null;
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    currentTag = parser.getName();
                    if ("windDirection".equals(currentTag)) {
                        windDirection = parser.getAttributeValue(null, "deg");
                    }
                    if ("windSpeed".equals(currentTag)) {
                        windSpeed = parser.getAttributeValue(null, "mps");
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    if (windSpeed != null && windDirection != null) {
                        break;
                    }
                }
                eventType = parser.next();
            }


            if (windDirection != null && windSpeed != null) {
                return new WindResult(Float.parseFloat(windDirection), Float.parseFloat(windSpeed));
            } else {
                return null;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
