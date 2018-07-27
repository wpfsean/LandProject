package com.zhketech.mstapp.client.land.project.base;

import android.app.Application;
import android.content.Context;

/**
 * Created by Root on 2018/7/11.
 */

public class App extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }


    public static Context getInstance() {
        return mContext;
    }


}
