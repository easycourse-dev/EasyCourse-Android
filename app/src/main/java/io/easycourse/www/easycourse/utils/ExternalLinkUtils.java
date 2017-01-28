package io.easycourse.www.easycourse.utils;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by markw on 1/5/2017.
 */

public class ExternalLinkUtils {
    public static void OpenLinkInChrome(String link, Context context) {
        try {
            // Launch web form in Chrome
            Intent i = new Intent("android.intent.action.MAIN");
            i.setComponent(ComponentName.unflattenFromString("com.android.chrome/com.android.chrome.Main"));
            i.addCategory("android.intent.category.LAUNCHER");
            i.setData(Uri.parse(link));
            context.startActivity(i);
        } catch (ActivityNotFoundException e) {
            // Chrome is not installed
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            context.startActivity(i);
        }
    }
}
