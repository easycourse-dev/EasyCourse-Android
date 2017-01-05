package com.example.markwen.easycourse.components;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.models.main.Room;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmResults;

/**
 * Created by nisarg on 5/1/17.
 */

public class RoomListViewAdapter extends BaseAdapter {

    @BindView(R.id.cardViewChatRoom)
    CardView roomCardView;
    @BindView(R.id.relativeLayoutChatRoom)
    RelativeLayout roomRelativeLayout;
    @BindView(R.id.textViewChatRoomName)
    TextView roomNameTextView;
    @BindView(R.id.textViewChatRoomCourse)
    TextView roomCourseTextView;
    @BindView(R.id.imageViewChatRoom)
    ImageView roomImageView;

    private Context context;
    private LayoutInflater mInflater;
    private RealmResults<Room> rooms;

    public RoomListViewAdapter(Context context, RealmResults rooms){
        this.context = context;
        this.rooms = rooms;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return rooms.size();
    }

    @Override
    public Object getItem(int i) {
        return rooms.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View rowView = mInflater.inflate(R.layout.share_room_item, viewGroup, false);
        ButterKnife.bind(this, rowView);

        Room room = rooms.get(i);

        roomNameTextView.setText(room.getCourseName());
        //roomCourseTextView.setText(room.getMemberCounts());

        return rowView;
    }
}
