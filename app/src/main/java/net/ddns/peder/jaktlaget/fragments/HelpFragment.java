package net.ddns.peder.jaktlaget.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import net.ddns.peder.jaktlaget.MainActivity;
import net.ddns.peder.jaktlaget.R;

import androidx.fragment.app.Fragment;

public class HelpFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

    public HelpFragment() {
        // Required empty public constructor
    }

    public static HelpFragment newInstance(String param1, String param2) {
        HelpFragment fragment = new HelpFragment();
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
        View view = inflater.inflate(R.layout.fragment_help, container, false);

        ImageView help_1 = (ImageView) view.findViewById(R.id.help_1);
        ImageView help_2 = (ImageView) view.findViewById(R.id.help_2);
        ImageView help_3 = (ImageView) view.findViewById(R.id.help_3);
        ImageView help_4 = (ImageView) view.findViewById(R.id.help_4);
        ImageView help_5 = (ImageView) view.findViewById(R.id.help_5);
        ImageView help_6 = (ImageView) view.findViewById(R.id.help_6);

        help_1.setImageResource(R.drawable.help_1);
        help_2.setImageResource(R.drawable.help_2);
        help_3.setImageResource(R.drawable.help_3);
        help_4.setImageResource(R.drawable.help_4);
        help_5.setImageResource(R.drawable.help_5);
        help_6.setImageResource(R.drawable.help_6);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((MainActivity)getActivity()).setActionBarTitle("Hjelp");
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
