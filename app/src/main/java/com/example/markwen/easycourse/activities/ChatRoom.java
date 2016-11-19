package com.example.markwen.easycourse.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.markwen.easycourse.EasyCourse;
import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.components.main.ChatRecyclerViewAdapter;
import com.example.markwen.easycourse.components.main.RoomRealmRecyclerView;
import com.example.markwen.easycourse.models.main.Message;
import com.example.markwen.easycourse.models.main.Room;
import com.example.markwen.easycourse.models.main.User;
import com.example.markwen.easycourse.models.signup.Course;
import com.example.markwen.easycourse.utils.APIFunctions;
import com.example.markwen.easycourse.utils.SocketIO;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.otto.Subscribe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class ChatRoom extends AppCompatActivity {

    private static final String TAG = "ChatRoom";

    Room currentRoom;
    User currentUser;

    Realm realm;
    SocketIO socketIO;


    ChatRecyclerViewAdapter chatAdapter;
    LinearLayoutManager chatLinearManager;
    ArrayList<Message> messages;


    @BindView(R.id.toolbarChatRoom) Toolbar toolbar;
    @BindView(R.id.toolbarTitleChatRoom) TextView toolbarTitleTextView;
    @BindView(R.id.toolbarSubtitleChatRoom) TextView toolbarSubtitleTextView;
    @BindView(R.id.chatRecyclerView) RecyclerView chatRecyclerView;
    @BindView(R.id.chatAddImageButton) ImageButton addImageButton;
    @BindView(R.id.chatMessageEditText) EditText messageEditText;
    @BindView(R.id.chatSendImageButton) ImageButton sendImageButton;

    //TODO: Animate from roomsview
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);


        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        realm = Realm.getDefaultInstance();

        socketIO = EasyCourse.getAppInstance().getSocketIO();
        currentUser = User.getCurrentUser(this, realm);

        ButterKnife.bind(this);

        //TODO: Parse currentRoom, ensure current room exists
        Intent intent = getIntent();
        String roomName = intent.getStringExtra("Roomname");
        String courseName = intent.getStringExtra("CourseName");
        String roomId = intent.getStringExtra("roomId");
        if (roomName != null) {
            currentRoom = new Room(roomName, courseName);

        } else {
            RealmResults<Room> results = realm.where(Room.class)
                    .equalTo("id", roomId)
                    .findAll();
            if (results.size() > 0) {
                currentRoom = results.first();
            }
        }

        //TODO: null room?
        if (currentRoom != null) {
            toolbarTitleTextView.setText(currentRoom.getRoomname());
            toolbarSubtitleTextView.setText(currentRoom.getCourseName());
        }

        setupChatRecyclerView();


        sendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageText = messageEditText.getText().toString();
                if (!TextUtils.isEmpty(messageText)) {
//                    sendTextMessage(messageText);
                    messageEditText.setText("");
                }
            }
        });
    }

    private void setupChatRecyclerView() {
        chatAdapter = new ChatRecyclerViewAdapter(messages, this);
        chatRecyclerView.setAdapter(chatAdapter);
        chatLinearManager = new LinearLayoutManager(this);
        chatLinearManager.setOrientation(LinearLayoutManager.VERTICAL);
        chatLinearManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(chatLinearManager);
        chatRecyclerView.setHasFixedSize(true);
    }

//    private void sendTextMessage(String messageText){
//        String fixed = messageText.replace("\\", "");
//        Message message;
//        try{
//            //Recieve message from socketIO
//            if(currentRoom.isToUser()){
//                socketIO.sendMessage(fixed, null, currentRoom.getId(), null, 0, 0);
//            }else {
//                socketIO.sendMessage(fixed, currentRoom.getId(), null, null, 0, 0);
//            }
//            message = new Message();
//        }catch (JSONException e) {
//            e.printStackTrace();
//            Log.e(TAG, "sendTextMessage: error");
//        }
//        chatAdapter.addMessage(message);
//        chatRecyclerView.scrollToPosition(messages.size() - 1);
//        socketIO.syncUser();
//    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
