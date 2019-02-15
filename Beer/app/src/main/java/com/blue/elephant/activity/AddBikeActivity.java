package com.blue.elephant.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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


public class AddBikeActivity extends BaseActivity {

    private EditText mSerialView,mFrameView,mIMEIView,mBlueView;
    private ImageView mSerialImage,mFrameImage,mIMEImage,mBlueImage;


    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Intent intent;
            switch (v.getId())
            {
                case R.id.add_serial_scan_icon:
                    intent =  new Intent(AddBikeActivity.this,ScanActivity.class);
                    intent.putExtra(ScanActivity.Status,4);
                    startActivityForResult(intent,1);
                    break;
                case R.id.add_frame_scan_icon:
                    intent =  new Intent(AddBikeActivity.this,ScanActivity.class);
                    intent.putExtra(ScanActivity.Status,5);
                    startActivityForResult(intent,1);
                    break;
                case R.id.add_ime_scan_icon:
                    intent =  new Intent(AddBikeActivity.this,ScanActivity.class);
                    intent.putExtra(ScanActivity.Status,6);
                    startActivityForResult(intent,1);
                    break;
                case R.id.add_bluetooth_scan_icon:
                    intent =  new Intent(AddBikeActivity.this,DeviceListActivity.class);
                    intent.putExtra(ScanActivity.Status,7);
                    startActivityForResult(intent,1);

                    break;
                case R.id.add_submit:
                    try {
                        intent = getIntent();
                        String mSerial = intent.getStringExtra("Serial");
                        if(mSerial.equals(""))
                        {
                            onAddBike();
                        }
                        else
                        {
                            onEdit();
                        }
                    }catch(Exception e)
                    {
                        Log.e("AddBike",""+ e.getMessage());
                        onAddBike();
                    }

                    break;

            }

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        ImageView actionBarLeft = findViewById(R.id.actionbar_left);
        TextView actionBarTitle = findViewById(R.id.actionbar_title);

        mSerialView = findViewById(R.id.add_serial);
        mFrameView = findViewById(R.id.add_frame);
        mIMEIView = findViewById(R.id.add_ime);
        mBlueView = findViewById(R.id.add_bluetooth);

        findViewById(R.id.add_serial_scan_icon).setOnClickListener(onClickListener);
        findViewById(R.id.add_frame_scan_icon).setOnClickListener(onClickListener);
        findViewById(R.id.add_ime_scan_icon).setOnClickListener(onClickListener);
        findViewById(R.id.add_bluetooth_scan_icon).setOnClickListener(onClickListener);
        findViewById(R.id.add_submit).setOnClickListener(onClickListener);

