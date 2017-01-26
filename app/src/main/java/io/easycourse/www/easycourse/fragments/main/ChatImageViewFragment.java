package io.easycourse.www.easycourse.fragments.main;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import io.easycourse.www.easycourse.R;
import io.easycourse.www.easycourse.utils.BitmapUtils;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by nrinehart on 12/26/16.
 */

public class ChatImageViewFragment extends Fragment {

    private static final String TAG = "ChatImageViewFragment";

    private String url;
    private byte[] imageData;
    PhotoViewAttacher photoAttacher;
    Vibrator vibrator;

    @BindView(R.id.relative_layout_chat_image)
    RelativeLayout relativeLayout;
    @BindView(R.id.chatImageUselessView)
    View uselessView;
    @BindView(R.id.chatImageView)
    ImageView chatImageView;
    @BindView(R.id.chatImageProgressBar)
    ProgressBar chatProgressBar;
    @BindView(R.id.chatImageViewShare)
    ImageButton chatImageViewShare;
    @BindView(R.id.chatImageViewForward)
    ImageButton chatImageViewForward;
    @BindView(R.id.chatImageViewDownload)
    ImageButton chatImageViewDownload;

    public static ChatImageViewFragment newInstance(String url, byte[] imageData) {
        ChatImageViewFragment fragment = new ChatImageViewFragment();
        fragment.url = url;
        fragment.imageData = imageData;
        return fragment;
    }

    //TODO: Viewpager to go to next image
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chat_image_view, container, false);
        ButterKnife.bind(this, v);

        vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);

        chatImageView.setDrawingCacheEnabled(true);


        Callback imageLoadedCallback = new Callback() {

            @Override
            public void onSuccess() {
                if (photoAttacher != null) {
                    photoAttacher.update();
                } else {
                    photoAttacher = getPhotoViewAttacher();
                }
                chatProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError() {
                Log.e(TAG, "onError: photoimage callback");
                if (imageData != null) {
                    Bitmap bitmap = BitmapUtils.byteArrayToBitmap(imageData);
                    Log.e(TAG, "onCreateView: " + Arrays.toString(imageData));
                    chatImageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), false));
                    if (photoAttacher != null) {
                        photoAttacher.update();
                    } else {
                        photoAttacher = getPhotoViewAttacher();
                    }
                    chatProgressBar.setVisibility(View.GONE);
                }
            }
        };

        Picasso.with(getContext()).load(url).into(chatImageView, imageLoadedCallback);

        chatImageViewShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chatImageViewShare.setEnabled(false);
                vibrator.vibrate(20);
                if (url != null) {
                    shareImageFromUrl();
                } else {
                    shareImageFromData();
                }
                chatImageViewShare.setEnabled(true);


            }
        });


        chatImageViewDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


        return v;


    }

    private void shareImageFromUrl() {
        Uri bmpUri = getLocalBitmapUri(chatImageView);
        if (bmpUri != null) {
            // Construct a ShareIntent with link to image
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
            shareIntent.setType("image/*");
            // Launch sharing dialog for image
            startActivity(Intent.createChooser(shareIntent, "Share Image"));
        }
    }

//    http://stackoverflow.com/questions/23527767/open-failed-eacces-permission-denied
    private void shareImageFromData() {
//                Bitmap bitmap = chatImageView.getDrawingCache();
//                File root = Environment.getExternalStorageDirectory();
//                File cachePath = null;
//                try {
//                    cachePath = File.createTempFile("temp", "jpg", getContext().getCacheDir());
//                    cachePath.createNewFile();
//                    FileOutputStream ostream = new FileOutputStream(cachePath);
//                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
//                    ostream.close();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//                if (cachePath != null) {
//                    Intent share = new Intent(Intent.ACTION_SEND);
//                    share.setType("image/*");
//                    share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(cachePath));
//
//                    startActivity(Intent.createChooser(share, "Share Image"));
//                }
    }

    private PhotoViewAttacher getPhotoViewAttacher() {
        PhotoViewAttacher photoViewAttacher = new PhotoViewAttacher(chatImageView);
        photoViewAttacher.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                closeFragment();
            }
        });
        return photoViewAttacher;
    }

    private void closeFragment() {
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }

    public Uri getLocalBitmapUri(ImageView imageView) {
        // Extract Bitmap from ImageView drawable
        Drawable drawable = imageView.getDrawable();
        Bitmap bmp = null;
        if (drawable instanceof BitmapDrawable) {
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } else {
            return null;
        }
        // Store image to default external storage directory
        Uri bmpUri = null;
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), "share_image_" + System.currentTimeMillis() + ".png");
            file.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

}
