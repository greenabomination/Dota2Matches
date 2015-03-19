package com.greenapp.dota2matches;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;

/**
 * Created by kabardinov133238 on 19.03.2015.
 */
public class MatchesSearchResult extends ListActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1,
                null, new String[]{MatchesProvider.KEY_SUMMURY}, new int[]{android.R.id.text1}, 0);
        setListAdapter(adapter);

        getLoaderManager().initLoader(0, null, this);

        parseIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        parseIntent(getIntent());
    }

    private static String QUERY_EXTRA_KEY = "QUERY_EXTRA_KEY";

    private void parseIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String searchQuery = intent.getStringExtra(SearchManager.QUERY);
            Bundle args = new Bundle();
            args.putString(QUERY_EXTRA_KEY, searchQuery);
            getLoaderManager().restartLoader(0, args, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String query = "0";

        if (args != null) {
            query = args.getString(QUERY_EXTRA_KEY);
        }
        String[] projection = {MatchesProvider.KEY_ID, MatchesProvider.KEY_SUMMURY};
        String where = MatchesProvider.KEY_SUMMURY + " LIKE \"%" + query + "%\"";
        String[] whereArgs = null;
        String sortOrder = MatchesProvider.KEY_SUMMURY + " COLLATE LOCALIZED ASC";
        return new CursorLoader(this, MatchesProvider.CONTENT_URI, projection, where, whereArgs, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}