        actionBarLeft.setImageResource(R.mipmap.back_arraw);
        actionBarTitle.setText(R.string.add_title);
        actionBarLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddBikeActivity.this.finish();
            }
        });

        //add Bike
        try {
            Intent intent = getIntent();
            String mSerial = intent.getStringExtra("Serial");
            String mFrame = intent.getStringExtra("Frame");
            String mIMEI = intent.getStringExtra("IMEI");
            String mBluetooth = intent.getStringExtra("Bluetooth");
            if(!mSerial.equals(""))
            {
                actionBarTitle.setText(R.string.add_edit_title);
                mSerialView.setText(mSerial);
                mFrameView.setText(mFrame);
                mIMEIView.setText(mIMEI);
                mBlueView.setText(mBluetooth);
            }
        }catch(Exception e)
        {
            Log.e("AddBike","" + e.getMessage());
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        Log.i("AddBikeActivity","" + requestCode + "\t "+ resultCode + "\t ");
        if(resultCode == RESULT_OK)
        {
            if(data == null)
                return;
            Bundle mBundle =  data.getExtras();
            if(mBundle == null)
                 return ;
            int Status =  mBundle.getInt("classic",0);
            String mScanText = mBundle.getString("result","");
//            Log.i("AddBikeActivity","classic\t  " + Status + "\t mScanText"+ mScanText + "\t ");
            switch (Status)
            {
                case 4:
                    mSerialView.setText(mScanText);
                    break;
                case 5:
                    mFrameView.setText(mScanText);
                    break;
                case 6:
                    mIMEIView.setText(mScanText);
                    break;
                case 7:
                    mBlueView.setText(mScanText);
                    break;
            }
        }

    }

    private void onAddBike()
    {
        if (!NetUtil.hasNet(AddBikeActivity.this)) {
            DialogUtils.showToast(AddBikeActivity.this,R.string.check_network_connect);
            return;
        }
        String mSerial = mSerialView.getText().toString().trim();
        String mFrame = mFrameView.getText().toString().trim();
        String mIMEI = mIMEIView.getText().toString().trim();
        String mBluetooth = mBlueView.getText().toString().trim();

        if(mSerial.equals(""))
        {
            String serialError = getResources().getString(R.string.add_serial_hint);
            mSerialView.setError(serialError);
            return ;
        }
        if(mFrame.equals(""))
        {
            String serialError = getResources().getString(R.string.add_frame_hint);
            mFrameView.setError(serialError);
            return ;
        }
        if(mIMEI.equals(""))
        {
            String serialError = getResources().getString(R.string.add_imei_hint);
            mIMEIView.setError(serialError);
            return ;
        }

        if(mBluetooth.equals(""))
        {
            String serialError = getResources().getString(R.string.add_bluetooth_hint);
            mBlueView.setError(serialError);
            return ;
        }

        String url = ContentPath.addBike ;
        RequestParams params = new RequestParams(url);
        params.addBodyParameter("bikecode", mSerial);
        params.addBodyParameter("vin", mFrame);
        params.addBodyParameter("bluetooth", mBluetooth);
        params.addBodyParameter("imei", mIMEI);
        String message = getResources().getString(R.string.add_load);
        DialogUtils.showProgressDialog(AddBikeActivity.this, message);
        onConnect(params, new CallBack() {
            @Override
            public void onResponse(String response) {
                DialogUtils.dismissProgressDialog();
                try{
                    JSONObject mResponseObject  = new JSONObject(response);
                    String status = mResponseObject.optString("status");
                    if(status.equals("1"))
                    {
//                        Log.i("AddBike",""+ response);
                        //添加车辆成功
                        setResult(0X01);
                        AddBikeActivity.this.finish();
                    }
                    else if(status.equals("0"))
                    {
//                        Log.i("BikeManageActivity","statue:"+status );
                        String errorcode =  mResponseObject.optString("errorcode");
                        if (errorcode.equals("60001")) {
                            quickLogin(new QuickLoadCallBack() {
                                @Override
                                public void doSomeThing(Boolean boolon) {
                                    if (boolon == true) {
                                        onAddBike();
                                    } else {
                                        reLogin(AddBikeActivity.this);
                                    }
                                }
                            });
                        } else {
                            String msg = mResponseObject.optString("msg");
                            DialogUtils.showToast(AddBikeActivity.this,msg);
                        }
                    }

                }
                catch(Exception e)
                {
                    Log.e("AddBikeActivity","" + e.getMessage());
                }
            }

            @Override
            public void onFailure() {
                DialogUtils.dismissProgressDialog();
            }
        });


    }


    private void onEdit()
    {
        if (!NetUtil.hasNet(AddBikeActivity.this)) {
            DialogUtils.showToast(AddBikeActivity.this,R.string.check_network_connect);
            return;
        }
        String mSerial = mSerialView.getText().toString().trim();
        String mFrame = mFrameView.getText().toString().trim();
        String mIMEI = mIMEIView.getText().toString().trim();
        String mBluetooth = mBlueView.getText().toString().trim();

        if(mSerial.equals(""))
        {
            String serialError = getResources().getString(R.string.add_serial_hint);
            mSerialView.setError(serialError);
            return ;
        }
        if(mFrame.equals(""))
        {
            String serialError = getResources().getString(R.string.add_frame_hint);
            mFrameView.setError(serialError);
            return ;
        }
        if(mIMEI.equals(""))
        {
            String serialError = getResources().getString(R.string.add_imei_hint);
            mIMEIView.setError(serialError);
            return ;
        }

        if(mBluetooth.equals(""))
        {
            String serialError = getResources().getString(R.string.add_bluetooth_hint);
            mBlueView.setError(serialError);
            return ;
        }
        String url = ContentPath.editBike ;
        RequestParams params = new RequestParams(url);
        params.addBodyParameter("bikecode", mSerial);
        params.addBodyParameter("vin", mFrame);
        params.addBodyParameter("bluetooth", mBluetooth);
        params.addBodyParameter("imei", mIMEI);
//        Log.i("EditBike","bikeCode: "+ mSerial + "\t vin: "+ mFrame + "\t bluetooth :" + mBluetooth + "\t imei:"+ mIMEI);
        String message = getResources().getString(R.string.add_edit_tip);
        DialogUtils.showProgressDialog(AddBikeActivity.this, message);
        onConnect(params, new CallBack() {
            @Override
            public void onResponse(String response) {
                DialogUtils.dismissProgressDialog();
                try{
                    JSONObject mResponseObject  = new JSONObject(response);
                    String status = mResponseObject.optString("status");
                    if(status.equals("1"))
                    {
//                        Log.i("EditBike",""+ response);
                        //添加车辆成功
                        setResult(0X01);
                        AddBikeActivity.this.finish();
                    }
                    else if(status.equals("0"))
                    {
//                        Log.i("BikeManageActivity","statue:"+status );
                        String errorcode =  mResponseObject.optString("errorcode");
                        if (errorcode.equals("60001")) {
                            quickLogin(new QuickLoadCallBack() {
                                @Override
                                public void doSomeThing(Boolean boolon) {
                                    if (boolon == true) {
                                        onAddBike();
                                    } else {
                                        reLogin(AddBikeActivity.this);
                                    }
                                }
                            });
                        } else {
                            String msg = mResponseObject.optString("msg");
                            DialogUtils.showToast(AddBikeActivity.this,msg);
                        }
                    }

                }
                catch(Exception e)
                {
                    Log.e("AddBikeActivity","" + e.getMessage());
                }
            }

            @Override
            public void onFailure() {
                DialogUtils.dismissProgressDialog();
            }
        });


    }



}
