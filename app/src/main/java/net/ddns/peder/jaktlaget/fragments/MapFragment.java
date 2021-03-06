package net.ddns.peder.jaktlaget.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;

import net.ddns.peder.jaktlaget.AsyncTasks.WeatherSynchronizer;
import net.ddns.peder.jaktlaget.Constants;
import net.ddns.peder.jaktlaget.MainActivity;
import net.ddns.peder.jaktlaget.R;
import net.ddns.peder.jaktlaget.database.LandmarksDbHelper;
import net.ddns.peder.jaktlaget.database.PositionsDbHelper;
import net.ddns.peder.jaktlaget.database.TeamLandmarksDbHelper;
import net.ddns.peder.jaktlaget.interfaces.WeatherSyncCompleteListener;
import net.ddns.peder.jaktlaget.providers.CachedTileProvider;
import net.ddns.peder.jaktlaget.providers.TileProviderFactory;
import net.ddns.peder.jaktlaget.utils.ScaleBar;
import net.ddns.peder.jaktlaget.weather.WindResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import static net.ddns.peder.jaktlaget.utils.BitmapUtil.createPureTextIcon;
import static net.ddns.peder.jaktlaget.utils.BitmapUtil.getBitmapFromVectorDrawable;

public class MapFragment extends Fragment implements OnMapReadyCallback,
        WeatherSyncCompleteListener {
    private OnFragmentInteractionListener mListener;
    private MapView mapView;
    private GoogleMap map;
    private final LatLngBounds NORWAY = new LatLngBounds(
            new LatLng(58.57, 3.71), new LatLng(71.51, 31.82));
    private String tag = "Map";
    private SQLiteDatabase db;
    private SQLiteDatabase tldb;
    private SQLiteDatabase posdb;
    private boolean shared;
    private SharedPreferences sharedPreferences;
    private Handler mHandler;
    private Marker myLocationMarker;
    private List<Marker> userMarkerList;
    private List<Marker> userNameMarkerList;
    private FloatingActionButton landmarkButton;
    private boolean landmarks_toggled;
    private FloatingActionButton teamButton;
    private boolean team_toggled;
    private FloatingActionButton weatherButton;
    private boolean weather_toggled;
    private FloatingActionButton lineButton;
    private boolean line_toggled;
    private FloatingActionMenu menu;
    private ImageButton myPositionButton;
    private List<Marker> markerList;
    private List<Marker> teamMarkerList;
    private Polyline traceLine;
    private List<Polyline> teamTraceLines;
    private Bitmap myselfBitmap;
    private Bitmap otherBitmap;
    private Bitmap otherInactiveBitmap;
    private Bitmap myLandmarkBitmap;
    private Bitmap otherLandmarkBitmap;
    private Bitmap windBitmap;
    private int colorMe;
    private int colorOther;
    private int colorInactive;
    private int MY_PERMISSIONS_REQUEST;
    private List<Marker> windMarkerList;
    private List<Marker> windSpeedMarkerList;
    private long time_last_weather_sync;
    private LatLng navigateTo;
    private long team_inactive_limit;

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

                if (getActivity() != null && ((MainActivity)getActivity()).isActive()) {
                    ((MainActivity) getActivity()).addToMyLocationHistory(latLng);
                    // Make polyline trace
                    if (traceLine != null) {
                        hideMyTraceLine();
                    }
                    if (line_toggled) {
                        showMyTraceLine();
                    }
                }
                // Clear old position
                if (myLocationMarker != null) {
                    myLocationMarker.remove();
                }
                // Set new position
                myLocationMarker = map.addMarker(new MarkerOptions()
                        .anchor(0.5f, 0.5f)
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.fromBitmap(myselfBitmap)));
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

    private Runnable updateTeamPositions = new Runnable() {
        @Override
        public void run() {
            if (map != null && getActivity() != null) {
                if (team_toggled) {
                    updateTeamPositions(map);
                    if (line_toggled) {
                        showTeamTraceLine();
                    }
                }
            }
            mHandler.postDelayed(this, Constants.MAP_TEAM_POSITION_UPDATE_INTERVAL);
        }
    };

    private Runnable updateTeamLandmarks = new Runnable() {
        @Override
        public void run() {
            if (map != null && getActivity() != null) {
                if (landmarks_toggled) {
                    addTeamLandmarks(map);
                }
            }
            mHandler.postDelayed(this, Constants.MAP_TEAM_POSITION_UPDATE_INTERVAL);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Request permissions
        if (getActivity() != null && ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST);
        }

        // Get any location to navigate to
        double lat = 0.0;
        double lon = 0.0;
        Bundle arguments = getArguments();
        if (arguments != null) {
            Log.d(tag, "Got bundle");
            lat = getArguments().getDouble("latitude");
            lon = getArguments().getDouble("longitude");
        }
        if (lat != 0.0 && lon != 0.0) {
            navigateTo = new LatLng(lat, lon);
        }

        LandmarksDbHelper landmarksDbHelper = new LandmarksDbHelper(getContext());
        db = landmarksDbHelper.getWritableDatabase();
        TeamLandmarksDbHelper teamLandmarksDbHelper = new TeamLandmarksDbHelper(getContext());
        tldb = teamLandmarksDbHelper.getReadableDatabase();
        PositionsDbHelper positionsDbHelper = new PositionsDbHelper(getContext());
        posdb = positionsDbHelper.getReadableDatabase();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        team_inactive_limit = 60000*Long.parseLong(Objects.requireNonNull(sharedPreferences.getString(
                requireContext().getResources().getString(R.string.pref_hideteamlimit_key),
                requireContext().getResources().getString(R.string.pref_hideteamlimit_default))));

        userMarkerList = new ArrayList<>();
        userNameMarkerList = new ArrayList<>();
        mHandler = new Handler();
        markerList = new ArrayList<>();
        teamMarkerList = new ArrayList<>();
        windMarkerList = new ArrayList<>();
        windSpeedMarkerList = new ArrayList<>();

        time_last_weather_sync = 0;

        // Create icons for map
        colorMe = getResources().getColor(R.color.mapMe);
        colorOther = getResources().getColor(R.color.mapOther);
        colorInactive = getResources().getColor(R.color.mapInactive);

        myselfBitmap = getBitmapFromVectorDrawable(R.drawable.ic_my_location_black_24dp, colorMe,
                getContext());
        otherBitmap = getBitmapFromVectorDrawable(R.drawable.ic_person_black_24dp, colorOther,
                getContext());
        myLandmarkBitmap = getBitmapFromVectorDrawable(R.drawable.ic_star_black_24dp, colorMe,
                getContext());
        otherLandmarkBitmap = getBitmapFromVectorDrawable(R.drawable.ic_star_black_24dp, colorOther,
                getContext());
        otherInactiveBitmap = getBitmapFromVectorDrawable(R.drawable.ic_person_black_24dp, colorInactive,
                getContext());
        windBitmap = getBitmapFromVectorDrawable(R.drawable.ic_arrow_upward_black_24dp,
                R.color.black, getContext());

        // Configure buttons
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

        menu = (FloatingActionMenu) view.findViewById(R.id.menu);

        lineButton = (FloatingActionButton) view.findViewById(R.id.menu_item_trace);
        if (sharedPreferences.getBoolean(Constants.SHARED_PREF_LINE_TOGGLE, true)) {
            line_toggled = true;
            lineButton.setLabelText(getString(R.string.hide_traces));
        } else {
            line_toggled = false;
            lineButton.setColorNormal(getResources().getColor(R.color.colorInactive));
            lineButton.setLabelText(getString(R.string.show_traces));
        }
        lineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                                                                                getContext());
                if (line_toggled) {
                    line_toggled = false;
                    if (traceLine != null) {
                        hideMyTraceLine();
                    }
                    hideTeamTraceLine();
                    sharedPreferences.edit().putBoolean(Constants.SHARED_PREF_LINE_TOGGLE,
                                            false).apply();
                    lineButton.setColorNormal(getResources().getColor(R.color.colorInactive));
                    lineButton.setLabelText(getString(R.string.show_traces));
                } else {
                    line_toggled = true;
                    if (traceLine != null) {
                        showMyTraceLine();
                    }
                    showTeamTraceLine();
                    sharedPreferences.edit().putBoolean(Constants.SHARED_PREF_LINE_TOGGLE,
                                            true).apply();
                    lineButton.setColorNormal(getResources().getColor(R.color.colorAccent));
                    lineButton.setLabelText(getString(R.string.hide_traces));
                }
            }
        });
        FloatingActionButton deleteLineButton = (FloatingActionButton) view.findViewById(R.id.menu_item_delete_trace);
        deleteLineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.trace_remove_dialog);
                builder.setPositiveButton(R.string.trace_remove, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ((MainActivity)getActivity()).clearMyLocationHistory();
                        ((MainActivity)getActivity()).clearTeamLocationHistory();
                        // Delete from pshared preferences
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(
                                getContext()).edit();
                        editor.remove(Constants.SHARED_PREF_LOCATION_HISTORY);
                        editor.remove(Constants.SHARED_PREF_TEAM_LOCATION_HISTORY);
                        editor.apply();
                        hideMyTraceLine();
                        hideTeamTraceLine();
                        Toast.makeText(getContext(), R.string.traces_deleted, Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton(R.string.trace_remove_abort, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        landmarkButton = (FloatingActionButton) view.findViewById(R.id.menu_item_landmarks);
        if (sharedPreferences.getBoolean(Constants.SHARED_PREF_LANDMARK_TOGGLE, true)) {
            landmarks_toggled = true;
            landmarkButton.setLabelText(getString(R.string.hide_landmarks));
        } else {
            landmarks_toggled = false;
            landmarkButton.setColorNormal(getResources().getColor(R.color.colorInactive));
            landmarkButton.setLabelText(getString(R.string.show_landmarks));
        }
        landmarkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                                                                                getContext());
                // Explain how to add landmarks if first time
                if (sharedPreferences.getBoolean(Constants.SHARED_PREF_FIRST_TIME_LANDMARK, true)) {
                    sharedPreferences.edit().putBoolean(Constants.SHARED_PREF_FIRST_TIME_LANDMARK,
                            false).apply();
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.landmarks_info);
                    builder.setMessage(R.string.landmarks_info_message);
                    builder.setPositiveButton(R.string.landmarks_info_ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                if (landmarks_toggled) {
                    landmarks_toggled = false;
                    for (int i=0; i<markerList.size(); i++) {
                        markerList.get(i).remove();
                    }
                    for (int i=0; i<teamMarkerList.size(); i++) {
                        teamMarkerList.get(i).remove();
                    }
                    sharedPreferences.edit().putBoolean(Constants.SHARED_PREF_LANDMARK_TOGGLE,
                                            false).apply();
                    landmarkButton.setColorNormal(getResources().getColor(R.color.colorInactive));
                    landmarkButton.setLabelText(getString(R.string.show_landmarks));
                } else {
                    landmarks_toggled = true;
                    addLandMarks(map);
                    addTeamLandmarks(map);
                    sharedPreferences.edit().putBoolean(Constants.SHARED_PREF_LANDMARK_TOGGLE,
                                            true).apply();
                    landmarkButton.setColorNormal(getResources().getColor(R.color.colorAccent));
                    landmarkButton.setLabelText(getString(R.string.hide_landmarks));
                }
            }
        });
        teamButton = (FloatingActionButton) view.findViewById(R.id.menu_item_team);
        if (sharedPreferences.getBoolean(Constants.SHARED_PREF_TEAM_TOGGLE, true)) {
            team_toggled = true;
            teamButton.setLabelText(getString(R.string.hide_team_members));
        } else {
            teamButton.setColorNormal(getResources().getColor(R.color.colorInactive));
            teamButton.setLabelText(getString(R.string.show_team_members));
            team_toggled = false;
        }
        teamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                                                                                getContext());
                if (team_toggled) {
                    // Clear users
                    hideTeamPositions();
                    // Clear any team trace lines
                    hideTeamTraceLine();
                    team_toggled = false;
                    sharedPreferences.edit().putBoolean(Constants.SHARED_PREF_TEAM_TOGGLE,
                                            false).apply();
                    teamButton.setColorNormal(getResources().getColor(R.color.colorInactive));
                    teamButton.setLabelText(getString(R.string.show_team_members));
                } else {
                    updateTeamPositions(map);
                    // Show trace lines if toggled
                    if (line_toggled) {
                        showTeamTraceLine();
                    }
                    team_toggled = true;
                    sharedPreferences.edit().putBoolean(Constants.SHARED_PREF_TEAM_TOGGLE,
                                            true).apply();
                    teamButton.setColorNormal(getResources().getColor(R.color.colorAccent));
                    teamButton.setLabelText(getString(R.string.hide_team_members));
                }
            }
        });
        weatherButton = (FloatingActionButton) view.findViewById(R.id.menu_item_wind);
        weather_toggled = false;
        weatherButton.setColorNormal(getResources().getColor(R.color.colorInactive));
        weatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (weather_toggled) {
                    weather_toggled = false;
                    hideWeatherIcons();
                    weatherButton.setColorNormal(getResources().getColor(R.color.colorInactive));
                    weatherButton.setLabelText(getString(R.string.get_weather));
                } else {
                    if (System.currentTimeMillis() - time_last_weather_sync >
                            Constants.WEATHER_SYNC_COOLDOWN) {
                        time_last_weather_sync = System.currentTimeMillis();
                        showWeatherIcons();
                        weather_toggled = true;
                        menu.close(false);
                        weatherButton.setColorNormal(getResources().getColor(R.color.colorAccent));
                        weatherButton.setLabelText(getString(R.string.hide_weather));
                    } else {
                        Toast.makeText(getContext(), getString(R.string.toast_weather_cooldown),
                                Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){

        // Get height of map
        try {
            if (map == null) {
                mapView = (MapView) view.findViewById(R.id.map);
                mapView.onCreate(savedInstanceState);
                mapView.onResume();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //Log.d("Tag", "Destroying map fragment. Cleaning up databases.");
        //if (db != null) {
        //    db.close();
        //}
        //if (posdb != null) {
        //    posdb.close();
        //}
        //if (tldb != null) {
        //    tldb.close();
        //}
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
        ((MainActivity)getActivity()).setActionBarTitle("Kart");
        ((MainActivity)getActivity()).navigationView.setCheckedItem(R.id.nav_map);
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
        db.beginTransaction();
        db.insert(LandmarksDbHelper.TABLE_NAME, null, values);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private void setUpMap() {
        //TileProvider wmsTileProvider = TileProviderFactory.getWmsTileProvider(getContext());
        CachedTileProvider cachedTileProvider = new CachedTileProvider("wms",
                            TileProviderFactory.getWmsTileProvider(getContext()),
                ((MainActivity)getActivity()).getTileCache(), getContext());
        map.addTileOverlay(new TileOverlayOptions().tileProvider(cachedTileProvider));

        // Make sure the google map is not visible in the background
        map.setMapType(GoogleMap.MAP_TYPE_NONE);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Do stuff with the map here!
        map = googleMap;

        //Disable Map Toolbar:
        map.getUiSettings().setMapToolbarEnabled(false);

        // Show team immediately if active and toggled
        if (getActivity() != null && ((MainActivity)getActivity()).isActive()) {
            if (team_toggled) {
                updateTeamPositions(map);
                if (line_toggled) {
                    showTeamTraceLine();
                }
            }
        }

        // Add scale bar
        RelativeLayout container = (RelativeLayout) getView().findViewById(R.id.map_container);
        float density = getContext().getResources().getDisplayMetrics().xdpi/160f;
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                (int)(Constants.SCALE_WIDTH*density),
                40*(int)density);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        ScaleBar mScaleBar = new ScaleBar(getContext(), map);
        mScaleBar.setLayoutParams(params);
        mScaleBar.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        container.addView(mScaleBar);

        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                // First, check if an existing landmark was clicked
                Log.d(tag, "Markerlist "+markerList.toString());
                boolean found = false;
                Projection projection = map.getProjection();
                Point point = projection.toScreenLocation(latLng);
                int mapHeight = mapView.getHeight();
                // Don't bother searching for nearby markers if not toggled
                if (landmarks_toggled) {
                    for (final Marker marker : markerList) {
                        Point markerPoint = projection.toScreenLocation(
                                new LatLng(marker.getPosition().latitude,
                                        marker.getPosition().longitude));
                        if (Math.abs(point.x - markerPoint.x) < 0.05 * mapHeight &&
                                Math.abs(point.y - markerPoint.y) < 0.05 * mapHeight) {
                            found = true;
                            final String selection = LandmarksDbHelper.COLUMN_NAME_DESCRIPTION + " = ?";
                            final String[] selectionArgs = {"" + marker.getTitle()};
                            final String[] PROJECTION = {
                                    LandmarksDbHelper.COLUMN_NAME_ID,
                                    LandmarksDbHelper.COLUMN_NAME_SHARED,
                                    LandmarksDbHelper.COLUMN_NAME_DESCRIPTION,
                            };
                            // Query database to get shared status
                            final Cursor cursor = db.query(LandmarksDbHelper.TABLE_NAME,
                                    PROJECTION,
                                    selection,
                                    selectionArgs,
                                    null,
                                    null,
                                    null);
                            cursor.moveToFirst();
                            shared = cursor.getInt(cursor.getColumnIndexOrThrow(
                                    LandmarksDbHelper.COLUMN_NAME_SHARED)) > 0;
                            cursor.close();
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            String desc = marker.getTitle();
                            builder.setTitle(getString(R.string.lm_title));
                            boolean[] checked_list = new boolean[1];
                            if (shared) {
                                Arrays.fill(checked_list, true);
                            } else {
                                Arrays.fill(checked_list, false);
                            }
                            builder.setMultiChoiceItems(R.array.lm_choices, checked_list,
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
                                    });
                            final EditText input = new EditText(getContext());
                            input.setText(desc);
                            input.setMaxLines(1);
                            input.setInputType(InputType.TYPE_CLASS_TEXT);
                            builder.setView(input);
                            builder.setPositiveButton(R.string.lm_update, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    ContentValues contentValues = new ContentValues();
                                    String desc_new = input.getText().toString();
                                    contentValues.put(LandmarksDbHelper.COLUMN_NAME_DESCRIPTION,
                                            desc_new);
                                    contentValues.put(LandmarksDbHelper.COLUMN_NAME_SHARED, shared);
                                    db.update(LandmarksDbHelper.TABLE_NAME, contentValues, selection,
                                            selectionArgs);
                                    addLandMarks(map);
                                }
                            });
                            builder.setNegativeButton(R.string.lm_delete, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // User cancelled the dialog
                                    db.delete(LandmarksDbHelper.TABLE_NAME, selection, selectionArgs);
                                    marker.setVisible(false);
                                    marker.remove();
                                    //markerList.remove(marker);
                                    addLandMarks(map);
                                }
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            break;
                        }
                    }
                }
                // If existing marker not found, create new one
                if (!found) {
                    final LatLng coords = latLng;
                    point.set(point.x, point.y-mapHeight/3);
                    map.moveCamera(CameraUpdateFactory.newLatLng(projection.fromScreenLocation(point)));
                    shared = false;
                    final MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(myLandmarkBitmap));
                    markerOptions.anchor(0.5f, 0.5f);
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
                    input.setMaxLines(1);
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    input.setHint(getString(R.string.description_hint));
                    builder.setView(input);

                    builder.setPositiveButton(R.string.lm_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            String description = input.getText().toString();
                            saveLandmarkToDatabase(description, shared, coords);
                            Toast.makeText(getContext(), R.string.landmark_added, Toast.LENGTH_SHORT).show();
                            tempMarker.remove();
                            addLandMarks(map);
                            addTeamLandmarks(map);
                            // Toogle landmarks if needed
                            if (!landmarks_toggled) {
                                landmarks_toggled = true;
                                sharedPreferences.edit().putBoolean(Constants.SHARED_PREF_LANDMARK_TOGGLE,
                                        true).apply();
                                landmarkButton.setColorNormal(getResources().getColor(R.color.colorAccent));
                                landmarkButton.setLabelText(getString(R.string.hide_landmarks));
                            }

                        }
                    });
                    builder.setNegativeButton(R.string.lm_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            tempMarker.remove();
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
            }
        });

        // check if map is created successfully or not
        if (map != null) {
            setUpMap();
        }

        updateMapPosition();

        if (landmarks_toggled) {
            addLandMarks(map);
            addTeamLandmarks(map);
        }
        if (team_toggled) {
            updateTeamPositions(map);
        }

        // Start update of own and team locations
        //mHandler.postDelayed(updateMyPosition, Constants.MAP_LOCATION_UPDATE_INTERVAL);
        //mHandler.postDelayed(updateTeamPositions, Constants.MAP_TEAM_POSITION_UPDATE_INTERVAL);
        //mHandler.postDelayed(updateTeamLandmarks, Constants.MAP_TEAM_POSITION_UPDATE_INTERVAL);

    }

    private void zoomToPosition(GoogleMap map) {
        double latitude = (double) sharedPreferences.getFloat(Constants.SHARED_PREF_LAT, 0);
        double longitude = (double) sharedPreferences.getFloat(Constants.SHARED_PREF_LON, 0);
        if (latitude != 0 && longitude != 0) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 9));
        }
        else {
            // If no position then just show Norway
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(NORWAY, 0));
        }
    }

    private void zoomToLatLng(GoogleMap map, LatLng position) {
        Log.d(tag, "Zooming to position "+position.latitude+" "+position.longitude);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 12));
    }

    private void addLandMarks(GoogleMap map) {
        // Remove old markers
        for (Marker m : markerList) {
            m.remove();
        }
        markerList.clear();
        final String[] PROJECTION = {
            LandmarksDbHelper.COLUMN_NAME_ID,
            LandmarksDbHelper.COLUMN_NAME_SHOWED,
            LandmarksDbHelper.COLUMN_NAME_DESCRIPTION,
            LandmarksDbHelper.COLUMN_NAME_LATITUDE,
            LandmarksDbHelper.COLUMN_NAME_LONGITUDE,
        };

        String selection = LandmarksDbHelper.COLUMN_NAME_SHOWED + " = ?";
        String[] selectionArgs = { "1" };
        Cursor cursor = db.query(LandmarksDbHelper.TABLE_NAME,
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
            markerOptions.anchor(0.5f, 0.5f);
            markerOptions.position(pos);
            markerOptions.title(description);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(myLandmarkBitmap));
            Marker marker = map.addMarker(markerOptions);
            markerList.add(marker);
            marker.setTag(cursor.getInt(cursor.getColumnIndexOrThrow(LandmarksDbHelper.COLUMN_NAME_ID)));
        }
        cursor.close();
    }

    private void addTeamLandmarks(GoogleMap map) {
        Log.d(tag, "Updating team landmarks on map");
        final String[] PROJECTION = {
            TeamLandmarksDbHelper.COLUMN_NAME_ID,
            TeamLandmarksDbHelper.COLUMN_NAME_SHOWED,
            TeamLandmarksDbHelper.COLUMN_NAME_USER,
            TeamLandmarksDbHelper.COLUMN_NAME_DESCRIPTION,
            TeamLandmarksDbHelper.COLUMN_NAME_LATITUDE,
            TeamLandmarksDbHelper.COLUMN_NAME_LONGITUDE,
        };

        Cursor cursor = tldb.query(TeamLandmarksDbHelper.TABLE_NAME,
                         PROJECTION,
                         null,
                         null,
                         null,
                         null,
                         null);
        hideTeamLandmarks();
        teamMarkerList.clear();
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
            markerOptions.anchor(0.5f, 0.5f);
            markerOptions.title(user+": "+description);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(otherLandmarkBitmap));
            Marker marker = map.addMarker(markerOptions);
            teamMarkerList.add(marker);
        }
        cursor.close();
    }

    private void hideTeamPositions() {
        for (int i=0; i<userMarkerList.size(); i++) {
            userMarkerList.get(i).remove();
            userNameMarkerList.get(i).remove();
        }
    }


    private void hideTeamLandmarks() {
        for (int i=0; i<teamMarkerList.size(); i++) {
            teamMarkerList.get(i).remove();
        }
    }

    private void updateTeamPositions(GoogleMap map) {
        if (map == null) {
            return;
        }
        Log.d(tag, "Updating team positions on map");
        final String[] PROJECTION = {
            PositionsDbHelper.COLUMN_NAME_ID,
            PositionsDbHelper.COLUMN_NAME_SHOWED,
            PositionsDbHelper.COLUMN_NAME_USER,
            PositionsDbHelper.COLUMN_NAME_TIME,
            PositionsDbHelper.COLUMN_NAME_LATITUDE,
            PositionsDbHelper.COLUMN_NAME_LONGITUDE,

        };

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                getContext());

        String selection = PositionsDbHelper.COLUMN_NAME_SHOWED + " = ?";
        String[] selectionArgs = { "1" };
        Cursor cursor = posdb.query(PositionsDbHelper.TABLE_NAME,
                         PROJECTION,
                         selection,
                         selectionArgs,
                         null,
                         null,
                         null);
        hideTeamPositions();
        userMarkerList.clear();
        userNameMarkerList.clear();
        while (cursor.moveToNext()) {
            Float latitude = cursor.getFloat(cursor.getColumnIndexOrThrow(
                    PositionsDbHelper.COLUMN_NAME_LATITUDE));
            Float longitude = cursor.getFloat(cursor.getColumnIndexOrThrow(
                    PositionsDbHelper.COLUMN_NAME_LONGITUDE));
            Long time = cursor.getLong(cursor.getColumnIndexOrThrow(
                    PositionsDbHelper.COLUMN_NAME_TIME));
            // Skip any team member with invalid positions
            if (latitude == 0.0 && longitude == 0.0) {
                continue;
            }
            LatLng pos = new LatLng(latitude, longitude);
            String user = cursor.getString(cursor.getColumnIndexOrThrow(
                    PositionsDbHelper.COLUMN_NAME_USER));
            ((MainActivity)getActivity()).addToTeamLocationHistory(user, pos);
            MarkerOptions markerOptions = new MarkerOptions();
            MarkerOptions nameMarkerOptions = new MarkerOptions();
            markerOptions.position(pos);
            markerOptions.anchor(0.5f, 0.5f);
            //markerOptions.title(user);
            if (System.currentTimeMillis()-time > team_inactive_limit) {
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(otherInactiveBitmap));
                nameMarkerOptions.icon(createPureTextIcon(user, colorInactive));
            } else {
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(otherBitmap));
                nameMarkerOptions.icon(createPureTextIcon(user, colorOther));
            }
            userMarkerList.add(map.addMarker(markerOptions));
            // Add name
            nameMarkerOptions.position(pos);
            nameMarkerOptions.anchor(0.5f, 1.5f);
            userNameMarkerList.add(map.addMarker(nameMarkerOptions));
        }
        cursor.close();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mHandler.postDelayed(updateMyPosition, Constants.MAP_LOCATION_UPDATE_INTERVAL);
        mHandler.postDelayed(updateTeamPositions, Constants.MAP_TEAM_POSITION_UPDATE_INTERVAL);
        mHandler.postDelayed(updateTeamLandmarks, Constants.MAP_TEAM_POSITION_UPDATE_INTERVAL);
        // Show team immediately if active and toggled
        if (getActivity() != null && ((MainActivity)getActivity()).isActive()) {
            if (team_toggled) {
                updateTeamPositions(map);
                if (line_toggled) {
                    showTeamTraceLine();
                }
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
        if (map != null) {
            ((MainActivity) getActivity()).cameraPosition = map.getCameraPosition();
        }
        mHandler.removeCallbacks(updateMyPosition);
        mHandler.removeCallbacks(updateTeamPositions);
        mHandler.removeCallbacks(updateTeamLandmarks);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (map != null) {
            ((MainActivity) getActivity()).cameraPosition = map.getCameraPosition();
        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private void updateMapPosition() {
        // Prioritized list of where to zoom in. As last resort show all of Norway
        if (map == null) {
            return;
        }
        if (navigateTo != null) {
            // Some time the map fragment is given a specific position to zoom to
            zoomToLatLng(map, navigateTo);
            navigateTo = null;
        }
        else if (getActivity() != null && ((MainActivity)getActivity()).cameraPosition != null) {
            // If a previous camera position is found, use that
            map.moveCamera(CameraUpdateFactory.newCameraPosition((
                                        (MainActivity)getActivity()).cameraPosition));
        } else {
            // Last resort is zoom to position
            zoomToPosition(map);
        }
    }

    private void showWeatherIcons() {
        if (map == null) {
            return;
        }
        int mapHeight = mapView.getHeight();
        int mapWidth = mapView.getWidth();
        Projection projection = map.getProjection();
        List<Point> arrowPoints = new ArrayList<>();
        arrowPoints.add(new Point(mapWidth/4, mapHeight/4));
        arrowPoints.add(new Point(mapWidth*3/4, mapHeight/4));
        arrowPoints.add(new Point(mapWidth*3/4, mapHeight*3/4));
        arrowPoints.add(new Point(mapWidth/4, mapHeight*3/4));

        List<LatLng> positions = new ArrayList<>();
        for (Point point : arrowPoints) {
            positions.add(projection.fromScreenLocation(point));
        }

        WeatherSynchronizer weatherSynchronizer = new WeatherSynchronizer(getContext(), this,
                                                    positions);
        weatherSynchronizer.execute();
    }

    @Override
    public void onWeatherSyncComplete(List<LatLng> positions, List<WindResult> results) {
        for (int i=0; i<positions.size(); i++) {
            // Get wind at location
            WindResult result = results.get(i);
            if (result != null) {
                float bearing = result.getBearing();
                float speed = result.getSpeed();
                MarkerOptions windArrowOptions = new MarkerOptions()
                        .position(positions.get(i))
                        .icon(BitmapDescriptorFactory.fromBitmap(windBitmap))
                        .anchor(0.5f, 0.5f)
                        .rotation(bearing-180.0f)
                        .flat(true);
                windMarkerList.add(map.addMarker(windArrowOptions));
                // Add wind speed
                MarkerOptions speedMarkerOptions = new MarkerOptions();
                speedMarkerOptions.position(positions.get(i));
                speedMarkerOptions.anchor(0.5f, 2.0f);
                speedMarkerOptions.icon(createPureTextIcon(Float.toString(speed) + " m/s", Color.BLACK));
                windSpeedMarkerList.add(map.addMarker(speedMarkerOptions));
            }
        }
    }

    private void hideWeatherIcons() {
        if (windMarkerList != null) {
            for (Marker marker : windMarkerList) {
                marker.remove();
            }
            for (Marker speedMarker : windSpeedMarkerList) {
                speedMarker.remove();
            }
            windMarkerList.clear();
            windSpeedMarkerList.clear();
        }
    }

    private void showMyTraceLine() {
        if (map != null && getActivity() != null) {
            traceLine = map.addPolyline(new PolylineOptions()
                    .addAll(((MainActivity) getActivity()).getMyLocationHistory())
                    .width(8)
                    .color(colorMe));
            // To keep the over the map layers
            traceLine.setZIndex(1000);
        }
    }

    private void hideMyTraceLine() {
        if (map != null && traceLine != null) {
            traceLine.remove();
        }
    }

    private void showTeamTraceLine() {
        if (map != null && getActivity() != null) {
            if (teamTraceLines == null) {
                teamTraceLines = new ArrayList<>();
            }
            // Find users that are not showed
            final String selection = PositionsDbHelper.COLUMN_NAME_SHOWED + " = ?";
            final String[] selectionArgs = {"0"};
            final String[] PROJECTION = {
                    PositionsDbHelper.COLUMN_NAME_ID,
                    PositionsDbHelper.COLUMN_NAME_USER,
                    LandmarksDbHelper.COLUMN_NAME_SHOWED,
            };
            // Query database to get shared status. Make blacklist
            final Cursor cursor = posdb.query(PositionsDbHelper.TABLE_NAME,
                            PROJECTION,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            null);
            List<String> blackList = new ArrayList<>();
            while (cursor.moveToNext()) {
                blackList.add(cursor.getString(cursor.getColumnIndexOrThrow(
                                                            PositionsDbHelper.COLUMN_NAME_USER)));
            }
            cursor.close();
            hideTeamTraceLine();
            teamTraceLines.clear();
            Map<String, List<LatLng>> teamLocationHistory =
                                        ((MainActivity) getActivity()).getTeamLocationHistory();
            if (teamLocationHistory != null) {
                List<String> users = new ArrayList<>();
                users.addAll(teamLocationHistory.keySet());
                for (int i = 0; i < teamLocationHistory.size(); i++) {
                    if (!blackList.contains(users.get(i))) {
                        Polyline line = map.addPolyline(new PolylineOptions()
                                .addAll(teamLocationHistory.get(users.get(i)))
                                .width(8)
                                .color(colorOther)
                                .zIndex(999)
                        );
                        teamTraceLines.add(line);
                    }
                }
            }
        }
    }

    private void hideTeamTraceLine() {
        if (map != null && teamTraceLines != null) {
            for (Polyline line : teamTraceLines) {
                line.remove();
            }
        }
    }
}
