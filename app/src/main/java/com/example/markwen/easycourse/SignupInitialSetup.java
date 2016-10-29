package com.example.markwen.easycourse;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Mark Wen on 10/20/2016.
 */

public class SignupInitialSetup extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.signup_initial_setup);
        // Hide toolbar for this specific activity
        getSupportActionBar().hide();
    }
}
