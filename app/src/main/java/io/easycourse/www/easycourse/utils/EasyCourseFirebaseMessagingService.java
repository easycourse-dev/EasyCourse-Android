package io.easycourse.www.easycourse.utils;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import io.easycourse.www.easycourse.EasyCourse;
import io.easycourse.www.easycourse.R;
import io.easycourse.www.easycourse.activities.ChatRoomActivity;

/**
 * Created by nisarg on 30/12/16.
 */
public class EasyCourseFirebaseMessagingService extends FirebaseMessagingService {
    String TAG = "MessagingService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String roomId = "";
        Map<String, String> data =  remoteMessage.getData();
        Log.e(TAG, data.toString());
        if (data.containsKey("senderId")) {
            roomId = data.get("senderId");
        } else if (data.containsKey("toRoom")) {
            roomId = data.get("toRoom");
        }

        if (EasyCourse.getAppInstance().getNotification() && !EasyCourse.getAppInstance().getInRoom().equals(roomId)) {
            sendNotification(remoteMessage.getNotification().getBody(), remoteMessage.getNotification().getTitle(), roomId);
        } else if (EasyCourse.getAppInstance().getNotification()) {
            // Vibrate phone
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(500);
        }
    }

    // Use this to create notification when the app is open
    private void sendNotification(String messageBody, String messageTitle, String roomId) {
        Intent intent = new Intent(this, ChatRoomActivity.class);
        intent.putExtra("roomId", roomId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        // Vibrate phone
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(500);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setColor(getColor(R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setContentTitle(messageTitle)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}