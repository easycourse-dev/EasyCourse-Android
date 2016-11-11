package com.example.markwen.easycourse.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.models.signup.UserSetup;

public class ChatRoom extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intentFromRooms = getIntent();
        CharSequence title = intentFromRooms.getStringExtra("UserSetup");
        getSupportActionBar().setTitle(title);
    }

}
