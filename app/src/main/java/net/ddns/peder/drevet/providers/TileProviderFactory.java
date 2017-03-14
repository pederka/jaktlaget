package net.ddns.peder.drevet.providers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import android.util.Log;

public class TileProviderFactory {

    public static WMSTileProvider getOsgeoWmsTileProvider() {

        final String OSGEO_WMS = "http://wms.geonorge.no/skwms1/wms.topo2?";
        //final String OSGEO_WMS =  "http://localhost/geoserver/magnamaps/wms?service=WMS&version=1.1.0&request=GetMap&layers=magnamaps:bang_apartments&styles=&bbox=%f%f%f%f&width=256&height=256&crs=EPSG:4326&format=image/png&transparent=true";

        WMSTileProvider tileProvider = new WMSTileProvider(256, 256) {

            @Override
            public synchronized URL getTileUrl(int x, int y, int zoom) {
                double[] bbox = getBoundingBox(x, y, zoom);
                String s = String.format(Locale.US, OSGEO_WMS, bbox[MINX], bbox[MINY], bbox[MAXX], bbox[MAXY]);
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