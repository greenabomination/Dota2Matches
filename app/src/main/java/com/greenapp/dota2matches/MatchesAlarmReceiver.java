package com.greenapp.dota2matches;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by herroino on 22.03.2015.
 */
public class MatchesAlarmReceiver extends BroadcastReceiver {

    public static final String ACTION_REFRESH_MATCHES_ALARM =
            "com.greenapp.dota2matches.ACTION_REFRESH_MATCHES_ALARM";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent startIntent = new Intent(context, MatchesService.class);
        context.startService(startIntent);
    }
}
