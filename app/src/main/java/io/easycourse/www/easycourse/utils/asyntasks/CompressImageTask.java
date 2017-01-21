package io.easycourse.www.easycourse.utils.asyntasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import io.easycourse.www.easycourse.utils.BitmapUtils;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

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
        if(uri == null) return null;
        try {
            File file = File.createTempFile("temp", "jpg", context.getCacheDir());
            OutputStream outputStream = new FileOutputStream(file);
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if(inputStream == null) return null;
            IOUtils.copy(inputStream, outputStream);
            outputStream.close();
            inputStream.close();
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
