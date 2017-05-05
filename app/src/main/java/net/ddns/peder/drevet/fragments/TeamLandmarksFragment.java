package net.ddns.peder.drevet.fragments;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import net.ddns.peder.drevet.AsyncTasks.DataSynchronizer;
import net.ddns.peder.drevet.R;
import net.ddns.peder.drevet.adapters.TeamLandmarksCursorAdapter;
import net.ddns.peder.drevet.database.TeamLandmarksDbHelper;
import net.ddns.peder.drevet.interfaces.OnSyncComplete;

public class TeamLandmarksFragment extends Fragment implements OnSyncComplete {
    private OnFragmentInteractionListener mListener;
    private TeamLandmarksDbHelper teamLandmarksDbHelper;
    private TeamLandmarksCursorAdapter mAdapter;
    private ListView listView;
    private SQLiteDatabase db;
    private SwipeRefreshLayout swipeRefreshLayout;
    private final static String[] PROJECTION = {
                TeamLandmarksDbHelper.COLUMN_NAME_ID,
                TeamLandmarksDbHelper.COLUMN_NAME_USER,
                TeamLandmarksDbHelper.COLUMN_NAME_SHOWED,
                TeamLandmarksDbHelper.COLUMN_NAME_DESCRIPTION,
                TeamLandmarksDbHelper.COLUMN_NAME_LATITUDE,
                TeamLandmarksDbHelper.COLUMN_NAME_LONGITUDE,
    };

    public TeamLandmarksFragment() {
        // Required empty public constructor
    }

    public static TeamLandmarksFragment newInstance(String param1, String param2) {
        TeamLandmarksFragment fragment = new TeamLandmarksFragment();
        return fragment;
    }

    @Override
    public void onSyncComplete(int result) {
        if (result == DataSynchronizer.SUCCESS) {
            // Update list
            TeamLandmarksDbHelper DbHelper = new TeamLandmarksDbHelper(getContext());
            SQLiteDatabase dbtmp = DbHelper.getReadableDatabase();
            final Cursor cursor = dbtmp.query(TeamLandmarksDbHelper.TABLE_NAME,
                    PROJECTION,
                    null,
                    null,
                    null,
                    null,
                    null);
            mAdapter.changeCursor(cursor);
            mAdapter.notifyDataSetChanged();
        }
        else {
            Toast.makeText(getContext(), R.string.toast_sync_failed, Toast.LENGTH_SHORT).show();
        }
        // Stop refresh animation
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_team_landmarks, container, false);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        DataSynchronizer dataSynchronizer = new DataSynchronizer(getContext(),
                                                    TeamLandmarksFragment.this, false);
                        dataSynchronizer.execute();
                    }
                }
        );

        teamLandmarksDbHelper = new TeamLandmarksDbHelper(getContext());
        db = teamLandmarksDbHelper.getReadableDatabase();

        final String[] fromColumns = {TeamLandmarksDbHelper.COLUMN_NAME_DESCRIPTION};

        final int[] toViews = {R.id.team_lm_list_desc};


        final Cursor cursor = db.query(TeamLandmarksDbHelper.TABLE_NAME,
                                 PROJECTION,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null);


        mAdapter = new TeamLandmarksCursorAdapter(getActivity(),
                            R.layout.team_lm_row,
                        cursor, fromColumns, toViews);

        listView = (ListView) view.findViewById(R.id.team_lm_list);
        listView.setAdapter(mAdapter);

        return view;
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

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}
