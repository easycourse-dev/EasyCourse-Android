package io.easycourse.www.easycourse.components.main;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.easycourse.www.easycourse.R;
import io.easycourse.www.easycourse.fragments.main.RoomsFragment;
import io.easycourse.www.easycourse.models.main.Message;
import io.easycourse.www.easycourse.models.main.Room;
import io.easycourse.www.easycourse.models.main.User;
import io.easycourse.www.easycourse.utils.DateUtils;
import io.easycourse.www.easycourse.utils.SocketIO;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.Sort;
import io.socket.client.Ack;
import su.levenetc.android.badgeview.BadgeView;


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
        @BindView(R.id.textViewChatRoomLastSender)
        TextView roomLastSenderTextView;
        @BindView(R.id.textViewChatRoomLastTime)
        TextView roomLastTimeTextView;
        @BindView(R.id.imageViewChatRoom)
        ImageView roomImageView;
        @BindView(R.id.imageViewBadge)
        BadgeView badgeView;

        RoomViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cell_room_list, viewGroup, false);
        RoomRecyclerViewAdapter.RoomViewHolder roomViewHolder = new RoomRecyclerViewAdapter.RoomViewHolder(v);
        return roomViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final Room room = rooms.get(viewHolder.getAdapterPosition());
        final RoomRecyclerViewAdapter.RoomViewHolder roomViewHolder = (RoomRecyclerViewAdapter.RoomViewHolder) viewHolder;
        roomViewHolder.roomNameTextView.setText(room.getRoomName());
        roomViewHolder.roomCourseTextView.setText(room.getCourseName());

        if (room.getUnread() <= 0) {
            roomViewHolder.badgeView.setVisibility(View.GONE);
        } else {
            roomViewHolder.badgeView.setVisibility(View.VISIBLE);
            roomViewHolder.badgeView.setValue(room.getUnread());
        }

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

        Realm realm = Realm.getDefaultInstance();
        User curUser = User.getCurrentUser(context, realm);

        Picasso.with(context).cancelRequest(roomViewHolder.roomImageView);

        if (!room.isToUser()) {
            roomViewHolder.roomImageView.setImageResource(R.drawable.ic_group_black_24px);
        }

        if (room.isToUser()) {
            User otherUser = Room.getOtherUserIfPrivate(room, curUser, realm);
            if (otherUser != null) {
                if (otherUser.getProfilePicture() != null) {
                    Bitmap bm = BitmapFactory.decodeByteArray(otherUser.getProfilePicture(), 0, otherUser.getProfilePicture().length);
                    roomViewHolder.roomImageView.setImageBitmap(bm);
                } else if (otherUser.getProfilePictureUrl() != null) {
                    Picasso.with(context).load(otherUser.getProfilePictureUrl()).error(R.drawable.ic_person_black_24px).into(roomViewHolder.roomImageView);
                } else {
                    roomViewHolder.roomImageView.setImageResource(R.drawable.ic_person_black_24px);
                }
            }
        }


        List<Message> messages;

        messages = realm.where(Message.class).equalTo("toRoom", room.getId()).findAllSorted("createdAt", Sort.DESCENDING);
        Message message;
        String messageText, senderText;
        if (messages.size() > 0) {
            if (messages.get(0) != null) {
                if (messages.get(0).getCreatedAt() != null) {
                    message = messages.get(0);
                } else {
                    message = messages.get(messages.size() - 1);
                }

                // Distinguish message type
                if (message.getText() == null && message.getImageData() != null) {
                    messageText = "[Image]";
                } else if (message.getText() == null && message.getSharedRoom() != null) {
                    messageText = "[Shared Room]";
                } else {
                    messageText = message.getText();
                }

                if (message.getSender() == null || message.getSender().getUsername() == null) {
                    senderText = "";
                } else {
                    senderText = message.getSender().getUsername() + ": ";
                }
                roomViewHolder.roomLastMessageTextView.setText(messageText);
                roomViewHolder.roomLastSenderTextView.setText(senderText);
                roomViewHolder.roomLastTimeTextView.setText(getTimeString(message));
            }
        } else {
            roomViewHolder.roomLastMessageTextView.setText(null);
            roomViewHolder.roomLastSenderTextView.setText(null);
            roomViewHolder.roomLastTimeTextView.setText(getTimeString(null));
        }

        realm.close();
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
            if (room.isToUser()) {
                deleteRoomInSocket(room);
            } else {
                socketIO.quitRoom(room.getId(), new Ack() {
                    @Override
                    public void call(Object... args) {
                        JSONObject obj = (JSONObject) args[0];

                        if (obj.has("error")) {
                            Log.e(TAG, obj.toString());
                        } else {

                            try {
                                boolean success = obj.getBoolean("success");
                                if (success) {
                                    deleteRoomInSocket(room);
                                }
                            } catch (JSONException e) {
                                Log.e(TAG, "call: ", e);
                            }
                        }
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socketIO.syncUser();
    }

    @Nullable
    private static String getTimeString(Message message) {
        if (message == null) return null;
        Date messageDate = DateUtils.getLocalDate(message.getCreatedAt());

        TimeZone timeZone = TimeZone.getTimeZone("GMT");
        //If today
        if (DateUtils.isToday(messageDate)) {
            //Exclude date in time
            DateFormat df = new SimpleDateFormat("h:mm a", Locale.US);
            df.setTimeZone(timeZone);
            return df.format(messageDate);

        } else {
            //Include date in time
            DateFormat df = new SimpleDateFormat("MM/dd/yy hh:mm a", Locale.US);
            df.setTimeZone(timeZone);
            return df.format(messageDate);
        }
    }


    public void deleteRoomInSocket(final Room room) {
        ((Activity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Realm tempRealm = Realm.getDefaultInstance();
                tempRealm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        Room realmRoom = realm.where(Room.class).equalTo("id", room.getId()).findFirst();
                        realmRoom.deleteFromRealm();
                        notifyDataSetChanged();
                    }
                });
            }
        });
    }
}
