package io.easycourse.www.easycourse.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.easycourse.www.easycourse.R;
import io.easycourse.www.easycourse.models.main.Language;
import io.easycourse.www.easycourse.utils.BitmapUtils;
import io.easycourse.www.easycourse.utils.asyntasks.CompressImageTask;
import io.realm.Realm;
import io.socket.client.Ack;

/**
 * Created by nisarg on 28/11/16.
 */

public class UserProfileActivity extends BaseActivity {

    private static final String TAG = "UserProfileActivity";

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;

    @BindView(R.id.toolbarUserProfile)
    Toolbar toolbar;
    @BindView(R.id.textViewUsername)
    TextView textViewUsername;
    @BindView(R.id.editTextUsername)
    EditText editTextUsername;
    @BindView(R.id.saveChangesButton)
    FloatingActionButton saveChangesButton;
    @BindView(R.id.editUsernameButton)
    ImageButton editUsernameButton;
    @BindView(R.id.editAvatarButton)
    FloatingActionButton editAvatarButton;
    @BindView(R.id.avatarImage)
    CircleImageView avatarImage;
    //    @BindView(R.id.userProfileLanguageView)
//    RecyclerView languageView;
//    @BindView(R.id.userProfileLanguageLabel)
//    TextView languageLabel;
    @BindView(R.id.userProfileProgressBar)
    ProgressBar profileProgressBar;

    boolean isInEditMode = false;


//    LanguageRecyclerViewAdapter languageAdapter;

//    RealmList<Language> userLanguages;
//    RealmList<Language> allLanguages;

    private static final int GALLERY_INTENT_CALLED = 1;
    private static final int GALLERY_KITKAT_INTENT_CALLED = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userprofile);

        //Binds all the views
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserProfileActivity.this.onBackPressed();
            }
        });


        saveChangesButton.hide();

        editUsernameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleProfileEdit();
            }
        });


        if (currentUser != null) {
            BitmapUtils.loadImage(this, avatarImage, currentUser.getProfilePicture(), currentUser.getProfilePictureUrl(), R.drawable.ic_person_black_24px);
            textViewUsername.setText(currentUser.getUsername());
            editTextUsername.setText(currentUser.getUsername());
        }

//        languageLabel.setText("Chosen language(s):");

        saveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                try {
                    socketIO.syncUser(editTextUsername.getText().toString(), null, Language.getCheckedLanguageCodeArrayList(realm), new Ack() {
                        @Override
                        public void call(Object... args) {
                            JSONObject obj = (JSONObject) args[0];
                            if (obj.has("error")) {
                                try {
                                    Snackbar.make(view, obj.getString("error"), Snackbar.LENGTH_LONG).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Log.e("syncUser", obj.toString());
                            }
                        }
                    });
//                    userLanguages = languageAdapter.getCheckedLanguageList();
//                    languageAdapter.setCheckable(false);
//                    languageAdapter.notifyDataSetChanged();
//                    languageLabel.setText("Chosen language(s):");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                toggleProfileEdit();
            }
        });

        editAvatarButton.show();
        editAvatarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(view.getContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    Activity activity = new Activity();
                    Context context = view.getContext();
                    while (context instanceof ContextWrapper) {
                        if (context instanceof Activity) {
                            activity = (Activity) context;
                        }
                        context = ((ContextWrapper) context).getBaseContext();
                    }

                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                } else {
                    openGallery();
                }
            }
        });

        // Setup language recycler view
