package com.example.markwen.easycourse.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.markwen.easycourse.MainActivity;
import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.SignupInitialSetup;
import com.example.markwen.easycourse.SignupLoginActivity;
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
 * Created by noahrinehart on 10/29/16.
 */

public class SignupLogin extends Fragment {

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
    Animation verifyPasswordAnimEnter;
    Animation usernameAnimEnter;
    Animation loginAnimEnter;
    Animation signupAnimEnter;
    Animation facebookAnimEnter;


    //http://stackoverflow.com/questions/9245408/best-practice-for-instantiating-a-new-android-fragment
    public static SignupLogin newInstance() {
        return new SignupLogin();
    }




    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        FacebookSdk.sdkInitialize(getContext());
        AppEventsLogger.activateApp(getActivity().getApplication());
        return inflater.inflate(R.layout.signup_login, container, false);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View v = getView();

        titleTextView = (TextView) v.findViewById(R.id.textViewTitle);
        emailEditText = (EditText) v.findViewById(R.id.editTextEmail);
        passwordEditText = (EditText) v.findViewById(R.id.editTextPassword);
        verifyPasswordEditText = (EditText) v.findViewById(R.id.editTextVerifyPassword);
        usernameEditText = (EditText) v.findViewById(R.id.editTextUsername);

        emailInputLayout = (TextInputLayout) v.findViewById(R.id.inputLayoutEmail);
        passwordInputLayout = (TextInputLayout) v.findViewById(R.id.inputLayoutPassword);
        verifyPasswordInputLayout = (TextInputLayout) v.findViewById(R.id.inputLayoutVerifyPassword);
        usernameInputLayout = (TextInputLayout) v.findViewById(R.id.inputLayoutUsername);


        signupButton = (Button) v.findViewById(R.id.buttonSignup);
        loginButton = (Button) v.findViewById(R.id.buttonLogin);
        callbackManager = CallbackManager.Factory.create();
        facebookButton = (LoginButton) v.findViewById(R.id.buttonFacebookLogin);
        signupLinearLayout = (LinearLayout) v.findViewById(R.id.linearLayoutSignup);

        // Set username and verify passwords inially gone
        verifyPasswordInputLayout.setVisibility(View.GONE);
        usernameInputLayout.setVisibility(View.GONE);

