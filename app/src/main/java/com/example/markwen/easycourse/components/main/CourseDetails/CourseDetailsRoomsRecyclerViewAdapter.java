package com.example.markwen.easycourse.components.main.CourseDetails;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.models.main.Message;
import com.example.markwen.easycourse.models.main.Room;
import com.example.markwen.easycourse.models.main.User;
import com.example.markwen.easycourse.utils.SocketIO;
import com.hanks.library.AnimateCheckBox;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.socket.client.Ack;

import static com.example.markwen.easycourse.utils.JSONUtils.checkIfJsonExists;

/**
 * Created by markw on 12/29/2016.
 */

public class CourseDetailsRoomsRecyclerViewAdapter extends RecyclerView.Adapter<CourseDetailsRoomsRecyclerViewAdapter.RoomViewHolder> {
    private ArrayList<Room> rooms = new ArrayList<>();
    private ArrayList<Room> joinedRooms = new ArrayList<>();
    private boolean isCourseJoined = false;
    private SocketIO socketIO;
    private Realm realm;
    private AppCompatActivity activity;
    private HttpURLConnection connection;

    public CourseDetailsRoomsRecyclerViewAdapter(ArrayList<Room> list, SocketIO socketIo, boolean isJoined, Realm realm, AppCompatActivity activity) {
        this.rooms = list;
        this.socketIO = socketIo;
        this.isCourseJoined = isJoined;
        this.realm = realm;
        this.activity = activity;
        RealmResults<Room> joinedRoomsResults = realm.where(Room.class).findAll();
        for (int i = 0; i < joinedRoomsResults.size(); i++) {
            joinedRooms.add(joinedRoomsResults.get(i));
        }
    }

