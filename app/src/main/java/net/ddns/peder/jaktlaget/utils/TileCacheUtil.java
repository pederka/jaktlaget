package net.ddns.peder.jaktlaget.utils;

import android.content.Context;

import com.jakewharton.disklrucache.DiskLruCache;

import net.ddns.peder.jaktlaget.Constants;

import java.io.File;
import java.io.IOException;

/**
 * Created by peder on 7/9/2017.
 */

public class TileCacheUtil {

    public static DiskLruCache initializeTileCache(Context context) throws IOException {
        // Initialize cache
        File cacheDir = context.getCacheDir();
        File cacheSubDir = new File(cacheDir, Constants.DISK_CACHE_SUBDIR);
        return DiskLruCache.open(cacheSubDir, 2, 3, Constants.DISK_CACHE_SIZE);
    }

    public static long getCacheMaxSize(DiskLruCache diskLruCache) {
        return diskLruCache.getMaxSize();
    }

    public static void setCacheMaxSize(DiskLruCache diskLruCache, long maxSize) {
        diskLruCache.setMaxSize(maxSize);
    }

    public static void deleteCache(DiskLruCache diskLruCache) throws IOException {
        diskLruCache.delete();
    }
}
