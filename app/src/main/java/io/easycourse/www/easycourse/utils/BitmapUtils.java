package io.easycourse.www.easycourse.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import io.easycourse.www.easycourse.utils.asyntasks.CompressImageTask;


public class BitmapUtils {


    public static Bitmap byteArrayToBitmap(byte[] bitmapdata) {
        return BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length);
    }

    public static Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "tempimage", null);
        return Uri.parse(path);
    }

    public static Bitmap decodeFile(File f) throws IOException {
        Bitmap b;

        //Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;

        FileInputStream fis = new FileInputStream(f);
        BitmapFactory.decodeStream(fis, null, o);
        fis.close();

        int scale = 1;
        if (o.outHeight > 1500 || o.outWidth > 1500) {
            scale = (int) Math.pow(2, (int) Math.ceil(Math.log(1500 /
                    (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
        }

        //Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        fis = new FileInputStream(f);
        b = BitmapFactory.decodeStream(fis, null, o2);
        fis.close();

        return b;
    }

    public static void compressBitmap(Uri uri, Context context, CompressImageTask.OnCompressImageTaskCompleted listener) {
        CompressImageTask task = new CompressImageTask(context, listener);
        task.execute(uri);
    }


    public static void loadImage(Context context, ImageView imageView, byte[] imageBytes, String imageUrl, int placeholder) {
        if (imageBytes != null && imageBytes.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            imageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), false));
        } else if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context.getApplicationContext())
                    .load(imageUrl)
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(placeholder)
                    .into(imageView);
        } else {
            imageView.setImageResource(placeholder);
        }
    }

}
