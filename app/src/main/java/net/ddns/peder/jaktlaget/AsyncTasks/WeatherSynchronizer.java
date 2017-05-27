package net.ddns.peder.jaktlaget.AsyncTasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import net.ddns.peder.jaktlaget.interfaces.WeatherSyncCompleteListener;
import net.ddns.peder.jaktlaget.weather.OpenWeatherHttpClient;
import net.ddns.peder.jaktlaget.weather.WindResult;

import java.util.ArrayList;
import java.util.List;

public class WeatherSynchronizer extends AsyncTask<Void, Void, List<WindResult>>{
    private Context mContext;
    private final static String tag = "WeatherSyncronizer";
    private ProgressDialog dialog;
    private SharedPreferences sharedPrefs;
    private WeatherSyncCompleteListener weatherSyncCompleteListener;
    private List<LatLng> positions;


    public WeatherSynchronizer(Context context, WeatherSyncCompleteListener weatherSyncCompleteListener,
                               List<LatLng> positions) {
        mContext = context;

        this.weatherSyncCompleteListener = weatherSyncCompleteListener;
        this.positions = positions;

        dialog = new ProgressDialog(context);
    }

    @Override
    protected void onPreExecute() {
        this.dialog.setMessage("Henter vær..");
        this.dialog.show();
    }

    @Override
    protected List<WindResult> doInBackground(Void... params) {
        List<WindResult> results = new ArrayList<>();
        for (int i=0; i<positions.size(); i++) {
            results.add(OpenWeatherHttpClient.getWindData(positions.get(i)));
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


