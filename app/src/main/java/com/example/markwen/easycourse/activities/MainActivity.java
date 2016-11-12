package com.example.markwen.easycourse.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.components.main.ViewPagerAdapter;
import com.example.markwen.easycourse.fragments.main.Rooms;
import com.example.markwen.easycourse.fragments.main.User;
import com.example.markwen.easycourse.models.signup.UserSetup;
import com.luseen.luseenbottomnavigation.BottomNavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private BottomNavigationView bottomNavigationView;

    int[] tabColors = {R.color.colorAccent, R.color.colorTabDefault};
    int[] tabImages = {R.drawable.ic_chatboxes, R.drawable.ic_contact_outline};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Checking if there is a fragment_user currently logged in
        // if there is, remain in MainActivity
        // if not, show SignupLoginActivity
        checkUserLogin();

        setContentView(R.layout.activity_main);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigation);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        //Get data from signup, may be null, fields may be null
        Intent intentFromSignup = getIntent();
        UserSetup userSetup = intentFromSignup.getParcelableExtra("UserSetup");
        if (userSetup != null) {
            Log.d(TAG, userSetup.getUniversityID());
            if (userSetup.getCourseCodeArray() != null && userSetup.getCourseCodeArray().length != 0)
                Log.d(TAG, userSetup.getCourseCodeArray()[0]);
            if (userSetup.getLanguageCodeArray() != null && userSetup.getLanguageCodeArray().length != 0)
                Log.d(TAG, Integer.toString(userSetup.getLanguageCodeArray()[0]));
        }

        // BottomNavigationView setup
        bottomNavigationView.disableShadow();
        bottomNavigationView.isWithText(true);
        bottomNavigationView.isColoredBackground(false);
        bottomNavigationView.setItemActiveColorWithoutColoredBackground(ContextCompat.getColor(this, tabColors[0]));

        // Viewpager setup
        pagerAdapter.addFragment(new Rooms(), "Rooms");
        pagerAdapter.addFragment(new User(), "User");
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                bottomNavigationView.selectTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        bottomNavigationView.setUpWithViewPager(viewPager, tabColors, tabImages);
    }

    private void checkUserLogin() {
        // launch a different activity
        Intent launchIntent = new Intent();
        // Use SharedPreferences to get users
        SharedPreferences sharedPref = getSharedPreferences("EasyCourse", Context.MODE_PRIVATE);

        String userToken = sharedPref.getString("userToken", null);
        String currentUser = sharedPref.getString("currentUser", null);

        if (userToken == null && currentUser == null) {
            launchIntent.setClass(getApplicationContext(), SignupLoginActivity.class);
            startActivity(launchIntent);
            finish();
        }
    }
}
