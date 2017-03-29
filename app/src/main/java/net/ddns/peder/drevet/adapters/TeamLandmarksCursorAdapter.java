package net.ddns.peder.drevet.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.View;
import android.widget.TextView;

import net.ddns.peder.drevet.R;
import net.ddns.peder.drevet.database.TeamLandmarksDbHelper;

/**
 * Created by peder on 3/19/17.
 */

public class TeamLandmarksCursorAdapter extends SimpleCursorAdapter {

    private SQLiteDatabase db;
    private TeamLandmarksDbHelper dbHelper;

    public TeamLandmarksCursorAdapter(Context context, int layout, Cursor cursor, String[] fromCols,
                                      int[] toViews) {
        super(context, layout, cursor, fromCols, toViews, 0);

        this.mContext = context;
        dbHelper = new TeamLandmarksDbHelper(context);
        db = dbHelper.getReadableDatabase();
    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {

        final long itemId = cursor.getInt(cursor.getColumnIndex(TeamLandmarksDbHelper.COLUMN_NAME_ID));
        final String description = cursor.getString(cursor.getColumnIndex(
                                            TeamLandmarksDbHelper.COLUMN_NAME_DESCRIPTION));
        final String userId = cursor.getString(cursor.getColumnIndex(
                                            TeamLandmarksDbHelper.COLUMN_NAME_USER));

        TextView descriptionView = (TextView) view.findViewById(R.id.team_lm_list_desc);
        descriptionView.setText(description);

        TextView userView = (TextView) view.findViewById(R.id.team_lm_list_user);
        userView.setText(userId);

        AppCompatCheckBox show = (AppCompatCheckBox) view.findViewById(R.id.show_check_box_team);
        show.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 AppCompatCheckBox show = (AppCompatCheckBox) v.findViewById(R.id.show_check_box_team);
                 ContentValues values = new ContentValues();
                 if (show.isChecked()) {
                     values.put(TeamLandmarksDbHelper.COLUMN_NAME_SHOWED, 1);
                 }
                 else {
                     values.put(TeamLandmarksDbHelper.COLUMN_NAME_SHOWED, 0);
                 }
                 db.update(TeamLandmarksDbHelper.TABLE_NAME, values,
                                                    TeamLandmarksDbHelper.COLUMN_NAME_ID+" = ?",
                                                        new String[]{Long.toString(itemId)});
                 cursor.requery();
             }
        });

        // Set show checkbox
        final boolean isShowed = cursor.getInt(cursor.getColumnIndex(
                TeamLandmarksDbHelper.COLUMN_NAME_SHOWED)) > 0;
        show.setChecked(isShowed);

    }
}
