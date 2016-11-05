package com.example.markwen.easycourse;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.example.markwen.easycourse.fragments.SignupChooseCourses;

import com.example.markwen.easycourse.fragments.SignupChooseLanguage;
import com.example.markwen.easycourse.fragments.SignupChooseUniversity;
import com.example.markwen.easycourse.fragments.SignupLogin;
import com.example.markwen.easycourse.models.UserSetup;
import com.example.markwen.easycourse.utils.APIFunctions;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Mark Wen on 10/18/2016.
 */

public class SignupLoginActivity extends AppCompatActivity {

    public UserSetup userSetup;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signuplogin);

        // Setup userSetup model to hold data
        userSetup = new UserSetup();

        // Hide toolbar for this specific activity and null check
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        if (savedInstanceState == null) {
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.activity_signuplogin_container, SignupLogin.newInstance(), "SignupLogin");
            transaction.commit();
        }
    }
}
