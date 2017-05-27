package net.ddns.peder.jaktlaget.interfaces;

import com.google.android.gms.maps.model.LatLng;

import net.ddns.peder.jaktlaget.weather.WindResult;

import java.util.List;

/**
 * Created by peder on 5/26/17.
 */

public interface WeatherSyncCompleteListener {
    void onWeatherSyncComplete(List<LatLng> positions, List<WindResult> results);
}
