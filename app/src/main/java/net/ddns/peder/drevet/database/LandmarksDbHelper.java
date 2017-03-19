package net.ddns.peder.drevet.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by peder on 04.03.2017.
 */

public class LandmarksDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 4;
    public static final String DATABASE_NAME = "Landmarks.db";

    public static final String TABLE_NAME = "landmarks";
    public static final String COLUMN_NAME_ID = "_id";
    public static final String COLUMN_NAME_TIME = "time";
    public static final String COLUMN_NAME_DESCRIPTION = "description";
    public static final String COLUMN_NAME_SHOWED = "showed";
    public static final String COLUMN_NAME_SHARED = "shared";
    public static final String COLUMN_NAME_LATITUDE = "latitude";
    public static final String COLUMN_NAME_LONGDITUDE = "longditude";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_NAME_ID + " INTEGER PRIMARY KEY," +
                    COLUMN_NAME_TIME + " TEXT," +
                    COLUMN_NAME_DESCRIPTION + " TEXT," +
                    COLUMN_NAME_SHOWED + " TEXT," +
                    COLUMN_NAME_SHARED + " INTEGER," +
                    COLUMN_NAME_LATITUDE + " REAL," +
                    COLUMN_NAME_LONGDITUDE + " REAL)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    public LandmarksDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public boolean deleteItem(SQLiteDatabase db, long id)
    {
        return db.delete(TABLE_NAME, COLUMN_NAME_ID + "=" + id, null) > 0;
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void clearTable(SQLiteDatabase db) {
        db.execSQL("DELETE FROM "+TABLE_NAME);
    }

}
