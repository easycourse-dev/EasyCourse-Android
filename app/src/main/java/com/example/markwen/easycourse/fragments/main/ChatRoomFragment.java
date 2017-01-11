package com.example.markwen.easycourse.fragments.main;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.markwen.easycourse.EasyCourse;
import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.activities.ChatRoomActivity;
import com.example.markwen.easycourse.components.main.chat.ChatRecyclerViewAdapter;
import com.example.markwen.easycourse.models.main.Message;
import com.example.markwen.easycourse.models.main.Room;
import com.example.markwen.easycourse.models.main.User;
import com.example.markwen.easycourse.utils.BitmapUtils;
import com.example.markwen.easycourse.utils.SocketIO;
import com.example.markwen.easycourse.utils.asyntasks.CompressImageTask;
import com.example.markwen.easycourse.utils.asyntasks.DownloadImagesTask;
import com.example.markwen.easycourse.utils.eventbus.Event;
import com.squareup.otto.Subscribe;

import org.json.JSONException;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;


public class ChatRoomFragment extends Fragment {

    private static final String TAG = "ChatRoomFragment";
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 3;
    private static final int PERMISSION_DENIED = -1;
    private static final int CHOOSE_IMAGE_INTENT = 4;
    private static final int TAKE_IMAGE_INTENT = 5;

    private ChatRoomActivity activity;
    private Realm realm;
    private SocketIO socketIO;

    private Room currentRoom;
    private User currentUser;


    @BindView(R.id.chatRecyclerView)
    RecyclerView chatRecyclerView;
    @BindView(R.id.chatAddImageButton)
    ImageButton addImageButton;
    @BindView(R.id.chatMessageEditText)
    EditText messageEditText;
    @BindView(R.id.chatSendImageButton)
    ImageButton sendImageButton;

    @BindView(R.id.chatSendImageProgressBar)
    ProgressBar sendImageProgressBar;


    private ChatRecyclerViewAdapter chatRecyclerViewAdapter;
    private RealmResults<Message> messages;

    public ChatRoomFragment() {
    }

    //TODO: streamline new fragments so current user/realm not reinitiated
    public static ChatRoomFragment newInstance(Room currentRoom, User currentUser) {
        ChatRoomFragment fragment = new ChatRoomFragment();
        fragment.currentRoom = currentRoom;
        fragment.currentUser = currentUser;
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chat_room, container, false);
        ButterKnife.bind(this, v);
        activity = (ChatRoomActivity) getActivity();

        realm = Realm.getDefaultInstance();
        socketIO = EasyCourse.getAppInstance().getSocketIO();

        setupChatRecyclerView();
        setupOnClickListeners();

        DownloadImagesTask task = new DownloadImagesTask();
        task.execute();

