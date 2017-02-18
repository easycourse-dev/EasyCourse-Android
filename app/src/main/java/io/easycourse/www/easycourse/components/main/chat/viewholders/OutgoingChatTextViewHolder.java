package io.easycourse.www.easycourse.components.main.chat.viewholders;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.util.Linkify;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.easycourse.www.easycourse.R;
import io.easycourse.www.easycourse.components.main.chat.ChatRecyclerViewAdapter;
import io.easycourse.www.easycourse.models.main.Message;
import io.easycourse.www.easycourse.models.main.User;
import io.easycourse.www.easycourse.utils.BitmapUtils;
import io.easycourse.www.easycourse.utils.DateUtils;
import io.realm.Realm;


public class OutgoingChatTextViewHolder extends RecyclerView.ViewHolder implements BaseChatViewHolder {

//    private static final String TAG = "OutgoingChatTextViewHol";

    private AppCompatActivity activity;


    @BindView(R.id.linearOutgoingChatCell)
    LinearLayout outgoingLinearLayout;
    @BindView(R.id.textViewOutgoingTextTime)
    TextView outgoingTime;
    @BindView(R.id.imageViewOutgoingTextImage)
    ImageView outgoingImageView;
    @BindView(R.id.textViewOutgoingTextName)
    TextView outgoingName;
    @BindView(R.id.textViewOutgoingTextMessage)
    TextView outgoingMessage;

    private boolean timeVisible;

    public OutgoingChatTextViewHolder(View itemView, AppCompatActivity activity) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        Linkify.addLinks(outgoingMessage, Linkify.ALL);
        this.activity = activity;
    }

    @Override
    public void setupView(final Message message, Message prevMessage, User curUser, String roomId, final Context context, ChatRecyclerViewAdapter chatRecyclerViewAdapter) {
        Realm tempRealm = Realm.getDefaultInstance();
        final String reportDateOutgoing = DateUtils.getTimeString(message, prevMessage);
        if (reportDateOutgoing != null) {
            outgoingTime.setVisibility(View.VISIBLE);
            outgoingTime.setText(reportDateOutgoing);
            timeVisible = true;
        } else {
            outgoingTime.setVisibility(View.GONE);
            timeVisible = false;
        }

        if (!message.isSuccessSent())
            outgoingMessage.setBackground(ContextCompat.getDrawable(context, R.drawable.cell_message_unsent));
        else
            outgoingMessage.setBackground(ContextCompat.getDrawable(context, R.drawable.cell_message_sent));

        if (curUser != null) {
            curUser = User.getCurrentUser(activity, tempRealm);
            if (curUser != null) {

                BitmapUtils.loadImage(context, outgoingImageView, curUser.getProfilePicture(), curUser.getProfilePictureUrl(), R.drawable.ic_person_black_24px);

                outgoingName.setText(curUser.getUsername());
                outgoingMessage.setText(message.getText());
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
                    if (reportDateOutgoing == null) return;
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
        tempRealm.close();
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