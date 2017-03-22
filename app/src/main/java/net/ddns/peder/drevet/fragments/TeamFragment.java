package net.ddns.peder.drevet.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import net.ddns.peder.drevet.Constants;
import net.ddns.peder.drevet.R;

public class TeamFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

    private EditText userText;
    private EditText teamText;

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

        // Read userid from preferences
        final SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
        String userId = prefs.getString(Constants.PREF_USER_ID, Constants.DEFAULT_USER_ID);
        // Read teamid from preferences
        String teamId = prefs.getString(Constants.PREF_TEAM_ID, Constants.DEFAULT_TEAM_ID);

        // Set ids if they exist
        userText = (EditText) view.findViewById(R.id.username_text);
        if (!userId.equals(Constants.DEFAULT_USER_ID)) {
            userText.setText(userId);
        }
        teamText = (EditText) view.findViewById(R.id.teamname_text);
        if (!teamId.equals(Constants.DEFAULT_TEAM_ID)) {
            teamText.setText(teamId);
        }

        Button saveUserButton = (Button) view.findViewById(R.id.save_username_button);
        saveUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = userText.getText().toString();
                if (userId.equals("") || userId.equals(Constants.DEFAULT_USER_ID)) {
                    Toast.makeText(getContext(), R.string.toast_invalid_username,
                                    Toast.LENGTH_SHORT).show();
                    userText.setText("");
                }
                else {
                    prefs.edit().putString(Constants.PREF_USER_ID, userId).apply();
                    Toast.makeText(getContext(), R.string.toast_username_saved,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button saveTeamButton = (Button) view.findViewById(R.id.save_teamname_button);
        saveTeamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String teamId = teamText.getText().toString();
                if (teamId.equals("") || teamId.equals(Constants.DEFAULT_TEAM_ID)) {
                    Toast.makeText(getContext(), R.string.toast_invalid_teamname,
                            Toast.LENGTH_SHORT).show();
                    teamText.setText("");
                }
                else {
                    prefs.edit().putString(Constants.PREF_TEAM_ID, teamId).apply();
                    Toast.makeText(getContext(), R.string.toast_teamname_saved,
                            Toast.LENGTH_SHORT).show();
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
