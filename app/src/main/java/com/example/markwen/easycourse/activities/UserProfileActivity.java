package com.example.markwen.easycourse.activities;

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
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.markwen.easycourse.EasyCourse;
import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.components.main.UserProfile.LanguageRecyclerViewAdapter;
import com.example.markwen.easycourse.components.signup.RecyclerViewDivider;
import com.example.markwen.easycourse.models.main.Language;
import com.example.markwen.easycourse.models.main.User;
import com.example.markwen.easycourse.utils.APIFunctions;
import com.example.markwen.easycourse.utils.BitmapUtils;
import com.example.markwen.easycourse.utils.SocketIO;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.socket.client.Ack;

/**
 * Created by nisarg on 28/11/16.
 */

public class UserProfileActivity extends AppCompatActivity {

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
    @BindView(R.id.userProfileLanguageView)
    RecyclerView languageView;
    @BindView(R.id.userProfileLanguageLabel)
    TextView languageLabel;

    boolean isInEditMode = false;

    User user = new User();

    Realm realm;
    SocketIO socket;
    LanguageRecyclerViewAdapter languageAdapter;

    RealmList<Language> userLanguages;

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
        socket = EasyCourse.getAppInstance().getSocketIO();

        saveChangesButton.hide();

        editUsernameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleProfileEdit();
            }
        });

        realm = Realm.getDefaultInstance();

        user = User.getCurrentUser(this, realm);
        user.addChangeListener(new RealmChangeListener<User>() {
            @Override
            public void onChange(User user) {
                updateUserInfoOnScreen();
            }
        });

        textViewUsername.setText(user.getUsername());
        editTextUsername.setText(user.getUsername());
        languageLabel.setText("Chosen language(s):");

        saveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    socket.syncUser(editTextUsername.getText().toString(), null, languageAdapter.getCheckedLanguageCodeArrayList(), new Ack() {
                        @Override
                        public void call(Object... args) {

                        }
                    });
                    userLanguages = languageAdapter.getCheckedLanguageList();
                    languageAdapter.setLanguageList(userLanguages, false);
                    languageAdapter.saveCheckedLanguages();
                    languageAdapter.notifyDataSetChanged();
                    languageLabel.setText("Chosen language(s):");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.e("com.example.easycourse", user.getUsername());
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
        userLanguages = Language.getCheckedLanguages(realm);
        LinearLayoutManager roomsLayoutManager = new LinearLayoutManager(this);
        roomsLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        languageAdapter = new LanguageRecyclerViewAdapter(userLanguages);
        languageAdapter.setCheckedLanguageList(userLanguages);
        languageView.setHasFixedSize(true);
        languageView.setLayoutManager(roomsLayoutManager);
        languageView.addItemDecoration(new RecyclerViewDivider(this));
        languageView.setAdapter(languageAdapter);
    }

    private void updateUserInfoOnScreen(){
        if (user.getProfilePicture() != null && user.getProfilePicture().length > 0) {
            Bitmap bm = BitmapFactory.decodeByteArray(user.getProfilePicture(), 0, user.getProfilePicture().length);
            avatarImage.setImageBitmap(bm);
        } else {
            avatarImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_account_circle_black_48dp));
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
            APIFunctions.getLanguages(this, new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                    RealmList<Language> allLanguages = new RealmList<>();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            allLanguages.add(new Language(
                                    response.getJSONObject(i).getString("code"),
                                    response.getJSONObject(i).getString("name"),
                                    response.getJSONObject(i).getString("translation")
                            ));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    languageAdapter.setLanguageList(allLanguages, true);
                    languageAdapter.notifyDataSetChanged();
                    languageLabel.setText("All languages: ");
                }
            });
        } else {
            textViewUsername.setText(editTextUsername.getText());
            editTextUsername.setVisibility(View.GONE);
            textViewUsername.setVisibility(View.VISIBLE);
            saveChangesButton.hide();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editTextUsername.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
            userLanguages.clear();
            userLanguages = Language.getCheckedLanguages(realm);
            languageAdapter.setLanguageList(userLanguages, false);
            languageAdapter.notifyDataSetChanged();
            languageLabel.setText("Chosen language(s):");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;
        if (null == data) return;
        Uri originalUri = null;
        Bitmap bitmap = null;
        if (requestCode == GALLERY_INTENT_CALLED) {
            originalUri = data.getData();
        } else if (requestCode == GALLERY_KITKAT_INTENT_CALLED) {
            originalUri = data.getData();
        }

        try {
            bitmap = BitmapUtils.getBitmapFromUri(originalUri, this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] byteArray = BitmapUtils.compressBitmapToBytes(bitmap, this, 50);
        try {
            socket.syncUser(null, byteArray, languageAdapter.getCheckedLanguageCodeArrayList(), new Ack() {
                @Override
                public void call(Object... args) {
                    
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
        if (Build.VERSION.SDK_INT < 19 || true) {
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
}
