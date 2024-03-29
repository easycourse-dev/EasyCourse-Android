package io.easycourse.www.easycourse.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.MySSLSocketFactory;
import com.squareup.otto.Subscribe;

import org.json.JSONException;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.easycourse.www.easycourse.EasyCourse;
import io.easycourse.www.easycourse.R;
import io.easycourse.www.easycourse.components.main.ViewPagerAdapter;
import io.easycourse.www.easycourse.fragments.main.RoomsFragment;
import io.easycourse.www.easycourse.fragments.main.UserFragment;
import io.easycourse.www.easycourse.models.main.User;
import io.easycourse.www.easycourse.utils.APIFunctions;
import io.easycourse.www.easycourse.utils.eventbus.Event;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";

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

        AsyncHttpClient client = APIFunctions.client;
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            MySSLSocketFactory sf = new MySSLSocketFactory(trustStore);
            sf.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            client.setSSLSocketFactory(sf);
        } catch (IOException | CertificateException | NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException | KeyManagementException e) {
            Log.e(TAG, "onCreate: ", e);
        }


        if (socketIO == null) {
            EasyCourse.getAppInstance().createSocketIO();
            socketIO = EasyCourse.getAppInstance().getSocketIO();
        }

        // Checking if there is a user currently logged in
        // if there is, remain in MainActivity
        // if not or the user doesn't have any rooms, show SignupLoginActivity
        checkUserLogin();

        try {
            socketIO.getUserInfo(User.getCurrentUser(this, realm).getId());
            //socketIO.getAllMessage();
        } catch (JSONException | NullPointerException e) {
            e.printStackTrace();
        }


        setSupportActionBar(toolbar);
        toolbar.showOverflowMenu();

        setupNavigation();

        //Setup snackbar for disconnect
        disconnectSnackbar = Snackbar.make(coordinatorMain, "Disconnected!", Snackbar.LENGTH_INDEFINITE);
    }


    private void checkUserLogin() {
        // launch a different activity
        Intent launchIntent = new Intent();
        // Use SharedPreferences to get users
        SharedPreferences sharedPref = getSharedPreferences("EasyCourse", Context.MODE_PRIVATE);

        String userToken = sharedPref.getString("userToken", null);
        String currentUserString = sharedPref.getString("currentUser", null);
        String universityId = sharedPref.getString("universityId", null);

        if (userToken == null || currentUserString == null || universityId == null || universityId.length() < 1) {
            launchIntent.setClass(getApplicationContext(), SignupLoginActivity.class);
            startActivity(launchIntent);
            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.DONUT) {
                MainActivity.this.overridePendingTransition(R.anim.enter_from_left, R.anim.enter_from_right);
            }
            finish();
        }
        try {
            socketIO.getUniversityInfo(universityId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setupNavigation() {

        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        // Add items and set titles
        AHBottomNavigationItem item1 = new AHBottomNavigationItem("Rooms", R.drawable.ic_chatboxes);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem("Me", R.drawable.ic_contact_outline);

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
                realm.close();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && viewPager.getCurrentItem() == 1) {
            viewPager.setCurrentItem(0, true);
            return true;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        EasyCourse.getAppInstance().setShowNotification(false);
        EasyCourse.getAppInstance().setInRoom("");
    }

    @Override
    protected void onPause() {
        super.onPause();
        EasyCourse.getAppInstance().setShowNotification(true);
    }


    @Subscribe
    public void disconnectEvent(Event.DisconnectEvent event) {
        disconnectSnackbar.show();
    }

    @Subscribe
    public void reconnectEvent(Event.ReconnectEvent event) {
        if (disconnectSnackbar != null) {
            disconnectSnackbar.dismiss();
        }
    }
}
