package io.easycourse.www.easycourse;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.facebook.FacebookSdk;
import com.facebook.stetho.Stetho;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.otto.ThreadEnforcer;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import java.net.URISyntaxException;

import io.easycourse.www.easycourse.utils.SocketIO;
import io.easycourse.www.easycourse.utils.eventbus.MainBus;
import io.realm.Realm;

/**
 * Created by noahrinehart on 11/5/16.
 */

public class EasyCourse extends Application {

    private static final String TAG = "EasyCourse";

    private SocketIO socketIO;
    private static EasyCourse appInstance = null;
    private String inRoom;
    private boolean notification;
    private String universityId;

    private String deviceToken;

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);

        FacebookSdk.sdkInitialize(this);

        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).build())
                        .build());

        appInstance = this;

        deviceToken = FirebaseInstanceId.getInstance().getToken();
    }

    public static MainBus bus = new MainBus(ThreadEnforcer.ANY);

    public void createSocketIO() {
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

    public void setDeviceToken(String deviceToken){
        this.deviceToken = deviceToken;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setNotification(boolean notify) {
        this.notification = notify;
    }

    public boolean getNotification() {
        return notification;
    }

    // When not in a room, set it to "" instead of null
    public void setInRoom(String inRoom) {
        this.inRoom = inRoom;
    }

    public String getInRoom() {
        return inRoom;
    }

    public void setUniversityId(String univId) {
        this.universityId = univId;
    }

    public String getUniversityId() {
        return universityId;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
