package net.ddns.peder.jaktlaget.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import net.ddns.peder.jaktlaget.R;
import net.ddns.peder.jaktlaget.database.LandmarksDbHelper;

/**
 * Created by peder on 3/19/17.
 */

public class LandmarksCursorAdapter extends SimpleCursorAdapter {

    private SQLiteDatabase db;
    private LandmarksDbHelper dbHelper;

    public LandmarksCursorAdapter (Context context, int layout, Cursor cursor, String[] fromCols,
                                            int[] toViews) {
        super(context, layout, cursor, fromCols, toViews, 0);

        this.mContext = context;
        dbHelper = new LandmarksDbHelper(context);
        db = dbHelper.getReadableDatabase();
    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {

        final long itemId = cursor.getInt(cursor.getColumnIndex(LandmarksDbHelper.COLUMN_NAME_ID));
        final String description = cursor.getString(cursor.getColumnIndex(
                                            LandmarksDbHelper.COLUMN_NAME_DESCRIPTION));

        TextView descriptionView = (TextView) view.findViewById(R.id.lm_list_desc);
        descriptionView.setText(description);

        AppCompatCheckBox show = (AppCompatCheckBox) view.findViewById(R.id.show_check_box);
        show.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 AppCompatCheckBox show = (AppCompatCheckBox) v.findViewById(R.id.show_check_box);
                 ContentValues values = new ContentValues();
                 if (show.isChecked()) {
                     values.put(LandmarksDbHelper.COLUMN_NAME_SHOWED, 1);
                 }
                 else {
                     values.put(LandmarksDbHelper.COLUMN_NAME_SHOWED, 0);
                 }
                 db.update(LandmarksDbHelper.TABLE_NAME, values, LandmarksDbHelper.COLUMN_NAME_ID+" = ?",
                                                        new String[]{Long.toString(itemId)});
                 cursor.requery();
             }
        }
        );

        AppCompatCheckBox share = (AppCompatCheckBox) view.findViewById(R.id.share_check_box);
        share.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 AppCompatCheckBox share = (AppCompatCheckBox) v.findViewById(R.id.share_check_box);
                 ContentValues values = new ContentValues();
                 if (share.isChecked()) {
                     values.put(LandmarksDbHelper.COLUMN_NAME_SHARED, 1);
                 }
                 else {
                     values.put(LandmarksDbHelper.COLUMN_NAME_SHARED, 0);
                 }
                 db.update(LandmarksDbHelper.TABLE_NAME, values, LandmarksDbHelper.COLUMN_NAME_ID+" = ?",
                                                        new String[]{Long.toString(itemId)});
                 cursor.requery();
             }
        }
        );

        // Set show checkbox
        final boolean isShowed = cursor.getInt(cursor.getColumnIndex(
                LandmarksDbHelper.COLUMN_NAME_SHOWED)) > 0;
        show.setChecked(isShowed);

        // Set share checkbox
        final boolean isShared = cursor.getInt(cursor.getColumnIndex(
                LandmarksDbHelper.COLUMN_NAME_SHARED)) > 0;
        share.setChecked(isShared);

        ImageButton deleteButton = (ImageButton) view.findViewById(R.id.lm_delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(R.string.lm_delete_title);
                builder.setPositiveButton(R.string.lm_delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dbHelper.deleteItem(db, itemId);
                        cursor.requery();
                        notifyDataSetChanged();
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
    }
}
