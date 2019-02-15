package com.blue.elephant.activity;

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.blue.elephant.R;
import com.blue.elephant.custom.bluetooth.BlueCallback;
import com.blue.elephant.custom.bluetooth.BlueService;
import com.blue.elephant.custom.bluetooth.SubBlueService;
import com.blue.elephant.custom.bluetooth.VehicleInfo;
import com.blue.elephant.util.CallBack;
import com.blue.elephant.util.ContentPath;
import com.blue.elephant.util.DateUtil;
import com.blue.elephant.util.DialogUtils;
import com.blue.elephant.util.NetUtil;
import com.blue.elephant.util.QuickLoadCallBack;

import org.json.JSONObject;
import org.xutils.http.RequestParams;

import java.util.HashMap;
import java.util.Map;

public class StopMaintenanceActivity extends BaseActivity implements BlueCallback {

    private TextView mVehicleNameView,
            mTipView,mSerialView,mStartView,mTimeView,mFailureView,mSubmitView;
    private EditText mAdviceText;

    private JSONObject mMaintanceObject;
    private String mBikeCode,mMainenanceID,mAdvice,mSerial,mMaintenanceSerial,mStartTime,mCause,mTimeZone;
    private int mTime;

    private SubBlueService mBlueService;
    private VehicleInfo mVehicle;

    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    };

    private static boolean isClose = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frag_detail);

        ImageView actionBarLeft = findViewById(R.id.actionbar_left);
        TextView actionBarTitle = findViewById(R.id.actionbar_title);

        mVehicleNameView = findViewById(R.id.detail_m_name);
        mTipView = findViewById(R.id.detail_m_tips);
        mSerialView = findViewById(R.id.detail_m_record);
        mStartView = findViewById(R.id.detail_m_start);
        mTimeView = findViewById(R.id.detail_m_maintenance);
        mFailureView = findViewById(R.id.detail_m_failure);
        mAdviceText = findViewById(R.id.detail_m_advice);
        mSubmitView = findViewById(R.id.detail_m_submit);
        mSubmitView.setEnabled(false);
        actionBarLeft.setImageResource(R.mipmap.back_arraw);
        actionBarTitle.setText(R.string.frag_detail_title);

        actionBarLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mBlueService != null)
                {
                    mBlueService.onDisConnected();
                }

                StopMaintenanceActivity.this.finish();

            }
        });

        mSubmitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdvice = mAdviceText.getText().toString();
                if(mAdvice.equals(""))
                {
                    String error = getResources().getString(R.string.frag_detail_advice_empty);
                    mAdviceText.setError(error);
                    return ;
                }
                if(mBlueService!= null)
                {
                    isClose = true;
                    String message = getResources().getString(R.string.frag_detail_finish);
                    DialogUtils.showProgressDialog(StopMaintenanceActivity.this, message);
                    byte[] byteCommand = SubBlueService.timeToRent(0, 0, 0, 0);
                    mBlueService.sendCommonData(byteCommand);

                }
            }
        });

