package io.easycourse.www.easycourse.utils.asyntasks;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import io.easycourse.www.easycourse.R;
import io.easycourse.www.easycourse.activities.ChatRoomActivity;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by noahrinehart on 1/28/17.
 */

public class DownloadImageTask extends AsyncTask<Void, Void, File> {

    private static final String TAG = "DownloadImageTask";

    private Context mContext;
    private Bitmap bitmap;


    public DownloadImageTask(Bitmap bitmap, Context mContext) {
        this.bitmap = bitmap;
        this.mContext = mContext;
    }

    @Override
    protected File doInBackground(Void... voids) {
        File file = getPictureFile();
        if (file == null) return null;
//        if(!file.exists()) return null;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        copyBytesToFile(file, byteArray);
        return file;
    }

    @Override
    protected void onPostExecute(File file) {
        super.onPostExecute(file);
        if (file == null) return;

        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        Uri photoUri = FileProvider.getUriForFile(mContext, getApplicationContext().getPackageName() + ".provider", file);
        intent.setDataAndType(photoUri, "image/*");
        PendingIntent pIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notif = new Notification.Builder(mContext)
                .setContentIntent(pIntent)
                .setContentTitle("Downloaded Image")
                .setContentText("EasyCourse")
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setLargeIcon(bitmap)
                .setStyle(new Notification.BigPictureStyle().bigPicture(bitmap))
                .build();
        notif.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(1, notif);
        Toast.makeText(mContext, "Picture downloaded", Toast.LENGTH_SHORT).show();

    }

    private void copyBytesToFile(File file, byte[] bytes) {
        try {
            if (file == null) return;
            FileOutputStream fis = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fis);
            bos.write(bytes);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            Log.e(TAG, "downloadImage: ", e);
        }
    }

    private File getPictureFile() {
        try {
            String name = "PIC" + UUID.randomUUID().toString() + ".jpg";
            File file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "EasyCourse");
            if (!file.exists()) {
                file.mkdirs();
                file.createNewFile();
            }
            File picFile = new File(file, name);
            return picFile;
        } catch (IOException e) {
            Log.e(TAG, "getPictureFile: ", e);
        }
        return null;
    }
}
