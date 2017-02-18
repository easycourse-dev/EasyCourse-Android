package io.easycourse.www.easycourse.utils.BroadcastRecievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import io.easycourse.www.easycourse.models.NotificationMessage;
import io.realm.Realm;
import io.realm.RealmResults;



public class NotificationClearBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.hasExtra("delete")) {
            Realm realm = Realm.getDefaultInstance();
            int intRoomId = intent.getIntExtra("delete", 0);
            RealmResults<NotificationMessage> messages = realm.where(NotificationMessage.class).equalTo("roomId", intRoomId).findAll();
            realm.beginTransaction();
            messages.deleteAllFromRealm();
            realm.commitTransaction();
            realm.close();
        }
    }
}
