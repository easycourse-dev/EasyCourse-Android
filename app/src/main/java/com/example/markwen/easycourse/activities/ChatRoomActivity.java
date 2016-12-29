package com.example.markwen.easycourse.activities;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
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

import static com.example.markwen.easycourse.EasyCourse.bus;

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
    Drawer privateDetailDrawer;

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

        ChatRoomFragment chatRoomFragment = ChatRoomFragment.newInstance(currentRoom, currentUser);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.activity_chat_room_content, chatRoomFragment)
                .commit();

        //Setup snackbar for disconnect
        disconnectSnackbar = Snackbar.make(findViewById(R.id.activity_chat_room), "Disconnected!", Snackbar.LENGTH_INDEFINITE);

        bus.register(this);
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
        if (!currentRoom.isToUser()) {
            builder.addDrawerItems(
                    new PrimaryDrawerItem().withName(R.string.classmates).withIcon(R.drawable.ic_group_black_24px).withIdentifier(1).withSelectable(false),
                    new PrimaryDrawerItem().withName(R.string.subgroups).withIcon(R.drawable.ic_chatboxes).withIdentifier(1).withSelectable(false),
                    new DividerDrawerItem(),
                    new SecondarySwitchDrawerItem().withName(R.string.silent).
                            withChecked(currentUser.getSilentRooms().contains(currentRoom))
                            .withOnCheckedChangeListener(new OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(IDrawerItem drawerItem, CompoundButton buttonView, boolean isChecked) {
                                    silenceRoom(isChecked);
                                }
                            }).withSelectable(false),
                    new SecondaryDrawerItem().withName(R.string.share_room).withSelectable(false),
                    new SecondaryDrawerItem().withName(R.string.quit_room).withSelectable(false));

            builder.withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                @Override
                public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                    switch (position) {
                        case 1:
                            //TODO: Add intent to Classmates
                            break;
                        case 2:
                            //TODO: Add intent to Subgroups
                            break;
                        case 5:
                            //TODO: Add intent to Share Room
                            break;
                        case 6:
                            try {
                                socketIO.quitRoom(currentRoom.getId());
                                socketIO.syncUser();
                                return true;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                    return false;
                }
            });
        } else {
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
                            break;
                        case 2:
                            //TODO: Add intent to shared groups
                            break;
                        case 5:
                            //TODO: Add intent to Share Room
                            break;
                        case 6:
                            //TODO: Add dialog to report user
                            showReportUserDialog();
                            break;
                        case 7:
                            //TODO: Dialog to block user and socket call
                            showBlockUserDialog();
                            break;
                        case 8:
//                            try {
//                                socketIO.quitRoom(currentRoom.getId());
//                                socketIO.syncUser();
//                                return true;
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
                            break;
                    }
                    return false;
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

    }

    private void silenceRoom(final boolean isChecked) {
        try {
            APIFunctions.setSilentRoom(getApplicationContext(), currentRoom.getId(), isChecked, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    if (isChecked) {
                        Log.e(TAG, "Room silented");
                    } else {
                        Log.e(TAG, "Room un-silented");
                    }
                    socketIO.syncUser();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                    Log.e(TAG, "onFailure: silentRoomOnCheckedListener", t);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
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
                .titleColor(getResources().getColor(R.color.colorAccent))
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
                .negativeColor(getResources().getColor(R.color.colorLogout))
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
                .titleColor(getResources().getColor(R.color.colorAccent))
                .positiveText("Block")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        blockUser(otherUser);
                    }
                })
                .negativeText("Cancel")
                .negativeColor(getResources().getColor(R.color.colorLogout))
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.cancel();
                    }
                })
                .build();
        dialog.show();
    }

    private void blockUser(User otherUser) {
        //TODO: socket call to block user
    }

    //Only call if isToUser
    private User getOtherUserIfPrivate() {
        if (!currentRoom.isToUser()) return null;
        List<User> users = currentRoom.getMemberList();
        for (User user : users) {
            if (!user.equals(currentUser)) return user;
        }
        return null;
    }

    public void openCourseDetail(View v) {
        Toast.makeText(getApplicationContext(), "openCourseDetail", Toast.LENGTH_LONG);
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
        disconnectSnackbar.show();
    }

    @Subscribe
    public void reconnectEvent(Event.ReconnectEvent event) {
        if (disconnectSnackbar != null) {
            disconnectSnackbar.dismiss();
        }
    }
}