        // Changing EditText background colors
        emailEditText.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.colorAccent), PorterDuff.Mode.SRC_IN);
        passwordEditText.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.colorAccent), PorterDuff.Mode.SRC_IN);
        verifyPasswordEditText.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.colorAccent), PorterDuff.Mode.SRC_IN);
        usernameEditText.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.colorAccent), PorterDuff.Mode.SRC_IN);

        // Change EditText text color
        emailEditText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorWhiteText));
        passwordEditText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorWhiteText));
        verifyPasswordEditText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorWhiteText));
        usernameEditText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorWhiteText));

        // Add textWatchers to EditTexts's
        emailEditText.addTextChangedListener(new SignupLogin.SignupLoginTextWatcher(emailEditText));
        passwordEditText.addTextChangedListener(new SignupLogin.SignupLoginTextWatcher(passwordEditText));
        verifyPasswordEditText.addTextChangedListener(new SignupLogin.SignupLoginTextWatcher(verifyPasswordEditText));
        usernameEditText.addTextChangedListener(new SignupLogin.SignupLoginTextWatcher(usernameEditText));


        // Animations for views when activity starts/resumes
        titleAnimEnter = AnimationUtils.loadAnimation(getContext(), R.anim.fade_move_in);
        emailAnimEnter = AnimationUtils.loadAnimation(getContext(), R.anim.fade_move_in);
        emailAnimEnter.setStartOffset(250);
        passwordAnimEnter = AnimationUtils.loadAnimation(getContext(), R.anim.fade_move_in);
        passwordAnimEnter.setStartOffset(250);
        verifyPasswordAnimEnter = AnimationUtils.loadAnimation(getContext(), R.anim.fade_move_in);
        verifyPasswordAnimEnter.setStartOffset(250);
        usernameAnimEnter = AnimationUtils.loadAnimation(getContext(), R.anim.fade_move_in);
        usernameAnimEnter.setStartOffset(250);
        loginAnimEnter = AnimationUtils.loadAnimation(getContext(), R.anim.fade_move_in);
        loginAnimEnter.setStartOffset(250 * 2);
        signupAnimEnter = AnimationUtils.loadAnimation(getContext(), R.anim.fade_move_in);
        signupAnimEnter.setStartOffset(250 * 2);
        facebookAnimEnter = AnimationUtils.loadAnimation(getContext(), R.anim.fade_move_in);
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

        startAnimations();

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

                if (!validateEmail())
                    return;
                if (!validatePassword())
                    return;
                if (!validateVerifyPassword())
                    return;
                if (!validateUsername())
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
                    APIFunctions.signUp(getContext(), email, password, username, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            String userToken = "";

                            //for each header in array Headers scan for Auth header
                            for (Header header : headers) {
                                if (header.toString().contains("Auth"))
                                    userToken = header.toString().substring(header.toString().indexOf(":") + 2);
                            }

                            sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("userToken", userToken);
                            editor.putString("currentUser", response.toString());
                            editor.apply();

                            // Make an Intent to move on to the next activity
                            Intent mainActivityIntent = new Intent(getContext(), SignupInitialSetup.class);
                            startActivity(mainActivityIntent);

                            getActivity().finish();
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

        facebookButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            final Snackbar fbLoginErrorSnackbar = Snackbar
                    .make(facebookButton.getRootView(), "Log in failed, please check your credentials and network connection.", Snackbar.LENGTH_LONG);

            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.i("com.example.easycourse", loginResult.getAccessToken().getToken());
                APIFunctions.facebookLogin(getContext(), loginResult.getAccessToken().getToken(), new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Log.i("com.example.easycourse", "Step: 2");

                        String userToken = "";

                        //for each header in array Headers scan for Auth header
                        for (Header header : headers) {
                            if (header.toString().contains("Auth"))
                                userToken = header.toString().substring(header.toString().indexOf(":") + 2);
                        }

                        // Store user at SharedPreferences
                        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("userToken", userToken);
                        editor.putString("currentUser", response.toString());
                        editor.apply();

                        // Make an Intent to move on to the next activity
                        Intent mainActivityIntent = new Intent(getContext(), MainActivity.class);
                        startActivity(mainActivityIntent);

                        getActivity().finish();
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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

                if (!validateEmail())
                    return;
                if (!validatePassword())
                    return;

                final Snackbar loginErrorSnackbar = Snackbar
                        .make(v, "Log in failed, please check your credentials and network connection.", Snackbar.LENGTH_LONG);

                APIFunctions.login(getContext(), email, pwd, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        String userToken = "";

                        //for each header in array Headers scan for Auth header
                        for (Header header : headers) {
                            if (header.toString().contains("Auth"))
                                userToken = header.toString().substring(header.toString().indexOf(":") + 2);
                        }

                        // Store user at SharedPreferences
                        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("userToken", userToken);
                        editor.putString("currentUser", response.toString());
                        editor.apply();

                        // Make an Intent to move on to the next activity
                        Intent mainActivityIntent = new Intent(getContext(), MainActivity.class);
                        startActivity(mainActivityIntent);

                        getActivity().finish();
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


    private void startAnimations() {
        titleTextView.startAnimation(titleAnimEnter);
        emailInputLayout.startAnimation(emailAnimEnter);
        passwordInputLayout.startAnimation(passwordAnimEnter);
        // If signup info visible, show animation
        if (verifyPasswordInputLayout.getVisibility() == View.VISIBLE) {
            verifyPasswordInputLayout.startAnimation(verifyPasswordAnimEnter);
            usernameInputLayout.startAnimation(usernameAnimEnter);
        }
        loginButton.startAnimation(loginAnimEnter);
        signupButton.startAnimation(signupAnimEnter);
        facebookButton.startAnimation(facebookAnimEnter);
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
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

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
