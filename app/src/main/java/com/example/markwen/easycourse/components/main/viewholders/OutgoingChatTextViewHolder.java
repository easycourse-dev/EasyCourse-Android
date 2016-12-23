package com.example.markwen.easycourse.components.main.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.components.main.ChatRecyclerViewAdapter;
import com.example.markwen.easycourse.models.main.Message;
import com.example.markwen.easycourse.models.main.User;
import com.example.markwen.easycourse.utils.DateUtils;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by nrinehart on 12/22/16.
 */

public class OutgoingChatTextViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "OutgoingChatTextViewHol";

    @BindView(R.id.linearOutgoingChatCell)
    public LinearLayout outgoingLinearLayout;
    @BindView(R.id.textViewOutgoingTextTime)
    public TextView outgoingTime;
    @BindView(R.id.imageViewOutgoingTextImage)
    public ImageView outgoingImageView;
    @BindView(R.id.textViewOutgoingTextName)
    public TextView outgoingName;
    @BindView(R.id.textViewOutgoingTextMessage)
    public TextView outgoingMessage;

    public OutgoingChatTextViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        Linkify.addLinks(outgoingMessage, Linkify.ALL);
    }

    public void setupView(final Message message, Message prevMessage, User curUser, Context context, final ChatRecyclerViewAdapter adapter) {
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
                            .placeholder(R.drawable.ic_person_black_24px)
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
                return adapter.showPopup(outgoingLinearLayout, outgoingMessage, message);
            }
        });
    }
}