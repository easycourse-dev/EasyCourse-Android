package com.example.markwen.easycourse.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.markwen.easycourse.R;

/**
 * Created by nisarg on 28/11/16.
 */

public class UserProfile extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userprofile);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }
}
