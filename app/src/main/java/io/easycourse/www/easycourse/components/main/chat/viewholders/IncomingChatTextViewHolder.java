package io.easycourse.www.easycourse.components.main.chat.viewholders;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
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

import io.easycourse.www.easycourse.models.main.Message;
import io.easycourse.www.easycourse.models.main.User;
import io.easycourse.www.easycourse.utils.DateUtils;
import io.easycourse.www.easycourse.components.main.chat.ChatRecyclerViewAdapter;

import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

/**
 * Created by nrinehart on 12/22/16.
 */

public class IncomingChatTextViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "IncomingChatTextViewHol";

    private AppCompatActivity activity;

    @BindView(io.easycourse.www.easycourse.R.id.linearIncomingChatCell)
    LinearLayout incomingLinearLayout;
    @BindView(io.easycourse.www.easycourse.R.id.textViewIncomingTextTime)
    TextView incomingTime;
    @BindView(io.easycourse.www.easycourse.R.id.imageViewIncomingTextImage)
    ImageView incomingImageView;
    @BindView(io.easycourse.www.easycourse.R.id.textViewIncomingTextName)
    TextView incomingName;
    @BindView(io.easycourse.www.easycourse.R.id.textViewIncomingTextMessage)
    TextView incomingMessage;

    public IncomingChatTextViewHolder(View itemView, AppCompatActivity activity) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        Linkify.addLinks(incomingMessage, Linkify.ALL);
        this.activity = activity;
    }

    public void setupView(final Message message, Message prevMessage, User curUser, Realm realm, final Context context, ChatRecyclerViewAdapter chatRecyclerViewAdapter) {
        String reportDateIncoming = DateUtils.getTimeString(message, prevMessage);
        if (reportDateIncoming != null) {
            incomingTime.setVisibility(View.VISIBLE);
            incomingTime.setText(reportDateIncoming);
        } else {
            incomingTime.setVisibility(View.GONE);
        }

        User thisUser = User.getUserFromRealm(realm, message.getSenderId());

        if (thisUser != null) {
            try {
                if (thisUser.getProfilePictureUrl() != null)
                    Picasso.with(context)
                            .load(curUser.getProfilePictureUrl()).resize(36, 36).centerInside()
                            .placeholder(io.easycourse.www.easycourse.R.drawable.ic_person_black_24px)
                            .into(incomingImageView);


            } catch (NullPointerException e) {
                Log.e(TAG, e.toString());
            }
            incomingName.setText(thisUser.getUsername());
            incomingMessage.setText(message.getText());

            incomingLinearLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    return showPopup(incomingLinearLayout, message, context);
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
                    case io.easycourse.www.easycourse.R.id.itemPopupCopy:
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
        inflater.inflate(io.easycourse.www.easycourse.R.menu.chat_message_popup, popup.getMenu());
        popup.show();
        return false;
    }
}