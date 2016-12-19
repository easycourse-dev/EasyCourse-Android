package com.example.markwen.easycourse.components.main;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.markwen.easycourse.EasyCourse;
import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.models.main.Room;
import com.example.markwen.easycourse.utils.SocketIO;

import org.json.JSONException;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

/**
 * Created by markw on 12/19/2016.
 */

public class ExistedRoomsRecyclerViewAdapter extends RealmRecyclerViewAdapter<Room, RecyclerView.ViewHolder> {

    private RealmResults<Room> roomsList;

    public ExistedRoomsRecyclerViewAdapter(@NonNull Context context, RealmResults<Room> rooms) {
        super(context, rooms, true);
        this.roomsList = rooms;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new ExistedRoomViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chat_room_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int i) {
        final Room room = roomsList.get(i);
        ExistedRoomsRecyclerViewAdapter.ExistedRoomViewHolder roomViewHolder = (ExistedRoomsRecyclerViewAdapter.ExistedRoomViewHolder) holder;
        roomViewHolder.roomNameTextView.setText(room.getRoomName());
        roomViewHolder.roomCourseTextView.setText(room.getCourseName());

        roomViewHolder.roomCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SocketIO socketIO = EasyCourse.getAppInstance().getSocketIO();
                try {
                    socketIO.joinRoom(room.getId());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    class ExistedRoomViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.cardViewChatRoom)
        CardView roomCardView;
        @BindView(R.id.relativeLayoutChatRoom)
        RelativeLayout roomRelativeLayout;
        @BindView(R.id.textViewChatRoomName)
        TextView roomNameTextView;
        @BindView(R.id.textViewChatRoomCourse)
        TextView roomCourseTextView;
        @BindView(R.id.textViewChatRoomLastMessage)
        TextView roomLastMessageTextView;
        @BindView(R.id.textViewChatRoomLastTime)
        TextView roomLastTimeTextView;
        @BindView(R.id.imageViewChatRoom)
        ImageView roomImageView;

        ExistedRoomViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
