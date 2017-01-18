package com.example.markwen.easycourse.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.markwen.easycourse.EasyCourse;
import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.components.RoomListViewAdapter;
import com.example.markwen.easycourse.models.main.Message;
import com.example.markwen.easycourse.models.main.Room;
import com.example.markwen.easycourse.models.main.User;
import com.example.markwen.easycourse.utils.SocketIO;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;
import io.socket.client.Ack;

import static com.example.markwen.easycourse.utils.JSONUtils.checkIfJsonExists;

/**
 * Created by nisarg on 5/1/17.
 */

public class ShareRoomActivity extends AppCompatActivity {

    private static final String TAG = "ShareRoomActivity";

    @BindView(R.id.rooms)
    ListView roomsList;
    @BindView(R.id.toolbarShareRoom)
    Toolbar toolbar;

    private Realm realm;
    private SocketIO socketIO;

    String roomShareId;

    RealmResults<Room> rooms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_room);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        Intent i = getIntent();
        roomShareId = i.getStringExtra("roomID");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        getSupportActionBar().setTitle("Share Room");

        realm = Realm.getDefaultInstance();
        socketIO = EasyCourse.getAppInstance().getSocketIO();

        rooms = realm.where(Room.class).findAll();

        RoomListViewAdapter adapter = new RoomListViewAdapter(getApplicationContext(), rooms);
        roomsList.setAdapter(adapter);
        roomsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Room room = rooms.get(position);

                sendSharedRoomMessage(room);

                finish();
            }

        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendSharedRoomMessage(Room room) {
        User currentUser = User.getCurrentUser(this, realm);
        final String localMessageId = UUID.randomUUID().toString();
        Room currentRoom = realm.where(Room.class).equalTo("id", roomShareId).findFirst();
        Message message = new Message(localMessageId, null, currentUser, null, null, null, room, false, 0, 0, roomShareId, currentRoom.isToUser(), new Date());
        message.updateMessageToRealm();

        int selector;
        if (currentRoom.isToUser())
            selector = SocketIO.ROOM_TO_USER;
        else
            selector = SocketIO.ROOM_TO_ROOM;

        socketIO.sendMessage(message, selector, new Ack() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject obj = (JSONObject) args[0];
                    JSONObject message = (JSONObject) checkIfJsonExists(obj, "msg", null);

                    JSONObject sender = (JSONObject) checkIfJsonExists(message, "sender", null);
                    String senderId = (String) checkIfJsonExists(sender, "_id", null);
                    String senderName = (String) checkIfJsonExists(sender, "displayName", null);
                    String senderImageUrl = (String) checkIfJsonExists(sender, "avatarUrl", null);

                    String id = (String) checkIfJsonExists(message, "_id", null);
                    String remoteId = (String) checkIfJsonExists(message, "id", null);
                    String text = (String) checkIfJsonExists(message, "text", null);
                    String imageUrl = (String) checkIfJsonExists(message, "imageUrl", null);
                    byte[] imageData = (byte[]) checkIfJsonExists(message, "imageData", null);
                    boolean successSent = (boolean) checkIfJsonExists(message, "successSent", false);
                    String toRoom = (String) checkIfJsonExists(message, "toRoom", null);
                    String toUser = (String) checkIfJsonExists(message, "toUser", null);
                    float imageWidth = Float.parseFloat((String) checkIfJsonExists(message, "imageWidth", "0.0"));
                    float imageHeight = Float.parseFloat((String) checkIfJsonExists(message, "imageHeight", "0.0"));
                    Room sharedRoom = null;
                    if(checkIfJsonExists(obj, "sharedRoom", null) != null) {
                        JSONObject sharedRoomJSON = obj.getJSONObject("sharedRoom");
                        Log.e(TAG, sharedRoomJSON.toString());
                        sharedRoom = new Room(sharedRoomJSON.getString("id"), sharedRoomJSON.getString("name"), sharedRoomJSON.getString("course"), sharedRoomJSON.getString("memberCountsDescription"));
                    }




                    String dateCreatedAt = (String) checkIfJsonExists(message, "createdAt", null);
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                    Date date = null;
                    try {
                        date = formatter.parse(dateCreatedAt);

                    } catch (ParseException e) {
                        Log.e(TAG, "saveMessageToRealm: parseException", e);
                    }


                    Realm tempRealm = Realm.getDefaultInstance();
                    tempRealm.beginTransaction();
                    User senderUser = tempRealm.where(User.class).equalTo("id", senderId).findFirst();
                    if (senderUser == null) {
                        senderUser = tempRealm.createObject(User.class, senderId);
                    }
                    senderUser.setUsername(senderName);
                    senderUser.setProfilePictureUrl(senderImageUrl);


                    Message localMessage = tempRealm.where(Message.class).equalTo("id", localMessageId).findFirst();
                    if (localMessage == null) {
                        localMessage = tempRealm.createObject(Message.class, localMessageId);
                    }
                    localMessage.setRemoteId(remoteId);
                    localMessage.setText(text);
                    localMessage.setImageUrl(imageUrl);
                    localMessage.setImageData(imageData);
                    localMessage.setSuccessSent(true);

                    if (toRoom != null) {
                        localMessage.setToRoom(toRoom);
                        localMessage.setToUser(false);
                    } else {
                        localMessage.setToRoom(toUser);
                        localMessage.setToUser(true);
                    }

                    localMessage.setSharedRoom(sharedRoom);

                    localMessage.setImageWidth(imageWidth);
                    localMessage.setImageHeight(imageHeight);
                    localMessage.setCreatedAt(date);
                    tempRealm.commitTransaction();
                    tempRealm.close();
                } catch (JSONException e) {
                    Log.e(TAG, "call: ", e);
                }
            }
        });
    }


}
