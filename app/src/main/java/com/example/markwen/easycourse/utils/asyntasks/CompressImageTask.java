package com.example.markwen.easycourse.utils.asyntasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.markwen.easycourse.utils.BitmapUtils;

/**
 * Created by nrinehart on 12/28/16.
 */

public class CompressImageTask extends AsyncTask<Uri, Void, Byte[]> {

    private static final String TAG = "CompressAndUploadImageT";

    private OnCompressImageTaskCompleted listener;
    private Context context;
    private String roomId;
    private CompressImageTask thisTask;

    public CompressImageTask(Context context, String roomId, OnCompressImageTaskCompleted listener) {
        this.context = context;
        this.roomId = roomId;
        this.listener = listener;
        thisTask = this;
    }

    @Override
    protected Byte[] doInBackground(Uri... params) {
        Uri uri = params[0];

        try {
            Bitmap bitmap = BitmapUtils.getBitmapFromUri(uri, context);
            byte[] compressedBytes = BitmapUtils.compressBitmapToBytes(bitmap, context, 50);
            return BitmapUtils.convertBytesToWrapper(compressedBytes);
//            uploadImage(compressedBytes, BitmapUtils.getFileName(uri, context), "image/jpg");
        } catch (Exception e) {
            Log.e(TAG, "doInBackground: ", e);
            this.cancel(false);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Byte[] bytes) {
        super.onPostExecute(bytes);
        listener.onTaskCompleted(bytes);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        listener.onTaskFailed();
    }


    public interface OnCompressImageTaskCompleted {
        void onTaskCompleted(Byte[] bytes);

        void onTaskFailed();
    }


}
