package net.ddns.peder.drevet.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import net.ddns.peder.drevet.AsyncTasks.DataSynchronizer;
import net.ddns.peder.drevet.Constants;
import net.ddns.peder.drevet.R;
import net.ddns.peder.drevet.adapters.TeamPagerAdapter;
import net.ddns.peder.drevet.interfaces.OnSyncComplete;

public class TeamManagementFragment extends Fragment implements OnSyncComplete {
    private EditText userText;
    private EditText teamText;
    private TextInputLayout textInputLayoutUser;
    private TextInputLayout textInputLayoutTeam;
    private OnFragmentInteractionListener mListener;
    private TeamFragment teamFragment;

    @Override
    public void onSyncComplete() {
        teamFragment.updateTeamList();
        Toast.makeText(getContext(), "Sync complete", Toast.LENGTH_SHORT).show();
    }

    public TeamManagementFragment() {
        // Required empty public constructor
    }

    public static TeamManagementFragment newInstance(String param1, String param2) {
        TeamManagementFragment fragment = new TeamManagementFragment();
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
        View view = inflater.inflate(R.layout.fragment_team_management, container, false);
        // Read userid from preferences
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String userId = prefs.getString(Constants.SHARED_PREF_USER_ID, Constants.DEFAULT_USER_ID);
        // Read teamid from preferences
        String teamId = prefs.getString(Constants.SHARED_PREF_TEAM_ID, Constants.DEFAULT_TEAM_ID);

        ViewPager vp = (ViewPager) getActivity().findViewById(R.id.team_pager);
        TeamPagerAdapter teamPagerAdapter = (TeamPagerAdapter)vp.getAdapter();
        teamFragment = (TeamFragment)teamPagerAdapter.getRegisteredFragment(1);

        // Set ids if they exist
        userText = (EditText) view.findViewById(R.id.username_text);
        if (!userId.equals(Constants.DEFAULT_USER_ID)) {
            userText.setText(userId);
        }
        teamText = (EditText) view.findViewById(R.id.teamname_text);
        if (!teamId.equals(Constants.DEFAULT_TEAM_ID)) {
            teamText.setText(teamId);
        }

        textInputLayoutUser = (TextInputLayout) view.findViewById(R.id.text_input_layout_user);
        textInputLayoutTeam = (TextInputLayout) view.findViewById(R.id.text_input_layout_team);

        Button saveTeamButton = (Button) view.findViewById(R.id.save_teamname_button);
        saveTeamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataSynchronizer dataSynchronizer = new DataSynchronizer(getContext(),
                                TeamManagementFragment.this);
                dataSynchronizer.execute();
                submitForm();
            }
        });

        return view;
    }

    private void submitForm() {
        if (!validateName()) {
            return;
        }
        if (!validateTeam()) {
            return;
        }
        String userId = userText.getText().toString().trim();
        String teamId = teamText.getText().toString().trim();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.edit().putString(Constants.SHARED_PREF_TEAM_ID, teamId).apply();
        prefs.edit().putString(Constants.SHARED_PREF_USER_ID, userId).apply();
        Toast.makeText(getContext(), R.string.toast_name_saved, Toast.LENGTH_SHORT).show();
    }

    private boolean validateName() {
        if (userText.getText().toString().trim().isEmpty() ||
                userText.getText().toString().equals(Constants.DEFAULT_USER_ID)) {
            userText.setError(getString(R.string.toast_invalid_username));
            return false;
        } else {
            textInputLayoutUser.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validateTeam() {
        if (teamText.getText().toString().trim().isEmpty() ||
                teamText.getText().toString().equals(Constants.DEFAULT_TEAM_ID)) {
            teamText.setError(getString(R.string.toast_invalid_teamname));
            return false;
        } else {
            textInputLayoutTeam.setErrorEnabled(false);
        }
        return true;
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
