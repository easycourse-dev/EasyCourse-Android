package com.example.markwen.easycourse.fragments.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.markwen.easycourse.EasyCourse;
import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.activities.CourseManagementActivity;
import com.example.markwen.easycourse.activities.SignupLoginActivity;
import com.example.markwen.easycourse.activities.UserProfileActivity;
import com.example.markwen.easycourse.models.main.User;
import com.example.markwen.easycourse.utils.APIFunctions;
import com.example.markwen.easycourse.utils.ExternalLinkUtils;
import com.example.markwen.easycourse.utils.SocketIO;
import com.facebook.login.LoginManager;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import cz.msebera.android.httpclient.Header;
import io.realm.Realm;
import io.socket.client.Ack;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by Mark Wen on 10/18/2016.
 */

public class UserFragment extends Fragment {

    private static final String TAG = "UserFragment";

    Button logoutButton;
    ImageView avatarImage;
    TextView textViewUsername;
    RelativeLayout cardProfile, cardCourses, joinUsCard, termsCard;
    CardView courseManageCard;

    User user = new User();
    Realm realm;
    SocketIO socketIO;

    public UserFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        realm = Realm.getDefaultInstance();
        socketIO = EasyCourse.getAppInstance().getSocketIO();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_user, container, false);

        textViewUsername = (TextView) v.findViewById(R.id.textViewUsername);
        avatarImage = (ImageView) v.findViewById(R.id.avatarImage);
        cardProfile = (RelativeLayout) v.findViewById(R.id.cardUserProfile);
        cardCourses = (RelativeLayout) v.findViewById(R.id.UserFragmentCourseManageView);
        joinUsCard = (RelativeLayout) v.findViewById(R.id.joinUsCard);
        courseManageCard = (CardView) v.findViewById(R.id.UserFragmentCourseManageCard);
        termsCard = (RelativeLayout) v.findViewById(R.id.TermsPrivacyCard);

        cardProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(), UserProfileActivity.class);
                Pair<View, String> p1 = Pair.create((View) avatarImage, "avatar");
                Pair<View, String> p2 = Pair.create((View) textViewUsername, "username");
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation((Activity) view.getContext(), p1, p2);
                startActivity(i, options.toBundle());
            }
        });

        cardCourses.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                startActivity(new Intent(view.getContext(), CourseManagementActivity.class));
            }
        });

        joinUsCard.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                ExternalLinkUtils.OpenLinkInChrome("https://docs.google.com/forms/d/e/1FAIpQLSeKu9p0Al-E9LAQyjeQw06KmXQQ1DyoJenH2_tRwO2sbhvA_g/viewform?c=0&w=1", getContext());
            }
        });

        termsCard.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                ExternalLinkUtils.OpenLinkInChrome("http://www.easycourse.io/docs", getContext());
            }
        });

        user = User.getCurrentUser(getActivity(), realm);
        textViewUsername.setText(user.getUsername());
        if (user.getProfilePicture() != null) {
            Bitmap bm = BitmapFactory.decodeByteArray(user.getProfilePicture(), 0, user.getProfilePicture().length);
            avatarImage.setImageBitmap(bm);
        } else {
            if (user.getProfilePictureUrl() != null) {
                try {
                    downloadImage(new URL(user.getProfilePictureUrl()));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            } else {
                avatarImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_account_circle_black_48dp, null));
            }
        }

        logoutButton = (Button) v.findViewById(R.id.buttonLogout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutDialog(v);
            }
        });

        return v;
    }

    private void logout(final View v) throws JSONException {

        socketIO.logout(new Ack() {
            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject) args[0];
                if (obj.has("success")) {
                    // Remove userToken and currentUser in SharedPreferences
                    SharedPreferences sharedPref = getActivity().getSharedPreferences("EasyCourse", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("userToken", null);
                    editor.putString("currentUser", null);
                    editor.apply();

                    // Clear Realm
                    Realm tempRealm = Realm.getDefaultInstance();
                    tempRealm.beginTransaction();
                    tempRealm.deleteAll();
                    tempRealm.commitTransaction();
                    tempRealm.close();

                    Log.i("Token after logout:", sharedPref.getString("userToken", "can't get token"));

                    // Go back to SignupLoginActivity
                    startActivity(new Intent(getContext(), SignupLoginActivity.class));
                    // Logout from Facebook
                    LoginManager.getInstance().logOut();
                } else {
                    // Make a Snackbar to notify user with error
                    try {
                        Snackbar.make(v, "Log out failed because of " + obj.getString("error"), Snackbar.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

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
                } catch (NullPointerException e) {
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

    private void showLogoutDialog(final View v) {
        MaterialDialog dialog = new MaterialDialog.Builder(getContext())
                .title("Log out")
                .content("Are you sure to log out? You will stop receiving messages from your friends.")
                .negativeText("No")
                .positiveText("Yes")
                .positiveColor(ResourcesCompat.getColor(getResources(), R.color.colorLogout, null))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        try {
                            logout(v);
                        } catch (JSONException e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                })
                .build();
        dialog.show();
    }

    private void downloadImage(final URL url){
        Thread thread = new Thread(){
            @Override
            public void run() {
                try  {
                    // Download image
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    Bitmap image = BitmapFactory.decodeStream(input);
                    // Convert image
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] byteArray = stream.toByteArray();
                    // Saving image
                    Realm tempRealm = Realm.getDefaultInstance();
                    User user = User.getCurrentUser(getApplicationContext(), tempRealm);
                    tempRealm.beginTransaction();
                    user.setProfilePicture(byteArray);
                    tempRealm.commitTransaction();
                    tempRealm.close();
                    // Setting image
                    setImageAfterDownload(image);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    private void setImageAfterDownload(final Bitmap image){
        Thread thread = new Thread(){
            @Override
            public void run() {
                synchronized (this) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            avatarImage.setImageBitmap(image);
                        }
                    });
                }
            }
        };
        thread.start();
    }
}
