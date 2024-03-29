package io.easycourse.www.easycourse.components.main.chat.viewholders;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.easycourse.www.easycourse.EasyCourse;
import io.easycourse.www.easycourse.R;
import io.easycourse.www.easycourse.activities.UserDetailActivity;
import io.easycourse.www.easycourse.fragments.main.ChatImageViewFragment;
import io.easycourse.www.easycourse.components.main.chat.ChatRecyclerViewAdapter;
import io.easycourse.www.easycourse.models.main.Message;
import io.easycourse.www.easycourse.models.main.User;
import io.easycourse.www.easycourse.utils.BitmapUtils;
import io.easycourse.www.easycourse.utils.DateUtils;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.socket.client.Ack;

/**
 * Created by nrinehart on 12/22/16.
 */

public class IncomingChatPictureViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "IncomingChatPictureView";

    private AppCompatActivity activity;


    @BindView(R.id.linearIncomingPicCell)
    LinearLayout incomingPicLinearLayout;
    @BindView(R.id.textViewIncomingPicTime)
    TextView incomingPicTime;
    @BindView(R.id.textViewIncomingPicName)
    TextView incomingPicName;
    @BindView(R.id.imageViewIncomingPicUserImage)
    ImageView incomingPicUserImage;
    @BindView(R.id.imageViewIncomingPicImage)
    ImageView incomingPicImageView;


    public IncomingChatPictureViewHolder(View itemView, AppCompatActivity activity) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.activity = activity;
    }

    public void setupView(final Message message, Message prevMessage, User curUser, final String roomId, Realm realm, final Context context, ChatRecyclerViewAdapter chatRecyclerViewAdapter) {

        String reportDateOutgoing = DateUtils.getTimeString(message, prevMessage);
        if (reportDateOutgoing != null) {
            incomingPicTime.setVisibility(View.VISIBLE);
            incomingPicTime.setText(reportDateOutgoing);
        } else {
            incomingPicTime.setVisibility(View.GONE);
        }

        User thisUser = User.getUserFromRealm(realm, message.getSenderId());
        if (thisUser == null) {
            try {
                EasyCourse.getAppInstance().getSocketIO().getUserInfoJson(message.getSenderId(), new Ack() {
                    @Override
                    public void call(Object... args) {
                        User thisUser = EasyCourse.getAppInstance().getSocketIO().parseUserJsonInfo((JSONObject) args[0]);
                        fillUserInfo(thisUser, roomId, context, message);
                    }
                });
            } catch (JSONException e) {
                Log.e(TAG, "setupView: ", e);
            }
        } else {
            fillUserInfo(thisUser, roomId, context, message);
        }


    }

    private void fillUserInfo(final User thisUser, final String roomId, final Context context, final Message message) {
        if (thisUser != null) {
            try {
                if (thisUser.getProfilePictureUrl() != null)
                    Picasso.with(context)
                            .load(thisUser.getProfilePictureUrl()).resize(36, 36).centerInside()
                            .placeholder(R.drawable.ic_person_black_24px)
                            .into(incomingPicUserImage);

                if (!message.getImageUrl().isEmpty()) {
                    if (message.getImageData() != null) {
                        Bitmap bitmap = BitmapUtils.byteArrayToBitmap(message.getImageData());
                        incomingPicImageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), false));
                    } else {
                        Picasso.with(context)
                                .load(message.getImageUrl())
                                .into(incomingPicImageView);
                    }
                }


            } catch (NullPointerException e) {
                Log.e(TAG, e.toString());
            }
            incomingPicName.setText(thisUser.getUsername());
            incomingPicUserImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, UserDetailActivity.class);
                    intent.putExtra("user", thisUser.getId());
                    context.startActivity(intent);
                }
            });
            incomingPicImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ChatImageViewFragment fragment = ChatImageViewFragment.newInstance(message.getImageUrl(), message.getImageData());
                    activity.getSupportFragmentManager()
                            .beginTransaction()
                            .add(android.R.id.content, fragment, message.getImageUrl())
                            .addToBackStack(fragment.getClass().getSimpleName())
                            .commit();
                }
            });
        }
    }

}
