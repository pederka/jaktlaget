package net.ddns.peder.jaktlaget.weather;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by peder on 5/27/17.
 */

public abstract class WeatherHttpClient {
    public abstract WindResult getWindData(LatLng position);
}
