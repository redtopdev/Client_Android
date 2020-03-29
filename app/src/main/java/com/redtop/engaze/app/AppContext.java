package com.redtop.engaze.app;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

public class AppContext extends Application {
    public static final String TAG = AppContext.class
            .getSimpleName();

    public boolean isFirstTimeLoading = false;
    public Context activityContext = null;
    public Boolean isInternetEnabled = true;
    private static AppContext mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static synchronized AppContext getInstance() {
        return mInstance;
    }


}
