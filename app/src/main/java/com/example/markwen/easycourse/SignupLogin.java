package com.example.markwen.easycourse;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.markwen.easycourse.utils.APIFunctions;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
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
    LoginButton facebookButton;
    CallbackManager callbackManager;
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
        // Facebook SDK setup
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(getApplication());

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
        callbackManager = CallbackManager.Factory.create();
        facebookButton = (LoginButton) findViewById(R.id.buttonFacebookLogin);
        signupLinearLayout = (LinearLayout) findViewById(R.id.linearLayoutSignup);

        // Set username and verify passwords inially gone
        verifyPasswordInputLayout.setVisibility(View.GONE);
        usernameInputLayout.setVisibility(View.GONE);

        // Changing EditText background colors
        emailEditText.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.SRC_IN);
        passwordEditText.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.SRC_IN);
        verifyPasswordEditText.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.SRC_IN);
        usernameEditText.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.SRC_IN);

        // Change EditText text color
        emailEditText.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorWhiteText));
        passwordEditText.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorWhiteText));
        verifyPasswordEditText.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorWhiteText));
        usernameEditText.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorWhiteText));

        // Add textWatchers to EditTexts's
        emailEditText.addTextChangedListener(new SignupLoginTextWatcher(emailEditText));
        passwordEditText.addTextChangedListener(new SignupLoginTextWatcher(passwordEditText));
        verifyPasswordEditText.addTextChangedListener(new SignupLoginTextWatcher(verifyPasswordEditText));
        usernameEditText.addTextChangedListener(new SignupLoginTextWatcher(usernameEditText));



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

    // http://www.androidhive.info/2015/09/android-material-design-floating-labels-for-edittext/

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

                if(!validateEmail())
                    return;
                if(!validatePassword())
                    return;
                if(!validateVerifyPassword())
                    return;
                if(!validateUsername())
                    return;


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

                            sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
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
        };

        facebookButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            final Snackbar fbLoginErrorSnackbar = Snackbar
                    .make(facebookButton.getRootView(), "Log in failed, please check your credentials and network connection.", Snackbar.LENGTH_LONG);

            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.i("com.example.easycourse", loginResult.getAccessToken().getToken().toString());
                APIFunctions.facebookLogin(getApplicationContext(), loginResult.getAccessToken().getToken().toString(), new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Log.i("com.example.easycourse", "Step: 2");

                        String userToken = "";

                        //for each header in array Headers scan for Auth header
                        for(Header header: headers){
                            if(header.toString().contains("Auth"))
                                userToken = header.toString().substring(header.toString().indexOf(":")+2);
                        }

                        // Store user at SharedPreferences
                        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("userToken", userToken);
                        editor.putString("currentUser", response.toString());
                        editor.commit();

                        // Make an Intent to move on to the next activity
                        Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(mainActivityIntent);

                        finish();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                        // Make a Snackbar to notify user with error
                        fbLoginErrorSnackbar.show();
                    }
                });
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                // Make a Snackbar to notify user with error
                fbLoginErrorSnackbar.show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
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

                if(!validateEmail())
                    return;
                if(!validatePassword())
                    return;

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

    // Validates the email for inconsistencies
    private boolean validateEmail() {
        String email = emailEditText.getText().toString().trim();

        if (email.isEmpty()) {
            emailInputLayout.setError("Missing email");
            emailEditText.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.setError("Email is not correct");
            emailEditText.requestFocus();
            return false;
        }

        emailInputLayout.setErrorEnabled(false);
        return true;
    }

    // Validates the password for inconsistencies
    private boolean validatePassword() {
        String password = passwordEditText.getText().toString().trim();

        if (password.isEmpty()) {
            passwordInputLayout.setError("Missing password");
            passwordEditText.requestFocus();
            return false;
        }

        if (password.length() < 6 || password.length() > 20) {
            passwordInputLayout.setError("Password length not between 6 and 20");
            passwordEditText.requestFocus();
            return false;
        }


        passwordInputLayout.setErrorEnabled(false);
        return true;
    }

    // Validates the verify password for inconsistencies
    private boolean validateVerifyPassword() {
        String password = passwordEditText.getText().toString().trim();
        String verifyPassword = verifyPasswordEditText.getText().toString().trim();

        if (!password.equals(verifyPassword)) {
            passwordInputLayout.setError("Passwords don't match");
            verifyPasswordInputLayout.setError("Passwords don't match");
            verifyPasswordEditText.requestFocus();
            return false;
        }

        verifyPasswordInputLayout.setErrorEnabled(false);
        passwordInputLayout.setErrorEnabled(false);
        return true;
    }


    // Validates the username for inconsistencies
    private boolean validateUsername() {
        String username = usernameEditText.getText().toString().trim();

        if (username.isEmpty()) {
            usernameInputLayout.setError("Missing username");
            usernameEditText.requestFocus();
            return false;
        }

        if (username.length() < 6 || username.length() > 20) {
            usernameInputLayout.setError("Username length not between 6 and 20");
            usernameEditText.requestFocus();
            return false;
        }

        usernameInputLayout.setErrorEnabled(false);
        return true;
    }

    // TextWatcher to show error when typing out email/password ..etc
    public class SignupLoginTextWatcher implements TextWatcher {

        private View view;

        private SignupLoginTextWatcher(View view) {
            this.view = view;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            switch (view.getId()) {
                case R.id.editTextEmail:
                    validateEmail();
                    break;
                case R.id.editTextPassword:
                    validatePassword();
                    break;
                case R.id.editTextVerifyPassword:
                    validateVerifyPassword();
                    break;
                case R.id.editTextUsername:
                    validateUsername();
                    break;
            }
        }
    }



}
