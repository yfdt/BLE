package com.blue.elephant.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.blue.elephant.R;
import com.blue.elephant.custom.scan.BeepManager;
import com.blue.elephant.custom.scan.CameraManager;
import com.blue.elephant.custom.scan.CaptureActivityHandler;
import com.blue.elephant.custom.scan.DecodeThread;
import com.blue.elephant.custom.scan.InactivityTimer;
import com.blue.elephant.util.CallBack;
import com.blue.elephant.util.ContentPath;
import com.blue.elephant.util.DialogUtils;
import com.blue.elephant.util.NetUtil;
import com.blue.elephant.util.QuickLoadCallBack;
import com.google.zxing.Result;

import org.json.JSONObject;
import org.xutils.http.RequestParams;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static com.blue.elephant.activity.MaintenanceActivity.RepairStatus;

public class ScanActivity extends BaseActivity implements SurfaceHolder.Callback ,View.OnClickListener{

    /***
     * 1 租用
     * 3 未出租尚未维修
     * 4 未出租维修中
     */
    public static final String Status = "Status";


    private final String TAG = "Test";
    private CameraManager cameraManager;
    private CaptureActivityHandler handler;
    private InactivityTimer inactivityTimer;
    private BeepManager beepManager;

    private SurfaceView scanPreview = null;
    private RelativeLayout tvLight;
    private ViewGroup mScreenArea;
    private ImageView mScanArea;
    private Rect mCropRect = null;
    private boolean isHasSurface = false;

    private RelativeLayout mNumberView,mLightView;

    /***
     * 1. 当前未租车用户租车
     * 2. 当前租车，进入车辆详情
     * 3. 当前为租车，
     * 4. 车辆编号
     * 5. 车架编号
     * 6. IMEI 编号
     * 7. 蓝牙编号
     */
    private int mScanAction = 1;

