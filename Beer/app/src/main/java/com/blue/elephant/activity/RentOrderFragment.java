package com.blue.elephant.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.blue.elephant.R;
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
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import static com.blue.elephant.activity.InsuranceActivity.OPTION;

public class RentOrderFragment extends Fragment {

    private String mBikeCode,mOrderID,mOrderCode,mInsurance= "",mStartTime;
    private String mImagePath="";
    private ConfirmationRentActivity mActivity;
    private TextView mSerialView,mOrderSerialView,
            mInsuranceView,mStartView,mStopView,
            mContractView;
    private boolean isEnd;

    private ConfirmationRentActivity.onConnectListener onListener = new ConfirmationRentActivity.onConnectListener() {
        @Override
        public void onShow() {
            mStopView.setEnabled(true);
            DialogUtils.dismissProgressDialog();
//            Log.i("Confirmation","stop is show ");
            //加载当前的订单


        }

        @Override
        public void init() {
            loadBike();

        }

        @Override
        public void onAction() {
            DialogUtils.dismissProgressDialog();
            DialogUtils.showToast(mActivity,R.string.confirm_load_error);
        }
    };


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (ConfirmationRentActivity) getActivity();
        String message = getResources().getString(R.string.order_detail_load);
        DialogUtils.showProgressDialog(mActivity, message);
        mActivity.setConnectListener(onListener);
        Bundle mOrderBundle =   getArguments();
        if(mOrderBundle == null)
        {
            Log.e("RentOrder"," bundle is  null");
            return ;
        }
        mBikeCode = mOrderBundle.getString("BikeCode");
        mOrderID = mOrderBundle.getString("OrderID");
        mOrderCode = mOrderBundle.getString("OrderCode");
        mImagePath = mOrderBundle.getString("ImagePath");
        mInsurance = mOrderBundle.getString(OPTION);
        mStartTime = mOrderBundle.getString("StartTime");
//        mOrderBundle.putBoolean("EndStatus",true);
        isEnd = mOrderBundle.getBoolean("EndStatus");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_order_detail,null);
        mSerialView = view.findViewById(R.id.detail_status_name);
        mOrderSerialView = view.findViewById(R.id.order_detail_serial);
        mInsuranceView = view.findViewById(R.id.order_detail_insurance);
        mStartView = view.findViewById(R.id.order_detail_start);
        mContractView = view.findViewById(R.id.order_detail_contract);
        mStopView = view.findViewById(R.id.order_detail_submit);
        mStopView.setEnabled(false);
        mStopView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                stopOrder();
                if(mActivity.mBlueService != null)
                {

                }

            }
        });

        mContractView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View view = LayoutInflater.from(mActivity).inflate(R.layout.pop_image,null);
                ImageView mBikeImage = view.findViewById(R.id.pop_image);
                ImageView mBikeClose = view.findViewById(R.id.pop_image_close);
                new AnsyImage(mBikeImage).execute(ContentPath.prefix + "/" + mImagePath);
                final PopupWindow mImageWindow = DialogUtils.showAllMenu(mActivity,view,mContractView);
                mBikeClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mImageWindow.dismiss();
                    }
                });


            }
        });

        //加载头部
        ImageView actinLeft = view.findViewById(R.id.actionbar_left);
        TextView actionTitle = view.findViewById(R.id.actionbar_title);
        actinLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.disConnectService();
            }
        });
        actionTitle.setText(R.string.order_title);
        actinLeft.setImageResource(R.mipmap.back_arraw);
        mSerialView.setText(mBikeCode);
        mOrderSerialView.setText(mOrderCode);
        if(mInsurance == null)
            mInsurance = "";
        String insuranceStatus = mInsurance.equals("1")? getResources().getString(R.string.insurance_ok): getResources().getString(R.string.insurance_no);
        mInsuranceView.setText(insuranceStatus);
        mStartView.setText(mStartTime);
        if(isEnd)
        {
            mStopView.setEnabled(true);
            DialogUtils.dismissProgressDialog();
        }
//        mActivity.mHandler.sendEmptyMessageDelayed(0X06,12000);
//        Bundle mOrderBundle =   getArguments();
//        if(mOrderBundle == null)
//            return view;
//        boolean isEnd = mOrderBundle.getBoolean("EndStatus",true);
//        if(isEnd)
//        {
//            mStopView.setEnabled(true);
//            DialogUtils.dismissProgressDialog();
//        }
        return view;
    }


    private void loadBike()
    {
        if (!NetUtil.hasNet(mActivity)) {
            DialogUtils.showToast(mActivity,R.string.check_network_connect);
            return;
        }
        String url = ContentPath.bikeSerial ;
        RequestParams params = new RequestParams(url);
        params.addBodyParameter("bikecode", mBikeCode);
        mActivity.onConnect(params, new CallBack() {
            @Override
            public void onResponse(String response) {

//                Log.i("RentOrder","" + response);
                try{
                    JSONObject mResponseObject = new JSONObject(response);
                    String status = mResponseObject.optString("status");
                    if(status.equals("1"))
                    {
                        JSONObject mResult = mResponseObject.getJSONObject("result");
                        JSONObject mBike = mResult.getJSONObject("bike");
                        String mBluetooth = mBike.optString("bluetooth");
//                        Log.i("RentOrder","" + mBluetooth);
                        //连接设备
                        mActivity.onConnectDevice(mBluetooth);
                    }
                    else if(status.equals("0"))
                    {
                        String errorCode =  mResponseObject.optString("errorcode");
                        if(errorCode.equals("60001"))
                        {
                            mActivity.quickLogin(new QuickLoadCallBack() {
                                @Override
                                public void doSomeThing(Boolean boolon) {
                                    if (boolon == true) {
                                        loadBike();
                                    } else {
                                        mActivity.reLogin(mActivity);
                                    }
                                }
                            });
                        }else
                        {
                            String msg = mResponseObject.optString("msg");
                            DialogUtils.showToast(mActivity,msg);
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





    private void stopOrder()
    {
        if (!NetUtil.hasNet(mActivity)) {
            DialogUtils.showToast(mActivity,R.string.check_network_connect);
            return;
        }
        String url = ContentPath.stopOrder ;
        RequestParams params = new RequestParams(url);
        params.addBodyParameter("orderid", mOrderID);
        String message = getResources().getString(R.string.order_detail_stop);
        DialogUtils.showProgressDialog(mActivity, message);
        mActivity.onConnect(params, new CallBack() {
            @Override
            public void onResponse(String response) {
                DialogUtils.dismissProgressDialog();
//                Log.i("RentOrder","stopOrder \t" + response);
                //	{"status":1}
                try{
                    JSONObject mResponseObject = new JSONObject(response);
                    String mStatus = mResponseObject.optString("status");
                    if(mStatus.equals("1"))
                    {
                        mActivity.disConnectService();
                    }
                    else
                    {
                        String errorCode =  mResponseObject.optString("errorcode");
                        if(errorCode.equals("60001"))
                        {
                            mActivity.quickLogin(new QuickLoadCallBack() {
                                @Override
                                public void doSomeThing(Boolean boolon) {
                                    if (boolon == true) {
                                        loadBike();
                                    } else {
                                        mActivity.reLogin(mActivity);
                                    }
                                }
                            });
                        }else
                        {
                            String msg = mResponseObject.optString("msg");
                            DialogUtils.showToast(mActivity,msg);
                        }
                    }

                }catch(Exception e)
                {

                }

            }

            @Override
            public void onFailure() {
                DialogUtils.dismissProgressDialog();
            }
        });
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
