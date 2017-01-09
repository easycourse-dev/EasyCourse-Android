package com.example.markwen.easycourse.components.main.chat.viewholders;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.markwen.easycourse.EasyCourse;
import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.components.main.chat.ChatRecyclerViewAdapter;
import com.example.markwen.easycourse.models.main.Message;
import com.example.markwen.easycourse.models.main.User;
import com.example.markwen.easycourse.utils.DateUtils;
import com.example.markwen.easycourse.utils.SocketIO;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.socket.client.Ack;

/**
 * Created by nisarg on 5/1/17.
 */

public class IncomingSharedRoomViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "IncomingSharedRoomView";

    private AppCompatActivity activity;

    @BindView(R.id.linearIncomingChatCell)
    LinearLayout incomingLinearLayout;
    @BindView(R.id.textViewIncomingTextTime)
    TextView incomingTime;
    @BindView(R.id.imageViewIncomingTextImage)
    ImageView incomingImageView;
    @BindView(R.id.textViewIncomingTextName)
    TextView incomingName;
    @BindView(R.id.relativeLayoutSharedRoomHolder)
    RelativeLayout sharedRoomHolder;
    @BindView(R.id.textViewChatRoomName)
    TextView textViewRoomName;

    private boolean timeVisible;

    private Realm realm;
    private User currentUser;
    private SocketIO socketIO;

    public IncomingSharedRoomViewHolder(View itemView, AppCompatActivity activity) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.activity = activity;

        realm = Realm.getDefaultInstance();
        currentUser = User.getCurrentUser(activity, realm);
        socketIO = EasyCourse.getAppInstance().getSocketIO();
    }

    public void setupView(final Message message, Message prevMessage, User curUser, Realm realm, final Context context, ChatRecyclerViewAdapter chatRecyclerViewAdapter) {
        String reportDateIncoming = DateUtils.getTimeString(message, prevMessage);
        if (reportDateIncoming != null) {
            incomingTime.setVisibility(View.VISIBLE);
            incomingTime.setText(reportDateIncoming);
        } else {
            incomingTime.setVisibility(View.GONE);
        }

//        if (!message.isSuccessSent())
//            incomingMessage.setBackground(ContextCompat.getDrawable(context, R.drawable.cell_message_unsent));
//        else
//            incomingMessage.setBackground(ContextCompat.getDrawable(context, R.drawable.cell_message_sent));

        User thisUser = User.getUserFromRealm(realm, message.getSenderId());
        if (thisUser == null) {
            try {
                EasyCourse.getAppInstance().getSocketIO().getUserInfoJson(message.getSenderId(), new Ack() {
                    @Override
                    public void call(Object... args) {
                        User thisUser = EasyCourse.getAppInstance().getSocketIO().parseUserJsonInfo((JSONObject) args[0]);
                        fillUserInfo(thisUser, context, message);
                    }
                });
            } catch (JSONException e) {
                Log.e(TAG, "setupView: ", e);
            }
        } else {
            fillUserInfo(thisUser, context, message);
        }
    }

    private void fillUserInfo(User thisUser, final Context context, final Message message) {
        if (thisUser != null && thisUser != User.getCurrentUser(activity, Realm.getDefaultInstance())) {

            if (thisUser.getProfilePictureUrl() == null || thisUser.getProfilePictureUrl().isEmpty()) {
                incomingImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_person_black_24px));
            } else {
                Picasso.with(context).load(thisUser.getProfilePictureUrl()).resize(36, 36).centerInside()
                        .placeholder(R.drawable.ic_person_black_24px)
                        .into(incomingImageView);
            }

            incomingName.setText(thisUser.getUsername());
            textViewRoomName.setText(message.getSharedRoom().getRoomName());
            sharedRoomHolder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        socketIO.joinRoom(message.getSharedRoom().getId(), new Ack() {
                            @Override
                            public void call(Object... args) {
                                JSONObject obj = (JSONObject) args[0];
                                Log.e(TAG, obj.toString());
                                try {
                                    if(!obj.has("error")) {
                                        JSONObject roomObj = obj.getJSONObject("room");
                                        Snackbar.make(activity.getWindow().getDecorView().getRootView(), obj.getString("msg"), Snackbar.LENGTH_LONG)
                                                .show();
                                    } else {
                                        Snackbar.make(activity.getWindow().getDecorView().getRootView(), "Error: Course not joined.", Snackbar.LENGTH_LONG)
                                                .show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });

            incomingLinearLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    return showPopup(incomingLinearLayout, message, context);
                }
            });

            incomingLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (timeVisible)
                        incomingTime.setVisibility(View.GONE);
                    else
                        incomingTime.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private boolean showPopup(LinearLayout linearLayout, final Message message, final Context context) {
        if (message.getText() == null) return false;

        PopupMenu popup = new PopupMenu(context, linearLayout);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.itemPopupCopy:
                        //Copy item to clipboard
                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText(message.getText(), message.getText());
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show();
                        return true;

//                    case R.id.itemPopupDelete:
//                        //Delete item from realm
//                        return true;
                }
                return false;
            }
        });
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.chat_message_popup, popup.getMenu());
        popup.show();
        return false;
    }
}
