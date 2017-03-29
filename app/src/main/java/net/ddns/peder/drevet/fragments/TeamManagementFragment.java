package net.ddns.peder.drevet.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import net.ddns.peder.drevet.Constants;
import net.ddns.peder.drevet.R;

public class TeamManagementFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    private EditText userText;
    private EditText teamText;

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

        // Set ids if they exist
        userText = (EditText) view.findViewById(R.id.username_text);
        if (!userId.equals(Constants.DEFAULT_USER_ID)) {
            userText.setText(userId);
        }
        teamText = (EditText) view.findViewById(R.id.teamname_text);
        if (!teamId.equals(Constants.DEFAULT_TEAM_ID)) {
            teamText.setText(teamId);
        }

        Button saveTeamButton = (Button) view.findViewById(R.id.save_teamname_button);
        saveTeamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = userText.getText().toString().trim();
                String teamId = teamText.getText().toString().trim();
                if (teamId.equals("") || teamId.equals(Constants.DEFAULT_TEAM_ID) ||
                        userId.equals("") || userId.equals(Constants.DEFAULT_USER_ID)) {
                    Toast.makeText(getContext(), R.string.toast_invalid_name,
                            Toast.LENGTH_SHORT).show();
                    teamText.setText("");
                    userText.setText("");
                }
                else {
                    prefs.edit().putString(Constants.SHARED_PREF_TEAM_ID, teamId).apply();
                    prefs.edit().putString(Constants.SHARED_PREF_USER_ID, userId).apply();
                    Toast.makeText(getContext(), R.string.toast_name_saved,
                            Toast.LENGTH_SHORT).show();
                    teamText.setText(teamId);
                    userText.setText(userId);
                }
            }
        });
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
