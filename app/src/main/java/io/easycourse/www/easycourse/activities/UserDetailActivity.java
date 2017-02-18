package io.easycourse.www.easycourse.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import io.easycourse.www.easycourse.R;
import io.easycourse.www.easycourse.fragments.main.UserDetailFragment;


public class UserDetailActivity extends BaseActivity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_userdetails);

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        Intent intent = getIntent();
        if (intent.hasExtra("user")) {
            String userId = intent.getStringExtra("user");
            UserDetailFragment userDetailFragment = UserDetailFragment.newInstance(userId);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.userDetailContent, userDetailFragment)
                    .commit();
        }else {
            Toast.makeText(this, "Use not found!", Toast.LENGTH_SHORT).show();
            this.finish();
        }
    }

}
