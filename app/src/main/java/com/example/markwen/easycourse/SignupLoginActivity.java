package com.example.markwen.easycourse;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.example.markwen.easycourse.fragments.SignupLogin;
import com.example.markwen.easycourse.models.signup.UserSetup;


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