//        //load data
//        Intent intent = getIntent();
//        String mMaintenance = intent.getStringExtra("Maintenance");
//        try {
//            mMaintanceObject = new JSONObject(mMaintenance);
//
//            mBikeCode = mMaintanceObject.optString("bikecode");
//            mMainenanceID = mMaintanceObject.optString("maintenanceid");
//            mMaintenanceSerial = mMaintanceObject.optString("maintenanceno");
//            mStartTime = mMaintanceObject.optString("starttime");
//            mCause = mMaintanceObject.optString("cause");
//            mTime = mMaintanceObject.optInt("time");
//
//            mVehicleNameView.setText(mBikeCode);
//            mSerialView.setText(mMaintenanceSerial);
//            mStartView.setText(mStartTime);
//            mTimeView.setText(mTime + " Min");
//            mFailureView.setText(mCause);
//
//        }catch(Exception e)
//        {
//            Log.e("",""+ e.getMessage());
//        }


    }


    @Override
    protected void onStart() {
        super.onStart();
        Intent bindService = new Intent(StopMaintenanceActivity.this,SubBlueService.class);
        bindService(bindService,mServiceConnection, Service.BIND_AUTO_CREATE);

    }


    protected ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service instanceof SubBlueService.BleBinder) {
                SubBlueService.BleBinder mBinder = (SubBlueService.BleBinder) service;
                mBlueService = mBinder.getService();
                mBlueService.setCallback(StopMaintenanceActivity.this);
                //请求车辆信息，加载蓝牙
                loadServer();
                Log.e("BlueService", "ServiceConnection is ok");
            } else {
                Log.e("BlueService", "ServiceConnection 当前未找到Bind 类");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e("BlueService", "onServiceDisconnected 服务器连接数据失败！");
        }
    };

    private void loadServer()
    {
        if (!NetUtil.hasNet(StopMaintenanceActivity.this)) {
            DialogUtils.showToast(StopMaintenanceActivity.this,R.string.check_network_connect);
            return;
        }
        Map<String,String> heads = new HashMap<>();
        heads.put("bikecode",mBikeCode);
        heads.put("appversion","1");
        String message = getResources().getString(R.string.frag_detail_load);
        DialogUtils.showProgressDialog(StopMaintenanceActivity.this, message);
        onServerTime(ContentPath.bikeSerial, heads, new CallBack() {
            @Override
            public void onResponse(String response) {
                String mServerTime = response;
                String[] mSplit = null;
                if(mServerTime!= null)
                {
                    mSplit = mServerTime.split(",");
                    if(mSplit != null)
                    {
                        mTimeZone = mSplit[1];
                    }
                }
                //load data
                Intent intent = getIntent();
                String mMaintenance = intent.getStringExtra("Maintenance");
                try {
                    mMaintanceObject = new JSONObject(mMaintenance);

                    mBikeCode = mMaintanceObject.optString("bikecode");
                    mMainenanceID = mMaintanceObject.optString("maintenanceid");
                    mMaintenanceSerial = mMaintanceObject.optString("maintenanceno");
                    mStartTime = mMaintanceObject.optString("starttime");
                    mCause = mMaintanceObject.optString("cause");
                    mTime = mMaintanceObject.optInt("time");
                    if(mTimeZone!= null)
                    {
                        mStartTime = DateUtil.getLocalTime(mStartTime,mTimeZone);
                    }
                    mVehicleNameView.setText(mBikeCode);
                    mSerialView.setText(mMaintenanceSerial);
                    mStartView.setText(mStartTime);
                    mTimeView.setText(mTime + " Min");
                    mFailureView.setText(mCause);

                }catch(Exception e)
                {
                    Log.e("",""+ e.getMessage());
                }
                loadBike();
            }

            @Override
            public void onFailure() {
                loadBike();
            }
        });
    }

    private void loadBike()
    {

        String url = ContentPath.bikeSerial ;
        RequestParams params = new RequestParams(url);
        params.addBodyParameter("bikecode", mBikeCode);
        onConnect(params, new CallBack() {
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
//                        mActivity.onConnectDevice(mBluetooth);
                        if(mBlueService != null)
                        {
                            mBlueService.setDeviceName(mBluetooth);
                            //检查权限，连接蓝牙
                            boolean isAccess = mBlueService.isMatch();
                            if(!isAccess)
                            {
                                mBlueService.openAccessBlue();
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mBlueService.onConnect();
                                    }
                                },4500);
                            }
                            else
                            {
                                //扫描设备
                                mBlueService.onConnect();
                            }
                        }
                        else
                        {
                            DialogUtils.showToast(StopMaintenanceActivity.this,R.string.confirm_error);
                        }
                    }
                    else if(status.equals("0"))
                    {
                        String errorCode =  mResponseObject.optString("errorcode");
                        if(errorCode.equals("60001"))
                        {
                            quickLogin(new QuickLoadCallBack() {
                                @Override
                                public void doSomeThing(Boolean boolon) {
                                    if (boolon == true) {
                                        loadBike();
                                    } else {
                                        reLogin(StopMaintenanceActivity.this);
                                    }
                                }
                            });
                        }else
                        {
                            String msg = mResponseObject.optString("msg");
                            DialogUtils.showToast(StopMaintenanceActivity.this,msg);
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
                DialogUtils.dismissProgressDialog();
            }
        });


    }


    private void stopRepair()
    {
        if (!NetUtil.hasNet(StopMaintenanceActivity.this)) {
            DialogUtils.showToast(StopMaintenanceActivity.this,R.string.check_network_connect);
            return;
        }
        String url = ContentPath.stopMaintenance ;
        RequestParams params = new RequestParams(url);
        params.addBodyParameter("maintenanceid", mMainenanceID);
        params.addBodyParameter("advice", mAdvice);

        onConnect(params, new CallBack() {
            @Override
            public void onResponse(String response) {

//                Log.i("RentOrder","" + response);
                try{
                    JSONObject mResponseObject = new JSONObject(response);
                    String status = mResponseObject.optString("status");
                    if(status.equals("1"))
                    {
//                        Log.i("RentOrder","" + response);
//                        mActivity.disConnectService();
                        if(mBlueService!= null)
                        {
                            mBlueService.onDisConnected();
                        }
                        StopMaintenanceActivity.this.finish();
                    }
                    else if(status.equals("0"))
                    {
                        String errorCode =  mResponseObject.optString("errorcode");
                        if(errorCode.equals("60001"))
                        {
                            quickLogin(new QuickLoadCallBack() {
                                @Override
                                public void doSomeThing(Boolean boolon) {
                                    if (boolon == true) {
                                        stopRepair();
                                    } else {
                                        reLogin(StopMaintenanceActivity.this);
                                    }
                                }
                            });
                        }else
                        {
                            String msg = mResponseObject.optString("msg");
                            DialogUtils.showToast(StopMaintenanceActivity.this,msg);
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


    @Override
    protected void onResume() {
        super.onResume();
        //处理6.0以上权限获取问题
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }
    }
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // TODO request success

                }
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(mBlueService!= null)
            {
                mBlueService.onDisConnected();
            }
            this.finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
        {
            if(mBlueService!= null)
            {
                mBlueService.onDisConnected();
            }
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onStop() {
        super.onStop();
        try {
            if(mBlueService!= null)
            {
                mBlueService.onDisConnected();
            }
            unbindService(mServiceConnection);
        }catch(Exception e)
        {
            Log.e("BlueService","该服务未注册");
        }
    }





    @Override
    public void onConnect(int statue) {
    }

    @Override
    public void onReceive(byte[] message) {
        if(message == null)
        {
            return ;
        }
        int[] bleCode = SubBlueService.arrayByteToInt(message);

        switch (bleCode[0])
        {
            case 0X01:
                if(mBlueService != null)
                {
                    mBlueService.sendCommonData(SubBlueService.GET_DEVICE_INFO);
                }
                break;

            case 0X02:
                String deadTime = "" ;
                try {
                    deadTime = mVehicle.getDeadline(message);
                }catch(Exception e)
                {
                    Log.e("BlueService",""+ e.getMessage());
                }
                if(mBlueService != null)
                {
                    mBlueService.sendCommonData(SubBlueService.GET_ELECTLOCK);//1. check lock status
                }
//                Log.i("BlueService"," rent bike  time :" + deadTime);
//                Log.e("BlueService","============  检查锁的状态  ==========");
                break;

            case 0X03:
                if(isClose)
                {
                    isClose = false;
                    //结束维修
                    stopRepair();
                    return ;
                }
                //3 check lock status
                String mStatue = "";
                try {
                    mStatue = mVehicle.getGuardStatus(message);
                }catch(Exception e)
                {
                    Log.e("BlueService","" + e.getMessage());
                }
                DialogUtils.dismissProgressDialog();
                mSubmitView.setEnabled(true);
//                Log.i("BlueService"," rent bike  status:" + mStatue);
                break;

            case 0X04:
                try {
                    mVehicle.getDeviceInfo(message);
                }catch(Exception e)
                {
                    String temp ="";
                    for(int i=0;i<bleCode.length;i++)
                    {
                        temp += bleCode[i];
                    }
                    Log.e("BlueService","device info " +temp);
                }
                if(mBlueService != null)
                {
                    mBlueService.sendCommonData(SubBlueService.GET_VERSION);
                }
                break;

            case 0X05:
                mVehicle.getErrorStatus(message);
                break;

            case 0X06:
                mVehicle.getElectric(message);
                break;

            case 0X07:
                mVehicle.getMileage(message);
                break;

            case 0X12:
//                DialogUtils.dismissProgressDialog();
//                String seatStatus = getSeatStatus(message);

                break;

            case 0X13:
//                DialogUtils.dismissProgressDialog();
                break;

            case 0X0E:
                try{
                    mVehicle.getVersion(message);
                }
                catch(Exception e)
                {
                    Log.e("BlueService","" + e.getMessage());
                }

//                byte[] mCommand= BlueService.timeToRent(1, 0, 0, 0);
                if(mBlueService != null)
                {
                    mBlueService.sendCommonData(SubBlueService.GET_DEADLINE);
                }
//                Log.i("BlueService"," rent bike  show");
//                Log.e("BlueService","============  修改锁的状态  ==========");
                break;
            case 0X0F:
                try {//2 check electric lock status
                    mVehicle.getElecLock(message);
                }catch(Exception e)
                {
                    Log.e("BlueService","" + e.getMessage());
                }
                if(mBlueService != null)
                {
                    mBlueService.sendCommonData(SubBlueService.SET_UNLOCK);
                }
//                Log.i("BlueService"," rent bike  SET_UNLOCK");
                break;

            case 0X30:
                if (bleCode[1] == 0x00) {
                    byte[] byteCommand =new byte[3];
                    byteCommand[0] = 0X30;
                    byteCommand[1] = 0X00;
                    byteCommand[2] = 0X00;
                    if(mBlueService != null)
                    {
                        mBlueService.sendCommonData(byteCommand);
                    }

                }
                break;

            case 0XE0: // 查询当前的设备信息
                if(bleCode[1] == 0X03)
                {
                    if(mBlueService != null)
                    {
                        mBlueService.sendCommonData(BlueService.BLE_CONNECT);
                    }
                }
                break;


        }
    }

    @Override
    public void onRead(String message, int state) {

    }

    @Override
    public void onDevice(BluetoothDevice mDevice, int sign) {

    }

    @Override
    public void onProgress(String percent) {

    }
}
