package net.ddns.peder.drevet.fragments;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import net.ddns.peder.drevet.R;
import net.ddns.peder.drevet.adapters.LandmarksCursorAdapter;
import net.ddns.peder.drevet.adapters.LandmarksPagerAdapter;
import net.ddns.peder.drevet.database.LandmarksDbHelper;

public class AllLandmarksFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    private LandmarksPagerAdapter landmarksPagerAdapter;

    public AllLandmarksFragment() {
        // Required empty public constructor
    }

    public static AllLandmarksFragment newInstance(String param1, String param2) {
        AllLandmarksFragment fragment = new AllLandmarksFragment();
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
        View view = inflater.inflate(R.layout.fragment_alllandmarks, container, false);
        landmarksPagerAdapter =
                new LandmarksPagerAdapter(
                        getActivity().getSupportFragmentManager());
        ViewPager mViewPager = (ViewPager) view.findViewById(R.id.landmarks_pager);
        mViewPager.setAdapter(landmarksPagerAdapter);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.landmarks_tabs);
        tabLayout.setupWithViewPager(mViewPager);

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
