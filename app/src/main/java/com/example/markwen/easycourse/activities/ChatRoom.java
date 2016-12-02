package com.example.markwen.easycourse.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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
import com.example.markwen.easycourse.utils.eventbus.Event;
import com.squareup.otto.Subscribe;

import org.json.JSONException;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

import static com.example.markwen.easycourse.EasyCourse.bus;

public class ChatRoom extends AppCompatActivity {

    private static final String TAG = "ChatRoom";


    Realm realm;
    SocketIO socketIO;
    Snackbar disconnectSnackbar;


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
        socketIO.syncUser();

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        handleIntent();

        setupChatRecyclerView();

        sendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finalSendMessage();
            }
        });

        messageEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled = false;
                if (i == EditorInfo.IME_ACTION_SEND) {
                    finalSendMessage();
                    handled = true;
                }
                return handled;
            }
        });

        //Setup snackbar for disconnect
        disconnectSnackbar = Snackbar.make(findViewById(R.id.relativeLayoutChatRoom), "Disconnected!", Snackbar.LENGTH_INDEFINITE);

        bus.register(this);
    }

    private void finalSendMessage() {
        String messageText = messageEditText.getText().toString();
        if (!TextUtils.isEmpty(messageText)) {
            if (sendTextMessage(messageText)) {
                messageEditText.setText("");
                chatRecyclerViewAdapter.notifyDataSetChanged();
                chatRecyclerView.smoothScrollToPosition(chatRecyclerViewAdapter.getItemCount()+1);
            }
        }
    }

    private void handleIntent() {
        Intent intent = getIntent();
        String roomId = intent.getStringExtra("roomId");
        this.currentRoom = Room.getRoomById(this, realm, roomId);
        if (this.currentRoom == null) {
            Log.d(TAG, "current room not found!");
            Toast.makeText(this, "Current room not found!", Toast.LENGTH_SHORT).show();
            this.finish();
        }
        //TODO: Get course title
        toolbarTitleTextView.setText(currentRoom.getRoomName());
        toolbarSubtitleTextView.setText(currentRoom.getCourseName());
    }

    //TODO: private messages
    private void setupChatRecyclerView() {
        messages = realm.where(Message.class).equalTo("toRoom", currentRoom.getId()).findAll();
        chatRecyclerViewAdapter = new ChatRecyclerViewAdapter(this, messages);
        chatRecyclerView.setAdapter(chatRecyclerViewAdapter);
        chatRecyclerView.setHasFixedSize(true);
        LinearLayoutManager chatLinearManager = new LinearLayoutManager(this);
        chatLinearManager.setOrientation(LinearLayoutManager.VERTICAL);
        chatLinearManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(chatLinearManager);

        messages.addChangeListener(new RealmChangeListener<RealmResults<Message>>() {
            @Override
            public void onChange(RealmResults<Message> element) {
                chatRecyclerView.smoothScrollToPosition(chatRecyclerViewAdapter.getItemCount());
            }
        });
    }

    private boolean sendTextMessage(String messageText) {
        String fixed = messageText.replace("\\", "");
        try {
            //Recieve message from socketIO
            if (this.currentRoom.isToUser()) {
                socketIO.sendMessage(fixed, null, this.currentRoom.getId(), null, 0, 0);
            } else {
                socketIO.sendMessage(fixed, this.currentRoom.getId(), null, null, 0, 0);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "sendTextMessage: error");
            return false;
        }
        socketIO.syncUser();
        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (socketIO != null)
            socketIO.syncUser();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @Subscribe
    public void disconnectEvent(Event.DisconnectEvent event) {
        disconnectSnackbar.show();
    }

    @Subscribe
    public void reconnectEvent(Event.ReconnectEvent event) {
        if(disconnectSnackbar != null) {
            disconnectSnackbar.dismiss();
        }
    }
}
