package io.easycourse.www.easycourse.fragments.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;
import io.easycourse.www.easycourse.EasyCourse;
import io.easycourse.www.easycourse.R;
import io.easycourse.www.easycourse.activities.MainActivity;
import io.easycourse.www.easycourse.models.main.User;
import io.easycourse.www.easycourse.utils.APIFunctions;
import io.easycourse.www.easycourse.utils.SocketIO;
import io.realm.Realm;


/**
 * Created by Mark Wen on 10/18/2016.
 */


public class ResetPassword extends AppCompatActivity {

    private static String userToken;
    private static String email;
    private static String pwd;
    private static final String TAG = "ResetPassword";
    private static String error="";
    SharedPreferences sharedPref;

    Realm realm;
    SocketIO socketIO;
    User currentUser;


    @BindView(R.id.resetpasswordToolbar)
    Toolbar toolbar;
    @BindView(R.id.submit)
    Button submit;
    @BindView(R.id.currentPassword)
    EditText curpw;
    @BindView(R.id.newPassword1)
    EditText npw1;
    @BindView(R.id.newPassword2)
    EditText npw2;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Reset Password");
            getSupportActionBar().setElevation(0);
        }

        socketIO = EasyCourse.getAppInstance().getSocketIO();
        realm = Realm.getDefaultInstance();
        currentUser = User.getCurrentUser(this, realm);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                check(v);
            }
        });

    }
    public void check(final View v)
    {
        final String pw=curpw.getText().toString().trim();
        try {
            final String np1 = npw1.getText().toString().trim();
            final String np2 = npw2.getText().toString().trim();
            if (np1.equals(np2) == false) {
                final Snackbar loginError = Snackbar
                        .make(v, "Please check your new passwords are incorrect.", Snackbar.LENGTH_LONG);
                loginError.show();
            } else if (!validatePassword(np1)) {
                final Snackbar loginError = Snackbar
                        .make(v, "Please check your new passwords." + error, Snackbar.LENGTH_LONG);
                loginError.show();
            } else {
                APIFunctions.updatePassword(v.getContext(), currentUser.getEmail(), pw, np1, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            for (Header header : headers) {
                                if (header.toString().contains("Auth"))
                                    userToken = header.toString().substring(header.toString().indexOf(":") + 2);
                            }
                            logout(v);
                            Intent i=new Intent(ResetPassword.this, MainActivity.class);
                            startActivity(i);
                        } catch (JSONException e) {
                            Log.e(TAG, e.toString());
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject response) {
                        System.out.println(statusCode+"---------");
                        final Snackbar loginError = Snackbar
                                .make(v, "Please check your network connection.", Snackbar.LENGTH_LONG);
                        loginError.show();
                    }
                });
            }
        }catch(JSONException | UnsupportedEncodingException e){
            Log.e(TAG, e.toString());
        }
    }
    private boolean validatePassword(String password) {

        if (password.isEmpty()) {
            error="Missing password";
            return false;
        }

        if (password.length() < 8 || password.length() > 32) {
            error="Password length not between 8 and 32";
            return false;
        }


        Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher("I am a string");

        if (m.find()) {
            error="Password cannot have a special character";
            return false;
        }
        return true;
    }
    public void logout(final View v) throws JSONException {
        try {
            EasyCourse.getAppInstance().socketIO.disconnect();
            sharedPref = getSharedPreferences("EasyCourse", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("userToken", userToken);
            editor.apply();
            EasyCourse.getAppInstance().createSocketIO();
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}