    public Handler getHandler() {
        return handler;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    private String mTimeZone;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_scan);
        initView();
    }

    private void initView()
    {
        ImageView ivBack = findViewById(R.id.actionbar_left);
        ivBack.setImageResource(R.mipmap.back_arraw);
        ivBack.setOnClickListener(this);
        TextView tvTitle = findViewById(R.id.actionbar_title);
        tvTitle.setText(R.string.scan_title);


//        findViewById(R.id.qr_lock).setOnClickListener(this);
//        findViewById(R.id.qr_light).setOnClickListener(this);
        mNumberView = findViewById(R.id.scan_number);
        mLightView = findViewById(R.id.scan_flash);
        mNumberView.setOnClickListener(this);
        mLightView.setOnClickListener(this);

        scanPreview = (SurfaceView)findViewById(R.id.qr_surface);
        mScanArea = findViewById(R.id.scan_rectangle);
        mScreenArea = findViewById(R.id.qr_container);
        tvLight = findViewById(R.id.scan_flash);
        inactivityTimer = new InactivityTimer(this);
        beepManager = new BeepManager(this);

        //modify song Camera authorization
        //检测摄像头权限
//        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1)
//        {
//            if (ContextCompat.checkSelfPermission(this,
//                    android.Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
//                //先判断有没有权限 ，没有就在这里进行权限的申请
//                ActivityCompat.requestPermissions(this,
//                        new String[]{android.Manifest.permission.CAMERA},0);
//
//            }else {
//                //说明已经获取到摄像头权限了 想干嘛干嘛
//            }
//        }else {
//            //这个说明系统版本在6.0之下，不需要动态获取权限。
//
//        }
        //modify song Camera authorization

        mScanAction = getIntent().getIntExtra(Status,1);

        switch(mScanAction)
        {
            case 4:
            case 5:
            case 6:
            case 7:
                mNumberView.setVisibility(View.GONE);
                int width = (int) getResources().getDimension(R.dimen.light_height);
                RelativeLayout.LayoutParams mParams = new RelativeLayout.LayoutParams(width,RelativeLayout.LayoutParams.MATCH_PARENT);
                mParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                mLightView.setLayoutParams(mParams);
                break;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case 0:
                if (grantResults.length>0&&grantResults[0]!= PackageManager.PERMISSION_GRANTED){
                    //提醒用户手动开启权限
                    DialogUtils.showToast(ScanActivity.this,R.string.qr_tip_open_camera);
                }
                break;
            default:
                break;

        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        // CameraManager must be initialized here, not in onCreate(). This is
        // necessary because we don't
        // want to open the camera driver and measure the screen size if we're
        // going to show the help on
        // first launch. That led to bugs where the scanning rectangle was the
        // wrong size and partially
        // off screen.
        cameraManager = new CameraManager(getApplication());

        handler = null;

        if (isHasSurface) {
            // The activity was paused but not stopped, so the surface still
            // exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(scanPreview.getHolder());
        } else {
            // Install the callback and wait for surfaceCreated() to init the
            // camera.
            scanPreview.getHolder().addCallback(this);
        }

        inactivityTimer.onResume();

    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            Log.w("Test", "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw IApplication
            // RuntimeException.
            if (handler == null) {
                handler = new CaptureActivityHandler(this, cameraManager, DecodeThread.ALL_MODE);
            }

            initCrop();
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
            ioe.printStackTrace();
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            e.printStackTrace();
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            Log.w(TAG, "Unexpected error initializing camera", e);
            // displayFrameworkBugMessageAndExit();

        }
    }

    /**
     * 初始化截取的矩形区域
     */
    private void initCrop() {
        int cameraWidth = cameraManager.getCameraResolution().y;
        int cameraHeight = cameraManager.getCameraResolution().x;

        /** 获取布局中扫描框的位置信息 */
        int[] location = new int[2];
        mScanArea.getLocationInWindow(location);

        int cropLeft = location[0];
        int cropTop = location[1] - getStatusBarHeight();

        int cropWidth = mScanArea.getWidth();
        int cropHeight = mScanArea.getHeight();

        /** 获取布局容器的宽高 */
        int containerWidth = mScreenArea.getWidth();
        int containerHeight = mScreenArea.getHeight();

        /** 计算最终截取的矩形的左上角顶点x坐标 */
        int x = cropLeft * cameraWidth / containerWidth;
        /** 计算最终截取的矩形的左上角顶点y坐标 */
        int y = cropTop * cameraHeight / containerHeight;

        /** 计算最终截取的矩形的宽度 */
        int width = cropWidth * cameraWidth / containerWidth;
        /** 计算最终截取的矩形的高度 */
        int height = cropHeight * cameraHeight / containerHeight;

        /** 生成最终的截取的矩形 */
        mCropRect = new Rect(x, y, width + x, height + y);
    }
    private int getStatusBarHeight() {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = Integer.parseInt(field.get(obj).toString());
            return getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void displayFrameworkBugMessageAndExit() {
        // camera error
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.app_name));
        builder.setMessage("Camera error");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }

        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        builder.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        inactivityTimer.onPause();
        beepManager.close();
        cameraManager.closeDriver();
        if (!isHasSurface) {
            scanPreview.getHolder().removeCallback(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        inactivityTimer.shutdown();
    }

    /**
     * A valid barcode has been found, so give an indication of success and show
     * the results.
     *
     * @param rawResult The contents of the barcode.
     * @param bundle    The extras
     */
    public void handleDecode(Result rawResult, Bundle bundle) {
        inactivityTimer.onActivity();
        beepManager.playBeepSoundAndVibrate();
        //进行网络请求判断
        //判断车辆是否出租，出租则进入订单详情界面，否则进入保险确认界面
        Intent intent;
        switch (mScanAction)
        {
            case 1:
                //车辆未出租
                //进行网络请求：
                loadServer(rawResult.getText());
//                getRentStatus(rawResult.getText());
                break;


            case 2:
                //车辆出租，进入详情页
                intent = new Intent(ScanActivity.this,OrderDetailActivity.class);
                startActivity(intent);
                ScanActivity.this.finish();
                break;

            case 3:
                //车辆未出租且不在维修中，车辆维修
//                intent = new Intent(ScanActivity.this,MaintenanceActivity.class);
//                intent.putExtra(MaintenanceActivity.RepairStatus,2);
//                startActivity(intent);
//                ScanActivity.this.finish();
                getRepair(rawResult.getText());
                break;
            case 4:
                bundle.putInt("classic", 4);
                intent = new Intent();
                bundle.putInt("width", mCropRect.width());
                bundle.putInt("height", mCropRect.height());
                bundle.putString("result", rawResult.getText());
                intent.putExtras(bundle);
                this.setResult(RESULT_OK, intent);
                ScanActivity.this.finish();
                break;
            case 5:
                bundle.putInt("classic", 5);
                intent = new Intent();
                bundle.putInt("width", mCropRect.width());
                bundle.putInt("height", mCropRect.height());
                bundle.putString("result", rawResult.getText());
                intent.putExtras(bundle);
                this.setResult(RESULT_OK, intent);
                ScanActivity.this.finish();
                break;
            case 6:
                bundle.putInt("classic", 6);
                intent = new Intent();
                bundle.putInt("width", mCropRect.width());
                bundle.putInt("height", mCropRect.height());
                bundle.putString("result", rawResult.getText());
                intent.putExtras(bundle);
                this.setResult(RESULT_OK, intent);
                ScanActivity.this.finish();
                break;
            case 7:
                bundle.putInt("classic", 7);
                intent = new Intent();
                bundle.putInt("width", mCropRect.width());
                bundle.putInt("height", mCropRect.height());
                bundle.putString("result", rawResult.getText());
                intent.putExtras(bundle);
                this.setResult(RESULT_OK, intent);
                ScanActivity.this.finish();
                break;

        }

    }

    private void loadServer(final String bikeCode)
    {
        if (!NetUtil.hasNet(ScanActivity.this)) {
            DialogUtils.showToast(ScanActivity.this,R.string.check_network_connect);
            return;
        }
        Map<String,String> heads = new HashMap<>();
        heads.put("bikecode",bikeCode);
        heads.put("appversion","1");
        String message = getResources().getString(R.string.scan_load_data);
        DialogUtils.showProgressDialog(ScanActivity.this, message);
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
                getRentStatus(bikeCode);
            }

            @Override
            public void onFailure() {
                getRentStatus(bikeCode);
            }
        });
    }


    private void getRentStatus(final String bikeCode)
    {
        String url = ContentPath.bikeSerial ;
        RequestParams params = new RequestParams(url);
        params.addBodyParameter("bikecode", bikeCode);
//        Log.i("ScanActivity","bike code : "+ bikeCode );
        onConnect(params, new CallBack() {
            @Override
            public void onResponse(String response) {
                DialogUtils.dismissProgressDialog();
//                Log.i("ScanActivity","ScanText  \t " + response);
                try{
                    JSONObject mResponseObject = new JSONObject(response);
                    String status = mResponseObject.optString("status");
                    if(status.equals("1"))
                    {
//                        Log.i("ScanActivity","扫描数据：  \t " + response);
                        JSONObject mResult = mResponseObject.getJSONObject("result");
                        JSONObject mBike = mResult.optJSONObject("bike");
                        String bikeStatus = mBike.optString("rentstatus").toLowerCase();
                        String bluetooth = mBike.optString("bluetooth");
                        String mBikeId = mBike.optString("bikeid");
                        String mMaintenance = mResult.optString("maintenance");
                        if(!mMaintenance.equals(""))
                        {
                            DialogUtils.showToast(ScanActivity.this,R.string.scan_text_main);
                            return ;
                        }
                        if(bikeStatus.equals("occupied"))
                        {

                            JSONObject mOrderObject = mResult.optJSONObject("order");
                            Intent intent = new Intent(ScanActivity.this,EndOfOrderActivity.class);
                            intent.putExtra("BikeID",mBike.optString("bikeid"));
                            intent.putExtra("BikeCode",mBike.optString("bikecode"));
                            intent.putExtra("OrderID",mOrderObject.optString("orderid"));
                            intent.putExtra("OrderCode",mOrderObject.optString("orderno"));
                            intent.putExtra("ImagePath",mOrderObject.optString("contractpath"));
                            intent.putExtra("Insurance",mOrderObject.optString("buyinsurance"));
                            intent.putExtra("StartTime",mOrderObject.optString("starttime"));
                            if(mTimeZone!= null)
                            {
                                intent.putExtra("TimeZone",mTimeZone);
                            }
                            startActivity(intent);
                            ScanActivity.this.finish();
                            return ;
                        }
                        Intent intent = new Intent(ScanActivity.this,UploadActivity.class);
                        intent.putExtra("BikeCode",bikeCode);
                        intent.putExtra("BikeID",mBikeId);
                        intent.putExtra("Bluetooth",bluetooth);
                        startActivity(intent);
                        ScanActivity.this.finish();

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
                                        getRentStatus(bikeCode);
                                    } else {
                                        reLogin(ScanActivity.this);
                                    }
                                }
                            });
                        }else
                        {
                            String msg = mResponseObject.optString("msg");
                            DialogUtils.showToast(ScanActivity.this,msg);
                        }
                    }
                }catch(Exception e)
                {
                    Log.e("ScanActivity",""+ e.getMessage());
                }

            }

            @Override
            public void onFailure() {
                DialogUtils.dismissProgressDialog();
            }
        });
    }

    private void getRepair(final String bikeCode)
    {
        if (!NetUtil.hasNet(ScanActivity.this)) {
            DialogUtils.showToast(ScanActivity.this,R.string.check_network_connect);
            return;
        }
        String url = ContentPath.bikeSerial ;
        RequestParams params = new RequestParams(url);
        params.addBodyParameter("bikecode", bikeCode);
        String message = getResources().getString(R.string.scan_load_data);
        DialogUtils.showProgressDialog(ScanActivity.this, message);
//        Log.i("ScanActivity","bike code : "+ bikeCode );
        onConnect(params, new CallBack() {
            @Override
            public void onResponse(String response) {
                DialogUtils.dismissProgressDialog();
//                Log.i("ScanActivity","ScanText  \t " + response);
                try{
                    JSONObject mResponseObject = new JSONObject(response);
                    String status = mResponseObject.optString("status");
                    if(status.equals("1"))
                    {
//                        Log.i("ScanActivity","扫描数据：  \t " + response);
                        JSONObject mResult = mResponseObject.getJSONObject("result");
                        JSONObject mBike = mResult.getJSONObject("bike");
                        String bikeStatus = mBike.optString("rentstatus").toLowerCase();
                        String bluetooth = mBike.optString("bluetooth");
                        String mBikeId = mBike.optString("bikeid");
                        String mMaintenanceStatus = mResult.optString("maintenance");
                        if(bikeStatus.equals("occupied"))
                        {
                            DialogUtils.showToast(ScanActivity.this,R.string.maintenance_rent_tip);
                            return ;
                        }
                        if(mMaintenanceStatus.equals(""))
                        {
                            Intent intent = new Intent(ScanActivity.this,StartMaintenanceActivity.class);
                            intent.putExtra("BikeCode",bikeCode);
                            intent.putExtra("BikeID",mBikeId);
                            intent.putExtra("Bluetooth",bluetooth);
                            ScanActivity.this.finish();
                        }
                        else
                        {
                            Intent intent = new Intent(ScanActivity.this,StopMaintenanceActivity.class);
                            intent.putExtra("Maintenance",mMaintenanceStatus);
                            startActivityForResult(intent,0X01);
                            ScanActivity.this.finish();

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
                                        getRepair(bikeCode);
                                    } else {
                                        reLogin(ScanActivity.this);
                                    }
                                }
                            });
                        }else
                        {
                            String msg = mResponseObject.optString("msg");
                            DialogUtils.showToast(ScanActivity.this,msg);
                        }
                    }
                }catch(Exception e)
                {
                    Log.e("ScanActivity",""+ e.getMessage());
                }

            }

            @Override
            public void onFailure() {
                DialogUtils.dismissProgressDialog();
            }
        });



    }


    public Rect getCropRect() {
        return mCropRect;
    }


    @Override
    public void onClick(View v) {

        switch (v.getId())
        {
            case R.id.actionbar_left:
                inactivityTimer.onActivity();
                // beepManager.playBeepSoundAndVibrate();
                this.setResult(RESULT_OK);
                ScanActivity.this.finish();
                break;

            case R.id.scan_flash:
                getCameraManager().flashHandler();
//                if(getCameraManager().isTurnOn){
//                    tvLight.setText(R.string.qr_text_light_off);
//                }
//                else{
//                    tvLight.setText(R.string.qr_text_light_on);
//                }
                break;
            case R.id.scan_number:
                Intent intent = new Intent(ScanActivity.this,VehicleNumberActivity.class);
                intent.putExtra(Status,mScanAction);
                startActivity(intent);
                ScanActivity.this.finish();
                break;


        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        if (holder == null) {
            Log.e("Test", "*** WARNING *** surfaceCreated() gave us IApplication null surface!");
        }
        if (!isHasSurface) {
            isHasSurface = true;
            //modify song Camera authorization
            // 设置权限请求：
            if (Build.VERSION.SDK_INT>22) {
                if (ContextCompat.checkSelfPermission(ScanActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(ScanActivity.this, new String[]{android.Manifest.permission.CAMERA}, 0);
                } else {

                    initCamera(holder);
                }
            }
            else
            {
                initCamera(holder);
            }
            //modify song Camera authorization
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isHasSurface = false;
    }
}
