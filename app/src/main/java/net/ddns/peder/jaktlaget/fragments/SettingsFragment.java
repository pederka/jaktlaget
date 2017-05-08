package net.ddns.peder.jaktlaget.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import net.ddns.peder.jaktlaget.MainActivity;
import net.ddns.peder.jaktlaget.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.fragment_settings);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((MainActivity)getActivity()).setActionBarTitle("Valg");
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
