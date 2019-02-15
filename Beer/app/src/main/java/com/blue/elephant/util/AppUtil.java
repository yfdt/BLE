package com.blue.elephant.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Created by song on 2018/6/25.
 */

public class AppUtil {

    /**
     * 查询应用程序的版本号
     * @param context 上下文环境
     * @return versionName 1.0.0
     */
    public static String getVersionName(Context context) {
        PackageManager pm = context.getPackageManager();
        ApplicationInfo ai = context.getApplicationInfo();
        try {
            PackageInfo pi = pm.getPackageInfo(ai.packageName, 0);
            return pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查询应用程序的版本值
     * @param context 上下文环境
     * @return versionCode 整数
     */
    public static int getVersionCode(Context context) {
        int versionCode = 0;
        try {
            versionCode = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * 查询应用程序的名称
     * @param context 上下文环境
     * @return 软件名
     */
    public static String getAppName(Context context) {
        return context.getApplicationInfo()
                .loadLabel(context.getPackageManager()).toString();
    }

    /**
     * 重启应用
     * @param context 上下文环境
     * @return
     */
    public static void restart(Context context) {
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * 查询手机型号
     * @return 手机型号
     */
    public static String getPhoneModel() {
        return android.os.Build.MODEL;
    }

    /**
     * 查询Android版本
     * @return Android版本号
     */
    public static String getAndroidVersion() {
        return android.os.Build.VERSION.RELEASE;
    }
}
