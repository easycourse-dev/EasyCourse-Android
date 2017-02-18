package io.easycourse.www.easycourse.components.main.chat.viewholders;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.easycourse.www.easycourse.R;
import io.easycourse.www.easycourse.components.main.chat.ChatRecyclerViewAdapter;
import io.easycourse.www.easycourse.fragments.main.ChatImageViewFragment;
import io.easycourse.www.easycourse.models.main.Message;
import io.easycourse.www.easycourse.models.main.User;
import io.easycourse.www.easycourse.utils.BitmapUtils;
import io.easycourse.www.easycourse.utils.DateUtils;


public class OutgoingChatPictureViewHolder extends RecyclerView.ViewHolder implements BaseChatViewHolder {

//    private static final String TAG = "OutgoingChatPictureView";

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

    @Override
    public void setupView(final Message message, Message prevMessage, User curUser, String roomId, Context context, ChatRecyclerViewAdapter chatRecyclerViewAdapter) {
        String reportDateOutgoing = DateUtils.getTimeString(message, prevMessage);
        if (reportDateOutgoing != null) {
            outgoingPicTime.setVisibility(View.VISIBLE);
            outgoingPicTime.setText(reportDateOutgoing);
        } else {
            outgoingPicTime.setVisibility(View.GONE);
        }


        if (curUser != null) {

            BitmapUtils.loadImage(context, outgoingPicUserView, curUser.getProfilePicture(), curUser.getProfilePictureUrl(), R.drawable.ic_person_black_24px);

            BitmapUtils.loadImage(context, outgoingPicImageView, message.getImageData(), message.getImageUrl(), R.drawable.ic_photo_black_24dp);

            outgoingPicName.setText(curUser.getUsername());
            outgoingPicImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager fragmentManager = activity.getSupportFragmentManager();
                    fragmentManager.executePendingTransactions();
                    ChatImageViewFragment fragment = ChatImageViewFragment.newInstance(message.getImageUrl(), message.getImageData());
                    fragmentManager
                            .beginTransaction()
                            .add(android.R.id.content, fragment, message.getImageUrl())
                            .addToBackStack(fragment.getClass().getSimpleName())
                            .commit();

                }
            });
        }
    }
}
