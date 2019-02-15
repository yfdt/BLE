package com.blue.elephant.activity;

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
import android.widget.Toast;

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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import static com.blue.elephant.activity.InsuranceActivity.OPTION;

public class RentBikeActivity extends BaseActivity implements BlueCallback {

    private String mInsurance,mImagePath,mBikeCode,mBikeID,mBluetooth,mTimeZone;

    private TextView mSerialView,mInsuranceView,mSubmit;
    private BlueService mBlueService;
    private VehicleInfo mVehicle;


    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);

        ImageView actionBarLeft = findViewById(R.id.actionbar_left);
        TextView actionBarTitle = findViewById(R.id.actionbar_title);
        mInsuranceView = findViewById(R.id.confirm_insurance);
        mSerialView = findViewById(R.id.confirm_serial);
        mSubmit = findViewById(R.id.confirm_submit);
        final TextView tvOrderImage = findViewById(R.id.confirm_order);
        actionBarLeft.setImageResource(R.mipmap.back_arraw);
        actionBarTitle.setText(R.string.confirm_title);

        actionBarLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 if(mBlueService != null)
                 {
                     mBlueService.onDisConnected();
                 }
                 RentBikeActivity.this.finish();
            }
        });

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createOrder();
            }
        });

        tvOrderImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View view = LayoutInflater.from(RentBikeActivity.this).inflate(R.layout.pop_image,null);
                ImageView mBikeImage = view.findViewById(R.id.pop_image);
                ImageView mBikeClose = view.findViewById(R.id.pop_image_close);
                try {
                    mBikeImage.setImageURI(Uri.fromFile(new File(mImagePath)));
                }catch(Exception e)
                {
                    Log.e("RentBike","" + e.getMessage());
                }
                final PopupWindow mImageWindow = DialogUtils.showAllMenu(RentBikeActivity.this,view,tvOrderImage);
                mBikeClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mImageWindow.dismiss();
                    }
                });
            }
        });


        //load data
        mVehicle = new VehicleInfo(RentBikeActivity.this);
        Intent intent = getIntent();
        if(intent == null)
        {
            DialogUtils.showToast(RentBikeActivity.this,"Failed to load data, please try again");
            return ;
        }
        mInsurance = intent.getStringExtra(OPTION);
        mImagePath = intent.getStringExtra("ImagePath");
        mBikeCode = intent.getStringExtra("BikeCode");
        mBikeID = intent.getStringExtra("BikeID");
        mBluetooth = intent.getStringExtra("Bluetooth");

        String option = mInsurance.equals("1")? getResources().getString(R.string.insurance_ok):getResources().getString(R.string.insurance_no);
        mSerialView.setText(mBikeCode);
        mInsuranceView.setText(option);
        String message = getResources().getString(R.string.confirm_load);
        DialogUtils.showProgressDialog(RentBikeActivity.this, message);
        mSubmit.setEnabled(false);
//        Log.e("BlueService", "Bluetooth:" + mBluetooth);
        loadServer(mBikeCode);
    }

    private void loadServer(String bikeCode)
    {
        Map<String,String> heads = new HashMap<>();
        heads.put("bikecode",bikeCode);
        heads.put("appversion","1");
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
            }

            @Override
            public void onFailure() {

            }
        });

    }


    private void createOrder()
    {
        if (!NetUtil.hasNet(RentBikeActivity.this)) {
            DialogUtils.showToast(RentBikeActivity.this,R.string.check_network_connect);
            return;
        }
        String url = ContentPath.uploadPic ;
        RequestParams params = new RequestParams(url);
        params.addBodyParameter("bikeid", mBikeID);
        params.addBodyParameter("buyinsurance", mInsurance);
        params.addBodyParameter("contract", new File(mImagePath), "multipart/form-data");
        String message = getResources().getString(R.string.confirm_load);
        DialogUtils.showProgressDialog(RentBikeActivity.this, message);
//        Log.i("Confirmation","bike code : "+ mBikeCode );
        onConnect(params, new CallBack() {
            @Override
            public void onResponse(String response) {
                DialogUtils.dismissProgressDialog();
//                Log.i("Confirmation","ScanText  \t " + response);
                try{
                    JSONObject mResponseObject = new JSONObject(response);
                    String status = mResponseObject.optString("status");
                    if(status.equals("1"))
                    {
//                        Log.i("Confirmation","数据：  \t " + response);
                        JSONObject mResult = mResponseObject.getJSONObject("result");
                        JSONObject mOrderObject = mResult.optJSONObject("order");
                        String mOrderID = mOrderObject.optString("orderid");
                        String mInsurance = mOrderObject.optString("buyinsurance");
                        String mBikeCode = mOrderObject.optString("bikecode");
                        String mOrderCode = mOrderObject.optString("orderno");
                        String mImagePath = mOrderObject.optString("contractpath");
                        String mStartTime = mOrderObject.optString("starttime");
                        if(mBlueService!= null)
                        {
                            mBlueService.onDisConnected();
                        }
                        Intent intent = new Intent(RentBikeActivity.this,EndOfOrderActivity.class);
                        intent.putExtra("BikeID",mBikeID);
                        intent.putExtra("BikeCode",mBikeCode);
                        intent.putExtra("OrderID",mOrderID);
                        intent.putExtra("OrderCode",mOrderCode);
                        intent.putExtra("ImagePath",mImagePath);
                        intent.putExtra("Insurance",mInsurance);
                        intent.putExtra("StartTime",mStartTime);
                        if(mTimeZone == null)
                            mTimeZone = "";
                        intent.putExtra("TimeZone",mTimeZone);
                        startActivity(intent);
                        RentBikeActivity.this.finish();
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
                                        createOrder();
                                    } else {
                                        reLogin(RentBikeActivity.this);
                                    }
                                }
                            });
                        }else
                        {
                            String msg = mResponseObject.optString("msg");
                            DialogUtils.showToast(RentBikeActivity.this,msg);
                        }
                    }
                }catch(Exception e)
                {
                    Log.e("Confirmation",""+ e.getMessage());
                }

            }

            @Override
            public void onFailure() {
                DialogUtils.dismissProgressDialog();
            }
        });

    }



    @Override
    protected void onStart() {
        super.onStart();
        Intent bindService = new Intent(RentBikeActivity.this,BlueService.class);
        bindService(bindService,mServiceConnection, Service.BIND_AUTO_CREATE);
    }


    protected ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service instanceof BlueService.BleBinder) {
                BlueService.BleBinder mBinder = (BlueService.BleBinder) service;
                mBlueService = mBinder.getService();
                mBlueService.setCallback(RentBikeActivity.this);
                if(mBluetooth== null)
                {
                    DialogUtils.showToast(RentBikeActivity.this,"Failed to load data, re-create order");
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

                    //modify song  reconnect
                    mHandler.postDelayed(mRestartRunnable,5000);
                    //modify song  reconnect
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
                if(mBlueService != null)
                {
                    mBlueService.sendCommonData(BlueService.GET_ELECTLOCK);//1. check lock status
                }
//                Log.i("BlueService"," rent bike  0X02");
//                Log.e("BlueService","============  检查锁的状态  ==========");
                break;

            case 0X03:                                         //3 check lock status
                String mStatue = mVehicle.getGuardStatus(message);
                DialogUtils.dismissProgressDialog();
                mSubmit.setEnabled(true);
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
                byte[] mCommand= BlueService.timeToRent(1, 0, 0, 0);
                if(mBlueService != null)
                {
                    mBlueService.sendCommonData(mCommand);
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
                    byte[] byteCommand = BlueService.timeToRent(1, 0, 0, 0);
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
}
