package net.ddns.peder.jaktlaget.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.ads.consent.ConsentForm;
import com.google.ads.consent.ConsentFormListener;
import com.google.ads.consent.ConsentStatus;

import net.ddns.peder.jaktlaget.Constants;
import net.ddns.peder.jaktlaget.MainActivity;
import net.ddns.peder.jaktlaget.R;

import java.net.MalformedURLException;
import java.net.URL;

import androidx.fragment.app.Fragment;

public class AboutFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    //private ConsentForm form;
    private static final String tag = "AboutFragment";

    public AboutFragment() {
        // Required empty public constructor
    }

    public static AboutFragment newInstance(String param1, String param2) {
        AboutFragment fragment = new AboutFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        URL privacyUrl = null;
        try {
            privacyUrl = new URL(Constants.privacy_url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        //form = new ConsentForm.Builder(getActivity(), privacyUrl)
        //.withListener(new ConsentFormListener() {
        //    @Override
        //    public void onConsentFormLoaded() {
        //        Log.i(tag, "Consent form loaded successfully");
        //    }

        //    @Override
        //    public void onConsentFormOpened() {
        //        Log.i(tag, "Consent form opened");
        //        // Consent form was displayed.
        //    }

        //    @Override
        //    public void onConsentFormClosed(
        //            ConsentStatus consentStatus, Boolean userPrefersAdFree) {
        //        Log.i(tag, "User closed consent form info updated");
        //        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //        if (consentStatus == ConsentStatus.PERSONALIZED) {
        //            Log.i(tag, "User has consented to personalized ads");
        //            prefs.edit()
        //                    .putInt(Constants.SHARED_PREF_AD_CONSENT, Constants.AD_PERSONALIZED)
        //                    .apply();
        //            ((MainActivity)getActivity()).requestAds(true);
        //        }
        //        else if (consentStatus == ConsentStatus.NON_PERSONALIZED) {
        //            Log.i(tag, "User has not consented to personalized ads");
        //            prefs.edit()
        //                    .putInt(Constants.SHARED_PREF_AD_CONSENT, Constants.AD_NONPERSONALIZED)
        //                    .apply();
        //            ((MainActivity)getActivity()).requestAds(false);
        //        }
        //        // Reload in case the user want to click the button again
        //        form.load();
        //    }

        //    @Override
        //    public void onConsentFormError(String errorDescription) {
        //        Log.e(tag, errorDescription);
        //        // Consent form error.
        //    }
        //})
        // .withPersonalizedAdsOption()
        // .withNonPersonalizedAdsOption()
        // .build();

        //form.load();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        //Button updateAdSettingsButton = (Button) view.findViewById(R.id.update_ad_settings_button);
        //updateAdSettingsButton.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View view) {
        //        form.show();
        //    }
        //});
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((MainActivity)getActivity()).setActionBarTitle("Om");
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