        return v;
    }

    //TODO: private messages
    private void setupChatRecyclerView() {
        messages = realm.where(Message.class).equalTo("toRoom", currentRoom.getId()).findAllSorted("createdAt", Sort.ASCENDING);
        chatRecyclerViewAdapter = new ChatRecyclerViewAdapter(activity, messages);
        chatRecyclerView.setAdapter(chatRecyclerViewAdapter);
        chatRecyclerView.setHasFixedSize(true);
        LinearLayoutManager chatLinearManager = new LinearLayoutManager(activity);
        chatLinearManager.setOrientation(LinearLayoutManager.VERTICAL);
        chatLinearManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(chatLinearManager);
        messages.addChangeListener(new RealmChangeListener<RealmResults<Message>>() {
            @Override
            public void onChange(RealmResults<Message> element) {
                chatRecyclerView.smoothScrollToPosition(chatRecyclerViewAdapter.getItemCount());
            }
        });
    }


    private void setupOnClickListeners() {
        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageDialog();
            }
        });

        sendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupTextMessageToSend();
            }
        });

        messageEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled = false;
                if (i == EditorInfo.IME_ACTION_SEND) {
                    setupTextMessageToSend();
                    handled = true;
                }
                return handled;
            }
        });
    }

    private void sendImageDialog(final Uri uri) {
        Log.d(TAG, "sendImageDialog: " + uri);
        if (uri == null) {
            Toast.makeText(activity, "Image failed to load!", Toast.LENGTH_SHORT).show();
            return;
        }

        MaterialDialog dialog = new MaterialDialog.Builder(activity)
                .title("Send Image?")
                .titleColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
                .customView(R.layout.dialog_send_image, true)
                .positiveText("Send")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Log.d(TAG, uri.toString());
                        sendImageProgressBar.setVisibility(View.VISIBLE);
                        compressAndSendImage(uri);
                    }
                })
                .negativeText("Cancel")
                .negativeColor(ContextCompat.getColor(getContext(), R.color.colorLogout))
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.cancel();
                    }
                })
                .build();

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), uri);
            int scaleToUse = 50; // this will be our percentage
            int sizeY = height * scaleToUse / 100;
            int sizeX = bitmap.getWidth() * sizeY / bitmap.getHeight();
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, sizeX, sizeY, false);

            ImageView image = (ImageView) dialog.getCustomView().findViewById(R.id.image_view_send_dialog);
            image.setImageBitmap(scaled);


        } catch (Exception e) {
            Toast.makeText(activity, "Image failed to load!", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "sendImageDialog:", e);
        }
        dialog.show();
    }


    private void showImageDialog() {
        new MaterialDialog.Builder(activity)
                .items(R.array.addImageDialog)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        handleAddImage(which, view);
                    }
                })
                .show();
    }

    private void handleAddImage(int which, View view) {
        switch (which) {
            case 0:  // choose image
                int permissionCheck1 = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

                if (permissionCheck1 == PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                } else {
                    chooseImage();
                }

                break;
            case 1:  // take image
                takeImage();
                break;
        }
    }

    private void chooseImage() {
        if (Build.VERSION.SDK_INT < 19) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), CHOOSE_IMAGE_INTENT);
        } else {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, CHOOSE_IMAGE_INTENT);
        }
    }

    private void takeImage() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, TAKE_IMAGE_INTENT);
        }
    }


    private void setupTextMessageToSend() {
        String messageText = messageEditText.getText().toString();
        String fixed = messageText.replace("\\", "");
        if (!TextUtils.isEmpty(fixed)) {
            if (sendMessage(fixed, true, null, 0, 0)) {
                messageEditText.setText("");
                chatRecyclerViewAdapter.notifyDataSetChanged();
                chatRecyclerView.smoothScrollToPosition(chatRecyclerViewAdapter.getItemCount() + 1);
            }
        }
    }

    private void compressAndSendImage(final Uri uri) {
        BitmapUtils.compressBitmap(uri, currentRoom.getId(), getContext(), new CompressImageTask.OnCompressImageTaskCompleted() {
            @Override
            public void onTaskCompleted(Bitmap bitmap, byte[] bytes) {
                if (ChatRoomFragment.this.sendMessage(null, false, bytes, bitmap.getWidth(), bitmap.getHeight())) {
                    chatRecyclerViewAdapter.notifyDataSetChanged();
                    chatRecyclerView.smoothScrollToPosition(chatRecyclerViewAdapter.getItemCount() + 1);
                    picSent(true);
                }
                picSent(false);
            }

            @Override
            public void onTaskFailed() {
                picSent(false);
            }
        });
    }

    private void picSent(boolean wasSent) {
        sendImageProgressBar.setVisibility(View.GONE);
        //TODO: handle pic send failure
//        if (!wasSent) Toast.makeText(activity, "Failed to send pic!", Toast.LENGTH_SHORT).show();
    }


    private boolean sendMessage(String messageText, boolean isTextMessage, byte[] imageData, int imageWidth, int imageHeight) {
        try {
            //Receive message from socketIO
            if (this.currentRoom.isToUser()) {
                if (isTextMessage)
                    socketIO.sendMessage(messageText, null, null, this.currentRoom.getId(), null, 0, 0);
                else
                    socketIO.sendMessage(null, null, null, this.currentUser.getId(), imageData, imageWidth, imageHeight);
                User otherUser = Room.getOtherUserIfPrivate(currentRoom, currentUser, realm);
                if (otherUser == null) return false;
                if (isTextMessage) //To user text
                    socketIO.sendMessage(messageText, null, otherUser.getId(), null, null, 0, 0);
                else //To user pic
                    socketIO.sendMessage(null, null, otherUser.getId(), null, imageData, imageWidth, imageHeight);

            } else {
                if (isTextMessage)
                    socketIO.sendMessage(messageText, this.currentRoom.getId(), null, null, null, 0, 0);
                else
                    socketIO.sendMessage(null, this.currentRoom.getId(), null, null, imageData, imageWidth, imageHeight);
                if (isTextMessage) //To room text
                    socketIO.sendMessage(messageText, this.currentRoom.getId(), null, null, null, 0, 0);
                else    //To room pic
                    socketIO.sendMessage(null, this.currentRoom.getId(), null, null, imageData, imageWidth, imageHeight);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "sendMessage: error");
            return false;
        }
        socketIO.syncUser();
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    chooseImage();
                }
                break;
            }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;
        Uri selectedImage = data.getData();
        if (selectedImage == null)
            Toast.makeText(getContext(), "Image not found!", Toast.LENGTH_SHORT).show();
        sendImageDialog(selectedImage);
    }


    @Override
    public void onResume() {
        super.onResume();
        if (socketIO != null)
            socketIO.syncUser();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
        chatRecyclerViewAdapter.closeRealm();
    }


    @Subscribe
    public void messageEvent(Event.MessageEvent event) {
        this.chatRecyclerViewAdapter.notifyDataSetChanged();
    }


}
