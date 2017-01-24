package io.easycourse.www.easycourse.fragments.main;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import io.easycourse.www.easycourse.R;
import io.easycourse.www.easycourse.utils.BitmapUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

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

    @BindView(R.id.relative_layout_chat_image)
    RelativeLayout relativeLayout;
    @BindView(R.id.chatImageUselessView)
    View uselessView;
    @BindView(R.id.chatImageView)
    ImageView chatImageView;
    @BindView(R.id.chatImageProgressBar)
    ProgressBar chatProgressBar;

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


        return v;


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
}
