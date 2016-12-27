package com.example.markwen.easycourse.fragments.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.activities.SignupLoginActivity;
import com.example.markwen.easycourse.activities.UserProfileActivity;
import com.example.markwen.easycourse.models.main.User;
import com.example.markwen.easycourse.utils.APIFunctions;
import com.facebook.login.LoginManager;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import io.realm.Realm;

/**
 * Created by Mark Wen on 10/18/2016.
 */

public class UserFragment extends Fragment {

    private static final String TAG = "UserFragment";

    Button logoutButton;
    ImageView avatarImage;
    TextView textViewUsername;
    RelativeLayout cardProfile;

    User user = new User();
    Realm realm;

    public UserFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        realm = Realm.getDefaultInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_user, container, false);

        textViewUsername = (TextView) v.findViewById(R.id.textViewUsername);
        avatarImage = (ImageView) v.findViewById(R.id.avatarImage);
        cardProfile = (RelativeLayout) v.findViewById(R.id.cardUserProfile);

        cardProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(), UserProfileActivity.class);
                Pair<View, String> p1 = Pair.create((View)avatarImage, "avatar");
                Pair<View, String> p2 = Pair.create((View)textViewUsername, "username");
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation((Activity) view.getContext(), p1, p2);
                startActivity(i, options.toBundle());
            }
        });

        SharedPreferences sharedPref = v.getContext().getSharedPreferences("EasyCourse", Context.MODE_PRIVATE);
        String currentUser = sharedPref.getString("currentUser", "");
        JSONObject currentUserObject;

        Realm.init(v.getContext());
        realm = Realm.getDefaultInstance();

        try {
            currentUserObject = new JSONObject(currentUser);
            Log.d(TAG, currentUserObject.toString());
            user = user.getByPrimaryKey(realm, currentUserObject.getString("_id"));
            textViewUsername.setText(user.getUsername());
            if (user.getProfilePicture() != null) {
                Bitmap bm = BitmapFactory.decodeByteArray(user.getProfilePicture(), 0, user.getProfilePicture().length);
                avatarImage.setImageBitmap(bm);
            } else {
                avatarImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_account_circle_black_48dp));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        logoutButton = (Button) v.findViewById(R.id.buttonLogout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout(v);
            }
        });

        return v;
    }

    private void logout(final View v) {

        APIFunctions.logout(getContext(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                // Remove userToken and currentUser in SharedPreferences
                SharedPreferences sharedPref = getActivity().getSharedPreferences("EasyCourse", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("userToken", null);
                editor.putString("currentUser", null);
                editor.apply();

                // Clear Realm
                try {
                    if (realm != null)
                        realm.beginTransaction();
                    realm.deleteAll();
                    realm.commitTransaction();
                }catch (NullPointerException e){
                    Log.e(TAG, "onSuccess: ", e);
                }

                Log.i("Token after logout:", sharedPref.getString("userToken", "can't get token"));

                // Go back to SignupLoginActivity
                startActivity(new Intent(getContext(), SignupLoginActivity.class));
                // Logout from Facebook
                LoginManager.getInstance().logOut();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                // Make a Snackbar to notify user with error
                Snackbar.make(v, "Log out failed because of " + res, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (realm != null)
            realm.close();
    }
}
