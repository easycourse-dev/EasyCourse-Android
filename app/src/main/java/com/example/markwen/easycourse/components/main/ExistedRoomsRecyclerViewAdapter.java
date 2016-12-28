package com.example.markwen.easycourse.components.main;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.activities.ChatRoomActivity;
import com.example.markwen.easycourse.models.main.Room;
import com.example.markwen.easycourse.utils.SocketIO;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by markw on 12/19/2016.
 */

public class ExistedRoomsRecyclerViewAdapter extends RecyclerView.Adapter<ExistedRoomsRecyclerViewAdapter.ExistedRoomViewHolder> {

    private ArrayList<Room> roomsList;
    private Context context;
    private SocketIO socketIO;

    public ExistedRoomsRecyclerViewAdapter(@NonNull Context context, ArrayList<Room> rooms, SocketIO socketIO) {
        super();
        this.roomsList = rooms;
        this.context = context;
        this.socketIO = socketIO;
    }

    @Override
    public ExistedRoomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new ExistedRoomViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chat_room_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ExistedRoomViewHolder roomViewHolder, int i) {
        final Room room = roomsList.get(i);
        roomViewHolder.roomNameTextView.setText(room.getRoomName());
        roomViewHolder.roomCourseTextView.setText(room.getCourseName());

        roomViewHolder.roomCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Future<Room> joiningRoom = socketIO.joinRoom(room.getId());
                    Room joinedRoom = joiningRoom.get();
                    Intent chatActivityIntent = new Intent(context, ChatRoomActivity.class);
                    chatActivityIntent.putExtra("roomId", joinedRoom.getId());
                    context.startActivity(chatActivityIntent);
                } catch (JSONException | InterruptedException | ExecutionException e) {
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

        ExistedRoomViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
