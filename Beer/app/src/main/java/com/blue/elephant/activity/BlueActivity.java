package com.blue.elephant.activity;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;

import com.blue.elephant.custom.bluetooth.BlueCallback;
import com.blue.elephant.custom.bluetooth.BlueService;

public abstract class BlueActivity extends BaseActivity implements BlueCallback {

    protected BlueService mBlueService;


    @Override
    public void onReceive(byte[] message) {

    }

    @Override
    public void onRead(String message, int state) {

    }

    @Override
    public void onDevice(BluetoothDevice mDevice,int sign) {

    }

    @Override
    public void onConnect(int statue) {

    }

    @Override
    public void onProgress(String percent) {

    }

    public abstract void initialization();
    protected ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service instanceof BlueService.BleBinder) {
                BlueService.BleBinder mBinder = (BlueService.BleBinder) service;
                mBlueService = mBinder.getService();
                mBlueService.setCallback(BlueActivity.this);
                initialization();
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

    @Override
    protected void onStart() {
        super.onStart();
        Intent bindService = new Intent(BlueActivity.this,BlueService.class);
        bindService(bindService,mServiceConnection, Service.BIND_AUTO_CREATE);
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




}
