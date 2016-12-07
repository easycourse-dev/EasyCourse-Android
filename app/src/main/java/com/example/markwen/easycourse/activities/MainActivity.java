package com.example.markwen.easycourse.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.example.markwen.easycourse.EasyCourse;
import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.fragments.main.UserFragment;
import com.example.markwen.easycourse.models.signup.UserSetup;
import com.example.markwen.easycourse.utils.SocketIO;

import com.example.markwen.easycourse.components.main.ViewPagerAdapter;
import com.example.markwen.easycourse.fragments.main.RoomsFragment;
import com.example.markwen.easycourse.fragments.main.UserFragment;
import com.example.markwen.easycourse.models.main.User;
import com.example.markwen.easycourse.models.signup.UserSetup;
import com.example.markwen.easycourse.utils.SocketIO;
import com.example.markwen.easycourse.utils.eventbus.Event;
import com.squareup.otto.Subscribe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

import static com.example.markwen.easycourse.EasyCourse.bus;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    Realm realm;
    SocketIO socketIO;
    Snackbar disconnectSnackbar;

    @BindView(R.id.toolbarMain)
    Toolbar toolbar;
    @BindView(R.id.viewpagerMain)
    ViewPager viewPager;
    @BindView(R.id.bottomNavigationMain)
    AHBottomNavigation bottomNavigation;
    @BindView(R.id.coordinatorMain)
    CoordinatorLayout coordinatorMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Binds all the views
        ButterKnife.bind(this);

        // Checking if there is a fragment_user currently logged in
        // if there is, remain in MainActivity
        // if not, show SignupLoginActivity
        checkUserLogin();

//        Checks for internet, displays snackbar if not found
//        checkForInternet();


        realm = Realm.getDefaultInstance();
        socketIO = EasyCourse.getAppInstance().getSocketIO();
        socketIO.syncUser();

        try {
            socketIO.getUserInfo(User.getCurrentUser(this, realm).getId());
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }


        setSupportActionBar(toolbar);
        toolbar.showOverflowMenu();

        setupNavigation();

        //Setup snackbar for disconnect
        disconnectSnackbar = Snackbar.make(coordinatorMain, "Disconnected!", Snackbar.LENGTH_INDEFINITE);

        bus.register(this);

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
        try {
            socketIO.getAllMessage();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

//    private void checkForInternet() {
//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                boolean connected = EasyCourse.isConnected();
//                if(!connected) {
//                    final Snackbar snackbar = Snackbar.make(coordinatorMain, "Disconnected!", Snackbar.LENGTH_INDEFINITE);
//                    snackbar.setAction("Retry", new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            if(EasyCourse.isConnected())
//                                snackbar.dismiss();
//                        }
//                    });
//                    snackbar.show();
//                }
//            }
//        }, 100);
//    }


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
            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.DONUT) {
                MainActivity.this.overridePendingTransition(R.anim.enter_from_left, R.anim.enter_from_right);
            }
        }
    }

    private void setupNavigation() {

        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        // Add items
        AHBottomNavigationItem item1 = new AHBottomNavigationItem("RoomsFragment", R.drawable.ic_chatboxes);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem("UserFragment", R.drawable.ic_contact_outline);

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
        pagerAdapter.addFragment(new RoomsFragment(), "Rooms");
        pagerAdapter.addFragment(new UserFragment(), "User");
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

        try {
            SocketIO socketIO = new SocketIO(this, getApplicationContext());
            socketIO.getUserInfo("5808237e5e6c6300115a381c");
        } catch (URISyntaxException e) {
            Log.e("com.example.easycourse", e.toString());
        } catch (JSONException e) {
            Log.e("com.example.easycourse", e.toString());
        }
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

//        checkForInternet();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @Subscribe
    public void disconnectEvent(Event.DisconnectEvent event) {
        disconnectSnackbar.show();
    }

    @Subscribe
    public void reconnectEvent(Event.ReconnectEvent event) {
        if(disconnectSnackbar != null) {
            disconnectSnackbar.dismiss();
        }
    }

}
