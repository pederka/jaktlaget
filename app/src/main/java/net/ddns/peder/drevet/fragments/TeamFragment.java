package net.ddns.peder.drevet.fragments;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import net.ddns.peder.drevet.R;
import net.ddns.peder.drevet.adapters.PositionCursorAdapter;
import net.ddns.peder.drevet.database.PositionsDbHelper;

public class TeamFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    private PositionsDbHelper positionsDbHelper;
    private SQLiteDatabase db;
    private PositionCursorAdapter mAdapter;
    private ListView listView;
    private final static String[] PROJECTION = {
                PositionsDbHelper.COLUMN_NAME_ID,
                PositionsDbHelper.COLUMN_NAME_USER,
                PositionsDbHelper.COLUMN_NAME_SHOWED,
                PositionsDbHelper.COLUMN_NAME_TIME,
    };

    public TeamFragment() {
        // Required empty public constructor
    }

    public static TeamFragment newInstance(String param1, String param2) {
        TeamFragment fragment = new TeamFragment();
        return fragment;
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
        listView.setAdapter(mAdapter);

        return view;
    }

    public void updateTeamList() {
        final Cursor cursor = db.query(PositionsDbHelper.TABLE_NAME,
                                 PROJECTION,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null);
        mAdapter.changeCursor(cursor);
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
