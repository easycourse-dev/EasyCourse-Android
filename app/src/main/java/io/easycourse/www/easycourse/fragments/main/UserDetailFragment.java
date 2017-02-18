package io.easycourse.www.easycourse.fragments.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;
import io.easycourse.www.easycourse.R;
import io.easycourse.www.easycourse.activities.ChatRoomActivity;
import io.easycourse.www.easycourse.models.main.Message;
import io.easycourse.www.easycourse.models.main.Room;
import io.easycourse.www.easycourse.models.main.User;
import io.easycourse.www.easycourse.utils.APIFunctions;
import io.easycourse.www.easycourse.utils.BitmapUtils;
import io.easycourse.www.easycourse.utils.JSONUtils;
import io.realm.RealmList;
import io.socket.client.Ack;

public class UserDetailFragment extends BaseFragment {

    private static final String TAG = "UserDetailFragment";

    @BindView(R.id.userDetailToolbar)
    Toolbar userDetailToolbar;
    @BindView(R.id.userDetailCardImage)
    CircleImageView userDetailCardImage;
    @BindView(R.id.userDetailCardName)
    TextView userDetailCardName;
    @BindView(R.id.userDetailMessage)
    Button userDetailMessage;
    @BindView(R.id.userDetailReport)
    TextView userDetailReport;

    private String userId;
    private User user;

    public UserDetailFragment() {
    }

    public static UserDetailFragment newInstance(String userId) {
        UserDetailFragment userdetailFragment = new UserDetailFragment();
        userdetailFragment.userId = userId;
        return userdetailFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_userdetail, container, false);
        ButterKnife.bind(this, v);

        user = realm.where(User.class).equalTo("id", userId).findFirst();

        setupViews();
        fetchUserDetails();


        return v;
    }

    private void setupViews() {
        if (user == null) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                userDetailToolbar.setTitle("Profile");

                BitmapUtils.loadImage(getContext(), userDetailCardImage, user.getProfilePicture(), user.getProfilePictureUrl(), R.drawable.ic_person_black_24px);

                userDetailCardName.setText(user.getUsername());

                userDetailMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        messageUser();
                    }
                });

                userDetailReport.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showReportUserDialog();
                    }
                });
            }
        });
    }

    private void fetchUserDetails() {
        try {
            socketIO.getUserInfoJson(userId, new Ack() {
                @Override
                public void call(Object... args) {
                    final JSONObject jsonObject = (JSONObject) args[0];
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            saveUser(jsonObject);
                        }
                    });
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "fetchUserDetails: ", e);
        }
    }

    private void saveUser(JSONObject jsonObject) {
        Log.d(TAG, "saveUser: " + jsonObject);
        if (jsonObject.has("error")) {
            Log.e(TAG, jsonObject.toString());
        } else {
            JSONObject userObj;
            String userIdString;
            String displayNameString;
            String avatarUrlString;
            String emailString;
            String universityId;

            try {
                userObj = jsonObject.getJSONObject("user");
                userIdString = (String) JSONUtils.checkIfJsonExists(userObj, "_id", null);
                displayNameString = (String) JSONUtils.checkIfJsonExists(userObj, "displayName", null);
                avatarUrlString = (String) JSONUtils.checkIfJsonExists(userObj, "avatarUrl", null);
                emailString = (String) JSONUtils.checkIfJsonExists(userObj, "email", null);
                universityId = (String) JSONUtils.checkIfJsonExists(userObj, "university", null);

                User otherUser = new User(
                        userIdString,
                        displayNameString,
                        null,
                        avatarUrlString,
                        emailString,
                        universityId);

                realm.beginTransaction();
                user = realm.copyToRealmOrUpdate(otherUser);
                realm.commitTransaction();
            } catch (JSONException | NullPointerException e) {
                Log.e(TAG, "saveUser: ", e);
            }
            setupViews();
        }
    }

    private void messageUser() {
        if (user == null) return;
        Room room = new Room(
                user.getId(),
                user.getUsername(),
                new RealmList<Message>(),
                0,
                false,
                null,
                null,
                null,
                new RealmList<>(currentUser, user),
                2,
                "<10",
                null,
                currentUser,
                false,
                false,
                true,
                true);

        realm.beginTransaction();
        room = realm.copyToRealmOrUpdate(room);
        realm.commitTransaction();
        Intent intent = new Intent(getContext(), ChatRoomActivity.class);
        intent.putExtra("roomId", room.getId());
        startActivity(intent);
    }


    private void showReportUserDialog() {
        new MaterialDialog.Builder(getContext())
                .title("Report User?")
                .content("Enter a reason:")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(null, null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        reportUser(input.toString());
                    }
                }).show();
    }

    private void reportUser(String input) {
        try {
            APIFunctions.reportUser(getContext(), user.getId(), input, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Toast.makeText(getContext(), user.getUsername() + " was reported.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                    Toast.makeText(getContext(), "Error reporting user! Try again later", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onFailure: " + res + statusCode, t);
                }
            });
        } catch (JSONException | UnsupportedEncodingException e) {
            Log.e(TAG, "reportUser: ", e);
        }
    }
}
