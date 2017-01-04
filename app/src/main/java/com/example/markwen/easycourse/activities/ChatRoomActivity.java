package com.example.markwen.easycourse.activities;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.markwen.easycourse.EasyCourse;
import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.fragments.main.ChatRoomFragment;
import com.example.markwen.easycourse.fragments.main.RoomUserListFragment;
import com.example.markwen.easycourse.models.main.Room;
import com.example.markwen.easycourse.models.main.User;
import com.example.markwen.easycourse.utils.APIFunctions;
import com.example.markwen.easycourse.utils.SocketIO;
import com.example.markwen.easycourse.utils.eventbus.Event;
import com.loopj.android.http.JsonHttpResponseHandler;
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
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;
import io.realm.Realm;
import io.socket.client.Ack;

import static com.example.markwen.easycourse.EasyCourse.bus;
import static com.example.markwen.easycourse.utils.ListsUtils.isRoomJoined;

public class ChatRoomActivity extends AppCompatActivity {

    private static final String TAG = "ChatRoomActivity";

    private Realm realm;
    private SocketIO socketIO;
    private Snackbar disconnectSnackbar;


    private Room currentRoom;
    private User currentUser;

    @BindView(R.id.toolbarChatRoom)
    Toolbar toolbar;
    @BindView(R.id.toolbarTitleChatRoom)
    TextView toolbarTitleTextView;
    @BindView(R.id.toolbarSubtitleChatRoom)
    TextView toolbarSubtitleTextView;


    Drawer roomDetailDrawer;

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

        setupDrawer();

        gotoChatRoomFragment(currentRoom, currentUser);

