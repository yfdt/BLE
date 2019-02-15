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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.blue.elephant.R;
import com.blue.elephant.custom.bluetooth.BlueCallback;
import com.blue.elephant.custom.bluetooth.BlueService;
import com.blue.elephant.custom.bluetooth.SubBlueService;
import com.blue.elephant.custom.bluetooth.VehicleInfo;
import com.blue.elephant.util.DialogUtils;


/***
 *维修流程：
 */
public class MaintenanceActivity extends BaseActivity {

    public static final String RepairStatus = "RepairStatus";

    private int mRepair = 0;
    private String mBikeID;
    private String mBikeCode;
    private String mBluetooth;
    private String mMaintenance;
    private FrameLayout mFrameLayout;

    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;

    private Fragment mConfirmFrag,mDetailFrag;




    private BlueService mBlueService;
    private VehicleInfo mVehicle;

    public static int isStart = 0;

    public void onChange(Bundle bundle)
    {
        bundle.putBoolean("Change",true);
        if(mFragmentTransaction != null && mDetailFrag != null)
        {
            mDetailFrag.setArguments(bundle);
            mFragmentTransaction.add(R.id.maintenance_frame,mDetailFrag);
            mFragmentTransaction.commit();
        }
        else{
            mFragmentManager =  getSupportFragmentManager();
            mFragmentTransaction = mFragmentManager.beginTransaction();
            mDetailFrag = new DetailFragment();
            mDetailFrag.setArguments(bundle);
            mFragmentTransaction.add(R.id.maintenance_frame,mDetailFrag);
            mFragmentTransaction.commit();
        }

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance);
        mVehicle = new VehicleInfo(MaintenanceActivity.this);
        mFrameLayout = findViewById(R.id.maintenance_frame);
        mRepair = getIntent().getIntExtra(RepairStatus,1);
        mBikeID = getIntent().getStringExtra("BikeID");
        mBikeCode = getIntent().getStringExtra("BikeCode");
        mBluetooth = getIntent().getStringExtra("Bluetooth");
        mMaintenance = getIntent().getStringExtra("Maintenance");
        initView();
    }


    private void initView()
    {
        mConfirmFrag = new ConfirmFragment();
        mDetailFrag = new DetailFragment();

        Bundle mConfigBundle = new Bundle();
        mConfigBundle.putString("BikeID",mBikeID);
        mConfigBundle.putString("BikeCode",mBikeCode);
        mConfigBundle.putString("Bluetooth",mBluetooth);
        mConfirmFrag.setArguments(mConfigBundle);

        Bundle mDetailBundle = new Bundle();
        mDetailBundle.putString("Maintenance",mMaintenance);

        mFragmentManager =  getSupportFragmentManager();

        mDetailFrag.setArguments(mDetailBundle);

        mFragmentTransaction = mFragmentManager.beginTransaction();
        if(mRepair ==1 )
        {
            mFragmentTransaction.add(R.id.maintenance_frame,mConfirmFrag);
        }
        else
        {
            mFragmentTransaction.add(R.id.maintenance_frame,mDetailFrag);
        }
        mFragmentTransaction.commit();
    }


    /****************************************** 蓝牙连接   ********************************/
    private onConnectListener mConnectListener;

    public void setConnectListener(onConnectListener mListener)
    {
        this.mConnectListener = mListener;
    }

    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };


    @Override
    protected void onStart() {
        super.onStart();
        Intent bindService = new Intent(MaintenanceActivity.this,BlueService.class);
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
                mBlueService.setCallback(mBlueCallback);
                //进行连接操作：
                if(mBlueService == null)
                {
                    DialogUtils.showToast(MaintenanceActivity.this,R.string.device_list_error);
                    return ;
                }
                if(mConnectListener!= null)
                    mConnectListener.init();
                else
                {
                    Log.i("BlueService","连接成功  mConnectListener is null  ");
                }
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
        if(mBlueService!= null)
        {
            mBlueService.onDisConnected();
        }
//        Log.i("BlueService","disConnectService");
        MaintenanceActivity.this.finish();
    }

    protected void disConnect()
    {
        mHandler.removeMessages(0X01);
        mHandler.removeMessages(0X02);
        mHandler.removeMessages(0X03);
        mHandler.removeMessages(0X04);
        mHandler.removeMessages(0X05);
        if(mBlueService!= null)
        {
            mBlueService.onDisConnected();
        }
    }


    public void setRentTime(long time)
    {
        int day = (int) (time/(24* 60*60*1000));
        int hour = (int) (time/(60*60*1000));
        int min = (int) (time / (60* 1000));
        int second = (int) (time / 1000);
        byte[] mCommand= SubBlueService.timeToRent(day, hour, min, second);
        if(mBlueService != null)
        {
            mBlueService.sendCommonData(mCommand);
        }
    }

    public void closeMaintenance()
    {
        isStart = 4;
        if(mBlueService != null)
        {
            mBlueService.sendCommonData(BlueService.SET_LOCK);
        }
    }


    private BlueCallback mBlueCallback = new BlueCallback() {
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
//                    Log.i("BlueService","请求蓝牙版本信息");
                    break;

                case 0X02:
                    //设置维修的租期
                    if(mBlueService != null)
                    {
                        mBlueService.sendCommonData(BlueService.SET_UNLOCK);
                    }
//                    Log.i("BlueService","请求解锁命令");
//                    if(mConnectListener!= null)
//                            mConnectListener.onAction();
//
//                    if (bleCode[1] == 1) {
//                        //显示按钮
//                        if(mBlueService != null)
//                        {
//                            mBlueService.sendCommonData(BlueService.GET_ELECTLOCK);//1. check lock status
//                        }
//                    }
//                    if(bleCode[1]==0){
//                        mVehicle.getDeadline(message);
//                    }
//                Log.e("BlueService","============  检查锁的状态  ==========");
                    break;

                case 0X03:                                         //3 check lock status
                    mVehicle.getGuardStatus(message);
                    int statue = bleCode[1];

                    if(mConnectListener!= null)
                    {
                        if(isStart ==4)
                        {
                            isStart = 0;
                            mConnectListener.onAction();
//                            Log.i("BlueService","关闭车辆锁");
                        }
                        else
                        {
                            mConnectListener.onShow(statue);
//                            Log.i("BlueService","打开车辆锁");
                        }
                    }
//                    Log.i("BlueService","返回状态");
//                    if(mBlueService != null)
//                    {
//                        mBlueService.sendCommonData(BlueService.GET_VERSION);
//                    }
//                Log.e("BlueService","============  请求鞍座锁改变状态  ==========");
                    break;

                case 0X04:
                    mVehicle.getDeviceInfo(message);
                    if(mBlueService != null)
                    {
                        mBlueService.sendCommonData(BlueService.GET_VERSION);
                    }
//                    Log.i("BlueService","请求蓝牙硬件信息");
//                logUserOperation();  ???
//                    byte[] mCommand= SubBlueService.timeToRent(1, 0, 0, 0);
//                    if(mBlueService != null)
//                    {
//                        mBlueService.sendCommonData(mCommand);
//                    }

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
//                    if(mBlueService != null)
//                    {
//                        mBlueService.sendCommonData(BlueService.GET_MILEAGE);
//                    }

                    break;

                case 0X07:                   //b
                    mVehicle.getMileage(message);
//                    if(mBlueService != null)
//                    {
//                        mBlueService.sendCommonData(BlueService.GET_ERROR_STATUS);
//                    }

                    break;

                case 0X12:
                    DialogUtils.dismissProgressDialog();
                    String seatStatus = getSeatStatus(message);
                    DialogUtils.showToast(MaintenanceActivity.this,seatStatus);
                    break;

                case 0X13:
//                    DialogUtils.dismissProgressDialog();
                    break;

                case 0X0E:
                    mVehicle.getVersion(message);                          //4. loading finished
                    if(mBlueService != null)
                    {
                        mBlueService.sendCommonData(BlueService.GET_ELECTLOCK);//1. check lock status
                    }
//                    Log.i("BlueService","查看车辆锁状态");
//                    if(mConnectListener!= null)
//                        mConnectListener.onShow();
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
                    boolean isOPen = message[1]==0x01? true:false;
                    if(mConnectListener!= null)
                    {
                        mConnectListener.onRent(isOPen);
                    }
//                    Log.i("BlueService","检查车锁的状态： "+ isOPen + "\t 设置租期");
//                    if(mBlueService != null)
//                    {
//                        mBlueService.sendCommonData(BlueService.SET_UNLOCK);
//                    }
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
                        byte[] byteCommand = BlueService.timeToRent(1, 0, 0, 0);
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

//                    if (bleCode[1] == 0x04) {
//                        if(mBlueService != null)
//                        {
//                            mBlueService.sendCommonData(BlueService.GET_DEVICE_INFO);
//                        }
//                    }

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
    };


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


    public void onConnectDevice(String bluetooth)
    {
        if(bluetooth == null)
        {
            DialogUtils.showToast(MaintenanceActivity.this,"The Bike Bluetooth address is not obtained, please try again");
            return ;
        }
        if(mBlueService == null)
        {
            mBluetooth = bluetooth;
            DialogUtils.showToast(MaintenanceActivity.this,"Failed to initialize device information, please try again");
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
//        mHandler.sendEmptyMessageDelayed(0X01,8000);
    }


    public interface onConnectListener{
        public void init();
        public void onRent(boolean isOPen);
        public void onShow(int statue);
        public void onAction();
    }


}
