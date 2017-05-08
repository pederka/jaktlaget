package net.ddns.peder.jaktlaget.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import net.ddns.peder.jaktlaget.AsyncTasks.DataSynchronizer;
import net.ddns.peder.jaktlaget.Constants;
import net.ddns.peder.jaktlaget.R;
import net.ddns.peder.jaktlaget.adapters.TeamPagerAdapter;
import net.ddns.peder.jaktlaget.database.PositionsDbHelper;
import net.ddns.peder.jaktlaget.database.TeamLandmarksDbHelper;
import net.ddns.peder.jaktlaget.interfaces.OnSyncComplete;

public class TeamManagementFragment extends Fragment implements OnSyncComplete {
    private EditText userText;
    private EditText teamText;
    private EditText codeText;
    private TextInputLayout textInputLayoutUser;
    private TextInputLayout textInputLayoutTeam;
    private OnFragmentInteractionListener mListener;
    private TeamFragment teamFragment;

    @Override
    public void onSyncComplete(int result) {
        if (result == DataSynchronizer.SUCCESS) {
            teamFragment.updateTeamList();
            teamText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check_24dp, 0);
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(
                                                getContext());
            codeText.setError(null);
            codeText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            codeText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check_24dp, 0);
            codeText.setText(sharedPref.getString(Constants.SHARED_PREF_TEAM_CODE, ""));
            Toast.makeText(getContext(), "Registrering vellykket", Toast.LENGTH_SHORT).show();
        }
        else if (result == DataSynchronizer.FAILED_TRANSFER) {
            Toast.makeText(getContext(), R.string.toast_no_contact_server, Toast.LENGTH_SHORT).show();
            teamText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            codeText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
        else if (result == DataSynchronizer.FAILED_TEAM) {
            Toast.makeText(getContext(), R.string.toast_no_team_or_user, Toast.LENGTH_SHORT).show();
            userText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            teamText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_clear_24dp, 0);
            codeText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
        else if (result == DataSynchronizer.FAILED_USER) {
            Toast.makeText(getContext(), R.string.toast_no_team_or_user, Toast.LENGTH_SHORT).show();
            teamText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            userText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_clear_24dp, 0);
            codeText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
        else if (result == DataSynchronizer.FAILED_CODE) {
            Toast.makeText(getContext(), R.string.toast_wrong_code, Toast.LENGTH_SHORT).show();
            teamText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_clear_24dp, 0);
            codeText.setError(getString(R.string.wrong_code));
            codeText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_clear_24dp, 0);
            // Set code to empty
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                                            getContext());
            sharedPreferences.edit().putString(Constants.SHARED_PREF_TEAM_CODE, "").apply();
        }
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
        String code = prefs.getString(Constants.SHARED_PREF_TEAM_CODE, "");

        // Set ids if they exist
        userText = (EditText) view.findViewById(R.id.username_text);
        if (!userId.equals(Constants.DEFAULT_USER_ID)) {
            userText.setText(userId);
        }
        teamText = (EditText) view.findViewById(R.id.teamname_text);
        codeText = (EditText) view.findViewById(R.id.teamcode_text);
        codeText.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
        codeText.setText(code);
        if (!code.equals("")) {
            teamText.setText(teamId);
            teamText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check_24dp, 0);
            codeText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check_24dp, 0);
        }

        textInputLayoutUser = (TextInputLayout) view.findViewById(R.id.text_input_layout_user);
        textInputLayoutTeam = (TextInputLayout) view.findViewById(R.id.text_input_layout_team);

        Button saveTeamButton = (Button) view.findViewById(R.id.save_teamname_button);
        saveTeamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (teamFragment == null) {
                    ViewPager vp = (ViewPager) getActivity().findViewById(R.id.team_pager);
                    TeamPagerAdapter teamPagerAdapter = (TeamPagerAdapter) vp.getAdapter();
                    teamFragment = (TeamFragment) teamPagerAdapter.getRegisteredFragment(1);
                }
                if (submitForm()) {
                    DataSynchronizer dataSynchronizer = new DataSynchronizer(getContext(),
                            TeamManagementFragment.this, true);
                    dataSynchronizer.execute();
                }
            }
        });
        Button resetTeamButton = (Button) view.findViewById(R.id.reset_team_button);
        resetTeamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (teamFragment == null) {
                    ViewPager vp = (ViewPager) getActivity().findViewById(R.id.team_pager);
                    TeamPagerAdapter teamPagerAdapter = (TeamPagerAdapter) vp.getAdapter();
                    teamFragment = (TeamFragment) teamPagerAdapter.getRegisteredFragment(1);
                }
                teamText.setText("");
                codeText.setText("");
                teamText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                codeText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                // Remove team and code from shared preferences
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(
                            getContext());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(Constants.SHARED_PREF_TEAM_ID, Constants.DEFAULT_TEAM_ID);
                editor.putString(Constants.SHARED_PREF_TEAM_CODE, "");
                editor.apply();
                // Clear database entries
                PositionsDbHelper positionsDbHelper = new PositionsDbHelper(getContext());
                TeamLandmarksDbHelper teamLandmarksDbHelper = new TeamLandmarksDbHelper(getContext());
                SQLiteDatabase posdb = positionsDbHelper.getWritableDatabase();
                SQLiteDatabase lmdb = teamLandmarksDbHelper.getReadableDatabase();
                positionsDbHelper.clearTable(posdb);
                teamLandmarksDbHelper.clearTable(lmdb);
                posdb.close();
                lmdb.close();
                // Update the list in team fragment
                teamFragment.updateTeamList();
            }
        });
        return view;
    }

    private boolean submitForm() {
        if (!validateName()) {
            return false;
        }
        if (!validateTeam()) {
            return false;
        }
        String userId = userText.getText().toString().trim();
        String teamId = teamText.getText().toString().trim();
        String code = codeText.getText().toString().trim();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.SHARED_PREF_TEAM_ID, teamId);
        editor.putString(Constants.SHARED_PREF_USER_ID, userId);
        editor.putString(Constants.SHARED_PREF_TEAM_CODE, code);
        editor.apply();
        return true;
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
                teamText.getText().toString().equals(Constants.DEFAULT_TEAM_ID) ||
                teamText.getText().toString().contains(":")) {
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
