package com.example.markwen.easycourse.components.main;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.fragments.main.RoomsFragment;
import com.example.markwen.easycourse.models.main.Message;
import com.example.markwen.easycourse.models.main.Room;
import com.example.markwen.easycourse.models.main.User;
import com.example.markwen.easycourse.utils.DateUtils;
import com.example.markwen.easycourse.utils.SocketIO;

import org.json.JSONException;

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
import io.socket.client.Ack;


/**
 * Created by noahrinehart on 11/19/16.
 */

public class RoomRecyclerViewAdapter extends RealmRecyclerViewAdapter<Room, RecyclerView.ViewHolder> {

    private static final String TAG = "RoomRecyclerViewAdapter";
    
    private RoomsFragment fragment;
    private Context context;
    private RealmResults<Room> rooms;
    private SocketIO socketIO;


    public RoomRecyclerViewAdapter(RoomsFragment fragment, Context context, RealmResults<Room> rooms, SocketIO socketIO) {
        super(context, rooms, true);
        this.fragment = fragment;
        this.context = context;
        this.rooms = rooms;
        this.socketIO = socketIO;
    }


    class RoomViewHolder extends RecyclerView.ViewHolder {
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

        RoomViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
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
        final RoomRecyclerViewAdapter.RoomViewHolder roomViewHolder = (RoomRecyclerViewAdapter.RoomViewHolder) viewHolder;
        roomViewHolder.roomNameTextView.setText(room.getRoomName());
        roomViewHolder.roomCourseTextView.setText(room.getCourseName());

        roomViewHolder.roomCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragment.startChatRoom(room);
            }
        });

        roomViewHolder.roomCardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return showPopup(roomViewHolder.roomCardView, room, context);
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

    private boolean showPopup(CardView cardView, final Room room, final Context context) {
        if (room == null) return false;

        PopupMenu popup = new PopupMenu(context, cardView);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.roomPopupQuit:
                        showQuitDialog(room);
                        return true;

                }
                return false;
            }
        });
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.room_item_popup, popup.getMenu());
        popup.show();
        return false;
    }

    private void showQuitDialog(final Room room) {

        MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title("Quit " + room.getRoomName() + "?")
                .titleColor(ContextCompat.getColor(context, R.color.colorAccent))
                .positiveText("Quit")
                .positiveColor(ContextCompat.getColor(context, R.color.colorLogout))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        quitRoom(room);
                    }
                })
                .negativeText("Cancel")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.cancel();
                    }
                })
                .build();
        dialog.show();


    }

    private void quitRoom(final Room room) {

        try {
            socketIO.quitRoom(room.getId(), new Ack() {
                @Override
                public void call(Object... args) {
                    fragment.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fragment.deleteRoom(room);
                            RoomRecyclerViewAdapter.this.notifyDataSetChanged();
                        }
                    });
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "quitRoom: ", e);
        }
        socketIO.syncUser();

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
