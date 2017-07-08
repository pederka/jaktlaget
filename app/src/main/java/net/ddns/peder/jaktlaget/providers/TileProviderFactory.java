package net.ddns.peder.jaktlaget.providers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

public class TileProviderFactory {

    public static WMSTileProvider getWmsTileProvider(Context context) {
        String map_type;
        if (context != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            map_type = sharedPreferences.getString("pref_map_type", "png");
        } else {
            map_type = "png";
        }
        final String WMS = "http://wms.geonorge.no/skwms1/wms.topo3?request=GetMap&version=1.3.0&layers=topo3_WMS&bbox=%f,%f,%f,%f&width=256&height=256&crs=EPSG:3857&format=image/"+map_type;
        WMSTileProvider tileProvider = new WMSTileProvider(256, 256) {
            @Override
            public synchronized URL getTileUrl(int x, int y, int zoom) {
                double[] bbox = getBoundingBox(x, y, zoom);
                String s = String.format(Locale.US, WMS, bbox[MINX], bbox[MINY], bbox[MAXX], bbox[MAXY]);
                URL url = null;
                try {
                    url = new URL(s);
                } catch (MalformedURLException e) {
                    throw new AssertionError(e);
                }
                return url;
            }
        };
        return tileProvider;
    }
}