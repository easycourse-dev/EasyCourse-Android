package com.example.markwen.easycourse.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.example.markwen.easycourse.EasyCourse;
import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.activities.ChatRoom;
import com.example.markwen.easycourse.activities.SettingsActivity;
import com.example.markwen.easycourse.models.main.Message;
import com.example.markwen.easycourse.utils.SocketIO;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by noahrinehart on 11/17/16.
 */

public class MainService extends Service {

    private static final String TAG = "MainService";

    private Realm realm;
    private RealmChangeListener messageListener;
    private SocketIO socketIO;

    @Override
    public void onCreate() {
        realm = Realm.getDefaultInstance();
        socketIO = EasyCourse.getAppInstance().getSocketIO();
        messageListener = new RealmChangeListener<RealmResults<Message>>() {
            @Override
            public void onChange(RealmResults<Message> messages) {
                // ... do something with the updated messages
                for(Message message : messages) {
                    showMessageNotification(message);
                }
            }};
        //Setup public listener
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "MainService started");
        WakefulBroadcastReceiver.completeWakefulIntent(intent);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "MainService destroyed");
        realm.removeAllChangeListeners();
        realm.close();
    }

    public void showMessageNotification(Message message) {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean notificationPref = sharedPref.getBoolean(SettingsActivity.KEY_PREF_NOTIFICATIONS, true);
        boolean vibratePref = sharedPref.getBoolean(SettingsActivity.KEY_PREF_VIBRATE, true);
        boolean soundPref = sharedPref.getBoolean(SettingsActivity.KEY_PREF_SOUND, true);

        if(!notificationPref) return;

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
