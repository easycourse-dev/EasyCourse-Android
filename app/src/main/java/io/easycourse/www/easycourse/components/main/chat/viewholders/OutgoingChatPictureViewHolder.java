package io.easycourse.www.easycourse.components.main.chat.viewholders;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.easycourse.www.easycourse.R;
import io.easycourse.www.easycourse.fragments.main.ChatImageViewFragment;
import io.easycourse.www.easycourse.components.main.chat.ChatRecyclerViewAdapter;
import io.easycourse.www.easycourse.models.main.Message;
import io.easycourse.www.easycourse.models.main.User;
import io.easycourse.www.easycourse.utils.BitmapUtils;
import io.easycourse.www.easycourse.utils.DateUtils;

import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by nrinehart on 12/22/16.
 */

public class OutgoingChatPictureViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "OutgoingChatPictureView";

    private AppCompatActivity activity;

    @BindView(R.id.linearOutgoingPicCell)
    LinearLayout outgoingPicLinearLayout;
    @BindView(R.id.textViewOutgoingPicTime)
    TextView outgoingPicTime;
    @BindView(R.id.imageViewOutgoingUserImage)
    ImageView outgoingPicUserView;
    @BindView(R.id.textViewOutgoingPicName)
    TextView outgoingPicName;
    @BindView(R.id.imageViewOutgoingImage)
    ImageView outgoingPicImageView;


    public OutgoingChatPictureViewHolder(View itemView, AppCompatActivity activity) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.activity = activity;
    }

    public void setupView(final Message message, Message prevMessage, User curUser, Context context, ChatRecyclerViewAdapter chatRecyclerViewAdapter) {
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
                            .load(curUser.getProfilePictureUrl()).resize(40, 40).centerInside()
                            .placeholder(R.drawable.ic_person_black_24px)
                            .into(outgoingPicUserView);

                if (!message.getImageUrl().isEmpty()) {
                    if (message.getImageData() != null) {
                        Bitmap bitmap = BitmapUtils.byteArrayToBitmap(message.getImageData());
                        outgoingPicImageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), false));
                    } else {
                        //TODO: placeholder image
                        Picasso.with(context)
                                .load(message.getImageUrl())
                                .into(outgoingPicImageView);
                    }
                }


            } catch (NullPointerException e) {
                Log.e(TAG, e.toString());
            }
            outgoingPicName.setText(curUser.getUsername());
            outgoingPicImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager fragmentManager = activity.getSupportFragmentManager();
                    fragmentManager.executePendingTransactions();
                    if (fragmentManager.findFragmentByTag(message.getImageUrl()) == null) {
                        ChatImageViewFragment fragment = ChatImageViewFragment.newInstance(message.getImageUrl(), message.getImageData());
                        fragmentManager
                                .beginTransaction()
                                .add(android.R.id.content, fragment, message.getImageUrl())
                                .addToBackStack(fragment.getClass().getSimpleName())
                                .commit();
                    }
                }
            });
        }
    }
}
