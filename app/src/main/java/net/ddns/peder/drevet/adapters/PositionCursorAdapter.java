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
import net.ddns.peder.drevet.database.PositionsDbHelper;

/**
 * Created by peder on 3/19/17.
 */

public class PositionCursorAdapter extends SimpleCursorAdapter {

    private SQLiteDatabase db;
    private PositionsDbHelper dbHelper;

    public PositionCursorAdapter(Context context, int layout, Cursor cursor, String[] fromCols,
                                 int[] toViews) {
        super(context, layout, cursor, fromCols, toViews, 0);

        this.mContext = context;
        dbHelper = new PositionsDbHelper(context);
        db = dbHelper.getReadableDatabase();
    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {

        final long itemId = cursor.getInt(cursor.getColumnIndex(PositionsDbHelper.COLUMN_NAME_ID));
        final String user = cursor.getString(cursor.getColumnIndex(
                                            PositionsDbHelper.COLUMN_NAME_USER));
        final String time = cursor.getString(cursor.getColumnIndex(
                                            PositionsDbHelper.COLUMN_NAME_TIME));
        TextView userTextView = (TextView) view.findViewById(R.id.pos_list_user);
        userTextView.setText(user);
        TextView timeTextView = (TextView) view.findViewById(R.id.pos_list_time);
        long minutes = (System.currentTimeMillis() - Long.parseLong(time))/60000;
        if (minutes == 0) {
            timeTextView.setText(R.string.less_than_1_min);
        }
        else if (minutes > 60) {
            timeTextView.setText(R.string.more_than_1_hour);
        }
        else {
            timeTextView.setText(String.format(context.getString(R.string.minutes),
                    String.valueOf(minutes)));
        }
        AppCompatCheckBox show = (AppCompatCheckBox) view.findViewById(R.id.show_check_box);
        show.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 AppCompatCheckBox show = (AppCompatCheckBox) v.findViewById(R.id.show_check_box);
                 ContentValues values = new ContentValues();
                 if (show.isChecked()) {
                     values.put(PositionsDbHelper.COLUMN_NAME_SHOWED, 1);
                 }
                 else {
                     values.put(PositionsDbHelper.COLUMN_NAME_SHOWED, 0);
                 }
                 db.update(PositionsDbHelper.TABLE_NAME, values, PositionsDbHelper.COLUMN_NAME_ID+" = ?",
                                                        new String[]{Long.toString(itemId)});
                 cursor.requery();
             }
        }
        );

        // Set show checkbox
        final boolean isShowed = cursor.getInt(cursor.getColumnIndex(
                PositionsDbHelper.COLUMN_NAME_SHOWED)) > 0;
        show.setChecked(isShowed);
    }
}
