package com.example.markwen.easycourse;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.markwen.easycourse.fragments.SignupLogin;

/**
 * Created by Mark Wen on 10/18/2016.
 */

public class EntryActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hide toolbar for this specific activity and null check
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        // launch a different activity
        Intent launchIntent = new Intent();
        Class<?> launchActivity;
        try
        {
            String className = getScreenClassName();
            launchActivity = Class.forName(className);
        }
        catch (ClassNotFoundException e)
        {
            launchActivity = SignupLogin.class;
        }
        launchIntent.setClass(getApplicationContext(), launchActivity);
        startActivity(launchIntent);

        finish();
    }

    /** return Class name of Activity to show **/
    private String getScreenClassName()
    {
        // Use SharedPreferences to get users
        String activity = MainActivity.class.getName();
        return activity;
    }
}
