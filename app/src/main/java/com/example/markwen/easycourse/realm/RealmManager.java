package com.example.markwen.easycourse.realm;

import com.example.markwen.easycourse.models.main.Message;
import com.squareup.otto.Subscribe;

import io.realm.Realm;

/**
 * Created by noahrinehart on 11/17/16.
 */

public class RealmManager {



    @Subscribe
    public static void saveMessageToRealm(Message message) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(message);
        realm.commitTransaction();
    }


}
