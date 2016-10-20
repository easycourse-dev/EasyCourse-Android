package com.example.markwen.easycourse;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Mark Wen on 10/18/2016.
 */

public class SignupLogin extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_login);
        // Hide toolbar for this specific activity
        getSupportActionBar().hide();

        Button signup = (Button) findViewById(R.id.buttonSignup);
        Button login = (Button) findViewById(R.id.buttonLogin);

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    EditText emailTxt = (EditText) findViewById(R.id.editTextEmail);
                    EditText pTxt = (EditText) findViewById(R.id.editTextPassword);
                    EditText uTxt = (EditText) findViewById(R.id.editTextUsername);

                    String email = emailTxt.getText().toString();
                    String pwd = pTxt.getText().toString();
                    String uname = uTxt.getText().toString();
                    APIFunctions.signUp(getApplicationContext(),email,pwd,uname, new JsonHttpResponseHandler(){
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Log.e("com.example.easycourse", "status success "+statusCode);
                            Log.e("com.example.easycourse", response.toString());
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                            Log.e("com.example.easycourse", "status failure "+statusCode);
                            Log.e("com.example.easycourse", res.toString());
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

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    EditText emailTxt = (EditText) findViewById(R.id.editTextEmail);
                    EditText pTxt = (EditText) findViewById(R.id.editTextPassword);

                    String email = emailTxt.getText().toString();
                    String pwd = pTxt.getText().toString();
                    APIFunctions.login(getApplicationContext(),email,pwd, new JsonHttpResponseHandler(){
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Log.e("com.example.easycourse", "status success "+statusCode);
                            Log.e("com.example.easycourse", response.toString());
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                            Log.e("com.example.easycourse", "status failure "+statusCode);
                            Log.e("com.example.easycourse", res.toString());
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
