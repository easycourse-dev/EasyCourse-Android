package io.easycourse.www.easycourse.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.easycourse.www.easycourse.EasyCourse;
import io.easycourse.www.easycourse.R;
import io.easycourse.www.easycourse.components.RoomListViewAdapter;
import io.easycourse.www.easycourse.models.main.Message;
import io.easycourse.www.easycourse.models.main.Room;
import io.easycourse.www.easycourse.models.main.User;
import io.easycourse.www.easycourse.utils.JSONUtils;
import io.easycourse.www.easycourse.utils.SocketIO;
import io.realm.Realm;
import io.realm.RealmResults;
import io.socket.client.Ack;

/**
 * Created by nisarg on 5/1/17.
 */

public class ShareRoomActivity extends BaseActivity {

    private static final String TAG = "ShareRoomActivity";

    @BindView(R.id.rooms)
    ListView roomsList;
    @BindView(R.id.toolbarShareRoom)
    Toolbar toolbar;



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


        rooms = realm.where(Room.class).equalTo("isJoinIn", true).findAll();

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

    private void sendSharedRoomMessage(Room destRoom) {
        User currentUser = User.getCurrentUser(this, realm);
        final String localMessageId = UUID.randomUUID().toString();
        Room sharedRoom = realm.where(Room.class).equalTo("id", roomShareId).findFirst();
//        Message message = new Message(localMessageId, null, currentUser, null, null, null, currentRoom, false, 0, 0, roomShareId, currentRoom.isToUser(), new Date());
        Message message = new Message(localMessageId, null, currentUser, null, null, null, sharedRoom, false, 0, 0, destRoom.getId(), destRoom.isToUser(), new Date());
        message.updateMessageToRealm();

        int selector;
        if (destRoom.isToUser())
            selector = SocketIO.ROOM_TO_USER;
        else
            selector = SocketIO.ROOM_TO_ROOM;

        socketIO.sendMessage(message, selector, new Ack() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject obj = (JSONObject) args[0];
                    JSONObject message = (JSONObject) JSONUtils.checkIfJsonExists(obj, "msg", null);
                    Log.e(TAG, message.toString());

                    JSONObject sender = (JSONObject) JSONUtils.checkIfJsonExists(message, "sender", null);
                    String senderId = (String) JSONUtils.checkIfJsonExists(sender, "_id", null);
                    String senderName = (String) JSONUtils.checkIfJsonExists(sender, "displayName", null);
                    String senderImageUrl = (String) JSONUtils.checkIfJsonExists(sender, "avatarUrl", null);

                    String id = (String) JSONUtils.checkIfJsonExists(message, "_id", null);
                    String remoteId = (String) JSONUtils.checkIfJsonExists(message, "id", null);
                    String text = (String) JSONUtils.checkIfJsonExists(message, "text", null);
                    String imageUrl = (String) JSONUtils.checkIfJsonExists(message, "imageUrl", null);
                    byte[] imageData = (byte[]) JSONUtils.checkIfJsonExists(message, "imageData", null);
                    boolean successSent = (boolean) JSONUtils.checkIfJsonExists(message, "successSent", false);
                    String toRoom = (String) JSONUtils.checkIfJsonExists(message, "toRoom", null);
                    String toUser = (String) JSONUtils.checkIfJsonExists(message, "toUser", null);
                    float imageWidth = Float.parseFloat((String) JSONUtils.checkIfJsonExists(message, "imageWidth", "0.0"));
                    float imageHeight = Float.parseFloat((String) JSONUtils.checkIfJsonExists(message, "imageHeight", "0.0"));
                    String dateCreatedAt = (String) JSONUtils.checkIfJsonExists(message, "createdAt", null);
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                    formatter.setTimeZone(TimeZone.getTimeZone("GMT")); // Super weird...
                    Date date = null;
                    try {
                        date = formatter.parse(dateCreatedAt);

                    } catch (ParseException e) {
                        Log.e(TAG, "saveMessageToRealm: parseException", e);
                    }
                    Realm tempRealm = Realm.getDefaultInstance();
                    tempRealm.beginTransaction();

                    Room sharedRoom = null;
                    if(JSONUtils.checkIfJsonExists(message, "sharedRoom", null) != null) {
                        JSONObject sharedRoomJSON = message.getJSONObject("sharedRoom");
                        //Log.e(TAG, sharedRoomJSON.toString());
                       // sharedRoom = new Room(sharedRoomJSON.getString("id"), sharedRoomJSON.getString("name"), sharedRoomJSON.getString("course"), sharedRoomJSON.getString("memberCountsDescription"));
                        sharedRoom = tempRealm.where(Room.class).equalTo("id", sharedRoomJSON.getString("id")).findFirst();
                        if (sharedRoom == null) {
                            sharedRoom = tempRealm.createObject(Room.class, sharedRoomJSON.getString("id"));
                        }
                        sharedRoom.setRoomName(sharedRoomJSON.getString("name"));
                        sharedRoom.setCourseID(sharedRoomJSON.getString("course"));
                        sharedRoom.setMemberCountsDesc(sharedRoomJSON.getString("memberCountsDescription"));
                    }

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
                    localMessage.setSharedRoom(sharedRoom);
                    if (toRoom != null) {
                        localMessage.setToRoom(toRoom);
                        localMessage.setToUser(false);
                    } else {
                        localMessage.setToRoom(toUser);
                        localMessage.setToUser(true);
                    }

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
        /*socketIO.sendMessage(message, selector, new Ack() {
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
                    tempRealm.copyToRealmOrUpdate(localMessage);

                    tempRealm.commitTransaction();
                    tempRealm.close();
                } catch (JSONException e) {
                    Log.e(TAG, "call: ", e);
                }
            }
        });*/
    }


}
