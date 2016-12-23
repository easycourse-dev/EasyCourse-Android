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
import io.realm.Realm;

/**
 * Created by nrinehart on 12/22/16.
 */

public class IncomingChatTextViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "IncomingChatTextViewHol";

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

    public IncomingChatTextViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        Linkify.addLinks(incomingMessage, Linkify.ALL);
    }

    public void setupView(Message message, Message prevMessage, User curUser, Realm realm, Context context, ChatRecyclerViewAdapter chatRecyclerViewAdapter) {
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
                            .placeholder(R.drawable.ic_person_black_24px)
                            .into(incomingImageView);


            } catch (NullPointerException e) {
                Log.e(TAG, e.toString());
            }
            incomingName.setText(thisUser.getUsername());
            incomingMessage.setText(message.getText());
        }
    }
}