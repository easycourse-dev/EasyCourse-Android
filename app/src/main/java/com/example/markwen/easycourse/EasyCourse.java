package com.example.markwen.easycourse;

import android.app.Application;

import com.example.markwen.easycourse.services.MainBus;
import com.example.markwen.easycourse.utils.SocketIO;
import com.facebook.stetho.Stetho;
import com.squareup.otto.ThreadEnforcer;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by noahrinehart on 11/5/16.
 */

public class EasyCourse extends Application {

    public SocketIO socketIO;

    private static EasyCourse appInstance = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);

        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).build())
                        .build());

        appInstance = this;

        socketIO = new SocketIO(this);
    }

    public static MainBus bus = new MainBus(ThreadEnforcer.ANY);


    public SocketIO getSocketIO() {
        return socketIO;
    }

    public static EasyCourse getAppInstance() {
        return appInstance;
    }
}
