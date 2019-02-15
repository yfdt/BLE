package com.blue.elephant.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.blue.elephant.R;
import com.blue.elephant.util.AppUtil;
import com.blue.elephant.util.CallBack;
import com.blue.elephant.util.ContentPath;
import com.blue.elephant.util.DialogUtils;
import com.blue.elephant.util.NetUtil;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.http.RequestParams;


public class MainActivity extends BaseActivity implements View.OnClickListener{


    private DrawerLayout mDrawLayout;
    private ScrollView mScrollerMenu;

    private TextView mTextVersion,mTextName;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {

        mDrawLayout = findViewById(R.id.main_drawer);
        mScrollerMenu = findViewById(R.id.main_menu);
        ActionBarDrawerToggle mActionBarToggle = new ActionBarDrawerToggle(MainActivity.this,mDrawLayout,R.string.main_drawer_open, R.string.main_drawer_close);
        mDrawLayout.setDrawerListener(mActionBarToggle);
        mActionBarToggle.syncState();

        //menu
        ImageView mMenuLeft = findViewById(R.id.actionbar_left);
        TextView mTitle = findViewById(R.id.actionbar_title);
        mTextVersion = findViewById(R.id.main_menu_version);
        mTextName = findViewById(R.id.main_menu_name);

        findViewById(R.id.main_menu_income).setOnClickListener(this);
        findViewById(R.id.main_menu_order).setOnClickListener(this);
        findViewById(R.id.main_menu_management).setOnClickListener(this);
        findViewById(R.id.main_menu_map).setOnClickListener(this);
        findViewById(R.id.main_menu_maintenance).setOnClickListener(this);
        findViewById(R.id.main_menu_billing).setOnClickListener(this);
        findViewById(R.id.main_menu_record).setOnClickListener(this);
        findViewById(R.id.main_menu_out).setOnClickListener(this);
        findViewById(R.id.main_rent).setOnClickListener(this);
        findViewById(R.id.main_office).setOnClickListener(this);
        findViewById(R.id.main_maintenance).setOnClickListener(this);
        findViewById(R.id.main_menu_header).setOnClickListener(this);


        mMenuLeft.setImageResource(R.mipmap.main_menu);
        mTitle.setText(R.string.main_title);
        mMenuLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mDrawLayout.isDrawerOpen(mScrollerMenu))
                {
                    mDrawLayout.closeDrawer(mScrollerMenu);
                }
                else
                {
                    mDrawLayout.openDrawer(mScrollerMenu);
                }
            }
        });
        String mName = mPreference.getString("username","");
        mTextName.setText(mName);
        String versionName = AppUtil.getVersionName(MainActivity.this);
        mTextVersion.setText(versionName);

        //检测有无信用卡
        showEmptyCard();
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        },1000);

    }

    private void showEmptyCard()
    {
        String url = ContentPath.getCardList ;
        RequestParams params = new RequestParams(url);
        String message = getResources().getString(R.string.main_load);
        DialogUtils.showProgressDialog(MainActivity.this, message);
        onConnect(params, new CallBack() {
            @Override
            public void onResponse(String response) {
                DialogUtils.dismissProgressDialog();
                try{
                    JSONObject mResponseObject = new JSONObject(response);
                    String status = mResponseObject.optString("status");
                    Log.i("Bill","" + response);
                    if(status.equals("1"))
                    {
                        if(mResponseObject.optString("result").equals("{}"))
                        {
                            showPopWindow();
                            return ;
                        }
                        JSONObject mResultObject = mResponseObject.optJSONObject("result");
                        JSONObject mCustomerObject = mResultObject.optJSONObject("customer");
                        String defaultCard = mCustomerObject.optString("defaultSource");
                        JSONObject mSourceObejct = mCustomerObject.optJSONObject("sources");
                        JSONArray mCardArray = mSourceObejct.optJSONArray("data");
                        if(mCardArray.length() <=0)
                        {
                            showPopWindow();
                        }
                    }else
                    {
                        //no card and interface is failure!!!!!
                        Toast.makeText(MainActivity.this, "Bank card business is temporarily unavailable, please contact staff", Toast.LENGTH_SHORT).show();
                    }

                }catch(Exception e)
                {
                    Log.e("Bill","" + e.getMessage());
                }

            }

            @Override
            public void onFailure() {
                DialogUtils.dismissProgressDialog();
            }
        });

    }


    private void showPopWindow()
    {
        try {
            final PopupWindow mPopWindow = new PopupWindow(MainActivity.this);
            Drawable mDrawable = getResources().getDrawable(R.drawable.shape_pop_gray);
            mPopWindow.setBackgroundDrawable(mDrawable);
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.pop_empty_card, null);
            Button btnOK = view.findViewById(R.id.empty_card_submit);
            btnOK.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPopWindow.dismiss();
                    Intent intent = new Intent(MainActivity.this,AddPayActivity.class);
                    startActivityForResult(intent,0X02);
                }
            });
            mPopWindow.setContentView(view);
            WindowManager mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            int width = mWindowManager.getDefaultDisplay().getWidth();
            int height = mWindowManager.getDefaultDisplay().getHeight();
            mPopWindow.setWidth(width);
            mPopWindow.setHeight(height);
            mPopWindow.setOutsideTouchable(true);
            View parent = findViewById(R.id.actionbar_title);
            mPopWindow.showAtLocation(parent, Gravity.NO_GRAVITY,0,0);
        }catch (Exception e)
        {
            Log.e("Main","" + e.getMessage());
        }
    }


    @Override
    public void onClick(View v) {
        Intent mIntent;
        switch (v.getId())
        {
            case R.id.main_menu_income:
                if(mDrawLayout.isDrawerOpen(mScrollerMenu))
                {
                    mDrawLayout.closeDrawer(mScrollerMenu);
                }
                mIntent = new Intent(MainActivity.this,IncomeActivity.class);
                startActivity(mIntent);

                break;
            case R.id.main_menu_order:
                if(mDrawLayout.isDrawerOpen(mScrollerMenu))
                {
                    mDrawLayout.closeDrawer(mScrollerMenu);
                }
                //进入订单列表
                mIntent = new Intent(MainActivity.this,OrderListActivity.class);
                startActivity(mIntent);

                break;
            case R.id.main_menu_management:
                if(mDrawLayout.isDrawerOpen(mScrollerMenu))
                {
                    mDrawLayout.closeDrawer(mScrollerMenu);
                }
                //进入车辆管理界面
                mIntent = new Intent(MainActivity.this,BikeManageActivity.class);
                startActivity(mIntent);

                break;
            case R.id.main_menu_map:
                if(mDrawLayout.isDrawerOpen(mScrollerMenu))
                {
                    mDrawLayout.closeDrawer(mScrollerMenu);
                }
                //跳转到车辆地图界面
                mIntent = new Intent(MainActivity.this,BikeMapActivity.class);
                startActivity(mIntent);
                break;
            case R.id.main_menu_maintenance:
                if(mDrawLayout.isDrawerOpen(mScrollerMenu))
                {
                    mDrawLayout.closeDrawer(mScrollerMenu);
                }
                //跳转到维修管理界面
                mIntent = new Intent(MainActivity.this,MaintenanceListActivity.class);
                startActivity(mIntent);
                break;
            case R.id.main_menu_billing:
                if(mDrawLayout.isDrawerOpen(mScrollerMenu))
                {
                    mDrawLayout.closeDrawer(mScrollerMenu);
                }
                mIntent = new Intent(MainActivity.this,BillingActivity.class);
                startActivity(mIntent);
                break;
            case R.id.main_menu_record:
                if(mDrawLayout.isDrawerOpen(mScrollerMenu))
                {
                    mDrawLayout.closeDrawer(mScrollerMenu);
                }
                mIntent = new Intent(MainActivity.this,DeductionRecordActivity.class);
                startActivity(mIntent);
                break;
            case R.id.main_menu_out:
                if(mDrawLayout.isDrawerOpen(mScrollerMenu))
                {
                    mDrawLayout.closeDrawer(mScrollerMenu);
                }
                onLoginOut();
//                MainActivity.this.finish();
                break;
            case R.id.main_rent:
                mIntent = new Intent(MainActivity.this,ScanActivity.class);
                mIntent.putExtra(ScanActivity.Status,1);
                startActivity(mIntent);
                break;
            case R.id.main_office:
                //进入订单列表
                mIntent = new Intent(MainActivity.this,OrderListActivity.class);
                startActivity(mIntent);
                break;
            case R.id.main_maintenance:
                mIntent = new Intent(MainActivity.this,ScanActivity.class);
                mIntent.putExtra(ScanActivity.Status,3);
                startActivity(mIntent);
                break;
            case R.id.main_menu_header:
                mIntent = new Intent(MainActivity.this,PersonalActivity.class);
                startActivity(mIntent);
                break;

        }

    }


    private void onLoginOut()
    {
        if (!NetUtil.hasNet(MainActivity.this)) {
            DialogUtils.showToast(MainActivity.this,R.string.check_network_connect);
            return;
        }
        String url = ContentPath.loginOut ;
        RequestParams params = new RequestParams(url);
        String message = getResources().getString(R.string.main_login_out);
        DialogUtils.showProgressDialog(MainActivity.this, message);
        onConnect(params, new CallBack() {
            @Override
            public void onResponse(String response) {
                DialogUtils.dismissProgressDialog();
                //{"errorcode":"90019002","msg":"","status":"0"}
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int status = jsonObject.optInt("status");
                    if(status == 1)
                    {
                        mEditor.clear();
                        mEditor.commit();
//                        Log.i("MainActivity",response);
                        Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                        startActivity(intent);
                        MainActivity.this.finish();
                    }
                    else if(status == 0)
                    {
                        DialogUtils.showToast(MainActivity.this,R.string.error_failure);
                    }
//                    switch (status)
//                    {
//                        case "1":
//                            mEditor.clear();
//                            mEditor.commit();
//                            Log.i("MainActivity",response);
//                            MainActivity.this.finish();
//                            break;
//                        case "0":

//                            break;
//                    }
                }catch(Exception e)
                {
                    Log.e("Test"," error: "+ e.getMessage());
                }

            }

            @Override
            public void onFailure() {
                DialogUtils.dismissProgressDialog();
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0X02)
        {
            showEmptyCard();
        }
    }
}
