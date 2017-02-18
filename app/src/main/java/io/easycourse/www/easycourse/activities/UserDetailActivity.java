package io.easycourse.www.easycourse.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.WindowManager;
import android.widget.Toast;

import io.easycourse.www.easycourse.R;
import io.easycourse.www.easycourse.fragments.main.UserDetailFragment;
import io.easycourse.www.easycourse.models.main.User;

/**
 * Created by noahrinehart on 2/18/17.
 */

public class UserDetailActivity extends BaseActivity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_userdetails);


        Intent intent = getIntent();
        if (intent.hasExtra("user")) {
            String userId = intent.getStringExtra("user");
            User user = realm.where(User.class).equalTo("id", userId).findFirst();
            UserDetailFragment userDetailFragment = UserDetailFragment.newInstance(user);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.userDetailContent, userDetailFragment)
                    .commit();

        } else {
            Toast.makeText(this, "Use not found!", Toast.LENGTH_SHORT).show();
            this.finish();
        }
    }

}
