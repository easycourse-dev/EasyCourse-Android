package com.example.markwen.easycourse.components.main.CourseDetails;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.models.main.Room;
import com.example.markwen.easycourse.models.main.User;
import com.example.markwen.easycourse.utils.SocketIO;
import com.hanks.library.AnimateCheckBox;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by markw on 12/29/2016.
 */

public class CourseDetailsRoomsRecyclerViewAdapter extends RecyclerView.Adapter<CourseDetailsRoomsRecyclerViewAdapter.RoomViewHolder> {
    private ArrayList<Room> rooms = new ArrayList<>();
    private ArrayList<Room> joinedRooms = new ArrayList<>();
    private SocketIO socketIO;
    private boolean isCourseJoined = false;
    private Realm realm;
    private HttpURLConnection connection;

    public CourseDetailsRoomsRecyclerViewAdapter(ArrayList<Room> list, SocketIO socketIo, boolean isJoined, Realm realm) {
        this.rooms = list;
        this.socketIO = socketIo;
        this.isCourseJoined = isJoined;
        this.realm = realm;
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
    public void onBindViewHolder(RoomViewHolder holder, int i) {
        Room room = rooms.get(i);
        if (!isCourseJoined) {
            // Check if course is joined, if not change text
            holder.checkBox.setClickable(false);
            holder.roomNameTextView.setTextColor(Color.parseColor("#a1a1a1")); // gray color
            holder.founderTextView.setTextColor(Color.parseColor("#a1a1a1")); // gray color
        } else {
            holder.checkBox.setClickable(true);
            holder.roomNameTextView.setTextColor(Color.parseColor("#333333")); // black color
            holder.founderTextView.setTextColor(Color.parseColor("#333333")); // black color
        }
        Room joinedRoom = isRoomJoined(joinedRooms, room);
        if (joinedRoom != null) {
            holder.checkBox.setChecked(true);
        } else {
            holder.checkBox.setChecked(false);
        }
        if (room.getFounder() != null) {
            User user = room.getFounder();
            holder.founderTextView.setText(user.getUsername());
            try {
                // TODO: try to optimize the speed of loading image
                downloadImage(new URL(user.getProfilePictureUrl()), holder.founderImageView);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } else {
            holder.founderTextView.setText("Official");
            holder.founderImageView.setImageResource(R.drawable.ic_group_black_24px);
        }

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

    private void downloadImage(final URL url, final ImageView imgView){
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
}
