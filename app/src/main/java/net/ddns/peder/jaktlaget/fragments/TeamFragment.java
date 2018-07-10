package net.ddns.peder.jaktlaget.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import net.ddns.peder.jaktlaget.AsyncTasks.DataSynchronizer;
import net.ddns.peder.jaktlaget.AsyncTasks.HttpsDataSynchronizer;
import net.ddns.peder.jaktlaget.AsyncTasks.JaktlagetAPISynchronizer;
import net.ddns.peder.jaktlaget.Constants;
import net.ddns.peder.jaktlaget.R;
import net.ddns.peder.jaktlaget.adapters.PositionCursorAdapter;
import net.ddns.peder.jaktlaget.database.PositionsDbHelper;
import net.ddns.peder.jaktlaget.interfaces.OnSyncComplete;

public class TeamFragment extends Fragment implements OnSyncComplete {
    private OnFragmentInteractionListener mListener;
    private PositionsDbHelper positionsDbHelper;
    private SQLiteDatabase db;
    private PositionCursorAdapter mAdapter;
    private ListView listView;
    private final String tag = "TeamFragment";
    private final static String[] PROJECTION = {
                PositionsDbHelper.COLUMN_NAME_ID,
                PositionsDbHelper.COLUMN_NAME_USER,
                PositionsDbHelper.COLUMN_NAME_SHOWED,
                PositionsDbHelper.COLUMN_NAME_TIME,
    };
    private SwipeRefreshLayout swipeRefreshLayout;

    public TeamFragment() {
        // Required empty public constructor
    }

    public static TeamFragment newInstance(String param1, String param2) {
        return new TeamFragment();
    }

    @Override
    public void onSyncComplete(int result) {
        if (result == HttpsDataSynchronizer.SUCCESS) {
            updateTeamList();
        }
        else if (result == HttpsDataSynchronizer.FAILED_TIMEOUT) {
            Toast.makeText(getContext(), R.string.toast_sync_timeout, Toast.LENGTH_SHORT).show();
        }
        else if (result == HttpsDataSynchronizer.FAILED_CODE) {
            // Reset team and team code
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(
                            getContext());
            preferences.edit().putString(Constants.SHARED_PREF_TEAM_CODE, "").apply();
            preferences.edit().putString(Constants.SHARED_PREF_TEAM_ID, "").apply();
        }
        else {
            Toast.makeText(getContext(), R.string.toast_sync_failed, Toast.LENGTH_SHORT).show();
        }

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
        View view = inflater.inflate(R.layout.fragment_team, container, false);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        HttpsDataSynchronizer dataSynchronizer = new HttpsDataSynchronizer(getContext(),
                                                    TeamFragment.this);
                        dataSynchronizer.execute();
                    }
                }
        );


        positionsDbHelper = new PositionsDbHelper(getContext());
        db = positionsDbHelper.getReadableDatabase();

        final String[] fromColumns = {PositionsDbHelper.COLUMN_NAME_USER};

        final int[] toViews = {R.id.lm_list_desc};


        final Cursor cursor = db.query(PositionsDbHelper.TABLE_NAME,
                                 PROJECTION,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null);


        mAdapter = new PositionCursorAdapter(getActivity(),
                            R.layout.pos_row,
                        cursor, fromColumns, toViews);

        listView = (ListView) view.findViewById(R.id.lm_list);
        LinearLayout emptyText = (LinearLayout) view.findViewById(android.R.id.empty);
        listView.setEmptyView(emptyText);
        listView.setAdapter(mAdapter);

        return view;
    }


    public void updateTeamList() {
        Log.d(tag, "Updates team list");
        PositionsDbHelper DbHelper = new PositionsDbHelper(getContext());
        SQLiteDatabase dbtmp = DbHelper.getReadableDatabase();
        final Cursor cursor = dbtmp.query(PositionsDbHelper.TABLE_NAME,
                                 PROJECTION,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null);
        mAdapter.changeCursor(cursor);
        mAdapter.notifyDataSetChanged();
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
