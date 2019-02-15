package com.blue.elephant.activity;

import android.Manifest;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.blue.elephant.R;
import com.blue.elephant.custom.adapter.DeviceAdapter;
import com.blue.elephant.util.DialogUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class DeviceListActivity extends BlueActivity {

    private TextView tvMenu;
    private LinearLayout mMenuContsiner;
    private DeviceAdapter mAdapter;
    private ListView mListView;
    private Handler mHandler = new Handler();
    private ArrayList<JSONObject> mDeviceList = new ArrayList<>();
    private ArrayList<String> mAddressList = new ArrayList<>();

    @Override
    public void onConnect(int statue) {

    }

    @Override
    public void onDevice(BluetoothDevice mDevice,int ress) {
        try{
            String address = mDevice.getAddress();
            String name = mDevice.getName();
            String sign = ress + "";
            if(!mAddressList.contains(address))
            {
                JSONObject mObject = new JSONObject();
                mObject.put("Address",address);
                mObject.put("Name",name);
                mObject.put("Sign",sign);
                mAddressList.add(address);
                mDeviceList.add(mObject);
                mAdapter.setData(mDeviceList);

            }
//            Log.i("DeviceList"," " + mDevice.getAddress() + "\t address: "+ mAddressList.size() + "\t device list:" + mDeviceList.size() );
        }catch(Exception e)
        {
            Log.e("DeviceList","" + e.getMessage());
        }
    }



    @Override
    public void initialization() {
        //判断权限
        if(mBlueService == null)
        {
            DialogUtils.showToast(DeviceListActivity.this,R.string.device_list_error);
            return ;
        }
        onScan();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        ImageView actionBarLeft = findViewById(R.id.actionbar_left);
        TextView actionBatTitle = findViewById(R.id.actionbar_title);
        tvMenu = findViewById(R.id.actionbar_sub_menu);
        actionBarLeft.setImageResource(R.mipmap.back_arraw);
        actionBatTitle.setText(R.string.device_list_title);
        tvMenu.setText(R.string.device_list_menu_scan);
        mListView = findViewById(R.id.device_list_content);
        mAdapter = new DeviceAdapter(DeviceListActivity.this);
        mListView.setAdapter(mAdapter);
        actionBarLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeviceListActivity.this.finish();
            }
        });
        mMenuContsiner = findViewById(R.id.actionbar_right);
        mMenuContsiner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMenuContsiner.setEnabled(false);
                tvMenu.setText(R.string.device_list_menu);
                onScan();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mMenuContsiner.setEnabled(true);
                        tvMenu.setText(R.string.device_list_menu_scan);
                        //刷新数据
                    }
                },3000);
            }
        });


        mMenuContsiner.setEnabled(false);
        tvMenu.setText(R.string.device_list_menu);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mMenuContsiner.setEnabled(true);
                tvMenu.setText(R.string.device_list_menu_scan);
                //刷新数据
            }
        },3000);


        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               JSONObject mDevice = (JSONObject) mAdapter.getItem(position);
               Intent intent = getIntent();
               intent.putExtra("classic",7);
               intent.putExtra("result",mDevice.optString("Name"));
               setResult(-1,intent);
               DeviceListActivity.this.finish();
            }
        });


    }


    private void onScan()
    {
        mAddressList.clear();
        mDeviceList.clear();
        boolean isAccess = mBlueService.isMatch();
        if(!isAccess)
        {
            mBlueService.openAccessBlue();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBlueService.onScanAllDevice();
                }
            },4500);
        }
        else
        {
            //扫描设备
            mBlueService.onScanAllDevice();
        }
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



}
