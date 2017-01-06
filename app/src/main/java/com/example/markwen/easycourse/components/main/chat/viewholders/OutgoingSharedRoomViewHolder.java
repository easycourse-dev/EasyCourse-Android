package com.example.markwen.easycourse.components.main.chat.viewholders;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.activities.CourseDetailsActivity;
import com.example.markwen.easycourse.components.main.chat.ChatRecyclerViewAdapter;
import com.example.markwen.easycourse.models.main.Message;
import com.example.markwen.easycourse.models.main.User;
import com.example.markwen.easycourse.utils.DateUtils;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by nisarg on 5/1/17.
 */

public class OutgoingSharedRoomViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = "OutgoingSharedRoomView";

    private AppCompatActivity activity;

    @BindView(R.id.linearOutgoingChatCell)
    LinearLayout outgoingLinearLayout;
    @BindView(R.id.textViewOutgoingTextTime)
    TextView outgoingTime;
    @BindView(R.id.imageViewOutgoingTextImage)
    ImageView outgoingImageView;
    @BindView(R.id.textViewOutgoingTextName)
    TextView outgoingName;
    @BindView(R.id.relativeLayoutSharedRoomHolder)
    RelativeLayout sharedRoomHolder;
    @BindView(R.id.textViewChatRoomName)
    TextView textViewRoomName;


    private boolean timeVisible;

    public OutgoingSharedRoomViewHolder(View itemView, AppCompatActivity activity) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.activity = activity;
    }

    public void setupView(final Message message, Message prevMessage, User curUser, final Context context, final ChatRecyclerViewAdapter adapter) {
        String reportDateOutgoing = DateUtils.getTimeString(message, prevMessage);
        if (reportDateOutgoing != null) {
            outgoingTime.setVisibility(View.VISIBLE);
            outgoingTime.setText(reportDateOutgoing);
            timeVisible = true;
        } else {
            outgoingTime.setVisibility(View.GONE);
            timeVisible = false;
        }

//        if (!message.isSuccessSent())
//            outgoingMessage.setBackground(ContextCompat.getDrawable(context, R.drawable.cell_message_unsent));
//        else
//            outgoingMessage.setBackground(ContextCompat.getDrawable(context, R.drawable.cell_message_sent));

        if (curUser != null) {
            if (curUser.getProfilePictureUrl() == null || curUser.getProfilePictureUrl().isEmpty()) {
                outgoingImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_person_black_24px));
            } else {
                Picasso.with(context).load(curUser.getProfilePictureUrl()).resize(36, 36).centerInside()
                        .placeholder(R.drawable.ic_person_black_24px)
                        .into(outgoingImageView);
                outgoingName.setText(curUser.getUsername());
                textViewRoomName.setText(message.getSharedRoom().getName());
                sharedRoomHolder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(context, CourseDetailsActivity.class);
                        i.putExtra("courseId", message.getSharedRoom().getCourseID());
                        context.startActivity(i);
                    }
                });
            }
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
