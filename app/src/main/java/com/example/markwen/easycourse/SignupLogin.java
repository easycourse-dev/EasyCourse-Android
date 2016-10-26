package com.example.markwen.easycourse;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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

    EditText emailEditText;
    EditText passwordEditText;
    EditText verifyPasswordEditText;
    EditText usernameEditText;

    Button signupButton;
    Button loginButton;
    Button fbLoginButton;
    SharedPreferences sharedPref;

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
        signupButton = (Button) findViewById(R.id.buttonSignup);
        loginButton = (Button) findViewById(R.id.buttonLogin);
        fbLoginButton = (Button) findViewById(R.id.buttonFacebookLogin);

        // Changing EditText colors
        emailEditText.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.SRC_IN);
        passwordEditText.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.SRC_IN);
        verifyPasswordEditText.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.SRC_IN);
        usernameEditText.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.SRC_IN);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String email = emailEditText.getText().toString();
                    String password = passwordEditText.getText().toString();
                    String verifyPassword = verifyPasswordEditText.getText().toString();
                    String username = usernameEditText.getText().toString();

                    // make API call only if the 2 passwords are the same
                    // make a Toast and don't do anything if they don't match
                    if(!password.equals(verifyPassword)){
                        Snackbar passwordMismatchSnackbar = Snackbar
                                .make(view, "Passwords don't match, please try again.", Snackbar.LENGTH_LONG);
                        passwordMismatchSnackbar.show();
                        passwordEditText.setText("");
                        verifyPasswordEditText.setText("");
                    }
                    else {
                        final Snackbar signupErrorSnackbar = Snackbar
                                .make(view, "User could not be created, check if the email is already registered.", Snackbar.LENGTH_LONG);
                        APIFunctions.signUp(getApplicationContext(), email, password, username, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                String userToken = "";

                                //for each header in array Headers scan for Auth header
                                for(Header header: headers){
                                    if(header.toString().contains("Auth"))
                                        userToken = header.toString().substring(header.toString().indexOf(":")+2);
                                }

                                sharedPref = getPreferences(Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString("userToken", userToken);
                                editor.putString("currentUser", response.toString());
                                editor.commit();

                                // Make an Intent to move on to the next activity
                                Intent mainActivityIntent = new Intent(getApplicationContext(), SignupInitialSetup.class);
                                startActivity(mainActivityIntent);

                                finish();
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                                Log.e("com.example.easycourse", "status failure " + statusCode);
                                Log.e("com.example.easycourse", res.toString());
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
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String email = emailEditText.getText().toString();
                    String pwd = passwordEditText.getText().toString();

                    final Snackbar loginErrorSnackbar = Snackbar
                            .make(view, "Log in failed, please check your credentials and network connection.", Snackbar.LENGTH_LONG);

                    APIFunctions.login(getApplicationContext(), email, pwd, new JsonHttpResponseHandler(){
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            String userToken = "";

                            //for each header in array Headers scan for Auth header
                            for(Header header: headers){
                                if(header.toString().contains("Auth"))
                                    userToken = header.toString().substring(header.toString().indexOf(":")+2);
                            }

                            // Store user at SharedPreferences
                            sharedPref = getPreferences(Context.MODE_PRIVATE);
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
        });

        fbLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
}
