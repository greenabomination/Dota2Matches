package com.greenapp.dota2matches;

import android.app.ListFragment;
import android.os.Bundle;
import android.os.Handler;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by herroino on 14.03.2015.
 * класс описывающий фрагмент со списком
 */
public class MatchesListFragment extends ListFragment {
    private static final String TAG = "DOTA2";

    private Handler handler = new Handler();
    //адаптер для списка
    ArrayAdapter<Match> aa;

    ArrayList<Match> matches = new ArrayList<Match>();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //выбираем стайл для списка из стандартных лэйаутов
        int layout_id = android.R.layout.simple_list_item_1;
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
                //и тут начинается истинная магия
                reader.beginObject();
                Log.d(TAG, reader.nextName());
                reader.beginObject();
                Log.d(TAG, reader.nextName());
                readMatchesArray(reader);
                Log.d(TAG, reader.nextName() + " : " + reader.nextInt());
                reader.endObject();
                reader.endObject();
            }


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }
    }

    public void readMatchesArray(JsonReader r) throws IOException {
        List mat = new ArrayList();
        r.beginArray();
        while (r.hasNext()) {
            getGameObject(r);
        }
        r.endArray();
    }

    public void getGameObject(JsonReader r) throws IOException {
        int match_id = -1;
        int league_id = -1;
        String team1 = "", team2 = "";
// пробегаемся по игрокам
        r.beginObject();
        Log.d(TAG, r.nextName());
        r.beginArray();
        while (r.hasNext()) {
            readPlayerData(r);
        }
        r.endArray();
        //получаем
        while (r.hasNext() && r.peek() == JsonToken.NAME) {
            String name = r.nextName();
            if (name.equals("match_id")) {
                match_id = r.nextInt();
            } else if (name.equals("league_id")) {
                league_id = r.nextInt();
            } else if (name.equals("radiant_team")) {
                team1 = getTeamInfo(r, "radiant_team");
            } else if (name.equals("dire_team")) {
                team2 = getTeamInfo(r, "dire_team");
            } else {
                r.skipValue();
            }
        }
        final Match match = new Match(match_id, team1, team2, league_id);
        handler.post(new Runnable() {
            public void run() {
                addNewMatch(match);
            }

        });
        r.endObject();
    }

    public void readPlayerData(JsonReader r) throws IOException

    {
        int account_id = 0;
        String player_name = "";
        int hero_id = 0;
        int team = -1;

        r.beginObject();
        while (r.hasNext()) {
            String name = r.nextName();
            if (name.equals("account_id")) {
                account_id = r.nextInt();
            } else if (name.equals("name")) {
                player_name = r.nextString();
            } else if (name.equals("hero_id")) {
                hero_id = r.nextInt();
            } else if (name.equals("team")) {
                team = r.nextInt();
            } else {
                r.skipValue();
            }
        }
        Log.d(TAG, "player: " + player_name + " (" + account_id + ") hero - " + hero_id + "; team - " + team);
        r.endObject();
    }

    public String getTeamInfo(JsonReader r, String t) throws IOException {
        String team_name = "";
        int team_id = -1;
        long team_logo = 0;
        boolean complete = false;

        r.beginObject();
        while (r.hasNext()) {
            String name = r.nextName();
            if (name.equals("team_name")) {
                team_name = r.nextString();
            } else if (name.equals("team_id")) {
                team_id = r.nextInt();
            } else if (name.equals("team_logo")) {
                team_logo = r.nextLong();
            } else if (name.equals("complete")) {
                complete = r.nextBoolean();
            } else {
                r.skipValue();
            }
        }
        r.endObject();
        Log.d(TAG, t + " - " + team_name);
        return team_name;
    }

    private void addNewMatch(Match m) {
        matches.add(m);
        aa.notifyDataSetChanged();
    }

}
