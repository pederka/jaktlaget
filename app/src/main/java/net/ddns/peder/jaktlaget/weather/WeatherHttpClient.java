package net.ddns.peder.jaktlaget.weather;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

import net.ddns.peder.jaktlaget.Constants;

/**
 * Created by peder on 5/27/17.
 */

public abstract class WeatherHttpClient {
    protected Context mContext;
    public WeatherHttpClient(Context context) {
        mContext = context;
    }

    public abstract WindResult getWindData(LatLng position);
}
