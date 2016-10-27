package com.example.markwen.easycourse;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.example.markwen.easycourse.utils.APIFunctions;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Mark Wen on 10/18/2016.
 */

public class SignupLogin extends AppCompatActivity {

    private static final String TAG = "SignupLogin";

    TextView titleTextView;

    EditText emailEditText;
    EditText passwordEditText;
    EditText verifyPasswordEditText;
    EditText usernameEditText;

    TextInputLayout emailInputLayout;
    TextInputLayout passwordInputLayout;
    TextInputLayout verifyPasswordInputLayout;
    TextInputLayout usernameInputLayout;

    Button signupButton;
    Button loginButton;
    Button facebookButton;


    LinearLayout signupLinearLayout;

    SharedPreferences sharedPref;

    Animation titleAnimEnter;
    Animation emailAnimEnter;
    Animation passwordAnimEnter;
    Animation loginAnimEnter;
    Animation signupAnimEnter;
    Animation facebookAnimEnter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_login);
        // Hide toolbar for this specific activity and null check
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        titleTextView = (TextView) findViewById(R.id.textViewTitle);
        emailEditText = (EditText) findViewById(R.id.editTextEmail);
        passwordEditText = (EditText) findViewById(R.id.editTextPassword);
        verifyPasswordEditText = (EditText) findViewById(R.id.editTextVerifyPassword);
        usernameEditText = (EditText) findViewById(R.id.editTextUsername);

        emailInputLayout = (TextInputLayout) findViewById(R.id.inputLayoutEmail);
        passwordInputLayout = (TextInputLayout) findViewById(R.id.inputLayoutPassword);
        verifyPasswordInputLayout = (TextInputLayout) findViewById(R.id.inputLayoutVerifyPassword);
        usernameInputLayout = (TextInputLayout) findViewById(R.id.inputLayoutUsername);


        signupButton = (Button) findViewById(R.id.buttonSignup);
        loginButton = (Button) findViewById(R.id.buttonLogin);
        facebookButton = (Button) findViewById(R.id.buttonFacebookLogin);
        signupLinearLayout = (LinearLayout) findViewById(R.id.linearLayoutSignup);


        // Set username and verify passwords inially gone
        verifyPasswordInputLayout.setVisibility(View.GONE);
        usernameInputLayout.setVisibility(View.GONE);

        // Changing EditText colors
        emailEditText.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.SRC_IN);
        passwordEditText.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.SRC_IN);
        verifyPasswordEditText.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.SRC_IN);
        usernameEditText.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.SRC_IN);

        // Animations for views when activity starts/resumes
        titleAnimEnter = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_move_in);
        emailAnimEnter = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_move_in);
        emailAnimEnter.setStartOffset(250);
        passwordAnimEnter = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_move_in);
        passwordAnimEnter.setStartOffset(250);
        loginAnimEnter = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_move_in);
        loginAnimEnter.setStartOffset(250 * 2);
        signupAnimEnter = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_move_in);
        signupAnimEnter.setStartOffset(250 * 2);
        facebookAnimEnter = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_move_in);
        facebookAnimEnter.setStartOffset(250 * 2);


        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup(v);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailLogin(v);
            }
        });

    }

    public void signup(View v) {

        // Brings in verify password and username edittexts if not visible
        if (verifyPasswordInputLayout.getVisibility() == View.GONE) {
            verifyPasswordInputLayout.setVisibility(View.VISIBLE);
            usernameInputLayout.setVisibility(View.VISIBLE);
            signupButton.setBackgroundResource(R.drawable.login_button);
            loginButton.setBackgroundResource(R.drawable.signup_button);

        } else { // Edittexts are shown, do logic
            Log.d(TAG, "Signup clicked-doing signup");
            // Get inputs and check if fields are empty
            // only execute login API when fields are all filled
            try {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                String verifyPassword = verifyPasswordEditText.getText().toString();
                String username = usernameEditText.getText().toString();


                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Snackbar.make(v, "Check your email", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() == 0) {
                    Snackbar.make(v, "No password", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6 || password.length() > 20) {
                    Snackbar.make(v, "Make sure your password is between 6 and 20 characters long.", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (!verifyPassword.equals(password)) {
                    Snackbar.make(v, "Your passwords don't match", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (username.length() < 6 || username.length() > 20) {
                    Snackbar.make(v, "Make sure your username is between 6 and 20 characters long.", Snackbar.LENGTH_SHORT).show();
                    return;
                }


                // make API call only if the 2 passwords are the same
                // make a Toast and don't do anything if they don't match
                if (!password.equals(verifyPassword)) {
                    Snackbar passwordMismatchSnackbar = Snackbar
                            .make(v, "Passwords don't match, please try again.", Snackbar.LENGTH_LONG);
                    passwordMismatchSnackbar.show();
                    passwordEditText.setText("");
                    verifyPasswordEditText.setText("");
                } else {
                    final Snackbar signupErrorSnackbar = Snackbar
                            .make(v, "User could not be created, check if the email is already registered.", Snackbar.LENGTH_LONG);
                    APIFunctions.signUp(getApplicationContext(), email, password, username, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            String userToken = "";

                            //for each header in array Headers scan for Auth header
                            for (Header header : headers) {
                                if (header.toString().contains("Auth"))
                                    userToken = header.toString().substring(header.toString().indexOf(":") + 2);
                            }

                            sharedPref = getPreferences(Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("userToken", userToken);
                            editor.putString("currentUser", response.toString());
                            editor.apply();

                            // Make an Intent to move on to the next activity
                            Intent mainActivityIntent = new Intent(getApplicationContext(), SignupInitialSetup.class);
                            startActivity(mainActivityIntent);

                            finish();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                            Log.e("com.example.easycourse", "status failure " + statusCode);
                            Log.e("com.example.easycourse", res);
                            signupErrorSnackbar.show();
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("com.example.easycourse", e.toString());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                Log.e("com.example.easycourse", e.toString());
            }
        }
    }

    public void emailLogin(View v) {

        // Removes verify password and username edittexts if visible
        if (verifyPasswordInputLayout.getVisibility() == View.VISIBLE) {
            verifyPasswordInputLayout.setVisibility(View.GONE);
            usernameInputLayout.setVisibility(View.GONE);
            signupButton.setBackgroundResource(R.drawable.signup_button);
            loginButton.setBackgroundResource(R.drawable.login_button);

        } else { // Edittexts are hidden, do logic
            Log.d(TAG, "Login clicked-doing login");
            // Get inputs and check if fields are empty
            // only execute login API when fields are all filled
            try {
                String email = emailEditText.getText().toString();
                String pwd = passwordEditText.getText().toString();

                if (email.length() == 0 || pwd.length() == 0) {
                    Snackbar.make(v, "No password", Snackbar.LENGTH_SHORT).show();
                    return;
                }


                final Snackbar loginErrorSnackbar = Snackbar
                        .make(v, "Log in failed, please check your credentials and network connection.", Snackbar.LENGTH_LONG);

                APIFunctions.login(getApplicationContext(), email, pwd, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        String userToken = "";

                        //for each header in array Headers scan for Auth header
                        for (Header header : headers) {
                            if (header.toString().contains("Auth"))
                                userToken = header.toString().substring(header.toString().indexOf(":") + 2);
                        }

                        // Store user at SharedPreferences
                        sharedPref = getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("userToken", userToken);
                        editor.putString("currentUser", response.toString());
                        editor.apply();

                        // Make an Intent to move on to the next activity
                        Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(mainActivityIntent);

                        finish();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                        // Make a Snackbar to notify user with error
                        loginErrorSnackbar.show();
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("com.example.easycourse", e.toString());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                Log.e("com.example.easycourse", e.toString());
            }
        }
    }

    public void facebookLogin(View v) {
        //implement facebooklogin here
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            titleTextView.startAnimation(titleAnimEnter);
            emailInputLayout.startAnimation(emailAnimEnter);
            passwordInputLayout.startAnimation(passwordAnimEnter);
            loginButton.startAnimation(loginAnimEnter);
            signupButton.startAnimation(signupAnimEnter);
            facebookButton.startAnimation(facebookAnimEnter);
        }
    }
}
