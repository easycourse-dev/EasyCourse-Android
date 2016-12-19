package com.example.markwen.easycourse.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.models.main.User;
import com.example.markwen.easycourse.utils.APIFunctions;
import com.example.markwen.easycourse.utils.SocketIO;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;
import io.realm.RealmChangeListener;

/**
 * Created by nisarg on 28/11/16.
 */

public class UserProfile extends AppCompatActivity {

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

    boolean isInEditMode = false;

    User user = new User();

    Realm realm;
    SocketIO socket;

    private static final int GALLERY_INTENT_CALLED = 1;
    private static final int GALLERY_KITKAT_INTENT_CALLED = 2;
    private String selectedImagePath;

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
        try {
            socket = new SocketIO(getApplicationContext());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        saveChangesButton.hide();

        editUsernameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                toggleProfileEdit();
            }
        });

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("EasyCourse", Context.MODE_PRIVATE);
        String currentUser = sharedPref.getString("currentUser", null);
        JSONObject currentUserObject;

        Realm.init(getApplicationContext());
        realm = Realm.getDefaultInstance();

        try {
            currentUserObject = new JSONObject(currentUser);
            Log.e("com.example.easycourse", currentUserObject.toString());
            user = user.getByPrimaryKey(realm, currentUserObject.getString("_id"));
            user.addChangeListener(new RealmChangeListener<User>() {
                @Override
                public void onChange(User user) {
                    updateUserInfoOnScreen();
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }

        textViewUsername.setText(user.getUsername());
        editTextUsername.setText(user.getUsername());

        saveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    socket.syncUser(editTextUsername.getText().toString(), null);
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


    }

    private void updateUserInfoOnScreen(){
        if (user.getProfilePicture().length > 0) {
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
        } else {
            textViewUsername.setText(editTextUsername.getText());
            editTextUsername.setVisibility(View.GONE);
            textViewUsername.setVisibility(View.VISIBLE);
            saveChangesButton.hide();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editTextUsername.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;
        if (null == data) return;
        Uri originalUri = null;
        if (requestCode == GALLERY_INTENT_CALLED) {
            originalUri = data.getData();
        } else if (requestCode == GALLERY_KITKAT_INTENT_CALLED) {
            originalUri = data.getData();
        }

        String path = getImagePath(originalUri);
        Log.e("com.example.easycourse", path);
        try {
            APIFunctions.uploadImage(getApplicationContext(), new File(path), "test123", "avatar", "", new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.e("com.example.easycourse", response.toString());
                    try {
                        socket.syncUser(null, response.getString("url"));
                    } catch (JSONException e) {
                        Log.e("com.example.easycourse", response.toString());
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                    Log.e("com.example.easycourse", res.toString());
                }
            });
        } catch (JSONException e) {
            Log.e("com.example.easycourse", e.toString());
        } catch (UnsupportedEncodingException e) {
            Log.e("com.example.easycourse", e.toString());
        } catch (FileNotFoundException e) {
            Log.e("com.example.easycourse", e.toString());
        }
    }

    /**
     * helper to retrieve the path of an image URI
     */
    public String getImagePath(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
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
