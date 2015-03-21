package com.greenapp.dota2matches;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.SimpleCursorAdapter;


/**
 * Created by herroino on 14.03.2015.
 * класс описывающий фрагмент со списком
 */
public class MatchesListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "DOTA2";

    private Handler handler = new Handler();
    //адаптер для списка
//    ArrayAdapter<Match> aa;

    //ArrayList<Match> matches = new ArrayList<Match>();
//теперь это не аррейадаптер а симплекурсорадаптер
    SimpleCursorAdapter adapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1, null,
                new String[]{MatchesProvider.KEY_SUMMURY}, new int[]{android.R.id.text1}, 0);
        //назначаем списку адаптер
        setListAdapter(adapter);

        getLoaderManager().initLoader(0, null, this);


        refreshMatches();

    }

    public void refreshMatches() {

        getLoaderManager().restartLoader(0, null, MatchesListFragment.this);

        getActivity().startService(new Intent(getActivity(), MatchesService.class));
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader");
        String[] projection = new String[]
                {
                        MatchesProvider.KEY_ID,
                        MatchesProvider.KEY_SUMMURY};
        Dota2MatchesActivity dota2MatchesActivity = (Dota2MatchesActivity) getActivity();
        CursorLoader loader = new CursorLoader(getActivity(),
                MatchesProvider.CONTENT_URI, projection, null, null, null);
        return loader;
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
