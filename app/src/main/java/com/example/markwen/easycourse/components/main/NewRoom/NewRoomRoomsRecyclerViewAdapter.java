package com.example.markwen.easycourse.components.main.NewRoom;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
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
import static com.google.android.gms.internal.zzs.TAG;

/**
 * Created by markw on 12/19/2016.
 */

public class NewRoomRoomsRecyclerViewAdapter extends RecyclerView.Adapter<NewRoomRoomsRecyclerViewAdapter.ExistedRoomViewHolder> {

    private ArrayList<Room> roomsList;
    private ArrayList<Room> joinedRooms = new ArrayList<>();
    private Context context;
    private SocketIO socketIO;
    private Realm realm;

    public NewRoomRoomsRecyclerViewAdapter(@NonNull Context context, ArrayList<Room> rooms, SocketIO socketIO) {
        super();
        this.roomsList = rooms;
        this.context = context;
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
                    final Room[] joinedRoom = {new Room()};
                    if (roomViewHolder.roomCheckbox.isChecked()) {
                        joinedRoom[0] = room;
                    } else {
                        socketIO.joinRoom(room.getId(), new Ack() {
                            @Override
                            public void call(Object... args) {
                                JSONObject obj = (JSONObject) args[0];
                                if (!obj.has("error")) {
                                    try {
                                        JSONObject temp = obj.getJSONObject("room");

                                        String id = (String) checkIfJsonExists(temp, "_id", null);
                                        String roomName = (String) checkIfJsonExists(temp, "name", null);
                                        String courseID = (String) checkIfJsonExists(temp, "course", null);
                                        String courseName = Course.getCourseById(courseID, realm).getCoursename();
                                        String universityID = (String) checkIfJsonExists(temp, "university", null);
                                        boolean isPublic = (boolean) checkIfJsonExists(temp, "isPublic", true);
                                        int memberCounts = Integer.parseInt((String) checkIfJsonExists(temp, "memberCounts", "1"));
                                        String memberCountsDesc = (String) checkIfJsonExists(temp, "memberCountsDescription", null);
                                        String language = (String) checkIfJsonExists(temp, "language", "0");
                                        boolean isSystem = (boolean) checkIfJsonExists(temp, "isSystem", true);

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
                                                null,
                                                language,
                                                isPublic,
                                                isSystem);

                                        Room.updateRoomToRealm(joinedRoom[0], realm);
                                        realm.close();
                                    } catch (JSONException e) {
                                        Log.e(TAG, e.toString());
                                    }

                                }
                            }
                        });
                    }

                    Intent chatActivityIntent = new Intent(context, ChatRoomActivity.class);
                    chatActivityIntent.putExtra("roomId", joinedRoom[0].getId());
                    context.startActivity(chatActivityIntent);
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

    private boolean isRoomJoined(ArrayList<Room> joinedRooms, Room room) {
        for (int i = 0; i < joinedRooms.size(); i++) {
            if (joinedRooms.get(i).getId().equals(room.getId())) {
                return true;
            }
        }
        return false;
    }
}
