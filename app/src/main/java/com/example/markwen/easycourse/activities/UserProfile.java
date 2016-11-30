package com.example.markwen.easycourse.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.models.main.User;
import com.example.markwen.easycourse.utils.SocketIO;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.realm.Realm;

/**
 * Created by nisarg on 28/11/16.
 */

public class UserProfile extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;
    TextView textViewUsername;
    EditText editTextUsername;
    ImageButton editUsernameButton;
    FloatingActionButton saveChangesButton;
    FloatingActionButton editAvatarButton;

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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        try {
            socket = new SocketIO(this, getApplicationContext());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        textViewUsername = (TextView) findViewById(R.id.textViewUsername);
        editTextUsername = (EditText) findViewById(R.id.editTextUsername);
        saveChangesButton = (FloatingActionButton) findViewById(R.id.saveChangesButton);
        editAvatarButton = (FloatingActionButton) findViewById(R.id.editAvatarButton);
        saveChangesButton.hide();

        editUsernameButton = (ImageButton) findViewById(R.id.editUsernameButton);
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
                                activity = (Activity)context;
                            }
                            context = ((ContextWrapper)context).getBaseContext();
                        }

                        ActivityCompat.requestPermissions(activity,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                } else{
                    openGallery();
                }


            }
        });


    }

    public void toggleProfileEdit(){
        isInEditMode = !isInEditMode;
        if(isInEditMode){
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

    }
    /**
     * helper to retrieve the path of an image URI
     */
    public String getImagePath(Uri uri){
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":")+1);
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

    private void openGallery(){
        if (Build.VERSION.SDK_INT <19){
            Intent intent = new Intent();
            intent.setType("image/jpeg");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"),GALLERY_INTENT_CALLED);
        } else {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/jpeg");
            startActivityForResult(intent, GALLERY_KITKAT_INTENT_CALLED);
        }
    }
}
