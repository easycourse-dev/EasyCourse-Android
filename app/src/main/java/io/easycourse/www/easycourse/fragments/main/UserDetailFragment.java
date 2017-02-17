package io.easycourse.www.easycourse.fragments.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.easycourse.www.easycourse.R;
import io.easycourse.www.easycourse.models.main.User;
import io.easycourse.www.easycourse.utils.APIFunctions;
import io.easycourse.www.easycourse.utils.JSONUtils;
import io.realm.Realm;
import io.socket.client.Ack;

/**
 * Created by nrinehart on 2/15/17.
 */

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

    private User user;

    public UserDetailFragment() {
    }

    public static UserDetailFragment newInstance(User user) {
        UserDetailFragment userdetailFragment = new UserDetailFragment();
        userdetailFragment.user = user;
        return userdetailFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_userdetail, container, false);
        ButterKnife.bind(this, v);


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

                Glide.with(UserDetailFragment.this)
                        .load(user.getProfilePicture())
                        .centerCrop()
                        .placeholder(R.drawable.ic_account_circle_black_48dp)
                        .crossFade()
                        .into(userDetailCardImage);


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
        if (user == null) return;
        try {
            socketIO.getUserInfoJson(user.getId(), new Ack() {
                @Override
                public void call(Object... args) {
                    JSONObject jsonObject = (JSONObject) args[0];
                    saveUser(jsonObject);
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
            JSONObject userObj = null;
            String userIdString = "";
            String displayNameString = "";
            String avatarUrlString = "";
            String emailString = "";
            String universityId = "";

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

                Realm realm = Realm.getDefaultInstance();
                user = realm.copyToRealmOrUpdate(otherUser);
                realm.close();
            } catch (JSONException | NullPointerException e) {
                Log.e(TAG, "saveUser: ", e);
            }
            setupViews();
        }
    }

    private void messageUser() {
    }


    private void showReportUserDialog() {
        new MaterialDialog.Builder(getContext())
                .title("Report User?")
                .content("Enter a reason:")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(null, null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        reportUser(input.toString());
                    }
                }).show();
    }

    private void reportUser(String input) {
        try {
            APIFunctions.reportUser(getContext(), user.getId(), input, null);
        } catch (JSONException | UnsupportedEncodingException e) {
            Log.e(TAG, "reportUser: ", e);
        }
    }
}
