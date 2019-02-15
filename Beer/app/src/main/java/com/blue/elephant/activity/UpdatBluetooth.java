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
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.blue.elephant.R;
import com.blue.elephant.custom.bluetooth.BlueCallback;
import com.blue.elephant.custom.bluetooth.BlueService;
import com.blue.elephant.custom.bluetooth.CircleView;
import com.blue.elephant.custom.bluetooth.UpdateBaseActivity;
import com.blue.elephant.custom.bluetooth.UpdateBluetoothService;
import com.blue.elephant.util.DialogUtils;

public class UpdatBluetooth extends UpdateBaseActivity implements BlueCallback {

    private final String TAG = "BlueService";
    private final boolean isDebug = true;
    private TextView tvCurrentVersion;


    private String mDeviceName;
    //    private String mUpgradeVeriosn;
    private String softVersion;
    private boolean isFailure = false;
    private TextView tvInfo;
    private TextView tvVersion;
    private TextView tvTipMessage;
    private Button btnBleAction;
    private CircleView mCircleView;
    private  Handler mHandler = new Handler();


    @Override
    public void onConnect(int statue) {
        if (statue == 0) {
//            tvConnectStatue.setText("断开连接");
//            if(isDebug)Log.i(TAG,"onConnect  断开连接");
            mCircleView.stopCircle();
        } else if (statue == 2) {
//            tvConnectStatue.setText("连接成功");
//            if(isDebug)Log.i(TAG,"onConnect  连接成功");
        } else if (statue == 3) {
//            tvConnectStatue.setText("正在连接");
//            if(isDebug) Log.i(TAG,"onConnect  正在连接");
        }


    }


    @Override
    public void onReceive(byte[] message) {
        int[] bleCode = BlueService.arrayByteToInt(message);
//        Log.e("BlueService","BleActivity  -> onReceive  ："+ message[0]+"\t " + BlueService.intArrToString(message)+ "\t "+ BlueService.MECHAL_LOCK);
        switch (bleCode[0]) {
            case BlueService.CONNECTED:
                if(mBlueService!= null)
                {
                    mBlueService.getInformation();
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
        if (state == 1) {
//            btnBleAction.setEnabled(true);
            tvInfo.setText("Configuration information:" + message);
            //检测当前的蓝牙固件版本信息
            softVersion = message;

        }
        else if(state == 2)
        {
            tvTipMessage.setText("Upgrade failed, please try again after restarting Bluetooth");
            btnBleAction.setText("Bluetooth write failed");
            btnBleAction.setEnabled(false);
            mCircleView.stopCircle();
            mBlueService.onDisConnected();
            isFailure = true;
        }
        else if(state == 3)
        {
            tvVersion.setText("Bluetooth version:" + message);
            if(softVersion != null && message != null )
            {
                if(softVersion.equals("L5180140A") && message.equals("2.2.13"))
                {
                    tvTipMessage.setText("Bluetooth is already the latest version");
                    btnBleAction.setText("Bluetooth is already the latest version");
                    btnBleAction.setEnabled(false);
                }
                else
                {
                    //蓝牙版本可以升级
                    //版本更新
                    tvTipMessage.setText("Bluetooth is connected and can be upgraded");
                    btnBleAction.setText("Start upgrading Bluetooth");
                    btnBleAction.setEnabled(true);
                }
            }
            else
            {
                tvTipMessage.setText("Bluetooth is already the latest version");
                btnBleAction.setText("Bluetooth is already the latest version");
                btnBleAction.setEnabled(false);
            }

        }
        else if(state == 4){
            //蓝牙版本可以升级
            //版本更新
            tvTipMessage.setText("Bluetooth is connected and can be upgraded");
            btnBleAction.setText("Start upgrading Bluetooth");
            btnBleAction.setEnabled(true);
        }
        else if(state == 5)
        {
            //蓝牙版本是最新版本
            tvTipMessage.setText("Bluetooth is already the latest version");
            btnBleAction.setText("Bluetooth is already the latest version");
            btnBleAction.setEnabled(false);
        }
    }

    @Override
    public void onDevice(BluetoothDevice mDevice, int sign) {

    }

    @Override
    public void onProgress(String percent) {
        tvTipMessage.setText(percent);
        if(percent.contains("Bluetooth"))
        {
            mCircleView.stopCircle();

        }
    }


//    @Override
    public void initialization() {
        Intent intent = getIntent();
        mDeviceName = intent.getStringExtra("Bluetooth");
        //开始连接蓝牙，进行OAD 升级
        mBlueService.setDeviceName(mDeviceName);
        boolean isAccess = mBlueService.isMatch();
        if(!isAccess)
        {
            mBlueService.openAccessBlue();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBlueService.onScann();
                }
            },4500);
        }
        else
        {
            //扫描设备
            mBlueService.onScann();
        }


    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble);
        initView();
    }


    private void initView()
    {
        tvInfo = findViewById(R.id.tv_ble_progress_info);
        tvVersion = findViewById(R.id.tv_ble_progress_version);
        tvTipMessage = findViewById(R.id.tv_ble_tip_message);
        btnBleAction = findViewById(R.id.btn_ble_oad_action);
//        tvCurrentVersion = findViewById(R.id.tv_ble_oad_name);
        mCircleView = findViewById(R.id.view_ble_oad_circle);
//        ActionBar mActionBar =  getSupportActionBar();
//        mActionBar.setDisplayHomeAsUpEnabled(true);//添加一个返回菜单
        ImageView actionBarLeft = findViewById(R.id.actionbar_left);
        TextView actionBarTitle = findViewById(R.id.actionbar_title);
        actionBarLeft.setImageResource(R.mipmap.back_arraw);
        actionBarTitle.setText("Update Bluetooth");
        actionBarLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBlueService!= null)
                {
                    mBlueService.onDisConnected();
                }
                UpdatBluetooth.this.finish();
            }
        });
    }

    public void onBleAction(View view)
    {
        boolean isConnect = mBlueService.isConnect();
        if(isConnect)
        {
//            mBlueService.onLoadOADFile(mUpgradeVeriosn);
            mBlueService.updateAssert("L5180140A_22D.bin");

        }
        else
        {
            DialogUtils.showToast(UpdatBluetooth.this,"Bluetooth is not connected yet!");
            return ;
        }
        mCircleView.startCircle();
        btnBleAction.setEnabled(false);
        btnBleAction.setText("Bluetooth upgrade");
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
    protected void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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





}
