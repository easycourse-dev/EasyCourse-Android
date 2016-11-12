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

import java.util.ArrayList;

/**
 * Created by noahrinehart on 11/5/16.
 */

public class ChatRoomRecyclerViewAdapter extends RecyclerView.Adapter<ChatRoomRecyclerViewAdapter.ChatRoomViewHolder> {
    private ArrayList<Room> chatRoomList = new ArrayList<>();

    private Context context;

    public ChatRoomRecyclerViewAdapter(ArrayList<Room> chatRoomList, Context context) {
        this.chatRoomList = chatRoomList;
        this.context = context;
    }

    static class ChatRoomViewHolder extends RecyclerView.ViewHolder {
        CardView chatCardView;
        LinearLayout chatRoomLayout;
        TextView chatRoomNameTextView;
        TextView chatRoomCourseTextView;

        ChatRoomViewHolder(View itemView) {
            super(itemView);
            chatRoomLayout = (LinearLayout) itemView.findViewById(R.id.linearLayoutChatRoom);
            chatRoomNameTextView = (TextView) itemView.findViewById(R.id.textViewChatRoomName);
            chatRoomCourseTextView = (TextView) itemView.findViewById(R.id.textViewChatRoomCourse);
            chatCardView = (CardView) itemView.findViewById(R.id.cardViewChatRoom);
        }
    }

    @Override
    public ChatRoomRecyclerViewAdapter.ChatRoomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chat_room_item, viewGroup, false);
        ChatRoomRecyclerViewAdapter.ChatRoomViewHolder chatRoomViewHolder = new ChatRoomRecyclerViewAdapter.ChatRoomViewHolder(v);
        return chatRoomViewHolder;
    }

    @Override
    public void onBindViewHolder(final ChatRoomRecyclerViewAdapter.ChatRoomViewHolder chatViewHolder, int i) {
        final Room room = chatRoomList.get(i);
        chatViewHolder.chatRoomNameTextView.setText(room.getRoomname());
        chatViewHolder.chatRoomCourseTextView.setText(room.getCourseName());

        chatViewHolder.chatCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent chatActivityIntent = new Intent(context, ChatRoom.class);
                chatActivityIntent.putExtra("Roomname", room.getRoomname());
                chatActivityIntent.putExtra("CourseName", room.getCourseName());
                context.startActivity(chatActivityIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatRoomList.size();
    }


    public ArrayList<Room> getChatRoomList() {
        return chatRoomList;
    }
}

