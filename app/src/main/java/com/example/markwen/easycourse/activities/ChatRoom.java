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
import android.widget.Toast;

import com.example.markwen.easycourse.EasyCourse;
import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.components.main.ChatRecyclerViewAdapter;
import com.example.markwen.easycourse.models.main.Message;
import com.example.markwen.easycourse.models.main.Room;
import com.example.markwen.easycourse.models.main.User;
import com.example.markwen.easycourse.utils.SocketIO;

import org.json.JSONException;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

public class ChatRoom extends AppCompatActivity {

    private static final String TAG = "ChatRoom";


    Realm realm;
    SocketIO socketIO;

    Room currentRoom;
    User currentUser;

    @BindView(R.id.toolbarChatRoom)
    Toolbar toolbar;
    @BindView(R.id.toolbarTitleChatRoom)
    TextView toolbarTitleTextView;
    @BindView(R.id.toolbarSubtitleChatRoom)
    TextView toolbarSubtitleTextView;
    @BindView(R.id.chatRecyclerView)
    RecyclerView chatRecyclerView;
    @BindView(R.id.chatAddImageButton)
    ImageButton addImageButton;
    @BindView(R.id.chatMessageEditText)
    EditText messageEditText;
    @BindView(R.id.chatSendImageButton)
    ImageButton sendImageButton;

    ChatRecyclerViewAdapter chatRecyclerViewAdapter;
    RealmResults<Message> messages;

    //TODO: Animate from roomsview
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        ButterKnife.bind(this);


        realm = Realm.getDefaultInstance();
        socketIO = EasyCourse.getAppInstance().getSocketIO();
        currentUser = User.getCurrentUser(this, realm);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        handleIntent();

        setupChatRecyclerView();

        sendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageText = messageEditText.getText().toString();
                if (!TextUtils.isEmpty(messageText)) {
                    boolean sent = sendTextMessage(messageText);
                    if(sent)
                    messageEditText.setText("");
                }
            }
        });
    }

    private void handleIntent() {
        Intent intent = getIntent();
        String roomId = intent.getStringExtra("roomId");
        currentRoom = Room.getRoomById(this, realm, roomId);
        if (currentRoom == null) {
            Log.d(TAG, "current room not found!");
            Toast.makeText(this,"Current room not found!", Toast.LENGTH_SHORT).show();
            this.finish();
        }
        //TODO: Get course title
        toolbarTitleTextView.setText(currentRoom.getRoomName());
        toolbarSubtitleTextView.setText(currentRoom.getCourseName());
    }

    //TODO: private messages
    private void setupChatRecyclerView() {
        messages = realm.where(Message.class).findAll();
        chatRecyclerViewAdapter = new ChatRecyclerViewAdapter(this, messages);
        chatRecyclerView.setAdapter(chatRecyclerViewAdapter);
        chatRecyclerView.setHasFixedSize(true);
        LinearLayoutManager chatLinearManager = new LinearLayoutManager(this);
        chatLinearManager.setOrientation(LinearLayoutManager.VERTICAL);
        chatLinearManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(chatLinearManager);
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

    public boolean sendTextMessage(String message){
        try {
            EasyCourse.getAppInstance().socketIO.sendMessage(message, currentRoom.getId(), currentUser.getId(), null, 0, 0);
        }catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
