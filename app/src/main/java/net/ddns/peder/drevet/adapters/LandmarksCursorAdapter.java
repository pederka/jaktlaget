package net.ddns.peder.drevet.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import net.ddns.peder.drevet.R;
import net.ddns.peder.drevet.database.LandmarksDbHelper;

import java.util.ArrayList;

/**
 * Created by peder on 3/19/17.
 */

public class LandmarksCursorAdapter extends SimpleCursorAdapter {

    private int item_layout;
    private SQLiteDatabase db;
    private LandmarksDbHelper dbHelper;
    private ArrayList<Boolean> isShowedChecked = new ArrayList<>();
    private ArrayList<Boolean> isSharedChecked = new ArrayList<>();

    public LandmarksCursorAdapter (Context context, int layout, Cursor cursor, String[] fromCols,
                                            int[] toViews) {
        super(context, layout, cursor, fromCols, toViews);
        item_layout = layout;

        this.mContext = context;
        dbHelper = new LandmarksDbHelper(context);
        db = dbHelper.getReadableDatabase();
        for (int i=0; i< this.getCount(); i++) {
            isShowedChecked.add(i, false);
            isSharedChecked.add(i, false);
        }
    }

    @Override
    public View getView(final int position, View inView, ViewGroup parent) {
        if (inView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(
                                                            Context.LAYOUT_INFLATER_SERVICE);
            inView = inflater.inflate(R.layout.lm_row, null);
        }

        final AppCompatCheckBox show = (AppCompatCheckBox) inView.findViewById(R.id.show_check_box);

        final AppCompatCheckBox share = (AppCompatCheckBox) inView.findViewById(R.id.share_check_box);

        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppCompatCheckBox cb = (AppCompatCheckBox) view.findViewById(R.id.show_check_box);
                if (cb.isChecked()) {
                    isShowedChecked.set(position, true);
                }
                else if (!cb.isChecked()) {
                    isShowedChecked.set(position, false);
                }
            }
        });
        show.setChecked(isShowedChecked.get(position));

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppCompatCheckBox cb = (AppCompatCheckBox) view.findViewById(R.id.share_check_box);
                if (cb.isChecked()) {
                    isSharedChecked.set(position, true);
                }
                else if (!cb.isChecked()) {
                    isSharedChecked.set(position, false);
                }
            }
        });
        share.setChecked(isSharedChecked.get(position));

        return inView;
    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {

        final long itemId = cursor.getInt(cursor.getColumnIndex(LandmarksDbHelper.COLUMN_NAME_ID));
        final String description = cursor.getString(cursor.getColumnIndex(
                                            LandmarksDbHelper.COLUMN_NAME_DESCRIPTION));

        TextView descriptionView = (TextView) view.findViewById(R.id.lm_list_desc);
        descriptionView.setText(description);

        AppCompatCheckBox show = (AppCompatCheckBox) view.findViewById(R.id.show_check_box);
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

        AppCompatCheckBox share = (AppCompatCheckBox) view.findViewById(R.id.share_check_box);
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
                dbHelper.deleteItem(db, itemId);
                cursor.requery();
                notifyDataSetChanged();
            }
        });
    }
}
