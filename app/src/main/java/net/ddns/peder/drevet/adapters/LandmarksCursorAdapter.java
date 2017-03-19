package net.ddns.peder.drevet.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import net.ddns.peder.drevet.R;
import net.ddns.peder.drevet.database.LandmarksDbHelper;

/**
 * Created by peder on 3/19/17.
 */

public class LandmarksCursorAdapter extends SimpleCursorAdapter {

    private int item_layout;
    private SQLiteDatabase db;
    private LandmarksDbHelper dbHelper;

    public LandmarksCursorAdapter (Context context, int layout, Cursor cursor, String[] fromCols,
                                            int[] toViews) {
        super(context, layout, cursor, fromCols, toViews);
        item_layout = layout;

        this.mContext = context;
        dbHelper = new LandmarksDbHelper(context);
        db = dbHelper.getReadableDatabase();
    }

    //@Override
    //public View getView(int position, View convertView, ViewGroup parent) {
    //    super.getView(position, convertView)
    //}

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {

        final long itemId = cursor.getInt(cursor.getColumnIndex(LandmarksDbHelper.COLUMN_NAME_ID));
        final String description = cursor.getString(cursor.getColumnIndex(
                                            LandmarksDbHelper.COLUMN_NAME_DESCRIPTION));

        TextView descriptionView = (TextView) view.findViewById(R.id.lm_list_desc);
        descriptionView.setText(description);

        // Set show checkbox
        AppCompatCheckBox show = (AppCompatCheckBox) view.findViewById(R.id.show_check_box);
        final boolean isShowed = cursor.getInt(cursor.getColumnIndex(
                LandmarksDbHelper.COLUMN_NAME_SHOWED)) > 0;
        show.setChecked(isShowed);

        // Set share checkbox
        AppCompatCheckBox share = (AppCompatCheckBox) view.findViewById(R.id.share_check_box);
        final boolean isShared = cursor.getInt(cursor.getColumnIndex(
                LandmarksDbHelper.COLUMN_NAME_SHARED)) > 0;
        share.setChecked(isShared);

        show.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
             @Override
             public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                 ContentValues values = new ContentValues();
                 if (isChecked) {
                     values.put(LandmarksDbHelper.COLUMN_NAME_SHOWED, 1);
                 }
                 else {
                     values.put(LandmarksDbHelper.COLUMN_NAME_SHOWED, 0);
                 }
                 db.update(LandmarksDbHelper.TABLE_NAME, values, LandmarksDbHelper.COLUMN_NAME_ID+" = ?",
                                                        new String[]{Long.toString(itemId)});
             }
        }
        );

        share.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
             @Override
             public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                 ContentValues values = new ContentValues();
                 if (isChecked) {
                     values.put(LandmarksDbHelper.COLUMN_NAME_SHARED, 1);
                 }
                 else {
                     values.put(LandmarksDbHelper.COLUMN_NAME_SHARED, 0);
                 }
                 db.update(LandmarksDbHelper.TABLE_NAME, values, LandmarksDbHelper.COLUMN_NAME_ID+" = ?",
                                                        new String[]{Long.toString(itemId)});
             }
        }
        );

        ImageButton deleteButton = (ImageButton) view.findViewById(R.id.lm_delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                dbHelper.deleteItem(db, itemId);
                cursor.requery();
                notifyDataSetChanged();
            }
        });
    }
}
