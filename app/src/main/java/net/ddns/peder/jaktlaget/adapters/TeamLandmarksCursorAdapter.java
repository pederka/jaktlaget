package net.ddns.peder.jaktlaget.adapters;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.TextView;

import net.ddns.peder.jaktlaget.R;
import net.ddns.peder.jaktlaget.database.TeamLandmarksDbHelper;

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

    }
}
