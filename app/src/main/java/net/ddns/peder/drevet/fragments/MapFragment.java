package net.ddns.peder.drevet.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
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

import net.ddns.peder.drevet.R;
import net.ddns.peder.drevet.database.LandmarksDbHelper;
import net.ddns.peder.drevet.providers.TileProviderFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MapFragment extends Fragment implements OnMapReadyCallback,
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private OnFragmentInteractionListener mListener;
    private MapView mapView;
    private GoogleMap map;
    private final LatLngBounds NORWAY = new LatLngBounds(
            new LatLng(58.57, 3.71), new LatLng(71.51, 31.82));
    private GoogleApiClient mGoogleApiClient;
    private Location lastPos;
    private LocationRequest mLocationRequest;
    private String tag = "Map";
    private LandmarksDbHelper landmarksDbHelper;
    private SQLiteDatabase db;
    private boolean shared;

    public MapFragment() {
        // Required empty public constructor
    }

    public static MapFragment newInstance(String param1, String param2) {
        MapFragment fragment = new MapFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(tag, "Connected to location API");
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);

            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(5000); //5 seconds
            mLocationRequest.setFastestInterval(3000); //3 seconds
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        else {
            Toast.makeText(getContext(), "No permission", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onLocationChanged(Location location) {
        // Clear old location marker
        lastPos = location;
    }

    private void setUpMap() {
        TileProvider wmsTileProvider = TileProviderFactory.getWmsTileProvider();
        map.addTileOverlay(new TileOverlayOptions().tileProvider(wmsTileProvider));

        // Make sure the google map is not visible in the background
        map.setMapType(GoogleMap.MAP_TYPE_NONE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (lastPos != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastPos.getLatitude(),
                                                        lastPos.getLongitude()), 7));
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
        values.put(LandmarksDbHelper.COLUMN_NAME_TIME, date);
        values.put(LandmarksDbHelper.COLUMN_NAME_SHOWED, 1);
        values.put(LandmarksDbHelper.COLUMN_NAME_SHARED, shared);
        values.put(LandmarksDbHelper.COLUMN_NAME_DESCRIPTION, desc);
        values.put(LandmarksDbHelper.COLUMN_NAME_LATITUDE, latLng.latitude);
        values.put(LandmarksDbHelper.COLUMN_NAME_LONGDITUDE, latLng.longitude);
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
        if (lastPos == null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(NORWAY.getCenter(), 4));
        }
        else {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastPos.getLatitude(),
                    lastPos.getLongitude()), 7));
        }
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        }
        else {
            Toast.makeText(getContext(), "No permission", Toast.LENGTH_SHORT).show();
        }
        buildGoogleApiClient();
        mGoogleApiClient.connect();

        addLandMarks(map);

    }

    private void addLandMarks(GoogleMap map) {
        final String[] PROJECTION = {
            LandmarksDbHelper.COLUMN_NAME_ID,
            LandmarksDbHelper.COLUMN_NAME_SHOWED,
            LandmarksDbHelper.COLUMN_NAME_DESCRIPTION,
            LandmarksDbHelper.COLUMN_NAME_LATITUDE,
            LandmarksDbHelper.COLUMN_NAME_LONGDITUDE,
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
                        LandmarksDbHelper.COLUMN_NAME_LONGDITUDE));
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
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}
