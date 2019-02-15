package com.blue.elephant.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.blue.elephant.R;
import com.blue.elephant.util.CallBack;
import com.blue.elephant.util.ContentPath;
import com.blue.elephant.util.DateUtil;
import com.blue.elephant.util.DialogUtils;
import com.blue.elephant.util.NetUtil;
import com.blue.elephant.util.QuickLoadCallBack;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.http.RequestParams;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;


public class DetailFragment extends Fragment {

    private TextView mVehicleStatusView,mVehicleNameView,
                    mTipView,mSerialView,mStartView,mTimeView,mFailureView,mSubmitView;
    private EditText mAdviceText;

    private MaintenanceActivity mActivity;
    private JSONObject mMaintanceObject;
    private String mBikeCode,mMainenanceID,mAdvice,mSerial,mMaintenanceSerial,mStartTime,mCause;
    private int mTime;

    private int status = -1;
    private boolean isChange = false;

    private MaintenanceActivity.onConnectListener  onConnectListener = new MaintenanceActivity.onConnectListener() {
        @Override
        public void onShow(int statue) {
            mSubmitView.setEnabled(true);
            DialogUtils.dismissProgressDialog();
//            Log.i("Detail","显示当前按钮" );

        }

        @Override
        public void init() {
            //连接设备
//            mActivity.onConnectDevice(mBluetooth);
            if(mBikeCode == null)
            {
                //数据丢失，请到管理界面结束改维修订单
                return ;
            }
            if(!isChange)
            {
                loadBike();
            }

        }

        @Override
        public void onRent(boolean isOPen) {
//            Log.i("Detail","设置租期");
            //判断当前的租期是否到期，如果到期关闭蓝牙，否则设置最新的租用时间
            String startTime = mMaintanceObject.optString("starttime");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String endTime = sdf.format(new Date(System.currentTimeMillis()));
            long periodMillins  = DateUtil.getPeriodTime(startTime,endTime);
            long currentTime =  mMaintanceObject.optInt("time") * 1000;
            if(currentTime <periodMillins )
            {
                //如果到期，提示用户租期到期，请更新车辆最新数据
//                Log.i("Detail","当前的租期已经结束");
                mActivity.disConnect();
                mSubmitView.setEnabled(true);
                DialogUtils.dismissProgressDialog();
                status = -1;
            }
            else
            {
                //如果未到期，设置租期时间，将车锁打开
                long time = currentTime - periodMillins;
                mActivity.setRentTime(time);
                status = 2;
//                Log.i("Detail","设置租期" +time );
            }
        }

        @Override
        public void onAction() {
            stopMaintenance();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MaintenanceActivity) getActivity();
        mActivity.setConnectListener(onConnectListener);
        Bundle bundle = getArguments();
        if(bundle == null)
            return ;
        String mMaintance = bundle.getString("Maintenance");
        isChange = bundle.getBoolean("Change");
        try {
            mMaintanceObject = new JSONObject(mMaintance);
            mBikeCode = mMaintanceObject.optString("bikecode");
            mMainenanceID = mMaintanceObject.optString("maintenanceid");
            mSerial = mMaintanceObject.optString("bikecode");
            mMaintenanceSerial = mMaintanceObject.optString("maintenanceno");
            mStartTime = mMaintanceObject.optString("starttime");
            mTime = mMaintanceObject.optInt("time");
            mCause = mMaintanceObject.optString("cause");
        }catch(JSONException e)
        {
            Log.e("MaintenanceDetail","" + e.getMessage());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.frag_detail, container, false);

        ImageView actionBarLeft = view.findViewById(R.id.actionbar_left);
        TextView actionBarTitle = view.findViewById(R.id.actionbar_title);

        mVehicleStatusView = view.findViewById(R.id.detail_m_status);
        mVehicleNameView = view.findViewById(R.id.detail_m_name);
        mTipView = view.findViewById(R.id.detail_m_tips);
        mSerialView = view.findViewById(R.id.detail_m_record);
        mStartView = view.findViewById(R.id.detail_m_start);
        mTimeView = view.findViewById(R.id.detail_m_maintenance);
        mFailureView = view.findViewById(R.id.detail_m_failure);
        mAdviceText = view.findViewById(R.id.detail_m_advice);
        mSubmitView = view.findViewById(R.id.detail_m_submit);

        actionBarLeft.setImageResource(R.mipmap.back_arraw);
        actionBarTitle.setText(R.string.frag_detail_title);

        actionBarLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               mActivity.disConnectService();
            }
        });

        mSubmitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(getActivity(),MainDetailActivity.class);
