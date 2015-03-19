package com.greenapp.dota2matches;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by herroino on 16.03.2015.
 * класс с базой данных и источником оных
 */
public class MatchesProvider extends ContentProvider {

    private static final String TAG = "DOTA2";
    //ури строка для внешних обращений к классу
    public static final Uri CONTENT_URI = Uri.parse("content://com.greenapp.dota2matches/matches");

    //поля таблицы
    public static final String KEY_ID = "_id";
    public static final String KEY_MATCH_ID = "match_id";
    public static final String KEY_TEAM1 = "team1";
    public static final String KEY_TEAM2 = "team2";
    public static final String KEY_LEAGUE_ID = "league_id";
    public static final String KEY_SUMMURY = "summary";

    MatchesDatabaseHelper dbHelper;

    private static final int MATCHES = 1;
    private static final int MATCH_ID = 2;
    private static final int SEARCH = 3;

    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI("com.greenapp.dota2matches", "matches", MATCHES);
        uriMatcher.addURI("com.greenapp.dota2matches", "matches/#", MATCH_ID);
        uriMatcher.addURI("com.greenapp.dota2matches",
                SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH);
        uriMatcher.addURI("com.greenapp.dota2matches",
                SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH);
        uriMatcher.addURI("com.greenapp.dota2matches",
                SearchManager.SUGGEST_URI_PATH_SHORTCUT, SEARCH);
        uriMatcher.addURI("com.greenapp.dota2matches",
                SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", SEARCH);
    }

    private static final HashMap<String, String> SEARCH_PROJECTION_MAP;

    static {
        SEARCH_PROJECTION_MAP = new HashMap<String, String>();
        SEARCH_PROJECTION_MAP.put(SearchManager.SUGGEST_COLUMN_TEXT_1,
                KEY_SUMMURY + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_1);
        SEARCH_PROJECTION_MAP.put("_id", KEY_ID + " AS " + "_id");
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();

        dbHelper = new MatchesDatabaseHelper(context, MatchesDatabaseHelper.DATABASE_NAME,
                null, MatchesDatabaseHelper.DATABASE_VERSION);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        //открываем базу
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        //создаем построитель запросов
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        //задаем таблицу
        qb.setTables(MatchesDatabaseHelper.DATABASE_TABLE);
        //определяем тип запроса
        switch (uriMatcher.match(uri)
                ) {   //если конкретная запись то
            case MATCH_ID:
                qb.appendWhere(KEY_ID + "=" + uri.getPathSegments().get(1));
                break;
            case SEARCH:
                qb.appendWhere(KEY_SUMMURY + " LIKE \"%" + uri.getPathSegments().get(1) + "%\"");
                qb.setProjectionMap(SEARCH_PROJECTION_MAP);
                break;
            default:
                break;
        }
        //определяем порядок сортировки
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = KEY_MATCH_ID;//по дефолту id матча
        } else {
            orderBy = sortOrder;
        }

        Cursor c = qb.query(database, projection, selection, selectionArgs, null, null, orderBy);
        //устанавливаем оповеститель для контекстрезольвера
        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case MATCHES:
                return "vnd.android.cursor.dir/vnd.greenapp.dota2matches";
            case MATCH_ID:
                return "vnd.android.cursor.item/vnd.greenapp.dota2matches";
            case SEARCH:
                return SearchManager.SUGGEST_MIME_TYPE;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Log.d(TAG, "insert");
        long rowID = database.insert(MatchesDatabaseHelper.DATABASE_TABLE, "match", values);

        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }

        throw new SQLException("Failed to insert row into " + uri);

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase database = dbHelper.getWritableDatabase();

        int cnt;
        switch (uriMatcher.match(uri)) {
            case MATCHES:
                cnt = database.delete(MatchesDatabaseHelper.DATABASE_TABLE, selection, selectionArgs);
                break;
            case MATCH_ID:
                String segment = uri.getPathSegments().get(1);
                cnt = database.delete(MatchesDatabaseHelper.DATABASE_TABLE, KEY_ID + "=" + segment +
                        (!TextUtils.isEmpty(selection) ? " AND ("
                                + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        int cnt;
        switch (uriMatcher.match(uri)) {
            case MATCHES:
                cnt = database.update(MatchesDatabaseHelper.DATABASE_TABLE, values, selection, selectionArgs);
                break;
            case MATCH_ID:

                String segment = uri.getPathSegments().get(1);
                cnt = database.update(MatchesDatabaseHelper.DATABASE_TABLE, values, KEY_ID + "=" + segment +
                        (!TextUtils.isEmpty(selection) ? " AND ("
                                + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unlnown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);

        return cnt;
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
                + KEY_SUMMURY + " TEXT, "
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
