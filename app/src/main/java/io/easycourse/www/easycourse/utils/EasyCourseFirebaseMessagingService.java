package io.easycourse.www.easycourse.utils;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import io.easycourse.www.easycourse.EasyCourse;
import io.easycourse.www.easycourse.R;
import io.easycourse.www.easycourse.activities.ChatRoomActivity;
import io.easycourse.www.easycourse.models.NotificationMessage;
import io.easycourse.www.easycourse.utils.BroadcastRecievers.NotificationClearBroadcastReceiver;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class EasyCourseFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "EasyCourseFirebaseMessa";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String roomId = null;
        String body = null;
        String title = null;
        Map<String, String> data = remoteMessage.getData();
        Log.e(TAG, data.toString());
        if (data.containsKey("senderId"))
            roomId = data.get("senderId");
        else if (data.containsKey("toRoom"))
            roomId = data.get("toRoom");
        if (data.containsKey("body"))
            body = data.get("body");
        if (data.containsKey("title"))
            title = data.get("title");


        if (!EasyCourse.getAppInstance().getShowNotification()) return;

        if (!EasyCourse.getAppInstance().getInRoom().equals(roomId)) {
            sendNotification(body, title, roomId);
        } else { // In room
            // Vibrate phone
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(500);
        }
    }

    // Use this to create notification when the app is open
    private void sendNotification(String messageBody, String messageTitle, String roomId) {

        int intRoomId = roomId.hashCode();

        Realm realm = Realm.getDefaultInstance();

        RealmResults<NotificationMessage> messages = saveMessage(realm, intRoomId, messageTitle, messageBody);

        Intent intent = new Intent(this, ChatRoomActivity.class);
        intent.putExtra("roomId", roomId);
        intent.putExtra("delete", intRoomId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, intRoomId /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Intent deleteIntent = new Intent(this, NotificationClearBroadcastReceiver.class);
        deleteIntent.putExtra("delete", intRoomId);
        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 0, deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT);


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean notificationPreference = preferences.getBoolean("prefNotifications", true);
        boolean notificationVibrate = preferences.getBoolean("prefVibrate", true);
        boolean notificationLED = preferences.getBoolean("prefSound", true);

        if (!notificationPreference) return;

        Log.d(TAG, "sendNotification: ");
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(this)
                .setColor(ContextCompat.getColor(this, R.color.colorAccent))
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setContentTitle(messageTitle)
                .setContentText("New Messages!")
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setDeleteIntent(deletePendingIntent)
                .setNumber(messages.size());


        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        for (int i = 0; i < messages.size(); i++) {
            inboxStyle.addLine(messages.get(i).getMessage());
        }
        inboxStyle.setBigContentTitle(messageTitle);
        inboxStyle.setSummaryText("EasyCourse");

        notifBuilder.setStyle(inboxStyle);

        if (notificationVibrate) {
            Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(500);
        }

        if (notificationLED)
            notifBuilder.setLights(ContextCompat.getColor(this, R.color.colorPrimary), 1000, 1000);


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(intRoomId /* ID of notification */, notifBuilder.build());
        realm.close();
    }

    private RealmResults<NotificationMessage> saveMessage(Realm realm, int intRoomId, String roomName, String messageBody) {
        NotificationMessage message = new NotificationMessage(UUID.randomUUID().toString(), intRoomId, roomName, messageBody, new Date());
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(message);
        realm.commitTransaction();
        return realm.where(NotificationMessage.class).equalTo("roomId", intRoomId).findAllSorted("createdAt", Sort.ASCENDING);
    }
}
