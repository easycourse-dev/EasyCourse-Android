package com.example.markwen.easycourse;

import android.app.Application;

import io.realm.Realm;

/**
 * Created by noahrinehart on 11/5/16.
 */

public class EasyCourse extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }
}
