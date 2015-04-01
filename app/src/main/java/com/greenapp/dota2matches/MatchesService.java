package com.greenapp.dota2matches;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

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
 * Created by herroino on 21.03.2015.
 * сервис для обновления данных
 */
public class MatchesService extends IntentService {

    public static String TAG = "MATCHES_UPDATE_SERVICE";

    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public MatchesService(String name) {
        super(name);
    }

    public MatchesService() {
        super("MatchesService");
    }


    @Override
    public void onCreate() {
        super.onCreate();
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        String ALARM_ACTION = MatchesAlarmReceiver.ACTION_REFRESH_MATCHES_ALARM;
        Intent intentToFire = new Intent(ALARM_ACTION);
        alarmIntent = PendingIntent.getBroadcast(this, 0, intentToFire, 0);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        int updateFreq = Integer.parseInt("60");
        boolean autoUpdateCheked = false;

        if (autoUpdateCheked) {
            int alarmType = AlarmManager.ELAPSED_REALTIME_WAKEUP;
            long timeToRefresh = SystemClock.elapsedRealtime() + updateFreq * 60 * 1000;
            alarmManager.setInexactRepeating(alarmType, timeToRefresh, updateFreq * 60 * 1000, alarmIntent);

        } else {
            alarmManager.cancel(alarmIntent);
            refreshMatches();
        }
    }

    private void addNewMatch(Match m) {

        ContentResolver cr = getContentResolver();
        //описываем условие where
        String w = MatchesProvider.KEY_MATCH_ID + "=" + m.getMatch_id();
        //выполняем запрос
        Cursor query = cr.query(MatchesProvider.CONTENT_URI, null, w, null, null);

        if (query.getCount() == 0) {
            ContentValues cv = new ContentValues();
            cv.put(MatchesProvider.KEY_MATCH_ID, m.getMatch_id());
            cv.put(MatchesProvider.KEY_TEAM1, m.getTeam1());
            cv.put(MatchesProvider.KEY_TEAM2, m.getTeam2());
            cv.put(MatchesProvider.KEY_LEAGUE_ID, m.getLeague_id());
            cv.put(MatchesProvider.KEY_SUMMURY, m.getSummury());
            cr.insert(MatchesProvider.CONTENT_URI, cv);
        }
        query.close();

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
                //удаляем старые строки
                ContentResolver cr = getContentResolver();
                cr.delete(MatchesProvider.CONTENT_URI,null,null);
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

        addNewMatch(match);

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
        //    Log.d(TAG, "player: " + player_name + " (" + account_id + ") hero - " + hero_id + "; team - " + team);
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
}
