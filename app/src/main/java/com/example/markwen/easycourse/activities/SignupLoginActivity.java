package com.example.markwen.easycourse.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.fragments.signup.SignupChooseUniversity;
import com.example.markwen.easycourse.fragments.signup.SignupLogin;
import com.example.markwen.easycourse.models.main.User;
import com.example.markwen.easycourse.models.signup.UserSetup;

import org.json.JSONArray;

import io.realm.Realm;


/**
 * Created by Mark Wen on 10/18/2016.
 */

public class SignupLoginActivity extends AppCompatActivity {

    public UserSetup userSetup;

    Realm realm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signuplogin);

        realm = Realm.getDefaultInstance();

        // Setup userSetup model to hold data
        userSetup = new UserSetup();
        // Use SharedPreferences to get users
        SharedPreferences sharedPref = getSharedPreferences("EasyCourse", Context.MODE_PRIVATE);
        String currentUser = sharedPref.getString("currentUser", "");
        User currentUserObject = User.getCurrentUser(this, realm);
        JSONArray joinedCourses = new JSONArray();


        // Hide toolbar for this specific activity and null check
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        if (savedInstanceState == null) {
            if(currentUserObject != null && (currentUserObject.getJoinedCourses() == null || currentUserObject.getJoinedCourses().size() < 1)) {
                SignupChooseUniversity chooseUniversity = new SignupChooseUniversity();
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.activity_signuplogin_container, chooseUniversity).commit();
                return;
            }
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();

            transaction.replace(R.id.activity_signuplogin_container, SignupLogin.newInstance(), "SignupLogin");
            transaction.commit();
        }    }
}
