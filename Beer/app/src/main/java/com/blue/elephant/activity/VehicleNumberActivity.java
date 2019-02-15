package com.blue.elephant.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.blue.elephant.R;
import com.blue.elephant.util.CallBack;
import com.blue.elephant.util.ContentPath;
import com.blue.elephant.util.DialogUtils;
import com.blue.elephant.util.NetUtil;
import com.blue.elephant.util.QuickLoadCallBack;

import org.json.JSONObject;
import org.xutils.http.RequestParams;

import java.util.HashMap;
import java.util.Map;

import static com.blue.elephant.activity.MaintenanceActivity.RepairStatus;


public class VehicleNumberActivity extends BaseActivity {

    private int mScanAction;
    private String mTimeZone;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_number);

        ImageView actionBarLeft =  findViewById(R.id.actionbar_left);
        TextView actionBarTitle = findViewById(R.id.actionbar_title);

        actionBarLeft.setImageResource(R.mipmap.back_arraw);
        actionBarTitle.setText(R.string.serial_title);

        actionBarLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VehicleNumberActivity.this.finish();
            }
        });

        final EditText mSerialNo = findViewById(R.id.number_serial);
        TextView mNext = findViewById(R.id.number_submit);
        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String serialText =  mSerialNo.getText().toString();
                if(serialText.isEmpty())
                {
                    String mError = getResources().getString(R.string.serial_hint_text);
                    mSerialNo.setError(mError);
                    return ;
                }
                //进行网络请求判断
                //判断车辆是否出租，出租则进入订单详情界面，否则进入保险确认界面
                Intent intent;
                switch (mScanAction)
                {
                    case 1:
                        loadServerTime(serialText);
                        break;


                    case 3:
                        //系统校验车辆未出租且不在维修中
                        getRepair(serialText);
                        break;


                }

            }
        });

        mScanAction= getIntent().getIntExtra(ScanActivity.Status,1);
    }

    private void loadServerTime(final String bikeCode)
    {
        if (!NetUtil.hasNet(VehicleNumberActivity.this)) {
            DialogUtils.showToast(VehicleNumberActivity.this,R.string.check_network_connect);
            return;
        }
        Map<String,String> heads = new HashMap<>();
        heads.put("bikecode",bikeCode);
        heads.put("appversion","1");
        String message = getResources().getString(R.string.scan_load_data);
        DialogUtils.showProgressDialog(VehicleNumberActivity.this, message);
        onServerTime(ContentPath.bikeSerial, heads, new CallBack() {
            @Override
            public void onResponse(String response) {
                String mServerTime = response;
//                Log.i("OrderDetailActivity","" + mServerTime);
                String[] mSplit = null;
                if(mServerTime!= null)
                {
                    mSplit = mServerTime.split(",");
                    if(mSplit != null)
                    {
                        mTimeZone = mSplit[1];
//                        mEndTime = DateUtil.getLocalTime(mEndTime,spliteTime[1]);
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

                        JSONObject mBike = mResult.getJSONObject("bike");
                        String bikeStatus = mBike.optString("rentstatus").toLowerCase();
                        String bluetooth = mBike.optString("bluetooth");
                        String mBikeId = mBike.optString("bikeid");
                        String mMaintenance = mResponseObject.optString("maintenance");//maintenance
                        if(!mMaintenance.equals(""))
                        {
                            DialogUtils.showToast(VehicleNumberActivity.this,R.string.scan_text_main);
                            return ;
                        }
                        if(bikeStatus.equals("occupied"))
                        {
                            JSONObject mOrderObject = mResult.optJSONObject("order");
                            Intent intent = new Intent(VehicleNumberActivity.this,EndOfOrderActivity.class);
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
                            VehicleNumberActivity.this.finish();
                            return ;
                        }
                        Intent intent = new Intent(VehicleNumberActivity.this,UploadActivity.class);
                        intent.putExtra("BikeCode",bikeCode);
                        intent.putExtra("BikeID",mBikeId);
                        intent.putExtra("Bluetooth",bluetooth);
                        startActivity(intent);
                        VehicleNumberActivity.this.finish();

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
                                        reLogin(VehicleNumberActivity.this);
                                    }
                                }
                            });
                        }else
                        {
                            String msg = mResponseObject.optString("msg");
                            DialogUtils.showToast(VehicleNumberActivity.this,msg);
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
        if (!NetUtil.hasNet(VehicleNumberActivity.this)) {
            DialogUtils.showToast(VehicleNumberActivity.this,R.string.check_network_connect);
            return;
        }
        String url = ContentPath.bikeSerial ;
        RequestParams params = new RequestParams(url);
        params.addBodyParameter("bikecode", bikeCode);
        String message = getResources().getString(R.string.scan_load_data);
        DialogUtils.showProgressDialog(VehicleNumberActivity.this, message);
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
                            DialogUtils.showToast(VehicleNumberActivity.this,R.string.maintenance_rent_tip);
                            return ;
                        }
                        if(mMaintenanceStatus.equals(""))
                        {
                            Intent intent = new Intent(VehicleNumberActivity.this,StartMaintenanceActivity.class);
                            intent.putExtra("BikeCode",bikeCode);
                            intent.putExtra("BikeID",mBikeId);
                            intent.putExtra("Bluetooth",bluetooth);
                            startActivity(intent);
                            VehicleNumberActivity.this.finish();
                        }
                        else
                        {
                            Intent intent = new Intent(VehicleNumberActivity.this,StopMaintenanceActivity.class);
                            intent.putExtra("Maintenance",mMaintenanceStatus);
                            startActivityForResult(intent,0X01);
                            VehicleNumberActivity.this.finish();
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
                                        reLogin(VehicleNumberActivity.this);
                                    }
                                }
                            });
                        }else
                        {
                            String msg = mResponseObject.optString("msg");
                            DialogUtils.showToast(VehicleNumberActivity.this,msg);
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


}
