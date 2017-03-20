package net.ddns.peder.drevet.providers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

public class TileProviderFactory {

    public static WMSTileProvider getWmsTileProvider() {
        final String WMS = "http://wms.geonorge.no/skwms1/wms.topo2?request=GetMap&version=1.3.0&layers=topo2_WMS&bbox=%f,%f,%f,%f&width=256&height=256&crs=EPSG:3857&format=image/jpeg";
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