package com.blue.elephant.custom.bluetooth;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.blue.elephant.R;
import com.blue.elephant.util.DialogUtils;

public abstract class UpdateBaseActivity extends AppCompatActivity implements BlueCallback {


    public UpdateBluetoothService mBlueService;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    @Override
    public void onProgress(String percent)
    {

    }

    @Override
    public void onDevice(BluetoothDevice mDevice ,int rss) {

    }

    @Override
    public void onConnect(int statue) {

    }

    @Override
    public void onReceive(byte[] message) {

    }

    public void onRead(String message,int state)
    {

    }

    public abstract void initialization();

    protected ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service instanceof UpdateBluetoothService.BleBinder) {
                UpdateBluetoothService.BleBinder mBinder = (UpdateBluetoothService.BleBinder) service;
                mBlueService = mBinder.getService();
                mBlueService.setCallback(UpdateBaseActivity.this);
                initialization();
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
    protected void onStart() {
        super.onStart();
        Intent bindService = new Intent(UpdateBaseActivity.this,UpdateBluetoothService.class);
        bindService(bindService,mServiceConnection, Service.BIND_AUTO_CREATE);
    }





    /* check if user agreed to enable BT */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // user didn't want to turn on BT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == 1) {
                if (resultCode == Activity.RESULT_CANCELED) {
                    DialogUtils.showToast(UpdateBaseActivity.this,getString(R.string.BleOadOpenBluetooth));
                    return;
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


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
    protected void onStop() {
        super.onStop();
        try {
            unbindService(mServiceConnection);
        }catch(Exception e)
        {
            Log.e("BlueService","该服务未注册");
        }
    }

}
