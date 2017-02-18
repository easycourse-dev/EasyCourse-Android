package io.easycourse.www.easycourse.components.main.chat.viewholders;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
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

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.easycourse.www.easycourse.EasyCourse;
import io.easycourse.www.easycourse.R;
import io.easycourse.www.easycourse.components.main.chat.ChatRecyclerViewAdapter;
import io.easycourse.www.easycourse.models.main.Message;
import io.easycourse.www.easycourse.models.main.User;
import io.easycourse.www.easycourse.utils.BitmapUtils;
import io.easycourse.www.easycourse.utils.DateUtils;
import io.easycourse.www.easycourse.utils.SocketIO;
import io.socket.client.Ack;

public class OutgoingSharedRoomViewHolder extends RecyclerView.ViewHolder implements BaseChatViewHolder {
    private static final String TAG = "OutgoingSharedRoomView";

    private AppCompatActivity activity;

    @BindView(R.id.linearOutgoingSharedChatCell)
    LinearLayout outgoingLinearLayout;
    @BindView(R.id.textViewOutgoingSharedTextTime)
    TextView outgoingTime;
    @BindView(R.id.imageViewOutgoingSharedTextImage)
    ImageView outgoingImageView;
    @BindView(R.id.textViewOutgoingSharedTextName)
    TextView outgoingName;
    @BindView(R.id.relativeLayoutOutgoingSharedRoomHolder)
    RelativeLayout sharedRoomHolder;
    @BindView(R.id.textViewChatSharedRoomName)
    TextView textViewRoomName;


    private boolean timeVisible;

    private SocketIO socketIO;

    public OutgoingSharedRoomViewHolder(View itemView, AppCompatActivity activity) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.activity = activity;
        socketIO = EasyCourse.getAppInstance().getSocketIO();
    }


    @Override
    public void setupView(final Message message, Message prevMessage, User curUser, String roomId, final Context context, ChatRecyclerViewAdapter chatRecyclerViewAdapter) {
        String reportDateOutgoing = DateUtils.getTimeString(message, prevMessage);
        if (reportDateOutgoing != null) {
            outgoingTime.setVisibility(View.VISIBLE);
            outgoingTime.setText(reportDateOutgoing);
            timeVisible = true;
        } else {
            outgoingTime.setVisibility(View.GONE);
            timeVisible = false;
        }

        if (!message.isSuccessSent())
            sharedRoomHolder.setBackground(ContextCompat.getDrawable(context, R.drawable.cell_message_unsent));
        else
            sharedRoomHolder.setBackground(ContextCompat.getDrawable(context, R.drawable.cell_message_sent));

        if (curUser != null) {

            BitmapUtils.loadImage(context, outgoingImageView, curUser.getProfilePicture(), curUser.getProfilePictureUrl(), R.drawable.ic_person_black_24px);

            outgoingName.setText(curUser.getUsername());
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
                                    if (!obj.has("error")) {
//                                        JSONObject roomObj = obj.getJSONObject("room");
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
        }

        outgoingLinearLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return showPopup(outgoingLinearLayout, message, context);
            }
        });

        outgoingLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (outgoingTime.getText().equals("time")) return;
                if (timeVisible) {
                    outgoingTime.setVisibility(View.GONE);
                    timeVisible = false;
                } else {
                    outgoingTime.setVisibility(View.VISIBLE);
                    timeVisible = true;
                }
            }
        });
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
