package io.easycourse.www.easycourse;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.facebook.stetho.Stetho;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.otto.ThreadEnforcer;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import java.net.URISyntaxException;
import java.util.Date;

import io.easycourse.www.easycourse.models.main.User;
import io.easycourse.www.easycourse.utils.SocketIO;
import io.easycourse.www.easycourse.utils.eventbus.MainBus;
import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

/**
 * Created by noahrinehart on 11/5/16.
 */

public class EasyCourse extends MultiDexApplication {

    private static final String TAG = "EasyCourse";

    private SocketIO socketIO;
    private static EasyCourse appInstance = null;
    private String inRoom;
    private boolean showNotification;
    private String deviceToken;

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);

        doRealmMigration();

        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).build())
                        .build());

        appInstance = this;

        deviceToken = FirebaseInstanceId.getInstance().getToken();

        // Memory leak detection
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
    }

    private void doRealmMigration() {
        //Realm migration
        RealmConfiguration config = new RealmConfiguration.Builder()
                .schemaVersion(1)
                .migration(new RealmMigration() {
                    @Override
                    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                        RealmSchema schema = realm.getSchema();
                        if (oldVersion == 0) {
                            schema.create("NotificationMessage")
                                    .addField("id", String.class, FieldAttribute.PRIMARY_KEY)
                                    .addField("roomId", int.class)
                                    .addField("roomName", String.class)
                                    .addField("message", String.class)
                                    .addField("createdAt", Date.class);
                            oldVersion++;
                        }
                    }
                })
                .build();
        Realm.setDefaultConfiguration(config);
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

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setShowNotification(boolean notify) {
        this.showNotification = notify;
    }

    public boolean getShowNotification() {
        return showNotification;
    }

    // When not in a room, set it to "" instead of null
    public void setInRoom(String inRoom) {
        this.inRoom = inRoom;
    }

    public String getInRoom() {
        return inRoom;
    }

    public void setUniversityId(Context context, String univId) {
        SharedPreferences sharedPref = context.getSharedPreferences("EasyCourse", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("universityId", univId);
        editor.apply();
    }

    public String getUniversityId(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("EasyCourse", Context.MODE_PRIVATE);
        return sharedPref.getString("universityId", null);
    }

}
