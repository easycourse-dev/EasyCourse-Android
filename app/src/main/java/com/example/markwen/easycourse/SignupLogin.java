package com.example.markwen.easycourse;

import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

/**
 * Created by Mark Wen on 10/18/2016.
 */

public class SignupLogin extends AppCompatActivity {
    private ViewGroup mContainerView;

    private void addItem() {
        EditText username = (EditText)findViewById(R.id.username);
        mContainerView.addView(username, 0);
    }
}
