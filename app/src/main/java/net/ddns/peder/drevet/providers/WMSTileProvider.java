package net.ddns.peder.drevet.providers;

import java.net.URLEncoder;
import com.google.android.gms.maps.model.UrlTileProvider;

public abstract class WMSTileProvider extends UrlTileProvider {

    // North-west corner of map
    private static final double[] TILE_ORIGIN = {-90, 90};
    // array indexes for that data
    private static final int ORIG_X = 0;
    private static final int ORIG_Y = 1; //

    // Size of world map in degrees
    private static final double MAP_SIZE_X = 180;
    private static final double MAP_SIZE_Y = 90;

    // array indexes for array to hold bounding boxes.
    protected static final int MINX = 0;
    protected static final int MAXX = 1;
    protected static final int MINY = 2;
    protected static final int MAXY = 3;

    // cql filters
    private String cqlString = "";

    // Construct with tile size in pixels, normally 256, see parent class.
    public WMSTileProvider(int x, int y) {
        super(x, y);
    }

    protected String getCql() {
        return URLEncoder.encode(cqlString);
    }

    public void setCql(String c) {
        cqlString = c;
    }

    // Return a web Mercator bounding box given tile x/y indexes and a zoom
    // level.
    protected double[] getBoundingBox(int x, int y, int zoom) {
        double tileSize_x = MAP_SIZE_X / Math.pow(2, zoom);
        double tileSize_y = MAP_SIZE_Y / Math.pow(2, zoom);
        double minx = TILE_ORIGIN[ORIG_X] + x * tileSize_x;
        double maxx = TILE_ORIGIN[ORIG_X] + (x + 1) * tileSize_x;
        double miny = TILE_ORIGIN[ORIG_Y] - (y + 1) * tileSize_y;
        double maxy = TILE_ORIGIN[ORIG_Y] - y * tileSize_y;

        double[] bbox = new double[4];
        bbox[MINX] = minx;
        bbox[MINY] = miny;
        bbox[MAXX] = maxx;
        bbox[MAXY] = maxy;

        return bbox;
    }
}