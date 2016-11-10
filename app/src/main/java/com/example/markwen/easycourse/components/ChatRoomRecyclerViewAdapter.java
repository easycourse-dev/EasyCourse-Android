package com.example.markwen.easycourse.components;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.models.main.Room;

import java.util.ArrayList;

/**
 * Created by noahrinehart on 11/5/16.
 */

public class ChatRoomRecyclerViewAdapter extends RecyclerView.Adapter<ChatRoomRecyclerViewAdapter.ChatRoomViewHolder> {
    private ArrayList<Room> chatRoomList = new ArrayList<>();

    public ChatRoomRecyclerViewAdapter(ArrayList<Room> chatRoomList) {
        this.chatRoomList = chatRoomList;
    }

    static class ChatRoomViewHolder extends RecyclerView.ViewHolder {
        LinearLayout chatRoomLayout;
        TextView chatRoomNameTextView;
        TextView chatRoomCourseTextView;

        ChatRoomViewHolder(View itemView) {
            super(itemView);
            chatRoomLayout = (LinearLayout) itemView.findViewById(R.id.linearLayoutChatRoom);
            chatRoomNameTextView = (TextView) itemView.findViewById(R.id.textViewChatRoomName);
            chatRoomCourseTextView = (TextView) itemView.findViewById(R.id.textViewChatRoomCourse);
        }
    }

    @Override
    public ChatRoomRecyclerViewAdapter.ChatRoomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chat_room_item, viewGroup, false);
        ChatRoomRecyclerViewAdapter.ChatRoomViewHolder chatViewHolder = new ChatRoomRecyclerViewAdapter.ChatRoomViewHolder(v);
        return chatViewHolder;
    }

    @Override
    public void onBindViewHolder(final ChatRoomRecyclerViewAdapter.ChatRoomViewHolder chatViewHolder, int i) {
        Room room = chatRoomList.get(i);
        chatViewHolder.chatRoomNameTextView.setText(room.getRoomname());
        chatViewHolder.chatRoomCourseTextView.setText(room.getCourseName());
    }

    @Override
    public int getItemCount() {
        return chatRoomList.size();
    }


    public ArrayList<Room> getChatRoomList() {
        return chatRoomList;
    }
}

