package com.blue.elephant.custom;

import android.content.SharedPreferences;
import android.support.multidex.MultiDexApplication;

import org.xutils.x;

public class IApplication extends MultiDexApplication {

    private SharedPreferences mPreference;
    private static IApplication mApplication;

    public static IApplication getApplication()
    {
        return mApplication;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = IApplication.this;
        mPreference = getSharedPreferences("ZBike",MODE_PRIVATE);
        x.Ext.init(this);
        x.Ext.setDebug(true);
    }

    public SharedPreferences getPreference()
    {
        return mPreference;
    }
}
