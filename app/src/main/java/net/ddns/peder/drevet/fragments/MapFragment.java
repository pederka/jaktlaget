package net.ddns.peder.drevet.fragments;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;

import net.ddns.peder.drevet.Constants;
import net.ddns.peder.drevet.MainActivity;
import net.ddns.peder.drevet.R;
import net.ddns.peder.drevet.database.LandmarksDbHelper;
import net.ddns.peder.drevet.providers.TileProviderFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private OnFragmentInteractionListener mListener;
    private MapView mapView;
    private GoogleMap map;
    private final LatLngBounds NORWAY = new LatLngBounds(
            new LatLng(58.57, 3.71), new LatLng(71.51, 31.82));
    private String tag = "Map";
    private LandmarksDbHelper landmarksDbHelper;
    private SQLiteDatabase db;
    private boolean shared;
    private SharedPreferences sharedPreferences;
    private Handler mHandler;
    private Marker myLocationMarker;

    public MapFragment() {
        // Required empty public constructor
    }

    public static MapFragment newInstance(String param1, String param2) {
        return new MapFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private Runnable updateMyPosition = new Runnable() {
        @Override
       public void run() {
            long currentTime = System.currentTimeMillis();
            long lastPositionTime = sharedPreferences.getLong(Constants.SHARED_PREF_TIME, 0);
            if ((currentTime-lastPositionTime)/1000 < 600 && map != null) {
                // Getting latitude and longitude of the last known location
                double latitude = (double) sharedPreferences.getFloat(Constants.SHARED_PREF_LAT, 0);
                double longitude = (double) sharedPreferences.getFloat(Constants.SHARED_PREF_LON, 0);
                // Creating a LatLng object for the current location
                LatLng latLng = new LatLng(latitude, longitude);
                // Clear old position
                if (myLocationMarker != null) {
                    myLocationMarker.remove();
                }
                // Set new position
                myLocationMarker = map.addMarker(new MarkerOptions()
                        .anchor(0.5f, 0.5f)
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.raw.my_location)));
            }
            mHandler.postDelayed(this, Constants.MAP_LOCATION_UPDATE_INTERVAL);
        }
    };



    private void setUpMap() {
        TileProvider wmsTileProvider = TileProviderFactory.getWmsTileProvider();
        map.addTileOverlay(new TileOverlayOptions().tileProvider(wmsTileProvider));

        // Make sure the google map is not visible in the background
        map.setMapType(GoogleMap.MAP_TYPE_NONE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mHandler = new Handler();
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        landmarksDbHelper = new LandmarksDbHelper(getContext());
        db = landmarksDbHelper.getReadableDatabase();
        try {
            if (map == null) {
                mapView = (MapView) view.findViewById(R.id.map);
                mapView.onCreate(savedInstanceState);
                mapView.onResume();
                if (((MainActivity)getActivity()).cameraPosition != null) {
                    map.moveCamera(CameraUpdateFactory.newCameraPosition((
                                                    (MainActivity)getActivity()).cameraPosition));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
         try {
            // Loading map
            if (map == null) {
                mapView.getMapAsync(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveLandmarkToDatabase(String desc, boolean shared, LatLng latLng) {
        ContentValues values = new ContentValues();
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.US);
        String date = df.format(c.getTime());
        Random r = new Random();
        r.setSeed(System.currentTimeMillis());
        String landmarkId = Integer.toString(r.nextInt(Constants.LANDMARK_ID_SIZE));
        values.put(LandmarksDbHelper.COLUMN_NAME_TIME, date);
        values.put(LandmarksDbHelper.COLUMN_NAME_SHOWED, 1);
        values.put(LandmarksDbHelper.COLUMN_NAME_SHARED, shared);
        values.put(LandmarksDbHelper.COLUMN_NAME_LANDMARKID, landmarkId);
        values.put(LandmarksDbHelper.COLUMN_NAME_DESCRIPTION, desc);
        values.put(LandmarksDbHelper.COLUMN_NAME_LATITUDE, latLng.latitude);
        values.put(LandmarksDbHelper.COLUMN_NAME_LONGITUDE, latLng.longitude);
        db.insert(LandmarksDbHelper.TABLE_NAME, null, values);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Do stuff with the map here!
        map = googleMap;

        // Setting a click event handler for the map
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
                final LatLng coords = latLng;
                shared = false;
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.lm_dialog_title)
                        .setMultiChoiceItems(R.array.lm_choices, null,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                                boolean isChecked) {
                                if (isChecked) {
                                    // If the user checked the item, add it to the selected items
                                    shared = true;
                                } else {
                                    // Else, if the item is already in the array, remove it
                                    shared = false;
                                }
                            }
                        }
                );
                final EditText input = new EditText(getContext());
                input.setHint(getString(R.string.description_hint));
                builder.setView(input);

                builder.setPositiveButton(R.string.lm_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String description = input.getText().toString();
                        saveLandmarkToDatabase(description, shared, coords);
                        Toast.makeText(getContext(), R.string.landmark_added, Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton(R.string.lm_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });

        // check if map is created successfully or not
        if (map != null) {
            setUpMap();
        }
        if (((MainActivity)getActivity()).cameraPosition != null) {
            map.moveCamera(CameraUpdateFactory.newCameraPosition((
                                        (MainActivity)getActivity()).cameraPosition));
        } else {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(NORWAY.getCenter(), 4));
        }

        addLandMarks(map);

        // Start update of own loaction
        mHandler.postDelayed(updateMyPosition, Constants.MAP_LOCATION_UPDATE_INTERVAL);

    }

    private void zoomToPosition(GoogleMap map) {
        double latitude = (double) sharedPreferences.getFloat(Constants.SHARED_PREF_LAT, 0);
        double longitude = (double) sharedPreferences.getFloat(Constants.SHARED_PREF_LON, 0);
        long lastPositionTime = sharedPreferences.getLong(Constants.SHARED_PREF_TIME, 0);
        long currentTime = System.currentTimeMillis();
        if ((currentTime-lastPositionTime)/1000 < 600) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 7));
        } else {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(NORWAY.getCenter(), 4));
        }
    }

    private void addLandMarks(GoogleMap map) {
        final String[] PROJECTION = {
            LandmarksDbHelper.COLUMN_NAME_ID,
            LandmarksDbHelper.COLUMN_NAME_SHOWED,
            LandmarksDbHelper.COLUMN_NAME_DESCRIPTION,
            LandmarksDbHelper.COLUMN_NAME_LATITUDE,
            LandmarksDbHelper.COLUMN_NAME_LONGITUDE,
        };

        String selection = LandmarksDbHelper.COLUMN_NAME_SHOWED + " = ?";
        String[] selectionArgs = { "1" };
        final Cursor cursor = db.query(LandmarksDbHelper.TABLE_NAME,
                         PROJECTION,
                         selection,
                         selectionArgs,
                         null,
                         null,
                         null);
        while (cursor.moveToNext()) {
            int showed = cursor.getInt(cursor.getColumnIndexOrThrow(
                                                        LandmarksDbHelper.COLUMN_NAME_SHOWED));
            if (showed > 0) {
                Float latitude = cursor.getFloat(cursor.getColumnIndexOrThrow(
                        LandmarksDbHelper.COLUMN_NAME_LATITUDE));
                Float longitude = cursor.getFloat(cursor.getColumnIndexOrThrow(
                        LandmarksDbHelper.COLUMN_NAME_LONGITUDE));
                LatLng pos = new LatLng(latitude, longitude);
                String description = cursor.getString(cursor.getColumnIndexOrThrow(
                        LandmarksDbHelper.COLUMN_NAME_DESCRIPTION));
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(pos);
                markerOptions.title(description);
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(
                                                        BitmapDescriptorFactory.HUE_RED));
                map.addMarker(markerOptions);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (map != null && ((MainActivity)getActivity()).cameraPosition != null) {
            map.moveCamera(CameraUpdateFactory.newCameraPosition((
                                                    (MainActivity)getActivity()).cameraPosition));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map != null) {
            map.moveCamera(CameraUpdateFactory.newCameraPosition(
                    ((MainActivity)getActivity()).cameraPosition));
        }
    }



    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        ((MainActivity)getActivity()).cameraPosition = map.getCameraPosition();
    }

    @Override
    public void onStop() {
        super.onStop();
        ((MainActivity)getActivity()).cameraPosition = map.getCameraPosition();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((MainActivity)getActivity()).cameraPosition = map.getCameraPosition();
    }
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}
