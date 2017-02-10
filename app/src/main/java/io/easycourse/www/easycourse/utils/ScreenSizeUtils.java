package io.easycourse.www.easycourse.utils;

import android.app.Activity;

import io.easycourse.www.easycourse.activities.MainActivity;

/**
 * Created by sripa on 2/9/2017.
 */

public class ScreenSizeUtils {

    public static void setActivityContent(Activity activity, int phoneLayout, int tabletLayout) {
        if(MainActivity.screensize>5)
            activity.setContentView(tabletLayout);
        else
            activity.setContentView(phoneLayout);
    }

}
