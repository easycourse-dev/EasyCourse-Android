package com.example.markwen.easycourse.components.main.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
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
import io.realm.Realm;

/**
 * Created by nrinehart on 12/22/16.
 */

public class IncomingChatPictureViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "IncomingChatPictureView";

    @BindView(R.id.linearIncomingChatCell)
    public LinearLayout incomingLinearLayout;
    @BindView(R.id.textViewIncomingTextTime)
    public TextView incomingTime;
    @BindView(R.id.imageViewIncomingTextImage)
    public ImageView incomingImageView;
    @BindView(R.id.textViewIncomingTextName)
    public TextView incomingName;
    @BindView(R.id.textViewIncomingTextMessage)
    public TextView incomingMessage;

    protected IncomingChatPictureViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void setupView(Message message, Message prevMessage, User curUser, Realm realm, Context context, ChatRecyclerViewAdapter chatRecyclerViewAdapter) {

        String reportDateOutgoing = DateUtils.getTimeString(message, prevMessage);
        if (reportDateOutgoing != null) {
            incomingPicTime.setVisibility(View.VISIBLE);
            incomingPicTime.setText(reportDateOutgoing);
        } else {
            incomingPicTime.setVisibility(View.GONE);
        }

        User thisUser = User.getUserFromRealm(realm, message.getSenderId());


        if (thisUser != null) {
            try {
                if (thisUser.getProfilePictureUrl() != null)
                    Picasso.with(context)
                            .load(curUser.getProfilePictureUrl()).resize(36, 36).centerInside()
                            .placeholder(R.drawable.ic_person_black_24px)
                            .into(incomingPicUserView);

                if (!message.getImageUrl().isEmpty()) {
                    Picasso.with(context)
                            .load(message.getImageUrl()).resize(36, 36).centerInside()
                            .into(incomingPicImageView);
                }


            } catch (NullPointerException e) {
                Log.e(TAG, e.toString());
            }
            incomingPicName.setText(thisUser.getUsername());
//                    outgoingViewHolder.outgoingpicme.setText(message.getText());
            //TODO: add click listener to fullsize image with animation
        }
    }
}
