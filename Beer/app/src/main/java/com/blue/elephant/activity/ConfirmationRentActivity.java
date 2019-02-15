package com.blue.elephant.activity;

import android.Manifest;
import android.app.Fragment;
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
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.blue.elephant.R;
import com.blue.elephant.custom.bluetooth.BlueCallback;
import com.blue.elephant.custom.bluetooth.BlueService;
import com.blue.elephant.custom.bluetooth.SubBlueService;
import com.blue.elephant.custom.bluetooth.VehicleInfo;
import com.blue.elephant.util.CallBack;
import com.blue.elephant.util.ContentPath;
import com.blue.elephant.util.DialogUtils;
import com.blue.elephant.util.NetUtil;
import com.blue.elephant.util.QuickLoadCallBack;

import org.json.JSONObject;
import org.xutils.http.RequestParams;

import static com.blue.elephant.activity.InsuranceActivity.OPTION;


public class ConfirmationRentActivity extends BaseActivity implements BlueCallback {

//    private TextView mInsuranceView, mSerialView;
    private boolean isRent;
    private String mInsurance;
    private String mImagePath;
    private String mBikeCode;
    private String mBikeID;
    private String mOrderID;
    private String mOrderCode;
    private String mStartTime;
    private String mBluetooth;
    private boolean isEnd;

    private onConnectListener mConnectListener;
    private RentBikeFragment mRentragment;
    private RentOrderFragment mRentOrder;

    protected BlueService mBlueService;

