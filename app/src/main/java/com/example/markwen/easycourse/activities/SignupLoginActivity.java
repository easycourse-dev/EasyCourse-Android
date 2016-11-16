package com.example.markwen.easycourse.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.fragments.signup.SignupLogin;
import com.example.markwen.easycourse.models.signup.UserSetup;

import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Created by Mark Wen on 10/18/2016.
 */

public class SignupLoginActivity extends spCompatActivity {

    public UserSetup userSetup;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signuplogin);

        // Setup userSetup model to hold data
        userSetup = new UserSetup();
        // Use SharedPreferences to get users
        SharedPreferences sharedPref = getSharedPreferences("EasyCourse", Context.MODE_PRIVATE);
        String currentUser = sharedPref.getString("currentUser", "");
        JSONObject currentUserObject;
        JSONArray joinedCourses = new JSONArray();
        try {
            // If user has no joined courses, bring the user to signup setup
            currentUserObject = new JSONObject(currentUser);
            joinedCourses = currentUserObject.getJSONArray("joinedCourse");
        } catch (Throwable t) {

        }

        // Hide toolbar for this specific activity and null check
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        if (savedInstanceState == null) {
//            Uncomment code below when syncUser is ready to check if a user has joined courses:
//            if (!currentUser.equals("") && joinedCourses.toString().equals("[]")) {
//                SignupChooseUniversity chooseUniversity = new SignupChooseUniversity();
//                FragmentManager fragmentManager = getSupportFragmentManager();
//                fragmentManager.beginTransaction()
//                        .replace(R.id.activity_signuplogin_container, chooseUniversity).commit();
//                return;
//            }
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();

            transaction.replace(R.id.activity_signuplogin_container, SignupLogin.newInstance(), "SignupLogin");
            transaction.commit();
        }    }
}
