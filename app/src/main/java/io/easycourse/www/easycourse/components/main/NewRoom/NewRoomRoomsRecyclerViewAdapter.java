package io.easycourse.www.easycourse.components.main.NewRoom;

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

import io.easycourse.www.easycourse.activities.ChatRoomActivity;
import io.easycourse.www.easycourse.models.main.Course;
import io.easycourse.www.easycourse.utils.JSONUtils;
import io.easycourse.www.easycourse.utils.ListsUtils;
import io.easycourse.www.easycourse.models.main.Message;
import io.easycourse.www.easycourse.models.main.Room;
import io.easycourse.www.easycourse.models.main.User;
import io.easycourse.www.easycourse.utils.SocketIO;
import com.hanks.library.AnimateCheckBox;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.socket.client.Ack;

import static io.easycourse.www.easycourse.utils.ListsUtils.isRoomJoined;
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
        return new ExistedRoomViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(io.easycourse.www.easycourse.R.layout.activity_new_room_room_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(final ExistedRoomViewHolder roomViewHolder, int i) {
        final Room room = roomsList.get(i);
        roomViewHolder.roomNameTextView.setText(room.getRoomName());
        roomViewHolder.roomCourseTextView.setText(room.getCourseName());
        roomViewHolder.roomCheckbox.setClickable(false);

        if (ListsUtils.isRoomJoined(joinedRooms, room)) {
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
                                        final String id = (String) JSONUtils.checkIfJsonExists(temp, "_id", null);
                                        final String roomName = (String) JSONUtils.checkIfJsonExists(temp, "name", null);
                                        final String courseID = selectedCourseId;
                                        final String courseName = selectedCourseName;
                                        final String universityID = (String) JSONUtils.checkIfJsonExists(temp, "university", null);
                                        final String language = (String) JSONUtils.checkIfJsonExists(temp, "language", null);
                                        final boolean isPublic = (boolean) JSONUtils.checkIfJsonExists(temp, "isPublic", true);
                                        final int memberCounts = Integer.parseInt((String) JSONUtils.checkIfJsonExists(temp, "memberCounts", "1"));
                                        final String memberCountsDesc = (String) JSONUtils.checkIfJsonExists(temp, "memberCountsDescription", null);
                                        final boolean isSystem = (boolean) JSONUtils.checkIfJsonExists(temp, "isSystem", true);

                                        // Get founder
                                        String founderId = null, founderName = null, founderAvatarUrl = null;
                                        if (temp.has("founder")) {
                                            JSONObject founderJSON = temp.getJSONObject("founder");
                                            founderId = (String) JSONUtils.checkIfJsonExists(founderJSON, "_id", null);
                                            founderName = (String) JSONUtils.checkIfJsonExists(founderJSON, "displayName", null);
                                            founderAvatarUrl = (String) JSONUtils.checkIfJsonExists(founderJSON, "avatarUrl", null);
                                        }

                                        // Save user to Realm
                                        joinedRoom[0] = new Room(
                                                id,
                                                roomName,
                                                new RealmList<Message>(),
                                                courseID,
                                                courseName,
                                                universityID,
                                                new RealmList<User>(),
                                                memberCounts,
                                                memberCountsDesc,
                                                new User(founderId, founderName, null, founderAvatarUrl, null, universityID),
                                                language,
                                                isPublic,
                                                isSystem);
                                        updateRoomInSocket(joinedRoom[0]);

                                        // Get messages
                                        socketIO.syncUser();

                                        // Go to that room
                                        goToChatRoom(joinedRoom[0]);
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
        @BindView(io.easycourse.www.easycourse.R.id.cardViewChatRoom)
        CardView roomCardView;
        @BindView(io.easycourse.www.easycourse.R.id.textViewChatRoomName)
        TextView roomNameTextView;
        @BindView(io.easycourse.www.easycourse.R.id.textViewChatRoomCourse)
        TextView roomCourseTextView;
        @BindView(io.easycourse.www.easycourse.R.id.new_room_room_check_box)
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

    private void updateRoomInSocket(final Room room){
        Thread thread = new Thread(){
            @Override
            public void run() {
                synchronized (this) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Room.updateRoomToRealm(room, realm);
                            joinedRooms.add(room);
                        }
                    });
                }
            }
        };
        thread.start();
    }
}
