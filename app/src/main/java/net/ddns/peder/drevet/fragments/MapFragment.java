package net.ddns.peder.drevet.fragments;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
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
import net.ddns.peder.drevet.database.PositionsDbHelper;
import net.ddns.peder.drevet.database.TeamLandmarksDbHelper;
import net.ddns.peder.drevet.providers.TileProviderFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
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
    private TeamLandmarksDbHelper teamLandmarksDbHelper;
    private PositionsDbHelper positionsDbHelper;
    private SQLiteDatabase db;
    private SQLiteDatabase tldb;
    private SQLiteDatabase posdb;
    private boolean shared;
    private SharedPreferences sharedPreferences;
    private Handler mHandler;
    private Marker myLocationMarker;
    private List<Marker> userMarkerList;
    private ImageButton landmarkButton;
    private boolean landmarks_toggled;
    private ImageButton teamButton;
    private boolean team_toggled;
    private ImageButton weatherButton;
    private boolean weather_toggled;
    private ImageButton myPositionButton;
    private List<Marker> markerList;

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
            // Avoid using old locations
            if (currentTime-lastPositionTime < 1800000 && map != null) {
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
                // Button should show when recent location is available
                myPositionButton.setVisibility(View.VISIBLE);
            } else {
                // If position is too old, both the location marker and the position button should
                // be removed
                if (myLocationMarker != null) {
                    myLocationMarker.remove();
                }
                myPositionButton.setVisibility(View.INVISIBLE);
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
        markerList = new ArrayList<>();

        View view = inflater.inflate(R.layout.fragment_map, container, false);

        myPositionButton = (ImageButton) view.findViewById(R.id.button_my_location);
        myPositionButton.setColorFilter(Color.argb(255, 255, 255, 255)); // White Tint
        myPositionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (map != null && myLocationMarker != null) {
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                            getContext());
                    double latitude = (double) sharedPreferences.getFloat(Constants.SHARED_PREF_LAT,
                            0);
                    double longitude = (double) sharedPreferences.getFloat(Constants.SHARED_PREF_LON,
                            0);
                    LatLng pos = new LatLng(latitude, longitude);
                    map.moveCamera(CameraUpdateFactory.newLatLng(pos));
                }
            }
        });
        if (myLocationMarker == null) {
            //((ViewManager)myPositionButton.getParent()).removeView(myPositionButton);
            myPositionButton.setVisibility(View.INVISIBLE);
        }

        landmarkButton = (ImageButton) view.findViewById(R.id.button_landmarks);
        landmarkButton.setColorFilter(Color.argb(255, 255, 255, 255)); // White Tint
        if (sharedPreferences.getBoolean(Constants.SHARED_PREF_LANDMARK_TOGGLE, true)) {
            landmarks_toggled = true;
            landmarkButton.setBackgroundResource(R.drawable.buttonshape);
        } else {
            landmarks_toggled = false;
            landmarkButton.setBackgroundResource(R.drawable.buttonshape_inactive);
        }
        landmarkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                                                                                getContext());
                if (landmarks_toggled) {
                    landmarks_toggled = false;
                    for (int i=0; i<markerList.size(); i++) {
                        markerList.get(i).remove();
                    }
                    sharedPreferences.edit().putBoolean(Constants.SHARED_PREF_LANDMARK_TOGGLE,
                                            false).apply();
                    landmarkButton.setBackgroundResource(R.drawable.buttonshape_inactive);
                } else {
                    landmarks_toggled = true;
                    addLandMarks(map);
                    sharedPreferences.edit().putBoolean(Constants.SHARED_PREF_LANDMARK_TOGGLE,
                                            true).apply();
                    landmarkButton.setBackgroundResource(R.drawable.buttonshape);
                }
            }
        });
        teamButton = (ImageButton) view.findViewById(R.id.button_team);
        teamButton.setColorFilter(Color.argb(255, 255, 255, 255)); // White Tint
        if (sharedPreferences.getBoolean(Constants.SHARED_PREF_TEAM_TOGGLE, true)) {
            team_toggled = true;
            teamButton.setBackgroundResource(R.drawable.buttonshape);
        } else {
            team_toggled = false;
            teamButton.setBackgroundResource(R.drawable.buttonshape_inactive);
        }
        teamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                                                                                getContext());
                if (team_toggled) {
                    team_toggled = false;
                    sharedPreferences.edit().putBoolean(Constants.SHARED_PREF_TEAM_TOGGLE,
                                            false).apply();
                    teamButton.setBackgroundResource(R.drawable.buttonshape_inactive);
                } else {
                    team_toggled = true;
                    sharedPreferences.edit().putBoolean(Constants.SHARED_PREF_TEAM_TOGGLE,
                                            true).apply();
                    teamButton.setBackgroundResource(R.drawable.buttonshape);
                }
            }
        });
        weatherButton = (ImageButton) view.findViewById(R.id.button_weather);
        weatherButton.setColorFilter(Color.argb(255, 255, 255, 255)); // White Tint
        if (sharedPreferences.getBoolean(Constants.SHARED_PREF_WEATHER_TOGGLE, true)) {
            weather_toggled = true;
            weatherButton.setBackgroundResource(R.drawable.buttonshape);
        } else {
            weather_toggled = false;
            weatherButton.setBackgroundResource(R.drawable.buttonshape_inactive);
        }
        weatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                                                                                getContext());
                if (weather_toggled) {
                    weather_toggled = false;
                    sharedPreferences.edit().putBoolean(Constants.SHARED_PREF_WEATHER_TOGGLE,
                                            false).apply();
                    weatherButton.setBackgroundResource(R.drawable.buttonshape_inactive);
                } else {
                    weather_toggled = true;
                    sharedPreferences.edit().putBoolean(Constants.SHARED_PREF_WEATHER_TOGGLE,
                                            true).apply();
                    weatherButton.setBackgroundResource(R.drawable.buttonshape);
                }
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        landmarksDbHelper = new LandmarksDbHelper(getContext());
        db = landmarksDbHelper.getReadableDatabase();
        teamLandmarksDbHelper = new TeamLandmarksDbHelper(getContext());
        tldb = teamLandmarksDbHelper.getReadableDatabase();
        positionsDbHelper = new PositionsDbHelper(getContext());
        posdb = positionsDbHelper.getReadableDatabase();
        userMarkerList = new ArrayList<>();
        // Get height of map
        try {
            if (map == null) {
                mapView = (MapView) view.findViewById(R.id.map);
                mapView.onCreate(savedInstanceState);
                mapView.onResume();
            }
            else {
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

        //Disable Map Toolbar:
        map.getUiSettings().setMapToolbarEnabled(false);

        // Setting a click event handler for the map
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
                final LatLng coords = latLng;
                int mapHeight = mapView.getHeight();
                Projection projection = map.getProjection();
                Point point = projection.toScreenLocation(latLng);
                point.set(point.x, point.y-mapHeight/3);
                map.moveCamera(CameraUpdateFactory.newLatLng(projection.fromScreenLocation(point)));
                shared = false;
                final MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.raw.my_landmark));
                final Marker tempMarker = map.addMarker(markerOptions);
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
                        markerOptions.title(description);
                        Marker finalMarker = map.addMarker(markerOptions);
                        markerList.add(finalMarker);
                        saveLandmarkToDatabase(description, shared, coords);
                        Toast.makeText(getContext(), R.string.landmark_added, Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton(R.string.lm_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        tempMarker.remove();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });

        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng latLng) {
                Projection projection = map.getProjection();
                Point point = projection.toScreenLocation(latLng);
                int mapHeight = mapView.getHeight();
                for(final Marker marker : markerList) {
                    Point markerPoint = projection.toScreenLocation(
                                        new LatLng(marker.getPosition().latitude,
                                           marker.getPosition().longitude));
                    if(Math.abs(point.x - markerPoint.x) < 0.05*mapHeight &&
                            Math.abs(point.y - markerPoint.y) < 0.05*mapHeight) {
                        final String selection = LandmarksDbHelper.COLUMN_NAME_ID + " = ?";
                        final String[] selectionArgs = {""+marker.getTag()};

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        String desc = marker.getTitle();
                        builder.setTitle(getString(R.string.lm_title));
                        final EditText input = new EditText(getContext());
                        input.setText(desc);
                        builder.setView(input);
                        final Marker mker = marker;
                        builder.setPositiveButton(R.string.lm_update, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                ContentValues contentValues = new ContentValues();
                                String desc_new = input.getText().toString();
                                contentValues.put(LandmarksDbHelper.COLUMN_NAME_DESCRIPTION, desc_new);
                                db.update(LandmarksDbHelper.TABLE_NAME, contentValues, selection, selectionArgs);
                                mker.setTitle(desc_new);
                            }
                        });
                        builder.setNegativeButton(R.string.lm_delete, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                                db.delete(LandmarksDbHelper.TABLE_NAME, selection, selectionArgs);
                                mker.remove();
                                markerList.remove(marker);
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        break;
                    }
                }

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
        if (landmarks_toggled) {
            addLandMarks(map);
        }
        addTeamLandmarks(map);
        if (team_toggled) {
            updateTeamPositions(map);
        }

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
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.raw.my_landmark));
            Marker marker = map.addMarker(markerOptions);
            markerList.add(marker);
            marker.setTag(cursor.getInt(cursor.getColumnIndexOrThrow(LandmarksDbHelper.COLUMN_NAME_ID)));
        }
        cursor.close();
    }

    private void addTeamLandmarks(GoogleMap map) {
        final String[] PROJECTION = {
            TeamLandmarksDbHelper.COLUMN_NAME_ID,
            TeamLandmarksDbHelper.COLUMN_NAME_SHOWED,
            TeamLandmarksDbHelper.COLUMN_NAME_USER,
            TeamLandmarksDbHelper.COLUMN_NAME_DESCRIPTION,
            TeamLandmarksDbHelper.COLUMN_NAME_LATITUDE,
            TeamLandmarksDbHelper.COLUMN_NAME_LONGITUDE,
        };

        String selection = TeamLandmarksDbHelper.COLUMN_NAME_SHOWED + " = ?";
        String[] selectionArgs = { "1" };
        final Cursor cursor = tldb.query(TeamLandmarksDbHelper.TABLE_NAME,
                         PROJECTION,
                         selection,
                         selectionArgs,
                         null,
                         null,
                         null);
        while (cursor.moveToNext()) {
            Float latitude = cursor.getFloat(cursor.getColumnIndexOrThrow(
                    TeamLandmarksDbHelper.COLUMN_NAME_LATITUDE));
            Float longitude = cursor.getFloat(cursor.getColumnIndexOrThrow(
                    TeamLandmarksDbHelper.COLUMN_NAME_LONGITUDE));
            LatLng pos = new LatLng(latitude, longitude);
            String description = cursor.getString(cursor.getColumnIndexOrThrow(
                    TeamLandmarksDbHelper.COLUMN_NAME_DESCRIPTION));
            String user = cursor.getString(cursor.getColumnIndexOrThrow(
                    TeamLandmarksDbHelper.COLUMN_NAME_USER));
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(pos);
            markerOptions.title(user+": "+description);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.raw.other_landmark));
            map.addMarker(markerOptions);
        }
        cursor.close();
    }

    private void updateTeamPositions(GoogleMap map) {
        final String[] PROJECTION = {
            PositionsDbHelper.COLUMN_NAME_ID,
            PositionsDbHelper.COLUMN_NAME_SHOWED,
            PositionsDbHelper.COLUMN_NAME_USER,
            PositionsDbHelper.COLUMN_NAME_LATITUDE,
            PositionsDbHelper.COLUMN_NAME_LONGITUDE,
        };

        String selection = PositionsDbHelper.COLUMN_NAME_SHOWED + " = ?";
        String[] selectionArgs = { "1" };
        final Cursor cursor = posdb.query(PositionsDbHelper.TABLE_NAME,
                         PROJECTION,
                         selection,
                         selectionArgs,
                         null,
                         null,
                         null);
        userMarkerList.clear();
        while (cursor.moveToNext()) {
            Float latitude = cursor.getFloat(cursor.getColumnIndexOrThrow(
                    PositionsDbHelper.COLUMN_NAME_LATITUDE));
            Float longitude = cursor.getFloat(cursor.getColumnIndexOrThrow(
                    PositionsDbHelper.COLUMN_NAME_LONGITUDE));
            LatLng pos = new LatLng(latitude, longitude);
            String user = cursor.getString(cursor.getColumnIndexOrThrow(
                    PositionsDbHelper.COLUMN_NAME_USER));
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(pos);
            markerOptions.title(user);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.raw.other_location));
            userMarkerList.add(map.addMarker(markerOptions));
        }
        cursor.close();
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
        if (map != null && ((MainActivity)getActivity()).cameraPosition != null) {
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
        if (map != null && ((MainActivity)getActivity()).cameraPosition != null) {
            ((MainActivity) getActivity()).cameraPosition = map.getCameraPosition();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (map != null && ((MainActivity)getActivity()).cameraPosition != null) {
            ((MainActivity) getActivity()).cameraPosition = map.getCameraPosition();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (map != null) {
            ((MainActivity) getActivity()).cameraPosition = map.getCameraPosition();
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}