        //Setup snackbar for disconnect
        disconnectSnackbar = Snackbar.make(findViewById(R.id.activity_chat_room), "Disconnected!", Snackbar.LENGTH_INDEFINITE);
        bus.register(this);
    }

    public void gotoChatRoomFragment(Room currentRoom, User currentUser) {
        ChatRoomFragment chatRoomFragment = ChatRoomFragment.newInstance(currentRoom, currentUser);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_chat_room_content, chatRoomFragment)
                .commit();
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24px);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatRoomActivity.this.finish();
            }
        });
    }

    public void gotoRoomUserListFragment() {
        RoomUserListFragment roomUserListFragment = RoomUserListFragment.newInstance(currentRoom, currentUser);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_chat_room_content, roomUserListFragment)
                .commit();
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24px);
    }


    private void handleIntent() {
        Intent intent = getIntent();
        String roomId = intent.getStringExtra("roomId");
        this.currentRoom = Room.getRoomById(realm, roomId);
        if (this.currentRoom == null) {
            Log.d(TAG, "current room not found!");
            Toast.makeText(this, "Current room not found!", Toast.LENGTH_SHORT).show();
            this.finish();
        }
        toolbarTitleTextView.setText(currentRoom.getRoomName());
        toolbarSubtitleTextView.setText(currentRoom.getCourseName());
    }

    private void setupDrawer() {
        DrawerBuilder builder = new DrawerBuilder(this);
        builder.withHeaderHeight(DimenHolder.fromDp(192));
        builder.withHeader(R.layout.room_detail_drawer_header);
        builder.withSelectedItem(-1);
        builder.withDrawerGravity(Gravity.END);
        if (!currentRoom.isToUser()) { //If group chat
            builder.addDrawerItems(
                    new PrimaryDrawerItem().withName(R.string.classmates).withIcon(R.drawable.ic_group_black_24px).withIdentifier(1).withSelectable(false),
                    new DividerDrawerItem(),
                    new SecondarySwitchDrawerItem().withName(R.string.silent)
                            .withChecked(isRoomJoined(currentUser.getSilentRooms(), currentRoom))
                            .withOnCheckedChangeListener(new OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(IDrawerItem drawerItem, CompoundButton buttonView, boolean isChecked) {
                                    silenceRoom(isChecked);
                                }
                            }).withSelectable(false),
                    new SecondaryDrawerItem().withName(R.string.share_room).withSelectable(false),
                    new SecondaryDrawerItem().withName(R.string.quit_room)
            );
            builder.withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                @Override
                public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                    switch (position) {
                        case 1:
                            //TODO: Add intent to classmates
                            gotoRoomUserListFragment();
                            return false;
                        case 4:
                            //TODO: Add intent to share room
                            return false;
                        case 5:
                            try {
                                socketIO.quitRoom(currentRoom.getId(), new Ack() {
                                    @Override
                                    public void call(Object... args) {
                                        JSONObject obj = (JSONObject) args[0];
                                        Log.e(TAG, obj.toString());

                                        if (obj.has("error")) {
                                            Log.e(TAG, obj.toString());
                                        } else {

                                            try {
                                                boolean success = obj.getBoolean("success");
                                                if (success) {
                                                    deleteRoomInSocket(currentRoom);
                                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                                }
                                            } catch (JSONException e) {
                                                Log.e(TAG, e.toString());
                                            }
                                        }
                                    }
                                });
                                socketIO.syncUser();
                                return false;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                    return true;
                }
            });
        }else { //If private chat
            builder.addDrawerItems(
                    new PrimaryDrawerItem().withName(R.string.classmates).withIcon(R.drawable.ic_group_black_24px),
                    new PrimaryDrawerItem().withName(R.string.sharedgroups).withIcon(R.drawable.ic_chatboxes),
                    new DividerDrawerItem(),
                    new SecondarySwitchDrawerItem().withName(R.string.silent).
                            withChecked(currentUser.getSilentRooms().contains(currentRoom))
                            .withOnCheckedChangeListener(new OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(IDrawerItem drawerItem, CompoundButton buttonView, boolean isChecked) {
                                    silenceRoom(isChecked);
                                }
                            }).withSelectable(false),
                    new SecondaryDrawerItem().withName(R.string.share_room),
                    new SecondaryDrawerItem().withName(R.string.block_user),
                    new SecondaryDrawerItem().withName(R.string.report_user),
                    new SecondaryDrawerItem().withName(R.string.quit_room));

            builder.withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                @Override
                public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                    switch (position) {
                        case 1:
                            //TODO: Add intent to classmates of all shared classes
                            gotoRoomUserListFragment();
                            return false;
                        case 5:
                            //TODO: Add intent to Share Room
                            return false;
                        case 6:
                            showReportUserDialog();
                            return false;
                        case 7:
                            showBlockUserDialog();
                            return false;
                        case 8:
                            quitRoom();
                            socketIO.syncUser();
                            return false;
                    }
                    return true;
                }
            });
        }


        roomDetailDrawer = builder.build();

        //TODO: picture for group and user
        View headView = roomDetailDrawer.getHeader();
        TextView headerCourseTitle = (TextView) headView.findViewById(R.id.headerCourseTitle);
        TextView headerCourseSubtitle = (TextView) headView.findViewById(R.id.headerCourseSubtitle);

        headerCourseTitle.setText(currentRoom.getRoomName());
        headerCourseTitle.setPaintFlags(headerCourseTitle.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        if (currentRoom.getCourseName() != null)
            headerCourseSubtitle.setText(currentRoom.getCourseName());


        headerCourseTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!currentRoom.getCourseName().equals("Private Room")) {
                    Intent i = new Intent(getApplication(), CourseDetailsActivity.class);
                    i.putExtra("courseId", currentRoom.getCourseID());
                    i.putExtra("isJoined", true);
                    startActivity(i);
                }
            }
        });

    }

    private void quitRoom() {
        try {
            socketIO.quitRoom(currentRoom.getId(), new Ack() {
                @Override
                public void call(Object... args) {
                    JSONObject obj = (JSONObject) args[0];
                    Log.e(TAG, obj.toString());

                    if (obj.has("error")) {
                        Log.e(TAG, obj.toString());
                    } else {

                        try {
                            boolean success = obj.getBoolean("success");
                            if (success) {
                                socketIO.syncUser();
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                }
            });
            socketIO.syncUser();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void silenceRoom(final boolean isChecked) {
        try {
            socketIO.silentRoom(currentRoom.getId(), isChecked, new Ack() {
                @Override
                public void call(Object... args) {
                    JSONObject obj = (JSONObject) args[0];
                    if (obj.has("error")) {
                        Log.e(TAG, "onFailure: silentRoomOnCheckedListener " + obj.toString());
                    } else {
                        if (isChecked) {
                            Log.e(TAG, "Room silented");
                        } else {
                            Log.e(TAG, "Room un-silented");
                        }
                        socketIO.syncUser();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showReportUserDialog() {
        final User otherUser = getOtherUserIfPrivate();
        if (otherUser == null) {
            Toast.makeText(this, "Error reporting user! Try again later", Toast.LENGTH_SHORT).show();
            return;
        }
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("Report " + otherUser.getUsername() + "?")
                .titleColor(ContextCompat.getColor(this, R.color.colorAccent))
                .customView(R.layout.dialog_report_user, true)
                .positiveText("Report")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        EditText reason = (EditText) dialog.findViewById(R.id.edit_text_report_user);
                        reportUser(otherUser, reason.getText().toString());
                    }
                })
                .negativeText("Cancel")
                .negativeColor(ContextCompat.getColor(this, R.color.colorLogout))
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.cancel();
                    }
                })
                .build();
        dialog.show();
    }

    private void reportUser(final User otherUser, String reason) {
        try {
            APIFunctions.reportUser(this, getOtherUserIfPrivate().getId(), reason, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Toast.makeText(ChatRoomActivity.this, otherUser.getUsername() + " was reported.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                    Toast.makeText(ChatRoomActivity.this, "Error reporting user! Try again later", Toast.LENGTH_SHORT).show();
                    return;
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "reportUser: ", e);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "reportUser: ", e);
        }
    }

    private void showBlockUserDialog() {
        final User otherUser = getOtherUserIfPrivate();
        if (otherUser == null) {
            Toast.makeText(this, "Error blocking user! Try again later", Toast.LENGTH_SHORT).show();
            return;
        }
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("Block " + otherUser.getUsername() + "?")
                .titleColor(ContextCompat.getColor(this, R.color.colorAccent))
                .positiveText("Block")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        blockUser(otherUser);
                    }
                })
                .negativeText("Cancel")
                .negativeColor(ContextCompat.getColor(this, R.color.colorLogout))
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.cancel();
                    }
                })
                .build();
        dialog.show();
    }

    private void blockUser(final User otherUser) {
        try {
            socketIO.blockUser(otherUser.getId(), new Ack() {
                @Override
                public void call(Object... args) {
                    JSONObject obj = (JSONObject) args[0];
                    if (obj.has("error")) {
                        Log.e(TAG, obj.toString());
                    } else {
                        try {
                            boolean successBool = obj.getBoolean("success");
                            if (successBool) {
                                Toast.makeText(ChatRoomActivity.this, otherUser.getUsername() + " was blocked!", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, e.toString());
                            Toast.makeText(ChatRoomActivity.this, "Blocking " + otherUser.getUsername() + "failed!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "blockUser: ", e);
            Toast.makeText(ChatRoomActivity.this, "Blocking " + otherUser.getUsername() + "failed!", Toast.LENGTH_SHORT).show();
        }
    }

    //Only call if isToUser
    @Nullable
    private User getOtherUserIfPrivate() {
        if (!currentRoom.isToUser()) return null;
        List<User> users = currentRoom.getMemberList();
        for (User user : users) {
            if (!user.equals(currentUser)) return user;
        }
        return null;
    }

    public void openCourseDetail(View v) {
        Toast.makeText(getApplicationContext(), "openCourseDetail", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
        if (disconnectSnackbar != null) {
            disconnectSnackbar.show();
        } else {
            disconnectSnackbar = Snackbar.make(findViewById(R.id.activity_chat_room), "Disconnected!", Snackbar.LENGTH_INDEFINITE);
            disconnectSnackbar.show();
        }
    }

    @Subscribe
    public void reconnectEvent(Event.ReconnectEvent event) {
        if (disconnectSnackbar != null) {
            disconnectSnackbar.dismiss();
        }
    }

    public Toolbar getToolbar() {
        return toolbar;
    }
    public void deleteRoomInSocket(final Room room){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Room.deleteRoomFromRealm(room, realm);
                realm.close();
            }
        });
    }
}
