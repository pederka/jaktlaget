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
import net.ddns.peder.drevet.adapters.LandmarksCursorAdapter;
import net.ddns.peder.drevet.database.LandmarksDbHelper;

public class LandmarksFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    private LandmarksDbHelper landmarksDbHelper;
    private LandmarksCursorAdapter mAdapter;
    private ListView listView;
    private SQLiteDatabase db;

    private final static String[] PROJECTION = {
                LandmarksDbHelper.COLUMN_NAME_ID,
                LandmarksDbHelper.COLUMN_NAME_SHOWED,
                LandmarksDbHelper.COLUMN_NAME_SHARED,
                LandmarksDbHelper.COLUMN_NAME_DESCRIPTION,
    };

    public LandmarksFragment() {
        // Required empty public constructor
    }

    public static LandmarksFragment newInstance(String param1, String param2) {
        LandmarksFragment fragment = new LandmarksFragment();
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
        View view = inflater.inflate(R.layout.fragment_landmarks, container, false);

        landmarksDbHelper = new LandmarksDbHelper(getContext());
        db = landmarksDbHelper.getReadableDatabase();

        final String[] fromColumns = {LandmarksDbHelper.COLUMN_NAME_DESCRIPTION};

        final int[] toViews = {R.id.lm_list_desc};


        final Cursor cursor = db.query(LandmarksDbHelper.TABLE_NAME,
                                 PROJECTION,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null);


        mAdapter = new LandmarksCursorAdapter(getActivity(),
                            R.layout.lm_row,
                        cursor, fromColumns, toViews);

        listView = (ListView) view.findViewById(R.id.lm_list);
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
