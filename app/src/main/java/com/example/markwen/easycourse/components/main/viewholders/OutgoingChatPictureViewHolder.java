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

/**
 * Created by nrinehart on 12/22/16.
 */

public class OutgoingChatPictureViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "OutgoingChatPictureView";
    
    @BindView(R.id.linearOutgoingPicCell)
    public LinearLayout outgoingPicLinearLayout;
    @BindView(R.id.textViewOutgoingPicTime)
    public TextView outgoingPicTime;
    @BindView(R.id.imageViewOutgoingUserImage)
    public  ImageView outgoingPicUserView;
    @BindView(R.id.textViewOutgoingPicName)
    public TextView outgoingPicName;
    @BindView(R.id.imageViewOutgoingImage)
    public ImageView outgoingPicImageView;


    public OutgoingChatPictureViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void setupView(Message message, Message prevMessage, User curUser, Context context, ChatRecyclerViewAdapter chatRecyclerViewAdapter) {
        String reportDateOutgoing = DateUtils.getTimeString(message, prevMessage);
        if (reportDateOutgoing != null) {
            outgoingPicTime.setVisibility(View.VISIBLE);
            outgoingPicTime.setText(reportDateOutgoing);
        } else {
            outgoingPicTime.setVisibility(View.GONE);
        }

        if (curUser != null) {
            try {
                if (!curUser.getProfilePictureUrl().isEmpty())
                    Picasso.with(context)
                            .load(curUser.getProfilePictureUrl()).resize(36, 36).centerInside()
                            .placeholder(R.drawable.ic_person_black_24px)
                            .into(outgoingPicUserView);

                if (!message.getImageUrl().isEmpty()) {
                    Picasso.with(context)
                            .load(message.getImageUrl()).resize(36, 36).centerInside()
                            .into(outgoingPicImageView);
                }


            } catch (NullPointerException e) {
                Log.e(TAG, e.toString());
            }
            outgoingPicName.setText(curUser.getUsername());
//                    outgoingpicme.setText(message.getText());
            //TODO: add click listner to fullsize image with animation
        }

//                outgoingPicLinearLayout.setOnLongClickListener(new View.OnLongClickListener() {
//                    @Override
//                    public boolean onLongClick(View view) {
//                        return showPopup(outgoingPicLinearLayout, outgoingMessage, message);
//                    }
//                });
    }
}
