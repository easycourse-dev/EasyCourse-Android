package com.example.markwen.easycourse.utils.asyntasks;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Toast;

import com.example.markwen.easycourse.utils.APIFunctions;
import com.example.markwen.easycourse.utils.BitmapUtils;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import cz.msebera.android.httpclient.Header;

/**
 * Created by nrinehart on 12/28/16.
 */

public class CompressAndUploadImageTask extends AsyncTask<Uri, Void, String> {

    private static final String TAG = "CompressAndUploadImageT";

    private OnCompressImageTaskCompleted listener;
    private Context context;
    private String roomId;
    private CompressAndUploadImageTask thisTask;

    public CompressAndUploadImageTask(Context context, String roomId, OnCompressImageTaskCompleted listener) {
        this.context = context;
        this.roomId = roomId;
        this.listener = listener;
        thisTask = this;
    }

    @Override
    protected String doInBackground(Uri... params) {
        Uri uri = params[0];

        try {
            Bitmap bitmap = BitmapUtils.getBitmapFromUri(uri, context);
            byte[] compressedBytes = BitmapUtils.compressBitmapToBytes(bitmap, context, 50);
            uploadImage(compressedBytes, BitmapUtils.getFileName(uri, context), "image/jpg");
        } catch (Exception e) {
            Log.e(TAG, "doInBackground: ", e);
            this.cancel(false);
        }
        return null;
    }

    @Override
    protected void onPostExecute(String url) {
        super.onPostExecute(url);
        listener.onTaskCompleted(url);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        listener.onTaskFailed();
    }


    private void uploadImage(final byte[] bytes, final String filename, final String mimeType) {
        try {
            APIFunctions.uploadImage(context, BitmapUtils.convertBytesToFile(bytes, filename, context), filename, mimeType, roomId, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    //TODO: geturl
                    onPostExecute(null);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                    //TODO: pass throwable
                    thisTask.cancel(false);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public interface OnCompressImageTaskCompleted {
        void onTaskCompleted(String url);

        void onTaskFailed();
    }


}
