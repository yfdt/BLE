package com.blue.elephant.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/***
 * 绑定蓝牙服务SubBlueSerice
 */
public class EndOfOrderActivity extends BaseActivity implements BlueCallback {

    private TextView mSerialView,mOrderSerialView,
            mInsuranceView,mStartView,mStopView,
            mContractView;

    private String mBikeID,mBikeCode,mOrderID,mOrderCode,mImagePath,mInsurance,mStartTime,mBluetooth,mTimeZone;
    private int mElectricity = -1;
    protected SubBlueService mBlueService;
    private VehicleInfo mVehicle;

    private Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        ImageView actionBarLeft = findViewById(R.id.actionbar_left);
        TextView actionBarTitle = findViewById(R.id.actionbar_title);
        actionBarLeft.setImageResource(R.mipmap.back_arraw);
        actionBarTitle.setText(R.string.order_title);

        mSerialView = findViewById(R.id.order_detail_status_name);
        mOrderSerialView = findViewById(R.id.order_detail_serial);
        mInsuranceView = findViewById(R.id.order_detail_insurance);
        mStartView = findViewById(R.id.order_detail_start);
        mContractView = findViewById(R.id.order_detail_contract);
        mStopView = findViewById(R.id.order_detail_submit);
        mStopView.setEnabled(false);
        actionBarLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mBlueService != null)
                {
                    mBlueService.onDisConnected();
                }
                EndOfOrderActivity.this.finish();
            }
        });

        mContractView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = LayoutInflater.from(EndOfOrderActivity.this).inflate(R.layout.pop_image,null);
                ImageView mBikeImage = view.findViewById(R.id.pop_image);
                ImageView mBikeClose = view.findViewById(R.id.pop_image_close);
                new AnsyImage(mBikeImage).execute(ContentPath.prefix + "/" + mImagePath);
                final PopupWindow mImageWindow = DialogUtils.showAllMenu(EndOfOrderActivity.this,view,mContractView);
                mBikeClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mImageWindow.dismiss();
                    }
                });
            }
        });

        mStopView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mBlueService!= null)
                {
                    byte[] byteCommand = SubBlueService.timeToRent(0, 0, 0, 0);
                    mBlueService.sendCommonData(byteCommand);
                }
            }
        });

        mVehicle = new VehicleInfo(EndOfOrderActivity.this);

        //load data
        Intent intent = getIntent();
        mBikeID = intent.getStringExtra("BikeID");
        mBikeCode = intent.getStringExtra("BikeCode");
        mOrderID = intent.getStringExtra("OrderID");
        mOrderCode = intent.getStringExtra("OrderCode");
        mImagePath = intent.getStringExtra("ImagePath");
        mInsurance = intent.getStringExtra("Insurance");
        mStartTime = intent.getStringExtra("StartTime");
        mTimeZone = intent.getStringExtra("TimeZone");
        mSerialView.setText(mBikeCode);
        mOrderSerialView.setText(mOrderCode);
        String option = mInsurance.equals("1")? getResources().getString(R.string.insurance_ok):getResources().getString(R.string.insurance_no);
        mInsuranceView.setText(option);
        if(mTimeZone!= null)
        {
            if(!mTimeZone.equals(""))
            {
                mStartTime = DateUtil.getLocalTime(mStartTime,mTimeZone);
            }
        }
        mStartView.setText(mStartTime);
    }


    @Override
    protected void onStart() {
        super.onStart();
        Intent bindService = new Intent(EndOfOrderActivity.this,SubBlueService.class);
        bindService(bindService,mServiceConnection, Service.BIND_AUTO_CREATE);

    }


    protected ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service instanceof SubBlueService.BleBinder) {
                SubBlueService.BleBinder mBinder = (SubBlueService.BleBinder) service;
                mBlueService = mBinder.getService();
                mBlueService.setCallback(EndOfOrderActivity.this);
                loadBike();
            } else {
                Log.e("BlueService", "ServiceConnection 当前未找到Bind 类");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e("BlueService", "onServiceDisconnected 服务器连接数据失败！");
        }
    };


    private void loadBike()
    {
        if (!NetUtil.hasNet(EndOfOrderActivity.this)) {
            DialogUtils.showToast(EndOfOrderActivity.this, R.string.check_network_connect);
            return;
        }
        String url = ContentPath.bikeSerial ;
        RequestParams params = new RequestParams(url);
        params.addBodyParameter("bikecode", mBikeCode);
        String message = getResources().getString(R.string.order_detail_load);

        //modify song  dialog
//        DialogUtils.showProgressDialog(EndOfOrderActivity.this, message);
        show(message);
        //modify song  dialog
        onConnect(params, new CallBack() {
            @Override
            public void onResponse(String response) {

                Log.i("EndOfOrder","" + response);
                try{
                    JSONObject mResponseObject = new JSONObject(response);
                    String status = mResponseObject.optString("status");
                    if(status.equals("1"))
                    {
                        JSONObject mResult = mResponseObject.getJSONObject("result");
                        JSONObject mBike = mResult.getJSONObject("bike");
                        mBluetooth = mBike.optString("bluetooth");
                        mElectricity = mBike.optInt("electricity");
                        if(mBluetooth == null)
                        {
                            DialogUtils.showToast(EndOfOrderActivity.this,"Order information failed to be obtained, please try again");
                            return ;
                        }
                       mBlueService.setDeviceName(mBluetooth);
                        //                //检查权限，连接蓝牙
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

                            //modify song  reconnect
                            mHandler.postDelayed(mRestartRunnable,5000);
                            //modify song  reconnect

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
                                        reLogin(EndOfOrderActivity.this);
                                    }
                                }
                            });
                        }else
                        {
                            String msg = mResponseObject.optString("msg");
                            DialogUtils.showToast(EndOfOrderActivity.this,msg);
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

    //modify song  reconnect
    private Runnable mRestartRunnable = new Runnable() {
        @Override
        public void run() {
            if(!mBlueService.isConnect())
            {
                mBlueService.onRestartConnect();
            }
        }
    };
    //modify song  reconnect
    private void stopOrder()
    {
        if (!NetUtil.hasNet(EndOfOrderActivity.this)) {
            DialogUtils.showToast(EndOfOrderActivity.this,R.string.check_network_connect);
            return;
        }
        String url = ContentPath.stopOrder ;
        RequestParams params = new RequestParams(url);
        params.addBodyParameter("orderid", mOrderID);
        String message = getResources().getString(R.string.order_detail_finish);
        DialogUtils.showProgressDialog(EndOfOrderActivity.this, message);
        onConnect(params, new CallBack() {
            @Override
            public void onResponse(String response) {

//                Log.e("EndOfOrder","取消当前的提示框！！！！");
                //	{"status":1}
                try{
                    JSONObject mResponseObject = new JSONObject(response);
                    String mStatus = mResponseObject.optString("status");
                    if(mStatus.equals("1"))
                    {
                        if(mBlueService!= null)
                        {
                            mBlueService.onDisConnected();
                        }
                        //upload electric
                        updateStatus();
                    }
                    else
                    {
                        String errorCode =  mResponseObject.optString("errorcode");
                        if(errorCode.equals("60001"))
                        {
                            quickLogin(new QuickLoadCallBack() {
                                @Override
                                public void doSomeThing(Boolean boolon) {
                                    if (boolon == true) {
                                        stopOrder();
                                    } else {
                                        reLogin(EndOfOrderActivity.this);
                                    }
                                }
                            });
                        }else
                        {
                            String msg = mResponseObject.optString("msg");
                            DialogUtils.showToast(EndOfOrderActivity.this,msg);
                        }
                    }

                }catch(Exception e)
                {

                }

            }

            @Override
            public void onFailure() {
                DialogUtils.dismissProgressDialog();
                Log.e("EndOfOrder","取消当前的提示框！！！！");
            }
        });

    }
    //modify song  update status
    private void updateStatus()
    {
        String url = ContentPath.updateStatue ;
        RequestParams params = new RequestParams(url);
        params.addBodyParameter("bikeid", mBikeID);
        params.addBodyParameter("electricity", mElectricity+"");
        onConnect(params, new CallBack() {
            @Override
            public void onResponse(String response) {
                DialogUtils.dismissProgressDialog();
                Log.i("EndOfOrder","" + response);
                Intent intent = new Intent(EndOfOrderActivity.this,OrderDetailActivity.class);
                intent.putExtra("OrderID",mOrderID);
                startActivity(intent);
                EndOfOrderActivity.this.finish();
            }

            @Override
            public void onFailure() {
                DialogUtils.dismissProgressDialog();
            }
        });

    }
   //modify song  update status

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
            mHandler.removeCallbacks(mRestartRunnable);
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
                if(mBlueService != null)
                {
                    mBlueService.sendCommonData(SubBlueService.SET_LOCK);
                }
                stopOrder(); // end of order
//                Log.i("BlueService"," rent bike  0X02");
//                Log.e("BlueService","============  检查锁的状态  ==========");
                break;

            case 0X03:                                         //3 check lock status
                String mStatue = mVehicle.getGuardStatus(message);
//                if(message[1] == 0X02)
//                {
//
//                }
//                Log.i("BlueService"," rent bike  status:" + mStatue);
                break;

            case 0X04:
                mVehicle.getDeviceInfo(message);
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
                mVehicle.getVersion(message);
                //modify song  dialog
                dimiss();
                //modify song  dialog
                DialogUtils.dismissProgressDialog();
                Log.e("EndOfOrder","取消当前的提示框！！！！");
                mStopView.setEnabled(true);
                if(mBlueService != null)
                {
                    mBlueService.sendCommonData(SubBlueService.GET_ELECTLOCK);//1. check lock status
                }
//                byte[] mCommand= SubBlueService.timeToRent(1, 0, 0, 0);
//                if(mBlueService != null)
//                {
//                    mBlueService.sendCommonData(mCommand);
//                }
//                Log.i("BlueService"," rent bike  show");
//                Log.e("BlueService","============  修改锁的状态  ==========");
                break;
            case 0X0F:                                           //2 check electric lock status
                mVehicle.getElecLock(message);
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


    private class AnsyImage extends AsyncTask<String,Void,Bitmap> {

        private ImageView mImage;
        public AnsyImage(ImageView mImage)
        {
            this.mImage = mImage;
        }

        @Override
        protected Bitmap doInBackground(String... url) {

            Bitmap bm = null;
            try {
                URL aURL = new URL(url[0]);
                URLConnection conn = aURL.openConnection();
                conn.connect();
                InputStream is = conn.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);
                bm = BitmapFactory.decodeStream(bis);
                bis.close();
                is.close();
            } catch (IOException e) {
                Log.e("Hub","Error getting the image from server : " + e.getMessage().toString());
            }
            return bm;
        }


        @Override
        protected void onPostExecute(Bitmap result) {
            if(result != null)
            {
                mImage.setImageBitmap(result);              // how do I pass a reference to mChart here ?
            }
            else
            {
                mImage.setImageResource(R.mipmap.upload_error);
            }
        }
    }
    //modify song  dialog
    private ProgressDialog progressDialog;
    //crate progress
    protected void show(String msg)
    {
        if(progressDialog == null)
        {
            progressDialog = new ProgressDialog(EndOfOrderActivity.this);
        }
        progressDialog.setMessage(msg);
        progressDialog.show();
    }

    protected void dimiss()
    {
        if (progressDialog != null ) {
            if(progressDialog.isShowing())
            {
                progressDialog.dismiss();
            }
            progressDialog = null;
        }
    }
    //modify song  dialog

}
