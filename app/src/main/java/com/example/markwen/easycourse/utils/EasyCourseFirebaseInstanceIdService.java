package com.example.markwen.easycourse.utils;

import android.util.Log;

import com.example.markwen.easycourse.EasyCourse;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;


/**
 * Created by nisarg on 30/12/16.
 */
public class EasyCourseFirebaseInstanceIdService extends FirebaseInstanceIdService {
    private String TAG = "InstanceIDService";

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        EasyCourse.getAppInstance().setDeviceToken(refreshedToken);
        Log.e(TAG, "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
    }
}
