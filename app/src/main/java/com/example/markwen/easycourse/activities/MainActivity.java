package com.example.markwen.easycourse.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationViewPager;
import com.example.markwen.easycourse.EasyCourse;
import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.components.main.ViewPagerAdapter;
import com.example.markwen.easycourse.fragments.main.Rooms;
import com.example.markwen.easycourse.fragments.main.User;
import com.example.markwen.easycourse.models.main.Message;
import com.example.markwen.easycourse.models.main.Room;
import com.example.markwen.easycourse.models.signup.UserSetup;
import com.example.markwen.easycourse.utils.SocketIO;

import org.json.JSONArray;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    Realm realm;
    SocketIO socketIO;

    @BindView(R.id.toolbarMain)
    Toolbar toolbar;
    @BindView(R.id.viewpagerMain)
    ViewPager viewPager;
    @BindView(R.id.bottomNavigationMain)
    AHBottomNavigation bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ButterKnife.bind(this);

        // Checking if there is a fragment_user currently logged in
        // if there is, remain in MainActivity
        // if not, show SignupLoginActivity
        checkUserLogin();

        realm = Realm.getDefaultInstance();
        socketIO = EasyCourse.getAppInstance().getSocketIO();
        socketIO.syncUser();


        setSupportActionBar(toolbar);
        toolbar.showOverflowMenu();

        setupNavigation();

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
    }

    private void checkUserLogin() {
        // launch a different activity
        Intent launchIntent = new Intent();
        // Use SharedPreferences to get users
        SharedPreferences sharedPref = getSharedPreferences("EasyCourse", Context.MODE_PRIVATE);

        String userToken = sharedPref.getString("userToken", null);
        String currentUser = sharedPref.getString("currentUser", null);
        JSONObject currentUserObject;
        JSONArray joinedCourses = new JSONArray();

        try {
            // If user has no joined courses, bring the user to signup setup
            currentUserObject = new JSONObject(currentUser);
            joinedCourses = currentUserObject.getJSONArray("joinedCourse");
        } catch (Throwable t) {

        }
        Log.e("joinedCourses", joinedCourses.toString());

        /*
            When syncUser is ready, add code below into the if statement:

                 || joinedCourses.toString().equals("[]")

            to make sure a user has joined courses.
            If a user doesn't have joined courses, then bring the user back to signup setup.
         */

        if (userToken == null || currentUser == null) {
            launchIntent.setClass(getApplicationContext(), SignupLoginActivity.class);
            startActivity(launchIntent);
            finish();
        }
    }

    private void setupNavigation() {

        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());


        // Add items
        AHBottomNavigationItem item1 = new AHBottomNavigationItem("Rooms", R.drawable.ic_chatboxes);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem("User", R.drawable.ic_contact_outline);

        bottomNavigation.addItem(item1);
        bottomNavigation.addItem(item2);
        // Customize Buttom Navigation
        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
        // Set colors
        bottomNavigation.setAccentColor(ContextCompat.getColor(this, R.color.colorAccent));
        bottomNavigation.setInactiveColor(ContextCompat.getColor(this, R.color.colorTabDefault));
        // Set background color
        bottomNavigation.setDefaultBackgroundColor(ContextCompat.getColor(this, R.color.colorBackground));
        bottomNavigation.setTranslucentNavigationEnabled(true);

        // Viewpager setup
        pagerAdapter.addFragment(new Rooms(), "Rooms");
        pagerAdapter.addFragment(new User(), "User");
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(bottomNavigation.getCurrentItem());
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                bottomNavigation.setCurrentItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                viewPager.setCurrentItem(position, true);
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "Options clicked");
        switch (item.getItemId()) {
            case R.id.actionSettingsMain:
                Log.d(TAG, "Settings clicked");
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (socketIO != null)
            socketIO.syncUser();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