    @Override
    public RoomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_course_details_room_item, viewGroup, false);
        return new RoomViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final RoomViewHolder holder, int i) {
        final Room room = rooms.get(i);
        if (!isCourseJoined) {
            // Different state based on course joining status
            holder.checkBox.setClickable(false);
            holder.roomNameTextView.setTextColor(Color.parseColor("#a1a1a1")); // gray color
            holder.founderTextView.setTextColor(Color.parseColor("#a1a1a1")); // gray color
        } else {
            holder.checkBox.setClickable(true);
            holder.roomNameTextView.setTextColor(Color.parseColor("#333333")); // black color
            holder.founderTextView.setTextColor(Color.parseColor("#333333")); // black color
        }

        // Check if this room is joined
        Room joinedRoom = isRoomJoined(joinedRooms, room);
        if (joinedRoom != null) {
            holder.checkBox.setChecked(true);
        } else {
            holder.checkBox.setChecked(false);
        }
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.checkBox.isChecked()) {
                    dropRoom(room.getId());
                    // TODO: update checkbox after dropRoom is called
                    holder.checkBox.setChecked(false);
                } else {
                    joinRoom(room.getId(), room.getCourseName());
                    // TODO: update checkbox after joinRoom is called
                    holder.checkBox.setChecked(true);
                }
            }
        });

        // Set founder and founder avatar
        if (room.getFounder() != null && room.getFounder().getUsername() != null) {
            User user = room.getFounder();
            holder.founderTextView.setText(user.getUsername());
            byte[] image = user.getProfilePicture();
            if (image == null || image.length == 0) {
                try {
                    downloadImage(new URL(user.getProfilePictureUrl()), holder.founderImageView, room);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            } else {
                holder.founderImageView.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.length, null));
            }
        } else {
            holder.founderTextView.setText("Official");
            holder.founderImageView.setImageResource(R.drawable.ic_group_black_24px);
        }

        // Set other texts
        holder.roomNameTextView.setText(room.getRoomName());
        holder.roomDescTextView.setText(room.getMemberCountsDesc());
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    public class RoomViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.CourseDetailsRoomName)
        TextView roomNameTextView;
        @BindView(R.id.CourseDetailsRoomDesc)
        TextView roomDescTextView;
        @BindView(R.id.CourseDetailsFounderImage)
        ImageView founderImageView;
        @BindView(R.id.CourseDetailsRoomFounder)
        TextView founderTextView;
        @BindView(R.id.CourseDetailsCheckbox)
        AnimateCheckBox checkBox;
        public RoomViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private Room isRoomJoined(ArrayList<Room> list, Room room) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(room.getId())) {
                return list.get(i);
            }
        }
        return null;
    }

    private void downloadImage(final URL url, final ImageView imgView, final Room room){
        Thread thread = new Thread(){
            @Override
            public void run() {
                try  {
                    // Download image
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    Bitmap image = BitmapFactory.decodeStream(input);
                    // Compress image
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.JPEG, 30, stream);
                    imgView.setImageBitmap(image);

                    // Saving image
                    byte[] byteArray = stream.toByteArray();
                    room.getFounder().setProfilePicture(byteArray);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    public void updateCourse(boolean status, ArrayList<Room> newRooms) {
        if (status) {
            // joinCourse
            isCourseJoined = true;
            joinedRooms.addAll(newRooms);
        } else {
            // dropCourse
            isCourseJoined = false;
            joinedRooms.clear();
            RealmResults<Room> joinedRoomsResults = realm.where(Room.class).findAll();
            for (int i = 0; i < joinedRoomsResults.size(); i++) {
                joinedRooms.add(joinedRoomsResults.get(i));
            }
        }
        notifyDataSetChanged();
    }

    private void joinRoom(final String roomId, final String courseName) {
        try {
            socketIO.joinRoom(roomId, new Ack() {
                @Override
                public void call(Object... args) {
                    JSONObject obj = (JSONObject) args[0];
                    if (!obj.has("error")) {
                        try {
                            Realm tempRealm = Realm.getDefaultInstance();
                            // Get room
                            JSONObject temp = obj.getJSONObject("room");
                            String roomName = (String) checkIfJsonExists(temp, "name", null);
                            String courseID = (String) checkIfJsonExists(temp, "course", null);
                            String universityID = (String) checkIfJsonExists(temp, "university", null);
                            String language = (String) checkIfJsonExists(temp, "language", null);
                            boolean isPublic = (boolean) checkIfJsonExists(temp, "isPublic", true);
                            int memberCounts = Integer.parseInt((String) checkIfJsonExists(temp, "memberCounts", "1"));
                            String memberCountsDesc = (String) checkIfJsonExists(temp, "memberCountsDescription", null);
                            boolean isSystem = (boolean) checkIfJsonExists(temp, "isSystem", true);

                            // Get founder
                            String founderId = null, founderName = null, founderAvatarUrl = null;
                            if (temp.has("founder")) {
                                JSONObject founderJSON = temp.getJSONObject("founder");
                                founderId = (String) checkIfJsonExists(founderJSON, "_id", null);
                                founderName = (String) checkIfJsonExists(founderJSON, "displayName", null);
                                founderAvatarUrl = (String) checkIfJsonExists(founderJSON, "avatarUrl", null);
                            }

                            // Save room to Realm
                            Room.updateRoomToRealm(
                                    new Room(
                                            roomId,
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
                                            isSystem),
                                    tempRealm);
                            tempRealm.close();

                        } catch (JSONException e) {
                            Log.e("joinRoom", e.toString());
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void dropRoom(final String roomId) {
        try {
            socketIO.quitRoom(roomId, new Ack() {
                @Override
                public void call(Object... args) {
                    JSONObject obj = (JSONObject) args[0];
                    if (obj.has("error")) {
                        Log.e("quitRoom", obj.toString());
                    } else {
                        try {
                            if (obj.has("success") && obj.getBoolean("success")) {
                                Realm tempRealm = Realm.getDefaultInstance();
                                Room deletedRoom = Room.getRoomById(tempRealm, roomId);
                                if (deletedRoom != null) {
                                    tempRealm.beginTransaction();
                                    deletedRoom.deleteFromRealm();
                                    tempRealm.commitTransaction();
                                }
                                tempRealm.close();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // TODO: synchronize dropRoom and joinRoom checkbox change
    private void updateCheckbox(boolean status) {
        Thread thread = new Thread(){
            @Override
            public void run() {
                synchronized (this) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });
                }
            }
        };
        thread.start();
    }
}
