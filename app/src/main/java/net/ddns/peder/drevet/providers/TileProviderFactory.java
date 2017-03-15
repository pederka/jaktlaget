package net.ddns.peder.drevet.providers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import android.util.Log;

public class TileProviderFactory {

    public static WMSTileProvider getOsgeoWmsTileProvider() {
        final String WMS = "http://wms.geonorge.no/skwms1/wms.topo2?request=GetMap&version=1.3.0&layers=topo2_WMS&bbox=%f%f%f%f&width=256&height=256&crs=EPSG:4326&format=image/png";
        //final String WMS = "http://wms.geonorge.no/skwms1/wms.topo2?request=GetMap&version=1.3.0&layers=topo2_WMS&bbox=61.973,6.783,62.05,6.95&width=256&height=256&crs=EPSG:4326&format=image/png";

        WMSTileProvider tileProvider = new WMSTileProvider(256, 256) {

            @Override
            public synchronized URL getTileUrl(int x, int y, int zoom) {
                double[] bbox = getBoundingBox(x, y, zoom);
                String s = String.format(Locale.US, WMS, bbox[MINX], bbox[MINY], bbox[MAXX], bbox[MAXY]);
                Log.d("WMSDEMO", s);
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