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

import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.components.main.ChatRecyclerViewAdapter;
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

import cz.msebera.android.httpclient.Header;
import io.realm.Realm;
import io.realm.RealmResults;

public class ChatRoom extends AppCompatActivity {

    private static final String TAG = "ChatRoom";

    private Room currentRoom;
    private User currentUser;

    private Realm realm;
    private SocketIO socketIO;

    private RecyclerView chatRecyclerView;

    private ChatRecyclerViewAdapter chatAdapter;
    private LinearLayoutManager chatLinearManager;
    private ArrayList<Message> messages;

    ImageButton addImageButton;
    //TODO: random hint
    EditText messageEditText;
    ImageButton sendImageButton;

    Toolbar toolbar;
    TextView toolbarTitleTextView;
    TextView toolbarSubtitleTextView;

    //TODO: Animate from roomsview
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        //TODO: Parse currentRoom
        Intent intentFromRooms = getIntent();
        String roomName = intentFromRooms.getStringExtra("Roomname");
        String courseName = intentFromRooms.getStringExtra("CourseName");
        currentRoom = new Room(roomName, courseName);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        realm = Realm.getDefaultInstance();
        Log.d(TAG, "path: " + realm.getPath());

        socketIO = new SocketIO(this, this);
        currentUser = User.getCurrentUser(this, realm);



        toolbar = (Toolbar) findViewById(R.id.toolbarChatRoom);
        toolbarTitleTextView = (TextView) findViewById(R.id.toolbarTitleChatRoom);
        toolbarSubtitleTextView = (TextView) findViewById(R.id.toolbarSubtitleChatRoom);
        chatRecyclerView = (RecyclerView) findViewById(R.id.chatRecyclerView);
        addImageButton = (ImageButton) findViewById(R.id.chatAddImageButton);
        messageEditText = (EditText) findViewById(R.id.chatMessageEditText);
        sendImageButton = (ImageButton) findViewById(R.id.chatSendImageButton);


        toolbarTitleTextView.setText(roomName);
        toolbarSubtitleTextView.setText(courseName);


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

    //TODO: Realm integration
    public ArrayList<Message> fetchMessagesFromRealm() {
        return null;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
//        socketIO.disconnect();
    }


    @Subscribe
    public void onMessageReceived(Message message) {
        chatAdapter.getChatRoomList().add(message);
        chatAdapter.notifyDataSetChanged();
    }
}
