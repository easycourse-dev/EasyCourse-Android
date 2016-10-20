package com.example.markwen.easycourse;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.EditText;

/**
 * Created by Mark Wen on 10/18/2016.
 */

public class SignupLogin extends AppCompatActivity {

    EditText emailEditText;
    EditText passwordEditText;
    EditText verifyPasswordEditText;
    EditText usernameEditText;
    ViewGroup.LayoutParams editTextParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_login);
        // Hide toolbar for this specific activity
        getSupportActionBar().hide();

        emailEditText = (EditText) findViewById(R.id.editTextEmail);
        passwordEditText = (EditText) findViewById(R.id.editTextPassword);
        verifyPasswordEditText = (EditText) findViewById(R.id.editTextVerifyPassword);
        usernameEditText = (EditText) findViewById(R.id.editTextUsername);

        // Changing EditText colors
        emailEditText.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.SRC_IN);
        passwordEditText.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.SRC_IN);
        verifyPasswordEditText.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.SRC_IN);
        usernameEditText.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.SRC_IN);

        // Hide Signup EditText fields at start
        ((ViewManager)verifyPasswordEditText.getParent()).removeView(verifyPasswordEditText);
        ((ViewManager)usernameEditText.getParent()).removeView(usernameEditText);
    }

    public void emailLogin(View v) {
        emailEditText = (EditText) findViewById(R.id.editTextEmail);
        passwordEditText = (EditText) findViewById(R.id.editTextPassword);
        verifyPasswordEditText = (EditText) findViewById(R.id.editTextVerifyPassword);
        usernameEditText = (EditText) findViewById(R.id.editTextUsername);

        // Hide Signup EditText fields
        ((ViewManager)verifyPasswordEditText.getParent()).removeView(verifyPasswordEditText);
        ((ViewManager)usernameEditText.getParent()).removeView(usernameEditText);

        // Get inputs and check if fields are empty
        // only execute login API when fields are all filled

    }

    public void signup(View v) {
        emailEditText = (EditText) findViewById(R.id.editTextEmail);
        passwordEditText = (EditText) findViewById(R.id.editTextPassword);
        verifyPasswordEditText = (EditText) findViewById(R.id.editTextVerifyPassword);
        usernameEditText = (EditText) findViewById(R.id.editTextUsername);

        // Show Signup EditText fields
        ((ViewManager)verifyPasswordEditText.getParent()).addView(verifyPasswordEditText, editTextParams);
        ((ViewManager)usernameEditText.getParent()).addView(usernameEditText, editTextParams);

        // Get inputs and check if fields are empty
        // only execute login API when fields are all filled

    }

    public void facebookLogin(View v) {

    }
}
