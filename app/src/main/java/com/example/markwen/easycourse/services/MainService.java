package com.example.markwen.easycourse.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.example.markwen.easycourse.EasyCourse;
import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.activities.ChatRoom;
import com.example.markwen.easycourse.models.main.Message;
import com.example.markwen.easycourse.utils.SocketIO;

/**
 * Created by noahrinehart on 11/17/16.
 */

public class MainService extends Service {

    private static final String TAG = "MainService";

    MainBus bus;
    SocketIO socketIO;

    @Override
    public void onCreate() {
        bus = EasyCourse.bus;
        bus.register(this);
        socketIO = EasyCourse.getAppInstance().getSocketIO();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "MainService started");
        WakefulBroadcastReceiver.completeWakefulIntent(intent);
        //Start listening for messages

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "MainService destroyed");
    }

    public void showMessageNotification(Message message) {

        Intent i = new Intent(this, ChatRoom.class);
        i.putExtra("roomId", message.getId());
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(message.getText())
                //TODO: Change to sender name not id
                //TODO: add intent to go to right room
                .setContentText("Sent by: " + message.getSenderId())
                .setContentIntent(pendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(Integer.parseInt(message.getId()), mBuilder.build());
    }
}