    public void setConnectListener(onConnectListener mListener)
    {
        this.mConnectListener = mListener;
    }
    private VehicleInfo mVehicle;
    protected Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what)
            {


                case 0X02:
                    if(mBlueService != null)
                        mBlueService.onRestartConnect();
                    else{
                        mHandler.sendEmptyMessage(0X03);
                    }
                    break;
                case 0X03:
                    if(mBlueService!= null)
                    {
                        boolean isConnect = mBlueService.isConnect();
                        if(!isConnect)
                        {
                            mBlueService.closeAccess();
                        }

                    }
                    break;
                case 0X04:
                    if(mBlueService!= null)
                    {
                        boolean isConnect = mBlueService.isConnect();
                        if(!isConnect)
                        {
                            mBlueService.openAccessBlue();
                            mBlueService.onConnect();
                        }
                    }
                    break;


                case 0X05:
                    if(mBlueService == null)
                        DialogUtils.showToast(ConfirmationRentActivity.this,R.string.confirm_load_error);
                    else
                    {
                        if(!mBlueService.isConnect())
                        {
                            DialogUtils.showToast(ConfirmationRentActivity.this, R.string.confirm_load_error);
                        }
                    }
                    break;

                case 0X06:
                    if(mConnectListener!= null)
                    {
                        if(mBlueService != null )
                        {
                            if(!mBlueService.isConnect())
                            {
                                mConnectListener.onShow();
                            }
                        }

                    }
                    break;

                case 0X07:
                    if(mBlueService!= null)
                    {
                        boolean isConnect =  mBlueService.isConnect();
                        if(!isConnect && mConnectListener!= null)
                        {
                            mConnectListener.onAction();
                        }
                    }
                    break;
            }
        }
    };





    @Override
    public void onConnect(int statue) {
        switch (statue)
        {
            case 0: //连接失败

                break;
            case 2:  //连接成功
                //发送版本信息
                if(mBlueService!= null)
                {
                    mBlueService.sendCommonData(BlueService.SET_CONNECT);
                }
//                Log.i("BlueService","连接成功  onConnect ");
                break;
            case 3: //正在连接

                break;
            case 4: // 重新连接
                mHandler.sendEmptyMessage(0X02);

        }
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
                if (bleCode[1] == 1) {
                    //显示按钮
                }
                if(bleCode[1]==0){
                    mVehicle.getDeadline(message);
                }
//                Log.i("BlueService"," rent bike  0X02");
//                Log.e("BlueService","============  检查锁的状态  ==========");
                break;

            case 0X03:                                         //3 check lock status
                mVehicle.getGuardStatus(message);
                if(mBlueService != null)
                {
                    mBlueService.sendCommonData(BlueService.GET_VERSION);
                }
//                Log.i("BlueService"," rent bike  GET_VERSION");
//                Log.e("BlueService","============  请求鞍座锁改变状态  ==========");
                break;

            case 0X04:
                mVehicle.getDeviceInfo(message);
//                logUserOperation();  ???
                byte[] mCommand= SubBlueService.timeToRent(1, 0, 0, 0);
                if(mBlueService != null)
                {
                    mBlueService.sendCommonData(mCommand);
                }
//                Log.i("BlueService"," rent bike  DeadLine");
                break;

            case 0X05:
                DialogUtils.dismissProgressDialog();//c
                mVehicle.getErrorStatus(message);
//                electricityPercent = mVehicle.electricityPercent;
//                strRemainderMileage = mVehicle.strRemainderMileage;
//                errorStatus = mVehicle.mErrorStatus;
//                String tip =  getString(R.string.BleVersion) + ": " + mVehicle.mVersion + "\n" + getString(R.string.BleFault) +
//                        ": " + errorStatus + "\n" + getString(R.string.Lat) + ": " + latitude + "\n" + getString(R.string.Lng) + ": " + longitude + "\n" +
//                        getString(R.string.BikeEleTitle) + ": " + electricityPercent +
//                        "(" + mVehicle.Qremainder  + "/" + mVehicle.Qmax + ")";
//                View view = LayoutInflater.from(EndOfOrderActivity.this).inflate(R.layout.item_bluetooth_status,null);
//                RelativeLayout mContainer = view.findViewById(R.id.item_bluetooth_static_container);
//                int popHeight = (int) (tip.split("\n").length * getResources().getDimension(R.dimen.pop_space_line) + getResources().getDimension(R.dimen.pop_space_height));
//                int popWidth = (int) getResources().getDimension(R.dimen.pop_width);
//                RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(popWidth,popHeight);
//                textParams.addRule(RelativeLayout.CENTER_IN_PARENT);
//                mContainer.setLayoutParams(textParams);
//                final PopupWindow mPopWindow = DialogUtils.showMapMenu(EndOfOrderActivity.this,btnRealTime,view);
//                TextView tvTip = view.findViewById(R.id.item_bluetooth_status_message);
//                tvTip.setText(tip);
//                Button btnCancel = view.findViewById(R.id.item_bluetooth_status_ok);
//                btnCancel.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        mPopWindow.dismiss();
//                        setCarStatus();
//                    }
//                });
                break;

            case 0X06:                  //a
                mVehicle.getElectric(message);
                if(mBlueService != null)
                {
                    mBlueService.sendCommonData(BlueService.GET_MILEAGE);
                }

                break;

            case 0X07:                   //b
                mVehicle.getMileage(message);
                if(mBlueService != null)
                {
                    mBlueService.sendCommonData(BlueService.GET_ERROR_STATUS);
                }

                break;

            case 0X12:
                DialogUtils.dismissProgressDialog();
                String seatStatus = getSeatStatus(message);
                DialogUtils.showToast(ConfirmationRentActivity.this,seatStatus);
                break;

            case 0X13:
                DialogUtils.dismissProgressDialog();
                break;

            case 0X0E:
                mVehicle.getVersion(message);                          //4. loading finished

                if(mConnectListener!= null)
                    mConnectListener.onShow();
                else
                {
//                    Log.i("BlueService"," rent bike  show   mConnectListener is null");
                }
//                Log.i("BlueService"," rent bike  show");
//                mStopView.setEnabled(true);
                //
//                enable();
//                if (mVehicle.isGuarded) {
//                    ivElectric.setImageResource(R.mipmap.connect_lock);
//                } else {
//                    ivElectric.setImageResource(R.mipmap.connect_unlock);
//                }
//                Log.e("BlueService","============  修改锁的状态  ==========");
                break;
            case 0X0F:                                           //2 check electric lock status
                mVehicle.getElecLock(message);
                if(mBlueService != null)
                {
                    mBlueService.sendCommonData(BlueService.SET_UNLOCK);
                }
//                Log.i("BlueService"," rent bike  SET_UNLOCK");
//                if (mVehicle.isElectOpen) {
//                    if(mBlueService != null)
//                    {
//                        mBlueService.sendCommonData(BlueService.SET_LOCK);
//                    }
//
//                } else {
//                    if(mBlueService != null)
//                    {
//                        mBlueService.sendCommonData(BlueService.SET_UNLOCK);
//                    }
//                }
//                Log.e("BlueService","============  请求电子锁改变状态  ==========");
                break;

            case 0X30:
                if (bleCode[1] == 0x00) {
                    byte[] byteCommand = SubBlueService.timeToRent(1, 0, 0, 0);
                    byteCommand[0] = 0X30;
                    byteCommand[1] = 0X00;
                    byteCommand[2] = 0X00;
                    if(mBlueService != null)
                    {
                        mBlueService.sendCommonData(byteCommand);
                    }

                }
//                boolean isHeart = true;
                break;

            case 0XE0: // 查询当前的设备信息
                if (bleCode[1] == 0x04) {

                    if(mBlueService != null)
                    {
                        mBlueService.sendCommonData(BlueService.GET_DEVICE_INFO);
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


    public String getSeatStatus(byte[] mByte){
        String seatStatus= "";
        switch(mByte[1]){
            case 0x00:
                seatStatus=getResources().getString(R.string.connect_saddle_unlock);
                break;
            case 0x01:
                seatStatus=getResources().getString(R.string.connect_saddle_lock);
                break;
            case 0x02:
                seatStatus=getResources().getString(R.string.connect_saddle_locking);
                break;
            case 0x03:
                seatStatus=getResources().getString(R.string.connect_saddle_unlocking);
                break;
            case 0x05:
                seatStatus=getResources().getString(R.string.connect_saddle_open);
                break;
            case 0x06:
                seatStatus=getResources().getString(R.string.connect_saddle_close);
                break;
        }
        return seatStatus;
    }

    public boolean getConnectState()
    {
        if(mBlueService == null)
        {
            return mBlueService.isConnect();
        }
        else
        {
            return false;
        }
    }

    public void onConnectDevice(String bluetooth)
    {
        if(bluetooth.isEmpty())
        {
            DialogUtils.showToast(ConfirmationRentActivity.this,"Bluetooth address is obtained, please try again");
            return ;
        }
        if(mBlueService == null)
        {
            mBluetooth = bluetooth;
            DialogUtils.showToast(ConfirmationRentActivity.this,"Failed to initialize device information, please try again");
            mHandler.sendEmptyMessage(0X03);//绑定失败！
            return ;
        }
        mBlueService.setDeviceName(bluetooth);
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
        mHandler.sendEmptyMessageDelayed(0X07,14000);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_rent);
         mVehicle = new VehicleInfo(ConfirmationRentActivity.this);
        try {
            Intent intent = getIntent();
            isRent = intent.getBooleanExtra("Rent", false);
            mInsurance = intent.getStringExtra(OPTION);
            mImagePath = intent.getStringExtra("ImagePath");
            mBikeCode = intent.getStringExtra("BikeCode");
            mBikeID  = intent.getStringExtra("BikeID");
            mOrderID = intent.getStringExtra("OrderID");
            mOrderCode = intent.getStringExtra("OrderCode");
            mStartTime = intent.getStringExtra("StartTime");
            mBluetooth = intent.getStringExtra("Bluetooth");
//            mBluetooth = "CC78AB6F1211";
            isEnd = intent.getBooleanExtra("EndStatus",false);
        }catch(Exception e)
        {
            Log.e("Rent","" + e.getMessage());
        }

        init();

    }


    private void init()
    {

        Bundle mRentBundle = new Bundle();
        mRentBundle.putString("ImagePath",mImagePath);
        mRentBundle.putString(OPTION,mInsurance);
        mRentBundle.putString("BikeCode",mBikeCode);
        mRentBundle.putString("Bluetooth",mBluetooth);
        mRentBundle.putString("BikeID",mBikeID);
        Bundle mOrderBundle = new Bundle();
        mOrderBundle.putString("BikeCode",mBikeCode);
        mOrderBundle.putString("OrderID",mOrderID);
        mOrderBundle.putString("OrderCode",mOrderCode);
        mOrderBundle.putString("ImagePath",mImagePath);
        mOrderBundle.putString(OPTION,mInsurance);
        mOrderBundle.putString("StartTime",mStartTime);
        mOrderBundle.putBoolean("EndStatus",isEnd);

        mRentOrder = new RentOrderFragment();
        mRentragment = new RentBikeFragment();

        mRentOrder.setArguments(mOrderBundle);
        mRentragment.setArguments(mRentBundle);
//        Log.i("Upload","Confirm : " + mImagePath);

        FragmentManager mFragmentManager = getSupportFragmentManager();
        FragmentTransaction mTransaction =  mFragmentManager.beginTransaction();
        if(isRent)
        {
            mTransaction.add(R.id.base_rent_container,mRentragment);
        }
        else
        {
            mTransaction.add(R.id.base_rent_container,mRentOrder);
        }
        mTransaction.commit();

    }



    @Override
    protected void onStart() {
        super.onStart();
        Intent bindService = new Intent(ConfirmationRentActivity.this,BlueService.class);
        bindService(bindService,mServiceConnection, Service.BIND_AUTO_CREATE);
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

    protected ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service instanceof BlueService.BleBinder) {
                BlueService.BleBinder mBinder = (BlueService.BleBinder) service;
                mBlueService = mBinder.getService();
                mBlueService.setCallback(ConfirmationRentActivity.this);
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


    public void initialization() {

        if(mBlueService == null)
        {
            DialogUtils.showToast(ConfirmationRentActivity.this,R.string.device_list_error);
            return ;
        }
        if(mConnectListener!= null)
        {
            mConnectListener.init();
//            Log.i("BlueService","连接成功 init  ");
        }
        else
        {
//            Log.i("BlueService","连接成功  mConnectListener is null  ");
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

    protected void disConnectService()
    {
        mHandler.removeMessages(0X01);
        mHandler.removeMessages(0X02);
        mHandler.removeMessages(0X03);
        mHandler.removeMessages(0X04);
        mHandler.removeMessages(0X05);
        mHandler.removeMessages(0X07);
        if(mBlueService!= null)
        {
            mBlueService.onDisConnected();
        }
//        Log.i("BlueService","disConnectService");
        ConfirmationRentActivity.this.finish();
    }

    public interface onConnectListener{
        public void onShow();
        public void init();
        public void onAction();
    }


}
