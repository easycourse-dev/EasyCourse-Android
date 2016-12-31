package com.example.markwen.easycourse.activities;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

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
                        new SecondarySwitchDrawerItem().withName(R.string.silent).
                                withChecked(currentUser.getSilentRooms().contains(currentRoom))
                                .withOnCheckedChangeListener(new OnCheckedChangeListener() {
                                    @Override
                                    public void onCheckedChanged(IDrawerItem drawerItem, CompoundButton buttonView, boolean isChecked) {
                                        silenceRoom(isChecked);
                                    }
                                }).withSelectable(false),
                        new SecondaryDrawerItem().withName(R.string.share_room).withSelectable(false),
                        new SecondaryDrawerItem().withName(R.string.quit_room).withSelectable(false)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
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
//                                try {
//                                    socketIO.quitRoom(currentRoom.getId());
//                                    socketIO.syncUser();
//                                    return false;
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
                                break;
                        }
                        return true;
                    }
                })
                .build();

        View headView = roomDetailDrawer.getHeader();

        TextView headerCourseTitle = ((TextView) headView.findViewById(R.id.headerCourseTitle));
        headerCourseTitle.setText(currentRoom.getRoomName());
        headerCourseTitle.setPaintFlags(headerCourseTitle.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        ((TextView) headView.findViewById(R.id.headerCourseSubtitle)).setText(currentRoom.getCourseName());
    }

    private void silenceRoom(final boolean isChecked) {
        try {
            APIFunctions.setSilentRoom(getApplicationContext(), currentRoom.getId(), isChecked, new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    if(isChecked){
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

    public void openCourseDetail(View v){
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
