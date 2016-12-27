package com.example.markwen.easycourse.components.main.chat;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.markwen.easycourse.EasyCourse;
import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.activities.ChatRoomActivity;
import com.example.markwen.easycourse.models.main.Message;
import com.example.markwen.easycourse.models.main.Room;
import com.example.markwen.easycourse.models.main.User;
import com.example.markwen.easycourse.utils.SocketIO;

import org.json.JSONException;

import java.io.IOException;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;


public class ChatRoomFragment extends Fragment {

    private static final String TAG = "ChatRoomFragment";
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 3;
    private static final int PERMISSION_DENIED = -1;
    private static final int CHOOSE_IMAGE_INTENT = 4;
    private static final int TAKE_IMAGE_INTENT = 5;

    private ChatRoomActivity activity;
    private Realm realm;
    private SocketIO socketIO;

    private Room currentRoom;
    private User currentUser;


    @BindView(R.id.chatRecyclerView)
    RecyclerView chatRecyclerView;
    @BindView(R.id.chatAddImageButton)
    ImageButton addImageButton;
    @BindView(R.id.chatMessageEditText)
    EditText messageEditText;
    @BindView(R.id.chatSendImageButton)
    ImageButton sendImageButton;


    private ChatRecyclerViewAdapter chatRecyclerViewAdapter;
    private RealmResults<Message> messages;

    public ChatRoomFragment() {
    }

    //TODO: streamline new fragments so current user/realm not reinitiated
    public static ChatRoomFragment newInstance(ChatRoomActivity activity, Room currentRoom, User currentUser) {
        ChatRoomFragment fragment = new ChatRoomFragment();
        fragment.activity = activity;
        fragment.currentRoom = currentRoom;
        fragment.currentUser = currentUser;
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chat_room, container, false);
        ButterKnife.bind(this, v);

        realm = Realm.getDefaultInstance();
        socketIO = EasyCourse.getAppInstance().getSocketIO();
        socketIO.syncUser();

        setupChatRecyclerView();
        setupOnClickListeners();


        return v;
    }

    //TODO: private messages
    private void setupChatRecyclerView() {
//
//        Message message1 = new Message(null, "57e2cdea8b59ae00115a8fc5", "581162b12e5c5000114f8df5", null, "http://www.planwallpaper.com/static/images/desktop-year-of-the-tiger-images-wallpaper.jpg", null, false, 0,0, "57e2cdea8b59ae00115a8fc5", new Date());
//        Message message2 = new Message(null, "57e2cdea8b59ae00115a8fc5", "581162b12e5c5000114f8df5", null, "http://www.planwallpaper.com/static/images/desktop-year-of-the-tiger-images-wallpaper.jpg", null, false, 0,0, "57e2cdea8b59ae00115a8fc5", new Date());
//        Message message3 = new Message(null, "57e2cdea8b59ae00115a8fc5", "581162b12e5c5000114f8df5", null, "http://www.planwallpaper.com/static/images/desktop-year-of-the-tiger-images-wallpaper.jpg", null, false, 0,0, "57e2cdea8b59ae00115a8fc5", new Date());
//        Message message4 = new Message(null, "57e2cdea8b59ae00115a8fc5", "581162b12e5c5000114f8df5", null, "http://www.planwallpaper.com/static/images/desktop-year-of-the-tiger-images-wallpaper.jpg", null, false, 0,0, "57e2cdea8b59ae00115a8fc5", new Date());
//        Message message5 = new Message(null, "57e2cdea8b59ae00115a8fc5", "581162b12e5c5000114f8df5", null, "http://www.planwallpaper.com/static/images/desktop-year-of-the-tiger-images-wallpaper.jpg", null, false, 0,0, "57e2cdea8b59ae00115a8fc5", new Date());
//        Message.updateMessageToRealm(message1, realm);
//        Message.updateMessageToRealm(message2, realm);
//        Message.updateMessageToRealm(message3, realm);
//        Message.updateMessageToRealm(message4, realm);
//        Message.updateMessageToRealm(message5, realm);


        messages = realm.where(Message.class).equalTo("toRoom", currentRoom.getId()).findAll();
        chatRecyclerViewAdapter = new ChatRecyclerViewAdapter(activity, messages);
        chatRecyclerView.setAdapter(chatRecyclerViewAdapter);
        chatRecyclerView.setHasFixedSize(true);
        LinearLayoutManager chatLinearManager = new LinearLayoutManager(activity);
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


    private void setupOnClickListeners() {
        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageDialog();
            }
        });

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
    }


    private void showImageDialog() {
        new MaterialDialog.Builder(activity)
                .items(R.array.addImageDialog)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        handleAddImage(which, view);
                    }
                })
                .show();
    }

    //TODO: fix add images
    private void handleAddImage(int which, View view) {
        switch (which) {
            case 0:  // choose image
                int permissionCheck1 = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
                if (permissionCheck1 == PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                } else {
                    chooseImage();
                }

                break;
            case 1:  // take image
                takeImage();
                break;
        }
    }

    private void chooseImage() {
        Intent chooseImageIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(chooseImageIntent, CHOOSE_IMAGE_INTENT);
    }

    private void takeImage() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, TAKE_IMAGE_INTENT);
        }
    }


    private void finalSendMessage() {
        String messageText = messageEditText.getText().toString();
        if (!TextUtils.isEmpty(messageText)) {
            if (sendTextMessage(messageText)) {
                messageEditText.setText("");
                chatRecyclerViewAdapter.notifyDataSetChanged();
                chatRecyclerView.smoothScrollToPosition(chatRecyclerViewAdapter.getItemCount() + 1);
            }
        }
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    chooseImage();
                }
                break;
            }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case CHOOSE_IMAGE_INTENT:
                if(resultCode == Activity.RESULT_OK){
                    Uri selectedImage = data.getData();
                    try {
                        Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), selectedImage);
                        if(imageBitmap == null) return;
                    }catch (IOException e) {
                        Log.e(TAG, "onActivityResult: ", e);
                    }
                    Log.d(TAG, "onActivityResult: " + selectedImage.toString());
                }
                break;

            case TAKE_IMAGE_INTENT:
                if (resultCode == Activity.RESULT_OK) {
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    if(imageBitmap == null) return;
                    Log.d(TAG, "onActivityResult: " + imageBitmap.toString());
                }
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (socketIO != null)
            socketIO.syncUser();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
        chatRecyclerViewAdapter.closeRealm();
    }


}
