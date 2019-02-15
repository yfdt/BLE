package com.blue.elephant.util;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blue.elephant.R;

public class DialogUtils {

    public static ProgressDialog progressDialog = null;

    public interface NoticeCallBack {
        /**
         * 提示对话框被确定后回调
         */
        void confirm();

        /**
         * 提示对话框被取消后回调
         */
        void cancel();
    }

    public static void showProgressDialog(Context context, String msg) {
        if(progressDialog==null)
        {
            try {
                progressDialog = new ProgressDialog(context);
                progressDialog.setMessage(msg);
                progressDialog.show();
            }catch(Exception e)
            {
//                e.printStackTrace();
                Log.e("Dialog","Util has exception :"+ e.getMessage());
            }
        }
        else
        {
            try {
                progressDialog.setMessage(msg);
                progressDialog.show();
            }catch(Exception e)
            {
//                e.printStackTrace();
                Log.e("Dialog","Util has exception :"+ e.getMessage());
            }
        }
    }

    public static void showProgressDialog(Context context, String msg, Boolean isClose, Boolean flag) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(msg);
        progressDialog.setCancelable(isClose);
        progressDialog.setCanceledOnTouchOutside(flag);
        progressDialog.show();
    }

    public static void dismissProgressDialog() {
        if (progressDialog != null ) {
            if(progressDialog.isShowing())
            {
                progressDialog.dismiss();
            }
            progressDialog = null;
        }
    }


    public static PopupWindow showAllMenu(Context context,View mLayoutView,View parent)
    {
        PopupWindow mPopWindow = new PopupWindow(context);
        mPopWindow.setContentView(mLayoutView);
        Drawable mDrawable = context.getResources().getDrawable(R.drawable.shape_pop_gray);
        mPopWindow.setBackgroundDrawable(mDrawable);
        WindowManager mWindowManage = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int width = mWindowManage.getDefaultDisplay().getWidth();
        int height = mWindowManage.getDefaultDisplay().getHeight();
        mPopWindow.setWidth(width);
        mPopWindow.setHeight(height);
        mPopWindow.showAtLocation(parent, Gravity.NO_GRAVITY,0,0);
        return mPopWindow;
    }


    /**
     * 显示提示对话框
     *
     * @param msg      提示信息
     * @param ctx      对话框所显示的Activity
     * @param callBack 回调操作
     */
    public static void showNoticeDialog(String msg, Context ctx, final NoticeCallBack callBack) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle(R.string.tip).setMessage(msg)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (callBack != null) {
                            callBack.confirm();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (callBack != null) {
                            callBack.cancel();
                        }
                    }
                })
                .setCancelable(false)
                .show();
    }

    public static void showCashDialog(String msg, Context ctx, final NoticeCallBack callBack) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle(R.string.tip).setMessage(msg)
                .setPositiveButton("默认设置", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (callBack != null) {
                            callBack.confirm();
                        }
                    }
                })
                .setNegativeButton("选择日期", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (callBack != null) {
                            callBack.cancel();
                        }
                    }
                })
                .setCancelable(false)
                .show();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static PopupWindow showVehicleMenu(Context mContext, View root, View contentView)
    {
        PopupWindow mPopWindow = new PopupWindow(mContext);
        Drawable mDrawable = mContext.getResources().getDrawable(R.drawable.shape_vehicle_search_color);
        mPopWindow.setBackgroundDrawable(mDrawable);
        mPopWindow.setContentView(contentView);
        int width = (int) mContext.getResources().getDimension(R.dimen.vehicle_menu_width);
        int height = (int) mContext.getResources().getDimension(R.dimen.vehicle_menu_height);
        mPopWindow.setWidth(width);
        mPopWindow.setHeight(height);
//        mPopWindow.setElevation(10);
        mPopWindow.setOutsideTouchable(false);
        mPopWindow.setFocusable(true);
        int offsetHeight = (int) mContext.getResources().getDimension(R.dimen.vehicle_menu_offsetheight);
        mPopWindow.showAsDropDown(root, Gravity.BOTTOM|Gravity.RIGHT,0,0);

        return mPopWindow;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static PopupWindow showBikeMenu(Context mContext, View root, View contentView)
    {
        PopupWindow mPopWindow = new PopupWindow(mContext);
        Drawable mDrawable = mContext.getResources().getDrawable(R.drawable.shape_vehicle_search_color);
        mPopWindow.setBackgroundDrawable(mDrawable);
        mPopWindow.setContentView(contentView);
        int width = (int) mContext.getResources().getDimension(R.dimen.vehicle_menu_width);
        int height = (int) mContext.getResources().getDimension(R.dimen.vehicle_menu_update_height);
        mPopWindow.setWidth(width);
        mPopWindow.setHeight(height);
//        mPopWindow.setElevation(10);
        mPopWindow.setOutsideTouchable(false);
        mPopWindow.setFocusable(true);
        int offsetHeight = (int) mContext.getResources().getDimension(R.dimen.vehicle_menu_offsetheight);
        mPopWindow.showAsDropDown(root, Gravity.BOTTOM|Gravity.RIGHT,0,0);

        return mPopWindow;
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static PopupWindow showModifyVehicleMenu(Context mContext, View root, View contentView)
    {
        PopupWindow mPopWindow = new PopupWindow(mContext);
        Drawable mDrawable = mContext.getResources().getDrawable(R.drawable.shape_vehicle_search_color);
        mPopWindow.setBackgroundDrawable(mDrawable);
        mPopWindow.setContentView(contentView);
        int width = (int) mContext.getResources().getDimension(R.dimen.vehicle_menu_width);
        int height = (int) mContext.getResources().getDimension(R.dimen.vehicle_modify_height);
        mPopWindow.setWidth(width);
        mPopWindow.setHeight(height);
//        mPopWindow.setElevation(10);
        mPopWindow.setOutsideTouchable(false);
        mPopWindow.setFocusable(true);
        int offsetHeight = (int) mContext.getResources().getDimension(R.dimen.vehicle_menu_offsetheight);
        mPopWindow.showAsDropDown(root, Gravity.BOTTOM|Gravity.RIGHT,0,0);

        return mPopWindow;
    }


    public static PopupWindow showMapMenu(Context mContext,View parent,View contentView)
    {
        PopupWindow mPopWindow = new PopupWindow(mContext);
        Drawable mDrawable = mContext.getResources().getDrawable(R.drawable.shape_pop_gray);
        mPopWindow.setBackgroundDrawable(mDrawable);
        mPopWindow.setContentView(contentView);
        WindowManager mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        int width = mWindowManager.getDefaultDisplay().getWidth();
        int height = mWindowManager.getDefaultDisplay().getHeight();
        mPopWindow.setWidth(width);
        mPopWindow.setHeight(height);
        mPopWindow.showAtLocation(parent,Gravity.NO_GRAVITY,0,0);
        return mPopWindow;
    }


    public static void showToast(Context mContext ,int id)
    {
        Toast mToast = new Toast(mContext);
        TextView tvMessage = new TextView(mContext);
        ViewGroup.LayoutParams mParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        tvMessage.setLayoutParams(mParams);
        tvMessage.setGravity(Gravity.CENTER);
        tvMessage.setText(id);
        tvMessage.setPadding(15,15,15,15);
        int mMessageColor = mContext.getResources().getColor(R.color.black);
        tvMessage.setTextColor(mMessageColor);
        mToast.setView(tvMessage);
        tvMessage.setBackgroundResource(R.drawable.shape_toast);
        mToast.setDuration(Toast.LENGTH_SHORT);
        mToast.show();
    }

    public static void showToast(Context mContext ,String id)
    {
        Toast mToast = new Toast(mContext);
        TextView tvMessage = new TextView(mContext);
        ViewGroup.LayoutParams mParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        tvMessage.setLayoutParams(mParams);
        tvMessage.setGravity(Gravity.CENTER);
        tvMessage.setText(id);
        tvMessage.setPadding(15,15,15,15);
        int mMessageColor = mContext.getResources().getColor(R.color.black);
        tvMessage.setTextColor(mMessageColor);
        mToast.setView(tvMessage);
        tvMessage.setBackgroundResource(R.drawable.shape_toast);
        mToast.setDuration(Toast.LENGTH_SHORT);
        mToast.show();
    }


}
