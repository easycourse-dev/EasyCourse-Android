package io.easycourse.www.easycourse.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import io.easycourse.www.easycourse.R;
import io.easycourse.www.easycourse.fragments.signup.SignupChooseUniversity;
import io.easycourse.www.easycourse.fragments.signup.SignupLogin;
import io.easycourse.www.easycourse.models.main.Course;
import io.easycourse.www.easycourse.models.signup.UserSetup;

import io.realm.Realm;
import io.realm.RealmResults;


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
        Realm realm = Realm.getDefaultInstance();
        // Use SharedPreferences to get users
        SharedPreferences sharedPref = getSharedPreferences("EasyCourse", Context.MODE_PRIVATE);
        String currentUser = sharedPref.getString("currentUser", "");
        RealmResults<Course> joinedCourses = realm.where(Course.class).findAll();

        // Hide toolbar for this specific activity and null check
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        if (savedInstanceState == null) {
            if (!currentUser.equals("") && joinedCourses.size() == 0) {
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
        }
    }
}
