package com.example.markwen.easycourse.services;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Created by noahrinehart on 11/17/16.
 */

public class NotificationBroadcastReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, MainService.class);
        startWakefulService(context, i);
    }
}