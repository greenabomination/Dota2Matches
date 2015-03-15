package com.greenapp.dota2matches;

import android.app.ListFragment;
import android.os.Bundle;
import android.util.JsonReader;
import android.widget.ArrayAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * Created by herroino on 14.03.2015.
 * класс описывающий фрагмент со списком
 */
public class MatchesListFragment extends ListFragment {
    private static final String TAG = "DOTA2";
    //адаптер для списка
    ArrayAdapter<Match> aa;

    ArrayList<Match> matches = new ArrayList<Match>();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //выбираем стайл для списка из стандартных лэйаутов
        int layout_id = android.R.layout.simple_list_item_2;
        //создаем аррейадаптер
        aa = new ArrayAdapter<Match>(getActivity(), layout_id, matches);
        //назначаем списку адаптер
        setListAdapter(aa);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                refreshMatches();
            }
        });
        t.start();
    }

    public void refreshMatches() {
        URL url;
        try {
            String matchFeed = getString(R.string.live_matches_url);
            //получаем ссылку на json
            url = new URL(matchFeed);
            //открываем подключение
            URLConnection conn = url.openConnection();
            //делаем подключение http ^_^
            HttpURLConnection connection = (HttpURLConnection) conn;
            //получаем ответ сервера
            int responceCode = connection.getResponseCode();

            //проверяем ответ на предмет ОК
            if (responceCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                //так как это джисон, то с этого момента все будет не как парсинг сайта
                JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));


            }


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }
    }


    private void addNewMatch(Match m) {

    }

}
