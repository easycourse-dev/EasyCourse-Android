package com.example.markwen.easycourse.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.components.ViewPagerAdapter;
import com.example.markwen.easycourse.fragments.main.Rooms;
import com.example.markwen.easycourse.fragments.main.User;
import com.example.markwen.easycourse.models.signup.UserSetup;
import com.luseen.luseenbottomnavigation.BottomNavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private BottomNavigationView bottomNavigationView;
    private ViewPager viewPager;
    private ViewPagerAdapter pagerAdapter;
    private UserSetup userSetup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigation);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        //Get data from signup, may be null, fields may be null
        Intent intentFromSignup = getIntent();
        userSetup=intentFromSignup.getParcelableExtra("UserSetup");
        if (userSetup != null) {
            Log.d(TAG, userSetup.getUniversityID());
            if (userSetup.getCourseCodeArray().length != 0)
                Log.d(TAG, userSetup.getCourseCodeArray()[0]);
            if (userSetup.getLanguageCodeArray().length != 0)
                Log.d(TAG, Integer.toString(userSetup.getLanguageCodeArray()[0]));
        }

        int[] tabColors = {R.color.colorAccent, R.color.colorTabDefault};
        int[] tabImages = {R.drawable.ic_chatboxes, R.drawable.ic_contact_outline};

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
        bottomNavigationView.setUpWithViewPager(viewPager , tabColors , tabImages);
    }
}
