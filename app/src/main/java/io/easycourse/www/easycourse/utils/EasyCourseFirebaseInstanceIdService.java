package io.easycourse.www.easycourse.utils;

import android.util.Log;

import io.easycourse.www.easycourse.EasyCourse;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;


public class EasyCourseFirebaseInstanceIdService extends FirebaseInstanceIdService {
    private static final String TAG = "InstanceIDService";

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
