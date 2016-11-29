package com.example.markwen.easycourse.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.models.main.User;
import com.example.markwen.easycourse.utils.SocketIO;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.realm.Realm;

/**
 * Created by nisarg on 28/11/16.
 */

public class UserProfile extends AppCompatActivity {

    TextView textViewUsername;
    EditText editTextUsername;
    ImageButton editUsernameButton;
    FloatingActionButton saveChangesButton;

    boolean isInEditMode = false;

    User user = new User();

    Realm realm;
    SocketIO socket;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userprofile);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        try {
            socket = new SocketIO(this, getApplicationContext());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        textViewUsername = (TextView) findViewById(R.id.textViewUsername);
        editTextUsername = (EditText) findViewById(R.id.editTextUsername);
        saveChangesButton = (FloatingActionButton) findViewById(R.id.saveChangesButton);
        saveChangesButton.hide();

        editUsernameButton = (ImageButton) findViewById(R.id.editUsernameButton);
        editUsernameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                toggleProfileEdit();
            }
        });

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("EasyCourse", Context.MODE_PRIVATE);
        String currentUser = sharedPref.getString("currentUser", null);
        JSONObject currentUserObject;

        Realm.init(getApplicationContext());
        realm = Realm.getDefaultInstance();

        try {
            currentUserObject = new JSONObject(currentUser);
            Log.e("com.example.easycourse", currentUserObject.toString());
            user = user.getByPrimaryKey(realm, currentUserObject.getString("_id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        textViewUsername.setText(user.getUsername());
        editTextUsername.setText(user.getUsername());

        saveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                realm.beginTransaction();
                user.setUsername(editTextUsername.getText().toString());
                realm.copyToRealmOrUpdate(user);
                realm.commitTransaction();
                Log.e("com.example.easycourse", user.getUsername());
                socket.syncUser();
                toggleProfileEdit();
            }
        });



    }

    public void toggleProfileEdit(){
        isInEditMode = !isInEditMode;
        if(isInEditMode){
            textViewUsername.setVisibility(View.GONE);
            editTextUsername.setVisibility(View.VISIBLE);
            saveChangesButton.show();
            editTextUsername.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
        } else {
            textViewUsername.setVisibility(View.VISIBLE);
            editTextUsername.setVisibility(View.GONE);
            saveChangesButton.hide();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editTextUsername.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
