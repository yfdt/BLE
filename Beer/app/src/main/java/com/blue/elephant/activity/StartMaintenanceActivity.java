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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.blue.elephant.R;
import com.blue.elephant.custom.bluetooth.BlueCallback;
import com.blue.elephant.custom.bluetooth.BlueService;
import com.blue.elephant.custom.bluetooth.VehicleInfo;
import com.blue.elephant.util.CallBack;
import com.blue.elephant.util.ContentPath;
import com.blue.elephant.util.DialogUtils;
import com.blue.elephant.util.NetUtil;
import com.blue.elephant.util.QuickLoadCallBack;

import org.json.JSONObject;
import org.xutils.http.RequestParams;

public class StartMaintenanceActivity extends BaseActivity implements BlueCallback {

    private TextView mSerialView,mSubmitView;
    private EditText mFailureView;
    private Spinner mSelectSpinner;

    private VehicleInfo mVehicle;
    private String mBikeID,mBikeCode,mBluetooth,mCauseText;
    private int indexTime;
    private boolean isDead = false;

    private BlueService mBlueService;

    private Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frag_confirm);

        ImageView actionBarLeft = findViewById(R.id.actionbar_left);
        TextView actionBarTitle = findViewById(R.id.actionbar_title);

        mSerialView = findViewById(R.id.confirm_m_serial);
        mFailureView = findViewById(R.id.confirm_m_failure);
        mSelectSpinner = findViewById(R.id.confirm_m_time);
        mSubmitView = findViewById(R.id.confirm_m_submit);

        actionBarLeft.setImageResource(R.mipmap.back_arraw);
        actionBarTitle.setText(R.string.frag_confirm_title);

        actionBarLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mBlueService != null)
                {
                    mBlueService.onDisConnected();
                }
                StartMaintenanceActivity.this.finish();
            }
        });

        mSubmitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mCauseText = mFailureView.getText().toString();
                if(mCauseText.equals(""))
                {
                    String error = getResources().getString(R.string.mainten_bike_empty);
                    mFailureView.setError(error);
                    return ;
                }
                if(mBlueService!= null)
                {
                    long longTime = indexTime * 30;
                    long day =longTime /(24* 60);
                    long hour = (longTime%(24*60))/60;
                    long min = longTime %60;
                    byte[] mCommand= BlueService.timeToRent(day, hour, min, 0);
                    mBlueService.sendCommonData(mCommand);
                    isDead = true;
                }
                else
                {
                    DialogUtils.showToast(StartMaintenanceActivity.this,"Bluetooth connection is broken,\n please recreate the repair order ");
                }

            }
        });

        ArrayAdapter<CharSequence> mTimeAdapter = ArrayAdapter.createFromResource(StartMaintenanceActivity.this,
                R.array.frag_confirm_array_time, android.R.layout.simple_spinner_item);
        mTimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSelectSpinner.setAdapter(mTimeAdapter);
        mSelectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                String item = (String) parent.getItemAtPosition(position);
                indexTime = position +1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Intent intent = getIntent();
        mBikeCode = intent.getStringExtra("BikeCode");
        mBikeID = intent.getStringExtra("BikeID");
        mBluetooth = intent.getStringExtra("Bluetooth");

        mSerialView.setText(mBikeCode);
        mSubmitView.setEnabled(false);
        mVehicle = new VehicleInfo(StartMaintenanceActivity.this);
        try {
            String message = getResources().getString(R.string.mainten_init_load);
            DialogUtils.showProgressDialog(StartMaintenanceActivity.this, message);
        }catch (Exception e)
        {

        }

        mSelectSpinner.setEnabled(false);
    }

    private void startRepair()
    {
        if (!NetUtil.hasNet(StartMaintenanceActivity.this)) {
            DialogUtils.showToast(StartMaintenanceActivity.this,R.string.check_network_connect);
            return;
        }

        String url = ContentPath.startMaintenance ;
        RequestParams params = new RequestParams(url);

        String mCauseText = mFailureView.getText().toString();
        indexTime = indexTime * 30;
        params.addBodyParameter("bikeid", mBikeID);
        params.addBodyParameter("time", indexTime + "");
        params.addBodyParameter("cause", mCauseText);

//        Log.i("Confirm",""+ mBikeCode);
        onConnect(params, new CallBack() {
            @Override
            public void onResponse(String response) {

//                Log.i("Confirm","" + response);
                try{
                    JSONObject mResponseObject = new JSONObject(response);
                    String status = mResponseObject.optString("status");
                    if(status.equals("1"))
                    {
                        JSONObject mResult = mResponseObject.getJSONObject("result");
                        String mBike = mResult.getString("maintenance");

                        if(mBlueService!= null)
                        {
                            mBlueService.onDisConnected();
                        }
                        Intent intent = new Intent(StartMaintenanceActivity.this,StopMaintenanceActivity.class);
                        intent.putExtra("Maintenance",mBike);
                        startActivity(intent);
                        StartMaintenanceActivity.this.finish();
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
                                        startRepair();
                                    } else {
                                        reLogin(StartMaintenanceActivity.this);
                                    }
                                }
                            });
                        }else
                        {
                            String msg = mResponseObject.optString("msg");
                            DialogUtils.showToast(StartMaintenanceActivity.this,msg);
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
    protected void onStart() {
        super.onStart();
        Intent bindService = new Intent(StartMaintenanceActivity.this,BlueService.class);
        bindService(bindService,mServiceConnection, Service.BIND_AUTO_CREATE);
    }


    protected ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service instanceof BlueService.BleBinder) {
                BlueService.BleBinder mBinder = (BlueService.BleBinder) service;
                mBlueService = mBinder.getService();
                mBlueService.setCallback(StartMaintenanceActivity.this);
                if(mBluetooth== null)
                {
                    DialogUtils.showToast(StartMaintenanceActivity.this,"Repair information failed to load, please try again");
                    return ;
                }
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
            } else {
                Log.e("BlueService", "ServiceConnection 当前未找到Bind 类");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e("BlueService", "onServiceDisconnected 服务器连接数据失败！");
        }
    };




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
            mHandler.removeMessages(0X01);
            mHandler.removeMessages(0X02);
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
        int[] bleCode = BlueService.arrayByteToInt(message);
        switch (bleCode[0])
        {
            case 0X01:
                if(mBlueService != null)
                {
                    mBlueService.sendCommonData(BlueService.GET_DEVICE_INFO);
                }
                break;

            case 0X02:
                String deadTime = mVehicle.getDeadline(message);
//                if(mBlueService != null)
//                {
//                    mBlueService.sendCommonData(BlueService.GET_ELECTLOCK);//1. check lock status
//                }

                if(isDead)
                {
                    startRepair();
                    return ;
                }
                if(mBlueService != null)
                {
                    mBlueService.sendCommonData(BlueService.GET_ELECTLOCK);
                }

//                Log.i("BlueService"," rent bike  time :" + deadTime);
//                Log.e("BlueService","============  检查锁的状态  ==========");
                break;

            case 0X03:                                         //3 check lock status
                String mStatue = mVehicle.getGuardStatus(message);
                DialogUtils.dismissProgressDialog();
                mSubmitView.setEnabled(true);
//                Log.i("BlueService"," rent bike  status:" + mStatue);
                break;

            case 0X04:
                mVehicle.getDeviceInfo(message);
                if(mBlueService != null)
                {
                    mBlueService.sendCommonData(BlueService.GET_VERSION);
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
                mVehicle.getVersion(message);
//                byte[] mCommand= BlueService.timeToRent(1, 0, 0, 0);
                if(mBlueService != null)
                {
                    mBlueService.sendCommonData(BlueService.GET_DEADLINE);
                }
//                Log.i("BlueService"," rent bike  show");
//                Log.e("BlueService","============  修改锁的状态  ==========");
                break;
            case 0X0F:                                           //2 check electric lock status
                mVehicle.getElecLock(message);
                if(mBlueService != null)
                {
                    mBlueService.sendCommonData(BlueService.SET_UNLOCK);
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