//        userLanguages = Language.getCheckedLanguages(realm);
//        allLanguages = Language.getCheckedLanguages(realm);
//        LinearLayoutManager roomsLayoutManager = new LinearLayoutManager(this);
//        roomsLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//        languageAdapter = new LanguageRecyclerViewAdapter(allLanguages);
//        languageAdapter.setCheckedLanguageList(userLanguages);
//        languageView.setHasFixedSize(true);
//        languageView.setLayoutManager(roomsLayoutManager);
//        languageView.addItemDecoration(new RecyclerViewDivider(this));
//        languageView.setAdapter(languageAdapter);
    }

    private void updateUserInfoOnScreen() {
        if (currentUser.getProfilePicture() != null && currentUser.getProfilePicture().length > 0) {
            Bitmap bm = BitmapFactory.decodeByteArray(currentUser.getProfilePicture(), 0, currentUser.getProfilePicture().length);
            avatarImage.setImageBitmap(bm);
        } else {
            avatarImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_account_circle_black_48dp));
        }
    }

    public void toggleProfileEdit() {
        isInEditMode = !isInEditMode;
        if (isInEditMode) {
            textViewUsername.setVisibility(View.GONE);
            editTextUsername.setVisibility(View.VISIBLE);
            saveChangesButton.show();
            editTextUsername.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
//            APIFunctions.getLanguages(this, new JsonHttpResponseHandler() {
//                @Override
//                public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
//                    allLanguages.clear();
//                    languageAdapter.setLanguageList(new RealmList<Language>());
//                    for (int i = 0; i < response.length(); i++) {
//                        try {
//                            allLanguages.add(new Language(
//                                    response.getJSONObject(i).getString("name"),
//                                    response.getJSONObject(i).getString("code"),
//                                    response.getJSONObject(i).getString("translation")
//                            ));
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    languageAdapter.setLanguageList(allLanguages);
//                    languageAdapter.setCheckable(true);
//                    languageAdapter.notifyDataSetChanged();
//                    languageLabel.setText("All languages: ");
//                }
//            });
            editUsernameButton.setVisibility(View.GONE);
        } else {
            textViewUsername.setText(editTextUsername.getText());
            editTextUsername.setVisibility(View.GONE);
            textViewUsername.setVisibility(View.VISIBLE);
            saveChangesButton.hide();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editTextUsername.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
//            languageAdapter.setCheckable(false);
//            allLanguages.clear();
//            allLanguages = Language.getCheckedLanguages(realm);
//            languageAdapter.setLanguageList(allLanguages);
//            languageAdapter.notifyDataSetChanged();
//            languageLabel.setText("Chosen language(s):");
            editUsernameButton.setVisibility(View.VISIBLE);
        }
    }

    private void setProfilePicture(Uri uri) {
        profileProgressBar.setVisibility(View.VISIBLE);

        BitmapUtils.compressBitmap(uri, this, new CompressImageTask.OnCompressImageTaskCompleted() {
            @Override
            public void onTaskCompleted(final Bitmap bitmap, final byte[] bytes) {
                if (bytes != null) {
                    try {
                        Log.d(TAG, "onTaskCompleted: calling sync user");
                        socketIO.syncUser(null, bytes, null, new Ack() {
                            @Override
                            public void call(Object... args) {
                                setUserImage(bitmap, bytes);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        profileProgressBar.setVisibility(View.INVISIBLE);
                                    }
                                });
                            }
                        });
                    } catch (JSONException e) {
                        Log.e(TAG, "onTaskCompleted: ", e);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                profileProgressBar.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                }
            }

            @Override
            public void onTaskFailed() {

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;
        if (null == data) return;
        Uri originalUri = data.getData();
        if (originalUri != null) setProfilePicture(originalUri);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery();

                }
                return;
            }
        }
    }

    private void openGallery() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_INTENT_CALLED);
        } else {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, GALLERY_KITKAT_INTENT_CALLED);
        }
    }

    private void setUserImage(final Bitmap image, final byte[] profile) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                synchronized (this) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            avatarImage.setImageBitmap(image);
                            Realm tempRealm = Realm.getDefaultInstance();
                            tempRealm.beginTransaction();
                            currentUser.setProfilePicture(profile);
                            tempRealm.commitTransaction();
                            tempRealm.close();
                        }
                    });
                }
            }
        };
        thread.start();
    }
}