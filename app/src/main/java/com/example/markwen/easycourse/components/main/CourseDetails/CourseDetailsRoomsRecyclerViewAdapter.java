package com.example.markwen.easycourse.components.main.CourseDetails;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.models.main.Room;
import com.example.markwen.easycourse.utils.SocketIO;

import java.util.ArrayList;

/**
 * Created by markw on 12/29/2016.
 */

public class CourseDetailsRoomsRecyclerViewAdapter extends RecyclerView.Adapter<CourseDetailsRoomsRecyclerViewAdapter.RoomViewHolder> {
    private ArrayList<Room> rooms = new ArrayList<>();
    private SocketIO socketIO;

    public CourseDetailsRoomsRecyclerViewAdapter(ArrayList<Room> list, SocketIO socketIo) {
        this.rooms = list;
        this.socketIO = socketIo;
    }

    @Override
    public RoomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.signup_choose_courses_item, viewGroup, false);
        return new RoomViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RoomViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    public class RoomViewHolder extends RecyclerView.ViewHolder {


        RoomViewHolder(View view) {
            super(view);
        }
    }

}
