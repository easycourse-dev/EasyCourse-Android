package com.example.markwen.easycourse.components.main;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.activities.ChatRoom;
import com.example.markwen.easycourse.models.main.Room;

import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;


/**
 * Created by noahrinehart on 11/19/16.
 */

public class RoomRecyclerViewAdapter extends RealmRecyclerViewAdapter<Room, RecyclerView.ViewHolder> {

    private Context context;
    private RealmResults<Room> rooms;


    public RoomRecyclerViewAdapter(Context context, RealmResults<Room> rooms) {
        super(context, rooms, true);
        this.context = context;
        this.rooms = rooms;
    }


    private class RoomViewHolder extends RecyclerView.ViewHolder{
        CardView roomCardView;
        LinearLayout roomLinearLayout;
        TextView roomNameTextView;
        TextView roomCourseTextView;

        RoomViewHolder(View itemView) {
            super(itemView);
            roomLinearLayout = (LinearLayout) itemView.findViewById(R.id.linearLayoutChatRoom);
            roomNameTextView = (TextView) itemView.findViewById(R.id.textViewChatRoomName);
            roomCourseTextView = (TextView) itemView.findViewById(R.id.textViewChatRoomCourse);
            roomCardView = (CardView) itemView.findViewById(R.id.cardViewChatRoom);
        }

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chat_room_item, viewGroup, false);
        RoomRecyclerViewAdapter.RoomViewHolder roomViewHolder = new RoomRecyclerViewAdapter.RoomViewHolder(v);
        return roomViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final Room room = rooms.get(position);
        RoomRecyclerViewAdapter.RoomViewHolder roomViewHolder = (RoomRecyclerViewAdapter.RoomViewHolder) viewHolder;
        roomViewHolder.roomNameTextView.setText(room.getRoomName());
        roomViewHolder.roomCourseTextView.setText(room.getCourseName());

        roomViewHolder.roomCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent chatActivityIntent = new Intent(context, ChatRoom.class);
                chatActivityIntent.putExtra("roomId", room.getId());
                context.startActivity(chatActivityIntent);
            }
        });
    }


}
