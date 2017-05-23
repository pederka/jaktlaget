package net.ddns.peder.jaktlaget.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import net.ddns.peder.jaktlaget.Constants;
import net.ddns.peder.jaktlaget.MainActivity;
import net.ddns.peder.jaktlaget.R;
import net.ddns.peder.jaktlaget.database.PositionsDbHelper;
import net.ddns.peder.jaktlaget.database.TeamLandmarksDbHelper;

public class TeamInfoFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

    public TeamInfoFragment() {
        // Required empty public constructor
    }

    public static TeamInfoFragment newInstance(String param1, String param2) {
        TeamInfoFragment fragment = new TeamInfoFragment();
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
        View view = inflater.inflate(R.layout.fragment_team_info, container, false);
        // Read userid from preferences
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String userId = prefs.getString(Constants.SHARED_PREF_USER_ID, Constants.DEFAULT_USER_ID);
        // Read teamid from preferences
        final String teamId = prefs.getString(Constants.SHARED_PREF_TEAM_ID, Constants.DEFAULT_TEAM_ID);
        final String code = prefs.getString(Constants.SHARED_PREF_TEAM_CODE, "");

        // Set text information
        TextView nameText = (TextView) view.findViewById(R.id.info_user);
        nameText.setText(userId);
        TextView teamText = (TextView) view.findViewById(R.id.info_team);
        teamText.setText(teamId);
        TextView codeText = (TextView) view.findViewById(R.id.info_code);
        codeText.setText(code);

        ImageButton nameHelpButton = (ImageButton) view.findViewById(R.id.help_name_button);
        nameHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setMessage(R.string.name_help)
                        .setTitle(R.string.name_help_title);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Naathing
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        ImageButton teamHelpButton = (ImageButton) view.findViewById(R.id.help_team_button);
        teamHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setMessage(R.string.teamname_guide)
                        .setTitle(R.string.team_help_title);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Naathing
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        ImageButton codeHelpButton = (ImageButton) view.findViewById(R.id.help_code_button);
        codeHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setMessage(R.string.code_help)
                        .setTitle(R.string.code_help_title);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Naathing
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        Button resetTeamButton = (Button) view.findViewById(R.id.reset_team_button);
        resetTeamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.reset_team_dialog);
                builder.setMessage(String.format(getString(R.string.reset_team_text), teamId));
                builder.setPositiveButton(R.string.reset_team_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Remove team and code from shared preferences
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(
                                    getContext());
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(Constants.SHARED_PREF_USER_ID, Constants.DEFAULT_USER_ID);
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
                        // Clear any team traces
                        ((MainActivity)getActivity()).clearTeamLocationHistory();
                        // Cancel any position services
                        if (((MainActivity)getActivity()).isActive()) {
                            ((MainActivity)getActivity()).goInactive();
                        }
                        // Go to team management fragment
                        Fragment fragment = new TeamManagementFragment();
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.replace(R.id.content_frame, fragment);
                        ft.commit();
                    }
                });
                builder.setNegativeButton(R.string.lm_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        Button shareTeamButton = (Button) view.findViewById(R.id.share_team_button);
        shareTeamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "For å bli med i jaktlaget "+teamId+" bruk" +
                        " følgende kode: "+code);
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent,
                                            getResources().getText(R.string.send_to)));
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
