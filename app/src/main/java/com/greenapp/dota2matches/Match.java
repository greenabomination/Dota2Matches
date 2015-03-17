package com.greenapp.dota2matches;

/**
 * Created by herroino on 14.03.2015.
 * класс для хранения матчей
 */
public class Match {
    private int match_id;
    private String team1;
    private String team2;
    private int league_id;


    // конструктор
    public Match(
            int _match_id,
            String _team1,
            String _team2,
            int _league_id
    ) {
        if (_team1.length() == 0) {
            team1 = "unknown";
        } else {
            team1 = _team1;
        }
        if (_team2.length() == 0) {
            team2 = "unknown";
        } else {
            team2 = _team2;
        }
        match_id = _match_id;
        league_id = _league_id;
    }

    //метод возвращает суммарно данные по матчу
    public String toString() {

        return match_id + ": " + team1 + " vs " + team2 + " (league:" + league_id + ")";

    }

    private String decodeTowerState(int d) {
        StringBuilder sb = new StringBuilder();

        int x = 1;
        for (int i = 0; i < 32; i++) {
            sb.append((d & x) == 0 ? "0" : "1");
            x <<= 1;
        }
        return "" + sb.reverse();
    }

}
