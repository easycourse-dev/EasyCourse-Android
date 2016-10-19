package com.example.markwen.easycourse;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.EditText;

/**
 * Created by Mark Wen on 10/18/2016.
 */

public class SignupLogin extends AppCompatActivity {
    private ViewGroup mContainerView;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        // Hide toolbar title for this specific activity
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void addItem() {
        EditText username = (EditText)findViewById(R.id.username);
        mContainerView.addView(username, 0);
    }
}
