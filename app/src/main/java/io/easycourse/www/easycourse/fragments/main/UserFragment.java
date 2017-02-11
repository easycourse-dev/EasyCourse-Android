package io.easycourse.www.easycourse.fragments.main;

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
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.facebook.login.LoginManager;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;
import io.easycourse.www.easycourse.EasyCourse;
import io.easycourse.www.easycourse.R;
import io.easycourse.www.easycourse.activities.CourseManagementActivity;
import io.easycourse.www.easycourse.activities.MainActivity;
import io.easycourse.www.easycourse.activities.MyCoursesActivity;
import io.easycourse.www.easycourse.activities.SignupLoginActivity;
import io.easycourse.www.easycourse.activities.UserProfileActivity;
import io.easycourse.www.easycourse.models.main.User;
import io.easycourse.www.easycourse.utils.APIFunctions;
import io.easycourse.www.easycourse.utils.BitmapUtils;
import io.easycourse.www.easycourse.utils.ExternalLinkUtils;
import io.easycourse.www.easycourse.utils.SocketIO;
import io.realm.Realm;
import io.socket.client.Ack;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by Mark Wen on 10/18/2016.
 */

public class UserFragment extends BaseFragment {

    private static final String TAG = "UserFragment";


    @BindView(R.id.buttonLogout)
    Button logoutButton;
    @BindView(R.id.textViewUsername)
    TextView textViewUsername;
    @BindView(R.id.avatarImage)
    ImageView avatarImage;
    @BindView(R.id.cardUserProfile)
    RelativeLayout cardProfile;
    @BindView(R.id.UserFragmentCourseManageView)
    RelativeLayout cardCourses;
    @BindView(R.id.joinUsCard)
    RelativeLayout joinUsCard;
    @BindView(R.id.UserFragmentCourseManageCard)
    CardView courseManageCard;
    @BindView(R.id.TermsPrivacyCard)
    RelativeLayout termsCard;
    @BindView(R.id.surveyCard)
    RelativeLayout surveyCard;


    public UserFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_user, container, false);

        ButterKnife.bind(this, v);

        setupOnClickListeners();

        setupUserViews();

        return v;
    }

    private void setupUserViews() {
        if (currentUser != null) {
            textViewUsername.setText(currentUser.getUsername());

            if (currentUser.getProfilePicture() != null) {
                Glide.with(this)
                        .load(currentUser.getProfilePicture())
                        .asBitmap()
                        .into(avatarImage);
            } else {
                if (currentUser.getProfilePictureUrl() != null) {
                    Glide.with(this)
                            .load(currentUser.getProfilePictureUrl())
                            .asBitmap()
                            .listener(new RequestListener<String, Bitmap>() {
                                @Override
                                public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                    if (resource == null) return false;
                                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                    resource.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                    byte[] byteArray = stream.toByteArray();
                                    if (byteArray == null) return false;
                                    Realm tempRealm = Realm.getDefaultInstance();
                                    User currentUser = User.getCurrentUser(getContext(), tempRealm);
                                    tempRealm.beginTransaction();
                                    currentUser.setProfilePicture(byteArray);
                                    tempRealm.commitTransaction();
                                    tempRealm.close();
                                    return false;
                                }
                            })
                            .into(avatarImage);
                } else {
                    avatarImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_account_circle_black_48dp, null));
                }
            }
        }
    }

    private void setupOnClickListeners() {
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
//                startActivity(new Intent(view.getContext(), CourseManagementActivity.class));
                Intent intent = new Intent(getContext(), MyCoursesActivity.class);
                startActivity(intent);
            }
        });

        surveyCard.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                ExternalLinkUtils.OpenLinkInChrome("https://goo.gl/forms/ipUtAuM0onOvuyX63", getContext());
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

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutDialog(v);
            }
        });

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
                    editor.putString("universityId", null);
                    editor.apply();

                    // Clear Realm
                    Realm tempRealm = Realm.getDefaultInstance();
                    tempRealm.beginTransaction();
                    tempRealm.deleteAll();
                    tempRealm.commitTransaction();
                    tempRealm.close();

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
                editor.putString("universityId", null);
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


    @Override
    public void onResume() {
        super.onResume();
        getActivity().getSupportFragmentManager().beginTransaction().detach(this).attach(this).commit();
        if (currentUser != null && currentUser.getProfilePicture() != null) {
            setupUserViews();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (realm != null)
            realm.close();
    }

}