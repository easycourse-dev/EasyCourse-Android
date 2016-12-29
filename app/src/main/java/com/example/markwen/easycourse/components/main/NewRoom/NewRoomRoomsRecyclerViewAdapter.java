package com.example.markwen.easycourse.components.main.NewRoom;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.activities.ChatRoomActivity;
import com.example.markwen.easycourse.models.main.Course;
import com.example.markwen.easycourse.models.main.Message;
import com.example.markwen.easycourse.models.main.Room;
import com.example.markwen.easycourse.models.main.User;
import com.example.markwen.easycourse.utils.SocketIO;
import com.hanks.library.AnimateCheckBox;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.socket.client.Ack;

import static com.example.markwen.easycourse.utils.JSONUtils.checkIfJsonExists;
import static com.example.markwen.easycourse.utils.ListsUtils.isRoomJoined;
import static com.example.markwen.easycourse.utils.ListsUtils.isUserInList;
import static com.google.android.gms.internal.zzs.TAG;

/**
 * Created by markw on 12/19/2016.
 */

public class NewRoomRoomsRecyclerViewAdapter extends RecyclerView.Adapter<NewRoomRoomsRecyclerViewAdapter.ExistedRoomViewHolder> {

    private ArrayList<Room> roomsList;
    private ArrayList<Room> joinedRooms = new ArrayList<>();
    private String selectedCourseId;
    private String selectedCourseName;
    private Context context;
    private SocketIO socketIO;
    private Realm realm;
    private AppCompatActivity activity;

    public NewRoomRoomsRecyclerViewAdapter(AppCompatActivity activity, Context context, ArrayList<Room> rooms, SocketIO socketIO) {
        super();
        this.activity = activity;
        this.context = context;
        this.roomsList = rooms;
        this.socketIO = socketIO;
        realm = Realm.getDefaultInstance();
        RealmResults<Room> joinedRoomsResults = realm.where(Room.class).findAll();
        for (int i = 0; i < joinedRoomsResults.size(); i++) {
            joinedRooms.add(joinedRoomsResults.get(i));
        }
    }

    @Override
    public ExistedRoomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new ExistedRoomViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_new_room_room_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(final ExistedRoomViewHolder roomViewHolder, int i) {
        final Room room = roomsList.get(i);
        roomViewHolder.roomNameTextView.setText(room.getRoomName());
        roomViewHolder.roomCourseTextView.setText(room.getCourseName());
        roomViewHolder.roomCheckbox.setClickable(false);

        if (isRoomJoined(joinedRooms, room)) {
            roomViewHolder.roomCheckbox.setChecked(true);
        } else {
            roomViewHolder.roomCheckbox.setChecked(false);
        }

        roomViewHolder.roomCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (roomViewHolder.roomCheckbox.isChecked()) {
                        goToChatRoom(room);
                    } else {
                        socketIO.joinRoom(room.getId(), new Ack() {
                            @Override
                            public void call(Object... args) {
                                final Room[] joinedRoom = {new Room()};
                                JSONObject obj = (JSONObject) args[0];
                                if (!obj.has("error")) {
                                    try {
                                        // Get room
                                        JSONObject temp = obj.getJSONObject("room");
                                        final String id = (String) checkIfJsonExists(temp, "_id", null);
                                        final String roomName = (String) checkIfJsonExists(temp, "name", null);
                                        final String courseID = selectedCourseId;
                                        final String courseName = selectedCourseName;
                                        final String universityID = (String) checkIfJsonExists(temp, "university", null);
                                        final boolean isPublic = (boolean) checkIfJsonExists(temp, "isPublic", true);
                                        final int memberCounts = Integer.parseInt((String) checkIfJsonExists(temp, "memberCounts", "1"));
                                        final String memberCountsDesc = (String) checkIfJsonExists(temp, "memberCountsDescription", null);
                                        final boolean isSystem = (boolean) checkIfJsonExists(temp, "isSystem", true);

                                        // Get founder
                                        JSONObject founderJSON = temp.getJSONObject("founder");
                                        final String founderId = (String) checkIfJsonExists(founderJSON, "_id", null);
                                        final String founderName = (String) checkIfJsonExists(founderJSON, "displayName", null);
                                        final String founderAvatarUrl = (String) checkIfJsonExists(founderJSON, "avatarUrl", null);

                                        // Get users
                                        final RealmList<User> userList = new RealmList<>();
                                        socketIO.getRoomMembers(id, new Ack() {
                                            @Override
                                            public void call(Object... args) {
                                                JSONObject obj = (JSONObject) args[0];
                                                try {
                                                    JSONArray userArray = obj.getJSONArray("users");
                                                    JSONObject tempUserJSON, tempUserEventJSON;
                                                    User tempUser;
                                                    for (int i = 0; i < userArray.length(); i++) {
                                                        tempUserEventJSON = (JSONObject) userArray.get(i);
                                                        tempUserJSON = tempUserEventJSON.getJSONObject("user");
                                                        tempUser = new User(
                                                                (String) checkIfJsonExists(tempUserJSON, "_id", null),
                                                                (String) checkIfJsonExists(tempUserJSON, "displayName", null),
                                                                null,
                                                                (String) checkIfJsonExists(tempUserJSON, "avatarUrl", null),
                                                                null,
                                                                null
                                                        );
                                                        if (!isUserInList(userList, tempUser)) {
                                                            userList.add(tempUser);
                                                        }
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }

                                                // Save user to Realm
                                                joinedRoom[0] = new Room(
                                                        id,
                                                        roomName,
                                                        new RealmList<Message>(),
                                                        courseID,
                                                        courseName,
                                                        universityID,
                                                        userList,
                                                        memberCounts,
                                                        memberCountsDesc,
                                                        new User(founderId, founderName, null, founderAvatarUrl, null, universityID),
                                                        null,
                                                        isPublic,
                                                        isSystem);
                                                updateRoomInSocket(joinedRoom[0]);

                                                // Get messages
                                                socketIO.syncUser();

                                                // Go to that room
                                                goToChatRoom(joinedRoom[0]);
                                            }
                                        });
                                    } catch (JSONException e) {
                                        Log.e(TAG, e.toString());
                                    }

                                } else {
                                    try {
                                        JSONObject temp = obj.getJSONObject("error");
                                        Log.e("joinRoom", temp.toString());
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return roomsList.size();
    }

    class ExistedRoomViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.cardViewChatRoom)
        CardView roomCardView;
        @BindView(R.id.textViewChatRoomName)
        TextView roomNameTextView;
        @BindView(R.id.textViewChatRoomCourse)
        TextView roomCourseTextView;
        @BindView(R.id.new_room_room_check_box)
        AnimateCheckBox roomCheckbox;

        ExistedRoomViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private void goToChatRoom(Room room) {
        Intent chatActivityIntent = new Intent(context, ChatRoomActivity.class);
        chatActivityIntent.putExtra("roomId", room.getId());
        context.startActivity(chatActivityIntent);
    }

    public void setCurrentCourse(Course course) {
        this.selectedCourseId = course.getId();
        this.selectedCourseName = course.getCoursename();
    }

    public void updateRoomInSocket(final Room room){
        Thread thread = new Thread(){
            @Override
            public void run() {
                synchronized (this) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Room.updateRoomToRealm(room, realm);
                        }
                    });
                }
            }
        };
        thread.start();
    }
}
