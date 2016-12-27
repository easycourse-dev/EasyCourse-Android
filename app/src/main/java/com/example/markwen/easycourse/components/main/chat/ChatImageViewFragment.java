package com.example.markwen.easycourse.components.main.chat;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.markwen.easycourse.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by nrinehart on 12/26/16.
 */

public class ChatImageViewFragment extends Fragment {

    private static final String TAG = "ChatImageViewFragment";

    private String url;
    private PhotoViewAttacher photoAttacher;

    @BindView(R.id.relative_layout_chat_image)
    RelativeLayout relativeLayout;
    @BindView(R.id.chatImageView)
    ImageView chatImageView;

    public static ChatImageViewFragment newInstance(String url) {
        ChatImageViewFragment fragment = new ChatImageViewFragment();
        fragment.url = url;
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
                if(photoAttacher!=null){
                    photoAttacher.update();
                }else{
                    photoAttacher = new PhotoViewAttacher(chatImageView);
                }
            }

            @Override
            public void onError() {
                Log.e(TAG, "onError: photoimage callback");

            }
        };

        if (url != null) {
            Picasso.with(getContext())
                    .load(url)
                    .into(chatImageView, imageLoadedCallback);
        }



        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeFragment();
            }
        });


        return v;


    }

    private void closeFragment() {
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }
}
