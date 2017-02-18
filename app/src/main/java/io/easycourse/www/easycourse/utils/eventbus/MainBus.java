package io.easycourse.www.easycourse.utils.eventbus;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;


public class MainBus extends Bus {
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    public MainBus(ThreadEnforcer enforcer) {
        super(enforcer);
    }

    @Override
    public void post(final Object event) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            super.post(event);
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    MainBus.super.post(event);
                }
            });
        }
    }
}
