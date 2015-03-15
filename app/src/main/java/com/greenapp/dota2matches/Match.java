package com.greenapp.dota2matches;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by herroino on 14.03.2015.
 * класс для хранения матчей
 */
public class Match {
    private int match_id;
    private String team1;
    private String team2;
    private Date datetime;
    private int status;
    private String league;
    private String link;
    private int winner;

    // конструктор
    public Match(
            int _match_id,
            String _team1,
            String _team2,
            Date _datetime,
            int _status,
            String _league,
            String _link,
            int _winner
    ) {
        match_id = _match_id;
        team1 = _team1;
        team2 = _team2;
        datetime = _datetime;
        status = _status;
        league = _league;
        link = _link;
        winner = _winner;
    }

    //метод возвращает суммарно данные по матчу
    public String getSummary() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String dateString = sdf.format(datetime);
        if (winner == 0) {
            return dateString + ": " + team1 + " vs " + team2;
        } else {
            return dateString + ": " + team1 + " vs " + team2 + " (winner:" + winner + ")";
        }
    }

}
