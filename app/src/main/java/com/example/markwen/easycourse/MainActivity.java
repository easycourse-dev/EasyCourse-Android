package com.example.markwen.easycourse;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.luseen.luseenbottomnavigation.BottomNavigation.BottomNavigationItem;
import com.luseen.luseenbottomnavigation.BottomNavigation.BottomNavigationView;
import com.luseen.luseenbottomnavigation.BottomNavigation.OnBottomNavigationItemClickListener;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigation);

        // Tabs setup
        BottomNavigationItem bottomNavigationItem = new BottomNavigationItem
                ("Chats", ContextCompat.getColor(this, R.color.colorAccent), R.drawable.ic_chatboxes);
        BottomNavigationItem bottomNavigationItem1 = new BottomNavigationItem
                ("User", ContextCompat.getColor(this, R.color.colorTabDefault), R.drawable.ic_contact_outline);
        bottomNavigationView.addTab(bottomNavigationItem);
        bottomNavigationView.addTab(bottomNavigationItem1);

        bottomNavigationView.setOnBottomNavigationItemClickListener(new OnBottomNavigationItemClickListener() {
            @Override
            public void onNavigationItemClick(int index) {
                Toast.makeText(MainActivity.this, "Item " +index +" clicked", Toast.LENGTH_SHORT).show();
            }
        });

        // Viewpager setup
        

        // Connect Viewpager with tab
        ContextCompat.getColor(getApplicationContext(), R.color.colorAccent);
        bottomNavigationView.setUpWithViewPager(yourPager , colorResources , imageResources);
    }
}
