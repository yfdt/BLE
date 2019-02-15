package com.blue.elephant.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

import java.io.File;


public class InsuranceActivity extends BaseActivity {

    public static String OPTION = "Option";
    private boolean isInsurance = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insurance);

        ImageView actionBarLeft = findViewById(R.id.actionbar_left);
        TextView actionBarTitle = findViewById(R.id.actionbar_title);
        Button actionYes = findViewById(R.id.insurance_yes);
        Button actionNo = findViewById(R.id.insurance_no);

        actionBarLeft.setImageResource(R.mipmap.back_arraw);
        actionBarTitle.setText(R.string.insurance_title);

        actionBarLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                InsuranceActivity.this.finish();
            }
        });

        actionYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isInsurance = true;
                onAction();
            }
        });


        actionNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isInsurance = false;
                onAction();
            }
        });


    }


    private void onAction()
    {
        String bikeCode = getIntent().getStringExtra("BikeCode");
        String mImagePath = getIntent().getStringExtra("ImagePath");
        String mBluetooth = getIntent().getStringExtra("Bluetooth");
        String mInsurance = isInsurance?"1": "0";
        String mBikeID = getIntent().getStringExtra("BikeID");

//        Log.i("Upload","insurance : " + mImagePath);
//        Intent intent = new Intent(InsuranceActivity.this,ConfirmationRentActivity.class);
//        intent.putExtra("Rent",true);
//        intent.putExtra(OPTION,mInsurance);
//        intent.putExtra("ImagePath",mImagePath);
//        intent.putExtra("BikeCode",bikeCode);
//        intent.putExtra("BikeID",mBikeID);
//        intent.putExtra("OrderID","");
//        intent.putExtra("OrderCode","");
//        intent.putExtra("StartTime","");
//        intent.putExtra("Bluetooth",mBluetooth);
//        intent.putExtra("EndStatus",false);
//        startActivity(intent);
        Intent intent = new Intent(InsuranceActivity.this,RentBikeActivity.class);
        intent.putExtra(OPTION,mInsurance);
        intent.putExtra("ImagePath",mImagePath);
        intent.putExtra("BikeCode",bikeCode);
        intent.putExtra("BikeID",mBikeID);
        intent.putExtra("Bluetooth",mBluetooth);
        startActivity(intent);
        InsuranceActivity.this.finish();
    }

    /***
     * 创建订单
     */
    private void onUpLoad()
    {
        if (!NetUtil.hasNet(InsuranceActivity.this)) {
            DialogUtils.showToast(InsuranceActivity.this,R.string.check_network_connect);
            return;
        }
        String bikeCode = getIntent().getStringExtra("BikeCode");
        String mImagePath = getIntent().getStringExtra("ImagePath");
        String mInsurance = isInsurance?"1": "0";
        if(bikeCode== null)
        {
            DialogUtils.showToast(InsuranceActivity.this,R.string.upload_bike_code_error);
            return ;
        }
        if(mImagePath== null)
        {
            DialogUtils.showToast(InsuranceActivity.this,R.string.upload_pic_error);
            return;
        }
        String url = ContentPath.uploadPic ;
        RequestParams params = new RequestParams(url);
        params.addBodyParameter("bikecode", bikeCode);
        params.addBodyParameter("buyinsurance", mInsurance);
        params.addBodyParameter("contract", new File(mImagePath), "multipart/form-data");
        String message = getResources().getString(R.string.upload_load);
        DialogUtils.showProgressDialog(InsuranceActivity.this, message);
        onConnect(params, new CallBack() {
            @Override
            public void onResponse(String response) {
                DialogUtils.dismissProgressDialog();
//                Log.i("UploadActivity",""+ response);
                try{
                    JSONObject mResponseObject = new JSONObject(response);
                    String status = mResponseObject.optString("status");
                    if(status.equals("1"))
                    {
                        Log.i("UploadActivity"," success :"+ response);
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
                                        onUpLoad();
                                    } else {
                                        reLogin(InsuranceActivity.this);
                                    }
                                }
                            });
                        }else
                        {
                            String msg = mResponseObject.optString("msg");
                            DialogUtils.showToast(InsuranceActivity.this,msg);
                        }
                    }
                }catch(Exception e)
                {
                    Log.e("UploadActivity",""+ e.getMessage());
                }
            }

            @Override
            public void onFailure() {
                DialogUtils.dismissProgressDialog();
            }
        });

//        Log.i("ScanActivity","bike code : "+ bikeCode );

    }


}