//                startActivity(intent);
//                getActivity().finish();
                if(mAdviceText.getText().toString().equals(""))
                {
                    String error = getResources().getString(R.string.frag_detail_advice_empty);
                    mAdviceText.setError(error);
                    return ;
                }
                mAdvice = mAdviceText.getText().toString();
                if(status> 0)
                {
                    mActivity.closeMaintenance();
                }
                else
                {
                    stopMaintenance();
                }

            }
        });

        //load data
        mVehicleNameView.setText(mSerial);
        mSerialView.setText(mMaintenanceSerial);
        mStartView.setText(mStartTime);
        int hour = mTime /60;
        int min = mTime % 60;
        mTimeView.setText(hour  + "H" + min + "Min");
        mFailureView.setText(mCause);
        return view;
    }

    private void loadBike()
    {
        if (!NetUtil.hasNet(mActivity)) {
            DialogUtils.showToast(mActivity,R.string.check_network_connect);
            return;
        }
        String url = ContentPath.bikeSerial ;
        RequestParams params = new RequestParams(url);
        params.addBodyParameter("bikecode", mBikeCode);
        mActivity.onConnect(params, new CallBack() {
            @Override
            public void onResponse(String response) {

//                Log.i("RentOrder","" + response);
                try{
                    JSONObject mResponseObject = new JSONObject(response);
                    String status = mResponseObject.optString("status");
                    if(status.equals("1"))
                    {
                        JSONObject mResult = mResponseObject.getJSONObject("result");
                        JSONObject mBike = mResult.getJSONObject("bike");
                        String mBluetooth = mBike.optString("bluetooth");
//                        Log.i("RentOrder","" + mBluetooth);
                        //连接设备
                        mActivity.onConnectDevice(mBluetooth);
                    }
                    else if(status.equals("0"))
                    {
                        String errorCode =  mResponseObject.optString("errorcode");
                        if(errorCode.equals("60001"))
                        {
                            mActivity.quickLogin(new QuickLoadCallBack() {
                                @Override
                                public void doSomeThing(Boolean boolon) {
                                    if (boolon == true) {
                                        loadBike();
                                    } else {
                                        mActivity.reLogin(mActivity);
                                    }
                                }
                            });
                        }else
                        {
                            String msg = mResponseObject.optString("msg");
                            DialogUtils.showToast(mActivity,msg);
                        }
                    }
                    mResponseObject.optJSONObject("result");
                }catch(Exception e)
                {
                    Log.e("RentOrder","" + e.getMessage());
                }


            }

            @Override
            public void onFailure() {

            }
        });
    }


    private void stopMaintenance()
    {
        if (!NetUtil.hasNet(mActivity)) {
            DialogUtils.showToast(mActivity,R.string.check_network_connect);
            return;
        }
        String url = ContentPath.stopMaintenance ;
        RequestParams params = new RequestParams(url);
        params.addBodyParameter("maintenanceid", mMainenanceID);
        params.addBodyParameter("advice", mAdvice);

        mActivity.onConnect(params, new CallBack() {
            @Override
            public void onResponse(String response) {

//                Log.i("RentOrder","" + response);
                try{
                    JSONObject mResponseObject = new JSONObject(response);
                    String status = mResponseObject.optString("status");
                    if(status.equals("1"))
                    {
//                        Log.i("RentOrder","" + response);
                        mActivity.disConnectService();

                    }
                    else if(status.equals("0"))
                    {
                        String errorCode =  mResponseObject.optString("errorcode");
                        if(errorCode.equals("60001"))
                        {
                            mActivity.quickLogin(new QuickLoadCallBack() {
                                @Override
                                public void doSomeThing(Boolean boolon) {
                                    if (boolon == true) {
                                        stopMaintenance();
                                    } else {
                                        mActivity.reLogin(mActivity);
                                    }
                                }
                            });
                        }else
                        {
                            String msg = mResponseObject.optString("msg");
                            DialogUtils.showToast(mActivity,msg);
                        }
                    }
                    mResponseObject.optJSONObject("result");
                }catch(Exception e)
                {
                    Log.e("RentOrder","" + e.getMessage());
                }


            }

            @Override
            public void onFailure() {

            }
        });
    }

}
