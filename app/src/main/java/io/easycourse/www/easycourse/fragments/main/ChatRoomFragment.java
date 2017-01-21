package io.easycourse.www.easycourse.fragments.main;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.easycourse.www.easycourse.EasyCourse;
import io.easycourse.www.easycourse.R;
import io.easycourse.www.easycourse.activities.ChatRoomActivity;
import io.easycourse.www.easycourse.components.main.chat.ChatRecyclerViewAdapter;
import io.easycourse.www.easycourse.models.main.Message;
import io.easycourse.www.easycourse.models.main.Room;
import io.easycourse.www.easycourse.models.main.User;
import io.easycourse.www.easycourse.utils.BitmapUtils;
import io.easycourse.www.easycourse.utils.JSONUtils;
import io.easycourse.www.easycourse.utils.SocketIO;
import io.easycourse.www.easycourse.utils.asyntasks.CompressImageTask;
import io.easycourse.www.easycourse.utils.asyntasks.DownloadImagesTask;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;
import io.socket.client.Ack;

import static com.facebook.FacebookSdk.getApplicationContext;


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

    private Uri imageUri;

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
                sendTextMessage();
            }
        });

        if (!currentRoom.isJoinIn() && !currentRoom.isToUser())
            messageEditText.setEnabled(false);


        messageEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled = false;
                if (i == EditorInfo.IME_ACTION_SEND) {
                    sendTextMessage();
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
                int permissionCheck2 = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
                if (permissionCheck2 == PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                } else {
                    takeImage();
                }
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
        File photo = new File(Environment.getExternalStorageDirectory(), new Date().toString() + ".jpg");
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(getContext(), getApplicationContext().getPackageName() + ".provider", photo));
        imageUri = Uri.fromFile(photo);
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, TAKE_IMAGE_INTENT);
        }
    }

    private void compressAndSendImage(final Uri uri) {
        BitmapUtils.compressBitmap(uri, getContext(), new CompressImageTask.OnCompressImageTaskCompleted() {
            @Override
            public void onTaskCompleted(Bitmap bitmap, byte[] bytes) {
                if (!currentRoom.isJoinIn() && currentRoom.isToUser()) {
                    Toast.makeText(getContext(), "You have not joined this room!", Toast.LENGTH_SHORT).show();
                    return;
                }
                final String localMessageId = UUID.randomUUID().toString();
                Message message = new Message(localMessageId, null, currentUser, null, null, bytes, null, false, bitmap.getWidth(), bitmap.getHeight(), currentRoom.getId(), currentRoom.isToUser(), new Date());
                message.updateMessageToRealm();

                int selector;
                if (currentRoom.isToUser())
                    selector = SocketIO.PIC_TO_USER;
                else
                    selector = SocketIO.PIC_TO_ROOM;

                socketIO.sendMessage(message, selector, new Ack() {
                    @Override
                    public void call(Object... args) {
                        try {
                            JSONObject obj = (JSONObject) args[0];
                            JSONObject message = (JSONObject) JSONUtils.checkIfJsonExists(obj, "msg", null);

                            JSONObject sender = message.getJSONObject("sender");
                            String senderId = (String) JSONUtils.checkIfJsonExists(sender, "_id", null);
                            String senderName = (String) JSONUtils.checkIfJsonExists(sender, "displayName", null);
                            String senderImageUrl = (String) JSONUtils.checkIfJsonExists(sender, "avatarUrl", null);

                            String id = (String) JSONUtils.checkIfJsonExists(message, "_id", null);
                            String remoteId = (String) JSONUtils.checkIfJsonExists(message, "id", null);
                            String text = (String) JSONUtils.checkIfJsonExists(message, "text", null);
                            String imageUrl = (String) JSONUtils.checkIfJsonExists(message, "imageUrl", null);
                            byte[] imageData = (byte[]) JSONUtils.checkIfJsonExists(message, "imageData", null);
                            boolean successSent = (boolean) JSONUtils.checkIfJsonExists(message, "successSent", false);
                            String toRoom = (String) JSONUtils.checkIfJsonExists(message, "toRoom", null);
                            String toUser = (String) JSONUtils.checkIfJsonExists(message, "toUser", null);
                            float imageWidth = Float.parseFloat((String) JSONUtils.checkIfJsonExists(message, "imageWidth", "0.0"));
                            float imageHeight = Float.parseFloat((String) JSONUtils.checkIfJsonExists(message, "imageHeight", "0.0"));
                            String dateCreatedAt = (String) JSONUtils.checkIfJsonExists(message, "createdAt", null);
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                            formatter.setTimeZone(Calendar.getInstance().getTimeZone());
                            Date date = null;
                            try {
                                date = formatter.parse(dateCreatedAt);

                            } catch (ParseException e) {
                                Log.e(TAG, "saveMessageToRealm: parseException", e);
                            }
                            Realm tempRealm = Realm.getDefaultInstance();
                            tempRealm.beginTransaction();
                            User senderUser = tempRealm.where(User.class).equalTo("id", senderId).findFirst();
                            if (senderUser == null) {
                                senderUser = tempRealm.createObject(User.class, senderId);
                            }
                            senderUser.setUsername(senderName);
                            senderUser.setProfilePictureUrl(senderImageUrl);


                            Message localMessage = tempRealm.where(Message.class).equalTo("id", localMessageId).findFirst();
                            if (localMessage == null) {
                                localMessage = tempRealm.createObject(Message.class, localMessageId);
                            }
                            localMessage.setRemoteId(remoteId);
                            localMessage.setText(text);
                            localMessage.setImageUrl(imageUrl);
                            localMessage.setImageData(imageData);
                            localMessage.setSuccessSent(true);

                            if (toRoom != null) {
                                localMessage.setToRoom(toRoom);
                                localMessage.setToUser(false);
                            } else {
                                localMessage.setToRoom(toUser);
                                localMessage.setToUser(true);
                            }

                            localMessage.setImageWidth(imageWidth);
                            localMessage.setImageHeight(imageHeight);
                            localMessage.setCreatedAt(date);
                            tempRealm.commitTransaction();
                        } catch (JSONException e) {
                            Log.e(TAG, "call: ", e);
                        }
                    }
                });

                chatRecyclerViewAdapter.notifyMessageViewChanged(localMessageId);
                chatRecyclerViewAdapter.notifyDataSetChanged();

                picSent(true);
            }

            @Override
            public void onTaskFailed() {
                picSent(false);
            }
        });
    }

    private void picSent(boolean wasSent) {
        sendImageProgressBar.setVisibility(View.GONE);
        if (!wasSent)
            Toast.makeText(activity, "Failed to send pic!", Toast.LENGTH_SHORT).show();
    }

    private void sendTextMessage() {

        if (!currentRoom.isJoinIn() && !currentRoom.isToUser()) {
            Toast.makeText(getContext(), "You have not joined this room!", Toast.LENGTH_SHORT).show();
            return;
        }

        String messageText = messageEditText.getText().toString();
        String fixed = messageText.replace("\\", "");
        if (!TextUtils.isEmpty(fixed)) {

            final String localMessageId = UUID.randomUUID().toString();
            Message message = new Message(localMessageId, null, currentUser, fixed, null, null, null, false, 0, 0, currentRoom.getId(), currentRoom.isToUser(), new Date());
            message.updateMessageToRealm();

            int selector;
            if (currentRoom.isToUser())
                selector = SocketIO.TEXT_TO_USER;
            else
                selector = SocketIO.TEXT_TO_ROOM;

            socketIO.sendMessage(message, selector, new Ack() {
                @Override
                public void call(Object... args) {
                    try {
                        final JSONObject obj = (JSONObject) args[0];

                        if (obj.has("error")) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Toast.makeText(getContext(), obj.getString("error"), Toast.LENGTH_LONG).show();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            return;
                        }

                        JSONObject message = (JSONObject) JSONUtils.checkIfJsonExists(obj, "msg", null);

                        JSONObject sender = (JSONObject) JSONUtils.checkIfJsonExists(message, "sender", null);
                        String senderId = (String) JSONUtils.checkIfJsonExists(sender, "_id", null);
                        String senderName = (String) JSONUtils.checkIfJsonExists(sender, "displayName", null);
                        String senderImageUrl = (String) JSONUtils.checkIfJsonExists(sender, "avatarUrl", null);

                        String id = (String) JSONUtils.checkIfJsonExists(message, "_id", null);
                        String remoteId = (String) JSONUtils.checkIfJsonExists(message, "id", null);
                        String text = (String) JSONUtils.checkIfJsonExists(message, "text", null);
                        String imageUrl = (String) JSONUtils.checkIfJsonExists(message, "imageUrl", null);
                        byte[] imageData = (byte[]) JSONUtils.checkIfJsonExists(message, "imageData", null);
                        boolean successSent = (boolean) JSONUtils.checkIfJsonExists(message, "successSent", false);
                        String toRoom = (String) JSONUtils.checkIfJsonExists(message, "toRoom", null);
                        String toUser = (String) JSONUtils.checkIfJsonExists(message, "toUser", null);
                        float imageWidth = Float.parseFloat((String) JSONUtils.checkIfJsonExists(message, "imageWidth", "0.0"));
                        float imageHeight = Float.parseFloat((String) JSONUtils.checkIfJsonExists(message, "imageHeight", "0.0"));
                        String dateCreatedAt = (String) JSONUtils.checkIfJsonExists(message, "createdAt", null);
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                        formatter.setTimeZone(TimeZone.getTimeZone("GMT")); // Super weird...
                        Date date = null;
                        try {
                            date = formatter.parse(dateCreatedAt);
                        } catch (ParseException e) {
                            Log.e(TAG, "saveMessageToRealm: parseException", e);
                        }

                        Realm tempRealm = Realm.getDefaultInstance();
                        tempRealm.beginTransaction();
                        User senderUser = tempRealm.where(User.class).equalTo("id", senderId).findFirst();
                        if (senderUser == null) {
                            senderUser = tempRealm.createObject(User.class, senderId);
                        }
                        senderUser.setUsername(senderName);
                        senderUser.setProfilePictureUrl(senderImageUrl);


                        Message localMessage = tempRealm.where(Message.class).equalTo("id", localMessageId).findFirst();
                        if (localMessage == null) {
                            localMessage = tempRealm.createObject(Message.class, localMessageId);
                        }
                        localMessage.setRemoteId(remoteId);
                        localMessage.setText(text);
                        localMessage.setImageUrl(imageUrl);
                        localMessage.setImageData(imageData);
                        localMessage.setSuccessSent(true);

                        if (toRoom != null) {
                            localMessage.setToRoom(toRoom);
                            localMessage.setToUser(false);
                        } else {
                            localMessage.setToRoom(toUser);
                            localMessage.setToUser(true);
                        }

                        localMessage.setImageWidth(imageWidth);
                        localMessage.setImageHeight(imageHeight);
                        localMessage.setCreatedAt(date);
                        tempRealm.commitTransaction();
                        tempRealm.close();
                    } catch (JSONException e) {
                        Log.e(TAG, "call: ", e);
                    }
                }
            });

//            chatRecyclerViewAdapter.notifyMessageViewChanged(localMessageId);
            chatRecyclerViewAdapter.notifyDataSetChanged();
            messageEditText.setText("");
        }
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

        if (requestCode == TAKE_IMAGE_INTENT) {
            Uri selectedImage = imageUri;
            sendImageDialog(selectedImage);
        } else {
            Uri selectedImage = data.getData();
            if (selectedImage == null)
                Toast.makeText(getContext(), "Image not found!", Toast.LENGTH_SHORT).show();
            sendImageDialog(selectedImage);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.beginTransaction();
        currentRoom.setUnread(0);
        realm.commitTransaction();
        realm.close();
        chatRecyclerViewAdapter.closeRealm();
    }


}
