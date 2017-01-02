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

import io.easycourse.www.easycourse.utils.DateUtils;
import io.easycourse.www.easycourse.components.main.chat.ChatRecyclerViewAdapter;
import io.easycourse.www.easycourse.models.main.Message;
import io.easycourse.www.easycourse.models.main.User;

import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by nrinehart on 12/22/16.
 */

public class OutgoingChatTextViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "OutgoingChatTextViewHol";

    private AppCompatActivity activity;


    @BindView(io.easycourse.www.easycourse.R.id.linearOutgoingChatCell)
    LinearLayout outgoingLinearLayout;
    @BindView(io.easycourse.www.easycourse.R.id.textViewOutgoingTextTime)
    TextView outgoingTime;
    @BindView(io.easycourse.www.easycourse.R.id.imageViewOutgoingTextImage)
    ImageView outgoingImageView;
    @BindView(io.easycourse.www.easycourse.R.id.textViewOutgoingTextName)
    TextView outgoingName;
    @BindView(io.easycourse.www.easycourse.R.id.textViewOutgoingTextMessage)
    TextView outgoingMessage;

    public OutgoingChatTextViewHolder(View itemView, AppCompatActivity activity) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        Linkify.addLinks(outgoingMessage, Linkify.ALL);
        this.activity = activity;
    }

    public void setupView(final Message message, Message prevMessage, User curUser, final Context context, final ChatRecyclerViewAdapter adapter) {
        String reportDateOutgoing = DateUtils.getTimeString(message, prevMessage);
        if (reportDateOutgoing != null) {
            outgoingTime.setVisibility(View.VISIBLE);
            outgoingTime.setText(reportDateOutgoing);
        } else {
            outgoingTime.setVisibility(View.GONE);
        }

        if (curUser != null) {
            try {
                if (!curUser.getProfilePictureUrl().isEmpty())
                    Picasso.with(context)
                            .load(curUser.getProfilePictureUrl()).resize(36, 36).centerInside()
                            .placeholder(io.easycourse.www.easycourse.R.drawable.ic_person_black_24px)
                            .into(outgoingImageView);


            } catch (NullPointerException e) {
                Log.e(TAG, e.toString());
            }
            outgoingName.setText(curUser.getUsername());
            outgoingMessage.setText(message.getText());

        }

        outgoingLinearLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return showPopup(outgoingLinearLayout, message, context);
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