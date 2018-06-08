package net.ddns.peder.jaktlaget.providers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import net.ddns.peder.jaktlaget.R;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

public class TileProviderFactory {

    public static WMSTileProvider getWmsTileProvider(Context context) {
        String map_type;
        if (context != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            map_type = sharedPreferences.getString(
                    context.getResources().getString(R.string.pref_map_type_key),
                    context.getResources().getString(R.string.pref_map_type_default));
        } else {
            map_type = "png";
        }
        final String WMS = "https://openwms.statkart.no/skwms1/wms.topo4?request=GetMap&version=1.3.0&layers=topo4_WMS&bbox=%f,%f,%f,%f&width=256&height=256&crs=EPSG:3857&format=image/"+map_type;
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