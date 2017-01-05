package com.example.markwen.easycourse.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.example.markwen.easycourse.EasyCourse;
import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.activities.ChatRoomActivity;
import com.example.markwen.easycourse.activities.SettingsActivity;
import com.example.markwen.easycourse.models.main.Message;
import com.example.markwen.easycourse.models.main.Room;
import com.example.markwen.easycourse.utils.eventbus.Event;
import com.squareup.otto.Subscribe;

import io.realm.Realm;

/**
 * Created by noahrinehart on 11/17/16.
 */

public class MainService extends Service {

    private static final String TAG = "MainService";

    private Realm realm;

    @Override
    public void onCreate() {
        realm = Realm.getDefaultInstance();
        EasyCourse.bus.register(this);
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

    @Subscribe
    public void showMessageNotification(Event.MessageEvent event) {
        Message message = event.getMessage();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean notificationPref = sharedPref.getBoolean(SettingsActivity.KEY_PREF_NOTIFICATIONS, true);
        boolean vibratePref = sharedPref.getBoolean(SettingsActivity.KEY_PREF_VIBRATE, true);
        boolean soundPref = sharedPref.getBoolean(SettingsActivity.KEY_PREF_SOUND, true);

        if(!notificationPref || message == null) return;

        Intent i = new Intent(this, ChatRoomActivity.class);
        i.putExtra("roomId", message.getToRoom());
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        //TODO: Null handling for everywhere as well
        //TODO: add message to notification
        Room room = realm.where(Room.class).equalTo("id", message.getToRoom()).findFirst();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle(room.getRoomName());
        builder.setContentText(message.getText());
        builder.setContentIntent(pendingIntent);

        if(vibratePref)
            builder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });

        if(soundPref)
            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(Integer.parseInt(room.getId()), builder.build());
    }
}
