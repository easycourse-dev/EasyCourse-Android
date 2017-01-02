package io.easycourse.www.easycourse.components.main;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.easycourse.www.easycourse.fragments.main.RoomsFragment;
import io.easycourse.www.easycourse.models.main.Message;
import io.easycourse.www.easycourse.models.main.User;
import io.easycourse.www.easycourse.utils.DateUtils;
import io.easycourse.www.easycourse.models.main.Room;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.Sort;


/**
 * Created by noahrinehart on 11/19/16.
 */

public class RoomRecyclerViewAdapter extends RealmRecyclerViewAdapter<Room, RecyclerView.ViewHolder> {

    private RoomsFragment fragment;
    private Context context;
    private RealmResults<Room> rooms;


    public RoomRecyclerViewAdapter(RoomsFragment fragment, Context context, RealmResults<Room> rooms) {
        super(context, rooms, true);
        this.fragment = fragment;
        this.context = context;
        this.rooms = rooms;
    }


    class RoomViewHolder extends RecyclerView.ViewHolder {
        @BindView(io.easycourse.www.easycourse.R.id.cardViewChatRoom)
        CardView roomCardView;
        @BindView(io.easycourse.www.easycourse.R.id.relativeLayoutChatRoom)
        RelativeLayout roomRelativeLayout;
        @BindView(io.easycourse.www.easycourse.R.id.textViewChatRoomName)
        TextView roomNameTextView;
        @BindView(io.easycourse.www.easycourse.R.id.textViewChatRoomCourse)
        TextView roomCourseTextView;
        @BindView(io.easycourse.www.easycourse.R.id.textViewChatRoomLastMessage)
        TextView roomLastMessageTextView;
        @BindView(io.easycourse.www.easycourse.R.id.textViewChatRoomLastTime)
        TextView roomLastTimeTextView;
        @BindView(io.easycourse.www.easycourse.R.id.imageViewChatRoom)
        ImageView roomImageView;

        RoomViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(io.easycourse.www.easycourse.R.layout.chat_room_item, viewGroup, false);
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
                fragment.startChatRoom(room);
            }
        });


        //TODO: Add Usernames
        Realm realm = Realm.getDefaultInstance();
        List<Message> messages = realm.where(Message.class).equalTo("toRoom", room.getId()).findAllSorted("createdAt", Sort.DESCENDING);
        Message message;
        User curUser = User.getCurrentUser((Activity) this.context, realm);
        String name = "";
        if (messages.size() > 0) {
            if (messages.get(0) != null) {
                if (messages.get(0).getCreatedAt() != null) {
                    message = messages.get(0);
                } else {
                    message = messages.get(messages.size() - 1);
                }

                roomViewHolder.roomLastMessageTextView.setText(message.getText());
                roomViewHolder.roomLastTimeTextView.setText(getMessageTime(message));
            }
        }
    }

    private String getMessageTime(Message message) {
        if (message == null) return null;
        Date messageDate = message.getCreatedAt();
        if (messageDate == null) return null;
        Date now = new Date();
        long diffInMinutes = DateUtils.timeDifferenceInMinutes(messageDate, now);
        if (diffInMinutes <= 1) {
            //If within a minute
            return "Just Now";
        } else if (diffInMinutes <= 1440) {
            DateFormat df = new SimpleDateFormat("hh:mm a", Locale.US);
            return df.format(messageDate);
        } else {
            DateFormat df = new SimpleDateFormat("mm dd", Locale.US);
            return df.format(messageDate);
        }
    }
}
