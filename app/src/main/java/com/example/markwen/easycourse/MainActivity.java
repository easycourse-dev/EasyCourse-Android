package com.example.markwen.easycourse;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.example.markwen.easycourse.components.Rooms;
import com.example.markwen.easycourse.components.User;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setSelectedTabIndicatorHeight(0);
        setupTabIcons();

    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new Rooms(), "Rooms");
        adapter.addFragment(new User(), "User");
        viewPager.setAdapter(adapter);
    }

    private void setupTabIcons() {
        TextView roomsTab = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        roomsTab.setText("Rooms");
        roomsTab.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_chatboxes, 0, 0);
        tabLayout.getTabAt(0).setCustomView(roomsTab);

        TextView userTab = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        userTab.setText("User");
        userTab.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_contact_outline, 0, 0);
        tabLayout.getTabAt(1).setCustomView(userTab);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>(); // List of Fragments
        private final List<String> mFragmentTitleList = new ArrayList<>(); // List of Fragment titles

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
