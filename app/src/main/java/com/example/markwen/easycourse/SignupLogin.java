package com.example.markwen.easycourse;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
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
                    String pwd = passwordEditText.getText().toString();
                    String uname = usernameEditText.getText().toString();

                    // TODO: add verify password function
                    // make API call only if the 2 passwords are the same
                    // make a Toast and don't do anything if they don't match

                    APIFunctions.signUp(getApplicationContext(),email,pwd,uname, new JsonHttpResponseHandler(){
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Log.e("com.example.easycourse", "status success " + statusCode);
                            Log.e("com.example.easycourse", response.toString());

                            // TODO: store user at SharedPreferences

                            // TODO: make an Intent to move on to the next activity
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                            Log.e("com.example.easycourse", "status failure " + statusCode);
                            Log.e("com.example.easycourse", res.toString());

                            // TODO: make a Toast to notify user with error

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

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String email = emailEditText.getText().toString();
                    String pwd = passwordEditText.getText().toString();

                    APIFunctions.login(getApplicationContext(), email, pwd, new JsonHttpResponseHandler(){
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            // Log.e("com.example.easycourse", "status success " + statusCode);
                            // Log.e("com.example.easycourse", response.toString());

                            // Store user at SharedPreferences
                            sharedPref = getPreferences(Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("currentUser", response.toString());
                            editor.commit();

                            // Log.e("currentUser", sharedPref.getString("currentUser", "0"));
                            // TODO: make an Intent to move on to the next activity
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                            Log.e("com.example.easycourse", "status failure "+statusCode);
                            Log.e("com.example.easycourse", res.toString());

                            // TODO: make a Toast to notify user with error

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
    }
}
