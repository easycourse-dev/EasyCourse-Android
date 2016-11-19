package com.example.markwen.easycourse.components.main;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.activities.ChatRoom;
import com.example.markwen.easycourse.models.main.Room;

import io.realm.RealmBasedRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.RealmViewHolder;

/**
 * Created by noahrinehart on 11/19/16.
 */

public class RoomRealmRecyclerView extends RealmBasedRecyclerViewAdapter<Room, RealmViewHolder> {

    public RoomRealmRecyclerView(Context context, RealmResults<Room> rooms, boolean autoUpdate, boolean animateIdType) {
        super(context, rooms, autoUpdate, animateIdType);
    }

    private class RoomViewHolder extends RealmViewHolder {
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
    public RoomRealmRecyclerView.RoomViewHolder onCreateRealmViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chat_room_item, viewGroup, false);
        RoomRealmRecyclerView.RoomViewHolder roomViewHolder = new RoomRealmRecyclerView.RoomViewHolder(v);
        return roomViewHolder;
    }

    @Override
    public void onBindRealmViewHolder(RealmViewHolder viewHolder, int position) {
        final Room room = realmResults.get(position);
        RoomRealmRecyclerView.RoomViewHolder roomViewHolder = (RoomRealmRecyclerView.RoomViewHolder) viewHolder;
        roomViewHolder.roomNameTextView.setText(room.getRoomname());
        roomViewHolder.roomCourseTextView.setText(room.getCourseName());

        roomViewHolder.roomCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent chatActivityIntent = new Intent(getContext(), ChatRoom.class);
                chatActivityIntent.putExtra("Roomname", room.getRoomname());
                chatActivityIntent.putExtra("CourseName", room.getCourseName());
                getContext().startActivity(chatActivityIntent);
            }
        });
    }


}
