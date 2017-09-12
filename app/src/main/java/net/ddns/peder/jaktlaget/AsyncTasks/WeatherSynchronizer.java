package net.ddns.peder.jaktlaget.AsyncTasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.model.LatLng;

import net.ddns.peder.jaktlaget.R;
import net.ddns.peder.jaktlaget.interfaces.WeatherSyncCompleteListener;
import net.ddns.peder.jaktlaget.weather.OpenWeatherHttpClient;
import net.ddns.peder.jaktlaget.weather.WeatherHttpClient;
import net.ddns.peder.jaktlaget.weather.WindResult;
import net.ddns.peder.jaktlaget.weather.YrWeatherHttpClient;

import java.util.ArrayList;
import java.util.List;

public class WeatherSynchronizer extends AsyncTask<Void, Void, List<WindResult>>{
    private Context mContext;
    private final static String tag = "WeatherSyncronizer";
    private ProgressDialog dialog;
    private SharedPreferences sharedPrefs;
    private WeatherSyncCompleteListener weatherSyncCompleteListener;
    private WeatherHttpClient weatherHttpClient;
    private List<LatLng> positions;


    public WeatherSynchronizer(Context context, WeatherSyncCompleteListener weatherSyncCompleteListener,
                               List<LatLng> positions) {
        mContext = context;

        this.weatherSyncCompleteListener = weatherSyncCompleteListener;
        this.positions = positions;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String weatherClient = sharedPreferences.getString(
                context.getResources().getString(R.string.pref_windprovider_key),
                context.getResources().getString(R.string.pref_windprovider_default));
        if (weatherClient.equals("yr.no")) {
            weatherHttpClient = new YrWeatherHttpClient(context);
        } else {
            weatherHttpClient = new OpenWeatherHttpClient(context);
        }

        dialog = new ProgressDialog(context);
    }

    @Override
    protected void onPreExecute() {
        this.dialog.setMessage(mContext.getString(R.string.weather_sync_dialog));
        this.dialog.show();
    }

    @Override
    protected List<WindResult> doInBackground(Void... params) {
        List<WindResult> results = new ArrayList<>();
        for (int i=0; i<positions.size(); i++) {
            results.add(weatherHttpClient.getWindData(positions.get(i)));
        }
        return results;
    }

    @Override
    protected void onPostExecute(List<WindResult> results) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        if (weatherSyncCompleteListener != null) {
                weatherSyncCompleteListener.onWeatherSyncComplete(positions, results);
        }
    }
}


