package com.greenapp.dota2matches;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

/**
 * Created by herroino on 16.03.2015.
 * класс с базой данных и источником оных
 */
public class MatchesProvider extends ContentProvider {
    //ури строка для внешних обращений к классу
    public static final Uri CONTENT_URI = Uri.parse("content://com.greenapp.dota2matches/matches");

    //поля таблицы
    public static final String KEY_ID = "_id";
    public static final String KEY_MATCH_ID = "match_id";
    public static final String KEY_TEAM1 = "team1";
    public static final String KEY_TEAM2 = "team2";
    public static final String KEY_LEAGUE_ID = "league_id";

    MatchesDatabaseHelper dbHelper;

    @Override
    public boolean onCreate() {
        Context context = getContext();

        dbHelper = new MatchesDatabaseHelper(context, MatchesDatabaseHelper.DATABASE_NAME,
                null, MatchesDatabaseHelper.DATABASE_VERSION);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    private static class MatchesDatabaseHelper extends SQLiteOpenHelper {

        private static final String TAG = "DOTA2";

        private static final String DATABASE_NAME = "dota2matches.db";
        private static final int DATABASE_VERSION = 1;
        private static final String DATABASE_TABLE = "matches";

        private static final String DATABASE_CREATE = "create table " + DATABASE_TABLE + " ("
                + KEY_ID + " integer primary key autoincrement, "
                + KEY_MATCH_ID + " INTEGER, "
                + KEY_TEAM1 + " TEXT, "
                + KEY_TEAM2 + " TEXT, "
                + KEY_LEAGUE_ID + " INTEGER);";

        private SQLiteDatabase matchesDB;

        public MatchesDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }
    }
}
