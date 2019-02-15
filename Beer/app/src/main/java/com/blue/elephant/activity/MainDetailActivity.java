package com.blue.elephant.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blue.elephant.R;
import com.blue.elephant.util.CallBack;
import com.blue.elephant.util.ContentPath;
import com.blue.elephant.util.DateUtil;
import com.blue.elephant.util.DialogUtils;
import com.blue.elephant.util.NetUtil;
import com.blue.elephant.util.QuickLoadCallBack;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.http.RequestParams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/***
 * 维修详情界面
 */
public class MainDetailActivity extends BaseActivity {

    private TextView mVehicleStatusView,mVehicleNameView,
            mSerialView,mStartView,mTimeView,mFailureView,mAdviceView;

    private String mTimeZone;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maindetail);

        ImageView actionBarLeft = findViewById(R.id.actionbar_left);
        TextView actionBarTitle = findViewById(R.id.actionbar_title);
        mVehicleNameView = findViewById(R.id.main_detail_name);
        mVehicleStatusView = findViewById(R.id.main_detail_status);
        mSerialView = findViewById(R.id.main_detail_serial);
        mStartView = findViewById(R.id.main_detail_start);
        mTimeView = findViewById(R.id.main_detail_time);
        mFailureView = findViewById(R.id.main_detail_failure);
        mAdviceView = findViewById(R.id.main_detail_advice);


        actionBarLeft.setImageResource(R.mipmap.back_arraw);
        actionBarTitle.setText(R.string.main_detail_title);
        actionBarLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainDetailActivity.this.finish();
            }
        });

        loadServer();

    }

    private void loadServer()
    {
        if (!NetUtil.hasNet(MainDetailActivity.this)) {
            DialogUtils.showToast(MainDetailActivity.this,R.string.check_network_connect);
            return;
        }
        Map<String,String> heads = new HashMap<>();
        String  mMaintenance = getIntent().getStringExtra("MaintenanceID");
        heads.put("maintenanceid",mMaintenance);
        heads.put("appversion","1");
        String message = getResources().getString(R.string.mianten_load);
        DialogUtils.showProgressDialog(MainDetailActivity.this, message);
        onServerTime(ContentPath.getMaintenance, heads, new CallBack() {
            @Override
            public void onResponse(String response) {
                String mServerTime = response;
//                Log.i("OrderDetailActivity","" + mServerTime);
                String[] mSplit = null;
                if(mServerTime!= null)
                {
                    mSplit = mServerTime.split(",");
                    if(mSplit != null)
                    {
                        mTimeZone = mSplit[1];
                    }
                }
                loadMaintenance();
            }

            @Override
            public void onFailure() {
                loadMaintenance();
            }
        });

    }


    private void loadMaintenance()
    {

        Intent intent = getIntent();
        if(intent == null)
        {
            return ;
        }
        String  mMaintenance = intent.getStringExtra("MaintenanceID");
        String url = ContentPath.getMaintenance ;
        final RequestParams params = new RequestParams(url);
        params.addBodyParameter("maintenanceid", mMaintenance);

        onConnect(params, new CallBack() {
            @Override
            public void onResponse(String response) {
                DialogUtils.dismissProgressDialog();
                try {
                    JSONObject mResponseObject = new JSONObject(response);
                    String status = mResponseObject.optString("status");
                    if(status.equals("1"))
                    {
//                        Log.i("MainDetail","" + response);
                         JSONObject mResultObject = mResponseObject.optJSONObject("result");
                         JSONObject mMaintenanceObject = mResultObject.optJSONObject("maintenance");
                        String mSerial = mMaintenanceObject.optString("bikecode");
                        String mNumber = mMaintenanceObject.optString("maintenanceno");
                        String mStart = mMaintenanceObject.optString("starttime");
                        String mEnd = mMaintenanceObject.optString("endtime");
                        String mFailure = mMaintenanceObject.optString("cause");
                        String mAdvice = mMaintenanceObject.optString("advice");
                        long time =  DateUtil.getPeriodTime(mStart,mEnd);
                        long hour = time/3600000;
                        long min = time/60000;
                        long second = time % 60000;
                        if(second > 0 )
                        {
                            min ++;
                        }
                        if(min > 59)
                        {
                            min = min %60;
                            hour +=1;
                        }
                        String mPeriod = "";
                        try {
                            mPeriod = getResources().getString(R.string.order_period,hour,min);
                        }catch(Exception e)
                        {

                        }
                        mStart =  DateUtil.getLocalTime(mStart,mTimeZone);
                        mVehicleNameView.setText(mSerial);
                        mSerialView.setText(mNumber);
                        mStartView.setText(mStart);
                        mTimeView.setText(mPeriod);
                        mFailureView.setText(mFailure);
                        mAdviceView.setText(mAdvice);
                    }
                    else if(status.equals("0"))
                    {
                        String errorcode =  mResponseObject.optString("errorcode");
                        if (errorcode.equals("60001")) {
                            quickLogin(new QuickLoadCallBack() {
                                @Override
                                public void doSomeThing(Boolean boolon) {
                                    if (boolon == true) {
                                        loadMaintenance();
                                    } else {
                                        reLogin(MainDetailActivity.this);
                                    }
                                }
                            });
                        } else {
                            String msg = mResponseObject.optString("msg");
                            DialogUtils.showToast(MainDetailActivity.this,msg);
                        }
                    }


                }catch(Exception e)
                {
                    Log.e("MaintenanceListActivity","" + e.getMessage());
                }
            }

            @Override
            public void onFailure() {
                DialogUtils.dismissProgressDialog();
            }
        });




    }

}
