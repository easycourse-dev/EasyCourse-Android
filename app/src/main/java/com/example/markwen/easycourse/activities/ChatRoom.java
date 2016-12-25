package com.example.markwen.easycourse.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.markwen.easycourse.EasyCourse;
import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.components.main.ChatRecyclerViewAdapter;
import com.example.markwen.easycourse.models.main.Message;
import com.example.markwen.easycourse.models.main.Room;
import com.example.markwen.easycourse.models.main.User;
import com.example.markwen.easycourse.utils.SocketIO;
import com.example.markwen.easycourse.utils.eventbus.Event;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.DimenHolder;
import com.mikepenz.materialdrawer.interfaces.OnCheckedChangeListener;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondarySwitchDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.squareup.otto.Subscribe;

import org.json.JSONException;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

import static com.example.markwen.easycourse.EasyCourse.bus;

public class ChatRoom extends AppCompatActivity {

    private static final String TAG = "ChatRoom";
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 3;
    private static final int PERMISSION_DENIED = -1;
    private static final int CHOOSE_IMAGE_INTENT = 4;
    private static final int TAKE_IMAGE_INTENT = 5;

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

    Drawer roomDetailDrawer;

    ChatRecyclerViewAdapter chatRecyclerViewAdapter;
    RealmResults<Message> messages;

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

        roomDetailDrawer = new DrawerBuilder()
                .withActivity(this)
                .withHeaderHeight(DimenHolder.fromDp(192))
                .withHeader(R.layout.room_detail_drawer_header)
                .withSelectedItem(-1)
                .withDrawerGravity(Gravity.END)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.classmates).withIcon(R.drawable.ic_group_black_24px).withIdentifier(1).withSelectable(false),
                        new PrimaryDrawerItem().withName(R.string.subgroups).withIcon(R.drawable.ic_chatboxes).withIdentifier(1).withSelectable(false),
                        new DividerDrawerItem(),
                        new SecondarySwitchDrawerItem().withName(R.string.silent).withChecked(true).withOnCheckedChangeListener(silentRoom).withSelectable(false),
                        new SecondaryDrawerItem().withName(R.string.share_room).withSelectable(false),
                        new SecondaryDrawerItem().withName(R.string.quit_room).withSelectable(false)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        switch (position){
                            case 1 :
                                //TODO: Add intent to Classmates
                                break;
                            case 2 :
                                //TODO: Add intent to Subgroups
                                break;
                            case 5:
                                //TODO: Add intent to Share Room
                                break;
                            case 6:
                                try {
                                    socketIO.quitRoom(currentRoom.getId());
                                    socketIO.syncUser();
                                    return false;
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;
                        }
                        return true;
                    }
                })
                .build();

        handleIntent();

        setupChatRecyclerView();

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

        //Setup snackbar for disconnect
        disconnectSnackbar = Snackbar.make(findViewById(R.id.relativeLayoutChatRoom), "Disconnected!", Snackbar.LENGTH_INDEFINITE);

        bus.register(this);
    }

    OnCheckedChangeListener silentRoom = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(IDrawerItem drawerItem, CompoundButton buttonView, boolean isChecked) {
            //TODO: Add silentRoom code
        }
    };

    public void openCourseDetail(View v){
        Toast.makeText(getApplicationContext(), "openCourseDetail", Toast.LENGTH_LONG);
    }

    private void showImageDialog() {
        new MaterialDialog.Builder(this)
                .items(R.array.addImageDialog)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        handleAddImage(which, view);
                    }
                })
                .show();
    }

    private void handleAddImage(int which, View view) {
        switch (which) {
            case 0:  // choose image
                int permissionCheck1 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
                if (permissionCheck1 == PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
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
        startActivityForResult(chooseImageIntent , CHOOSE_IMAGE_INTENT);
    }

    private void takeImage() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
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

    private void handleIntent() {
        Intent intent = getIntent();
        String roomId = intent.getStringExtra("roomId");
        this.currentRoom = Room.getRoomById(this, realm, roomId);
        if (this.currentRoom == null) {
            Log.d(TAG, "current room not found!");
            Toast.makeText(this, "Current room not found!", Toast.LENGTH_SHORT).show();
            this.finish();
        }
        toolbarTitleTextView.setText(currentRoom.getRoomName());
        toolbarSubtitleTextView.setText(currentRoom.getCourseName());


        View headView = roomDetailDrawer.getHeader();

        TextView headerCourseTitle = ((TextView) headView.findViewById(R.id.headerCourseTitle));
        headerCourseTitle.setText(currentRoom.getRoomName());
        headerCourseTitle.setPaintFlags(headerCourseTitle.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
        ((TextView) headView.findViewById(R.id.headerCourseSubtitle)).setText(currentRoom.getCourseName());

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case CHOOSE_IMAGE_INTENT:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = data.getData();
                    try {
                        Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                        if(imageBitmap == null) return;
                    }catch (IOException e) {
                        Log.e(TAG, "onActivityResult: ", e);
                    }
                    Log.d(TAG, "onActivityResult: " + selectedImage.toString());
                }
                break;

            case TAKE_IMAGE_INTENT:
                if (resultCode == RESULT_OK) {
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    if(imageBitmap == null) return;
                    Log.d(TAG, "onActivityResult: " + imageBitmap.toString());
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
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
        if (disconnectSnackbar != null) {
            disconnectSnackbar.dismiss();
        }
    }
}
