package com.example.markwen.easycourse;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.markwen.easycourse.models.main.User;
import com.example.markwen.easycourse.services.MainBus;
import com.example.markwen.easycourse.utils.SocketIO;
import com.facebook.stetho.Stetho;
import com.squareup.otto.ThreadEnforcer;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import java.io.IOException;
import java.net.URISyntaxException;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * Created by noahrinehart on 11/5/16.
 */

public class EasyCourse extends Application {

    private static final String TAG = "EasyCourse";

    private SocketIO socketIO;
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
        createSockeIO();
    }

    public static MainBus bus = new MainBus(ThreadEnforcer.ANY);

    public void createSockeIO() {
        try {
            socketIO = new SocketIO(this);
        } catch (URISyntaxException e) {
            Log.e(TAG, e.toString());
        }
    }


    public SocketIO getSocketIO() {
        return socketIO;
    }


    public static EasyCourse getAppInstance() {
        return appInstance;
    }



    public static boolean isConnected() {
        String command = "ping -c 1 https://zengjintaotest.com/api";
        try {
            return (Runtime.getRuntime().exec(command).waitFor() == 0);
        } catch (InterruptedException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }

}
