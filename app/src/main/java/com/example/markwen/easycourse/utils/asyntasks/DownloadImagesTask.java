package com.example.markwen.easycourse.utils.asyntasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.example.markwen.easycourse.models.main.Message;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import id.zelory.compressor.Compressor;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by nrinehart on 12/27/16.
 */

public class DownloadImagesTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "DownloadImagesTask";

    @Override
    protected Void doInBackground(Void... params) {
        Log.d(TAG, "fetching messages with images");
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Message> messages = realm.where(Message.class).findAll();

        for (Message message : messages) {
            if (message.getText() != null) continue;

            Bitmap bitmap = fetchBitmap(message.getImageUrl());
            if(bitmap == null) continue;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
            byte[] compressedBytes = out.toByteArray();
            realm.beginTransaction();
            message.setImageData(compressedBytes);
            realm.commitTransaction();

        }
        realm.close();
        return null;
    }

    private Bitmap fetchBitmap(String url) {
        Bitmap bitmap = null;
        HttpURLConnection connection = null;
        InputStream is = null;
        ByteArrayOutputStream out = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            is = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);

        } catch (Throwable e) {
            this.cancel(true);
        } finally {
            try {
                if (connection != null)
                    connection.disconnect();
                if (out != null) {
                    out.flush();
                    out.close();
                }
                if (is != null)
                    is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return bitmap;
    }

}
