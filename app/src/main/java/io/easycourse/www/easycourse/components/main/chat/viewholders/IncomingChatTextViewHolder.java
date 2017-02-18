package io.easycourse.www.easycourse.components.main.chat.viewholders;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.util.Linkify;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.easycourse.www.easycourse.EasyCourse;
import io.easycourse.www.easycourse.R;
import io.easycourse.www.easycourse.activities.UserDetailActivity;
import io.easycourse.www.easycourse.components.main.chat.ChatRecyclerViewAdapter;
import io.easycourse.www.easycourse.models.main.Message;
import io.easycourse.www.easycourse.models.main.User;
import io.easycourse.www.easycourse.utils.BitmapUtils;
import io.easycourse.www.easycourse.utils.DateUtils;
import io.realm.Realm;
import io.socket.client.Ack;

public class IncomingChatTextViewHolder extends RecyclerView.ViewHolder implements BaseChatViewHolder {

    private static final String TAG = "IncomingChatTextViewHol";


    @BindView(R.id.linearIncomingChatCell)
    LinearLayout incomingLinearLayout;
    @BindView(R.id.textViewIncomingTextTime)
    TextView incomingTime;
    @BindView(R.id.imageViewIncomingTextImage)
    ImageView incomingImageView;
    @BindView(R.id.textViewIncomingTextName)
    TextView incomingName;
    @BindView(R.id.textViewIncomingTextMessage)
    TextView incomingMessage;

    private boolean timeVisible;


    public IncomingChatTextViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        Linkify.addLinks(incomingMessage, Linkify.ALL);
    }

    @Override
    public void setupView(final Message message, Message prevMessage, User curUser, String roomId, final Context context, ChatRecyclerViewAdapter chatRecyclerViewAdapter) {

        Realm realm = Realm.getDefaultInstance();

        final String reportDateIncoming = DateUtils.getTimeString(message, prevMessage);
        if (reportDateIncoming != null) {
            incomingTime.setVisibility(View.VISIBLE);
            incomingTime.setText(reportDateIncoming);
        } else {
            incomingTime.setVisibility(View.GONE);
        }

        User thisUser = User.getUserFromRealm(realm, message.getSenderId());
        if (thisUser == null) {
            try {
                EasyCourse.getAppInstance().getSocketIO().getUserInfoJson(message.getSenderId(), new Ack() {
                    @Override
                    public void call(Object... args) {
                        User thisUser = EasyCourse.getAppInstance().getSocketIO().parseUserJsonInfo((JSONObject) args[0]);
                        fillUserInfo(thisUser, context, message, reportDateIncoming);
                    }
                });
            } catch (JSONException e) {
                Log.e(TAG, "setupView: ", e);
            }
        } else {
            fillUserInfo(thisUser, context, message, reportDateIncoming);
        }
        realm.close();
    }


    private void fillUserInfo(final User thisUser, final Context context, final Message message, final String reportDateIncoming) {
        Realm realm = Realm.getDefaultInstance();
        User currentUser = User.getCurrentUser(context, realm);
        if (thisUser != null && currentUser != null && !thisUser.getId().equals(currentUser.getId())) {

            BitmapUtils.loadImage(context, incomingImageView, thisUser.getProfilePicture(), thisUser.getProfilePictureUrl(), R.drawable.ic_person_black_24px);

            incomingName.setText(thisUser.getUsername());
            incomingMessage.setText(message.getText());

            incomingImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, UserDetailActivity.class);
                    intent.putExtra("user", thisUser.getId());
                    context.startActivity(intent);
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
                    if (reportDateIncoming == null) return;
                    if (timeVisible) {
                        incomingTime.setVisibility(View.GONE);
                        timeVisible = false;
                    } else {
                        incomingTime.setVisibility(View.VISIBLE);
                        timeVisible = true;
                    }
                }
            });
        }
        realm.close();
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