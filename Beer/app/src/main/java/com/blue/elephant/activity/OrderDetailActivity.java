package com.blue.elephant.activity;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blue.elephant.R;
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
import java.util.HashMap;
import java.util.Map;


public class OrderDetailActivity extends BaseActivity {

    // serial,contract,insurance,start
    private TextView mTipView,mSubmitView,mContractView;

    private TextView  mVehicleStatue,mVehicleName,mEndText,mRidingText,
            mChargeText,mProfitText,mTotalText,mSerialView,mInsuranceView
            ,mStartText;

    private RelativeLayout mSubView,mStatusView;
    private String mTimeZone;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        initView();


    }

    private void initView() {

        ImageView actionBarLeft = findViewById(R.id.actionbar_left);
        TextView actionBarTitle = findViewById(R.id.actionbar_title);

        mTipView = findViewById(R.id.detail_tips);
        mSubmitView = findViewById(R.id.detail_submit);

        mSerialView = findViewById(R.id.detail_serial);
        mContractView = findViewById(R.id.detail_contract);
        mInsuranceView = findViewById(R.id.detail_insurance);
        mStartText = findViewById(R.id.detail_start);

        mVehicleStatue = findViewById(R.id.detail_status_bike);
        mVehicleName = findViewById(R.id.detail_status_name);
        mEndText = findViewById(R.id.detail_end);
        mRidingText = findViewById(R.id.detail_riding);
        mChargeText = findViewById(R.id.detail_charge);
        mProfitText = findViewById(R.id.detail_profit);
        mTotalText = findViewById(R.id.detail_total);
        mSubView = findViewById(R.id.detail_sub_option);
        mStatusView = findViewById(R.id.detail_status);

        actionBarLeft.setImageResource(R.mipmap.back_arraw);
        actionBarTitle.setText(R.string.order_title);

        //初始化布局
        mSubView.setVisibility(View.GONE);
        mStatusView.setVisibility(View.GONE);
        //加载数据


        actionBarLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OrderDetailActivity.this.finish();
            }
        });

//        mSubmitView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                mSubView.setVisibility(View.VISIBLE);
//                mStatusView.setVisibility(View.VISIBLE);
//                mTipView.setVisibility(View.GONE);
//                mSubmitView.setVisibility(View.GONE);
//            }
//        });
        mSubView.setVisibility(View.VISIBLE);
        mStatusView.setVisibility(View.VISIBLE);
        mTipView.setVisibility(View.GONE);
        mSubmitView.setVisibility(View.GONE);

        loadServer();

    }

    private void loadServer()
    {
        if (!NetUtil.hasNet(OrderDetailActivity.this)) {
            DialogUtils.showToast(OrderDetailActivity.this,R.string.check_network_connect);
            return;
        }
        String orderID = getIntent().getStringExtra("OrderID");
        if(orderID.equals(""))
        {
            DialogUtils.showToast(OrderDetailActivity.this,R.string.order_detail_error);
            return ;
        }

        Map<String,String> heads = new HashMap<>();
        heads.put("orderid",orderID);
        heads.put("appversion","1");
        String message = getResources().getString(R.string.order_detail_load);
        DialogUtils.showProgressDialog(OrderDetailActivity.this, message);
        onServerTime(ContentPath.orderDetail,heads,new CallBack(){

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
                onLoadDetail();

            }

            @Override
            public void onFailure() {
                onLoadDetail();
            }
        });

    }



    private void onLoadDetail()
    {
        String orderID = getIntent().getStringExtra("OrderID");
        String url = ContentPath.orderDetail ;
        RequestParams params = new RequestParams(url);
        params.addBodyParameter("orderid",orderID);

        onConnect(params, new CallBack() {
            @Override
            public void onResponse(String response) {
                DialogUtils.dismissProgressDialog();
                try{
                    JSONObject mResponseObject = new JSONObject(response);
                    String status = mResponseObject.optString("status");
                    if(status.equals("1"))
                    {
//                        Log.i("OrderDetailActivity","" + response);
                        JSONObject mResultObject = mResponseObject.optJSONObject("result");
                        JSONObject mOrderObject = mResultObject.optJSONObject("order");
                        String mSerial = mOrderObject.optString("bikecode");
                        String mOrderNumber = mOrderObject.optString("orderno");
                        String mInsurance = mOrderObject.optString("buyinsurance");
                        String mStart = mOrderObject.optString("starttime");
                        String mEnd = mOrderObject.optString("endtime");
                        final String mImagePath = ContentPath.prefix+ "/" +  mOrderObject.optString("contractpath");
                        String insuranceStatus = mInsurance.equals("1")? getResources().getString(R.string.insurance_ok): getResources().getString(R.string.insurance_no);
                        //riding
                        String mPeriod =  getPeriod(mStart,mEnd);
                        double mAmount = mOrderObject.optDouble("amount");
                        double mProfit = mOrderObject.optDouble("profit");
//                        String mTotal = mOrderObject.optString("totalamount");
                        String mTotal = String.valueOf(mProfit + mAmount);
                        if(mTimeZone!= null)
                        {
                            mStart = DateUtil.getLocalTime(mStart,mTimeZone);
                            mEnd =  DateUtil.getLocalTime(mEnd,mTimeZone);
                        }

                        String mAmounts = mAmount==0 ? "$0.0":"$"+ mAmount;
                        String mProfits = mProfit==0 ? "$0.0" : "$" + mProfit;
                        mTotal = mTotal.equals("")? "$0.0" : "$" + mTotal;
                        mVehicleName.setText(mSerial);
                        mSerialView.setText(mOrderNumber);
                        mInsuranceView.setText(insuranceStatus);
                        mStartText.setText(mStart);
                        mEndText.setText(mEnd);
                        mRidingText.setText(mPeriod);
                        mChargeText.setText(mAmounts);
                        mProfitText.setText(mProfits);
                        mTotalText.setText(mTotal);
                        mContractView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                View view = LayoutInflater.from(OrderDetailActivity.this).inflate(R.layout.pop_image,null);
                                ImageView mBikeImage = view.findViewById(R.id.pop_image);
                                ImageView mBikeClose = view.findViewById(R.id.pop_image_close);
                                new AnsyImage(mBikeImage).execute(mImagePath);
                                final PopupWindow mImageWindow = DialogUtils.showAllMenu(OrderDetailActivity.this,view,mContractView);
                                mBikeClose.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mImageWindow.dismiss();
                                    }
                                });
                            }
                        });

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
                                        onLoadDetail();
                                    } else {
                                        reLogin(OrderDetailActivity.this);
                                    }
                                }
                            });
                        }else
                        {
                            String msg = mResponseObject.optString("msg");
                            DialogUtils.showToast(OrderDetailActivity.this,msg);
                        }
                    }

                }catch(Exception e)
                {
                    Log.e("OrderDetailActivity","" + e.getMessage());
                }
            }

            @Override
            public void onFailure() {
                DialogUtils.dismissProgressDialog();
            }
        });

    }

    private String getPeriod(String start,String end)
    {
        long time =  DateUtil.getPeriodTime(start,end);
        long hour = time/3600000;
        long min = time/60000;
        long second = time % 60000;
        if(second > 0 )
        {
            min ++;
        }
        if(min > 59)
        {
            min = min %60;
            hour +=1;
        }
        String mPeriod = "";
        try {
            mPeriod = getResources().getString(R.string.order_period,hour,min);
        }catch(Exception e)
        {

        }
        return mPeriod;
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
