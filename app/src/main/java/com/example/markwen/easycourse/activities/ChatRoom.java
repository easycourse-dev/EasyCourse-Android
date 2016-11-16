package com.example.markwen.easycourse.activities;

import android.content.Intent;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import cz.msebera.android.httpclient.Header;
import io.realm.Realm;

public class ChatRoom extends AppCompatActivity {

    private static final String TAG = "ChatRoom";

    private Room currentRoom;

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

        toolbar = (Toolbar) findViewById(R.id.toolbarChatRoom);
        toolbarTitleTextView = (TextView) findViewById(R.id.toolbarTitleChatRoom);
        toolbarSubtitleTextView = (TextView) findViewById(R.id.toolbarSubtitleChatRoom);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //TODO: Parse currentRoom
        Intent intentFromRooms = getIntent();
        String roomName = intentFromRooms.getStringExtra("Roomname");
        String courseName = intentFromRooms.getStringExtra("CourseName");
        currentRoom = new Room(roomName, courseName);

        toolbarTitleTextView.setText(roomName);
        toolbarSubtitleTextView.setText(courseName);

        realm = Realm.getDefaultInstance();
        Log.d(TAG, "path: " + realm.getPath());

//        socketIO = new SocketIO(this, this);

        Message message1 = new Message("Noah Rinehart", "LOL", "https://avatars0.githubusercontent.com/u/7402294?v=3&s=460.jpg", Calendar.getInstance().getTime());
        message1.setToUser(false);
        Message message2 = new Message("Swag", "LMAO", "https://avatars0.githubusercontent.com/u/7402294?v=3&s=460.jpg", Calendar.getInstance().getTime());
        Message message3 = new Message("Swag", "Funny", "https://avatars0.githubusercontent.com/u/7402294?v=3&s=460.jpg", Calendar.getInstance().getTime());
        Message message4 = new Message("Noah Rinehart", "I don't know how this works lol \n lol\n", "https://avatars0.githubusercontent.com/u/7402294?v=3&s=460.jpg", Calendar.getInstance().getTime());
        message4.setToUser(false);


        messages = new ArrayList<>();
        messages.add(message1);
        messages.add(message2);
        messages.add(message3);
        messages.add(message4);




        chatRecyclerView = (RecyclerView) findViewById(R.id.chatRecyclerView);
        chatAdapter = new ChatRecyclerViewAdapter(messages, this);
        chatRecyclerView.setAdapter(chatAdapter);
        chatLinearManager = new LinearLayoutManager(this);
        chatLinearManager.setOrientation(LinearLayoutManager.VERTICAL);
        chatLinearManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(chatLinearManager);
        chatRecyclerView.setHasFixedSize(true);

        addImageButton = (ImageButton) findViewById(R.id.chatAddImageButton);
        messageEditText = (EditText) findViewById(R.id.chatMessageEditText);
        sendImageButton = (ImageButton) findViewById(R.id.chatSendImageButton);

        sendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageText = messageEditText.getText().toString();
                if (!TextUtils.isEmpty(messageText)) {
                    String fixed = messageText.replace("\\", "");
                    Message message = new Message("Noah Rinehart", fixed, "https://avatars0.githubusercontent.com/u/7402294?v=3&s=460", Calendar.getInstance().getTime());
                    //TODO: remove escape charactors
                    chatAdapter.getChatRoomList().add(message);
                    chatAdapter.notifyDataSetChanged();
                    chatRecyclerView.scrollToPosition(messages.size() - 1);
                    messageEditText.setText("");
//                    socketIO.syncUser();
//                    User curUser = User.getCurrentUser(realm);
                    if (!messageEditText.getText().toString().trim().isEmpty()) {
                        //TODO: fill out message, animate
                        Message msg = new Message();
//                        try {
//                            socketIO.sendMessage(messageText, currentRoom.getId(), curUser.getId(), null, 0, 0);
//                            Message.updateMessageToRealm(msg, realm);
//                        } catch (JSONException e) {
//                            //TODO:Message not sent
//                            e.printStackTrace();
//                            Log.e(TAG, "message sent");
//                        }
                    }
                }
            }
        });

    }

    //TODO: Realm integration
    public ArrayList<Message> fetchMessagesFromRealm() {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        socketIO.disconnect();
    }


}
