package com.example.markwen.easycourse;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.example.markwen.easycourse.fragments.SignupChooseCourses;

/**
 * Created by Mark Wen on 10/18/2016.
 */

public class SignupLoginActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signuplogin);

        // Hide toolbar for this specific activity and null check
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        if (savedInstanceState == null) {
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.activity_signuplogin_container, SignupChooseCourses.newInstance(), "SignupLogin");
            transaction.commit();
        }
    }
}
