package com.example.markwen.easycourse.utils.asyntasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.markwen.easycourse.utils.BitmapUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * Created by nrinehart on 12/28/16.
 */

public class CompressImageTask extends AsyncTask<Uri, Void, Bitmap> {

    private static final String TAG = "CompressAndUploadImageT";

    private OnCompressImageTaskCompleted listener;
    private Context context;

    public CompressImageTask(Context context, OnCompressImageTaskCompleted listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected Bitmap doInBackground(Uri... params) {
        Uri uri = params[0];

        try {
            File file = new File(BitmapUtils.getImagePath(uri, context));
            return BitmapUtils.decodeFile(file);

        } catch (Exception e) {
            Log.e(TAG, "doInBackground: ", e);
            this.cancel(false);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        listener.onTaskCompleted(bitmap, byteArray);

    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        listener.onTaskFailed();
    }


    public interface OnCompressImageTaskCompleted {
        void onTaskCompleted(Bitmap bitmap, byte[] bytes);

        void onTaskFailed();
    }


}
