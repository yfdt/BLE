package com.blue.elephant.activity;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import java.io.File;

import static com.blue.elephant.activity.InsuranceActivity.OPTION;

public class RentBikeFragment extends Fragment {

    private TextView mInsuranceView, mSerialView;
    private ConfirmationRentActivity mActivity;
    private TextView mSubmit;
    private String mSerial,mInsurance,mImagePath,mBluetooth,mBikeID;

    private ConfirmationRentActivity.onConnectListener onListener = new ConfirmationRentActivity.onConnectListener() {
        @Override
        public void onShow() {
            mSubmit.setEnabled(true);
            DialogUtils.dismissProgressDialog();
        }

        @Override
        public void init() {

            mActivity.onConnectDevice(mBluetooth);
        }

        @Override
        public void onAction() {
            DialogUtils.showToast(mActivity,R.string.confirm_load_error);
        }
    };



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_confirm,null);
        ImageView actionBarLeft = view.findViewById(R.id.actionbar_left);
        TextView actionBarTitle = view.findViewById(R.id.actionbar_title);
        mInsuranceView = view.findViewById(R.id.confirm_insurance);
        mSerialView = view.findViewById(R.id.confirm_serial);
        mSubmit = view.findViewById(R.id.confirm_submit);
        final TextView tvOrderImage = view.findViewById(R.id.confirm_order);
        actionBarLeft.setImageResource(R.mipmap.back_arraw);
        actionBarTitle.setText(R.string.confirm_title);

        actionBarLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//               getActivity().finish();
               mActivity.disConnectService();
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

                View view = LayoutInflater.from(mActivity).inflate(R.layout.pop_image,null);
                ImageView mBikeImage = view.findViewById(R.id.pop_image);
                ImageView mBikeClose = view.findViewById(R.id.pop_image_close);
                try {
                    mBikeImage.setImageURI(Uri.fromFile(new File(mImagePath)));
                }catch(Exception e)
                {
                    Log.e("RentBike","" + e.getMessage());
                }
                final PopupWindow mImageWindow = DialogUtils.showAllMenu(mActivity,view,tvOrderImage);
                mBikeClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mImageWindow.dismiss();
                    }
                });


            }
        });

        mSubmit.setEnabled(false);

        mActivity = (ConfirmationRentActivity) getActivity();
        mActivity.setConnectListener(onListener);
        Bundle mRentBundle =  getArguments();
        if(mRentBundle == null)
            return view;
        mImagePath = mRentBundle.getString("ImagePath");
        mInsurance = mRentBundle.getString(InsuranceActivity.OPTION);
        mSerial = mRentBundle.getString("BikeCode");
        mBluetooth = mRentBundle.getString("Bluetooth");
        mBikeID = mRentBundle.getString("BikeID");
        String option = mInsurance.equals("1")? getResources().getString(R.string.insurance_ok):getResources().getString(R.string.insurance_no);
        mSerialView.setText(mSerial);
        mInsuranceView.setText(option);
        String message = getResources().getString(R.string.confirm_load);
        DialogUtils.showProgressDialog(mActivity, message);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                DialogUtils.dismissProgressDialog();
            }
        },5000);
        return view;
    }



    private void loadBike()
    {
        if (!NetUtil.hasNet(mActivity)) {
            return;
        }
        String url = ContentPath.bikeSerial ;
        RequestParams params = new RequestParams(url);
        params.addBodyParameter("bikecode", mSerial);
        String message = getResources().getString(R.string.confirm_load);
        DialogUtils.showProgressDialog(mActivity, message);
//        Log.i("Confirmation","bike code : "+ mSerial );
        mActivity.onConnect(params, new CallBack() {
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
                        JSONObject mBike = mResult.getJSONObject("bike");
                        String bikeStatus = mBike.optString("rentstatus").toLowerCase();
//                        Intent intent = new Intent(ConfirmationRentActivity.this,UploadActivity.class);
//                        intent.putExtra("BikeCode",bikeCode);
//                        startActivity(intent);
//                        ConfirmationRentActivity.this.finish();

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

    private void createOrder()
    {
        if (!NetUtil.hasNet(mActivity)) {
            DialogUtils.showToast(mActivity,R.string.check_network_connect);
            return;
        }
        String url = ContentPath.uploadPic ;
        RequestParams params = new RequestParams(url);
        params.addBodyParameter("bikeid", mBikeID);
        params.addBodyParameter("buyinsurance", mInsurance);
        params.addBodyParameter("contract", new File(mImagePath), "multipart/form-data");
        String message = getResources().getString(R.string.confirm_load);
        DialogUtils.showProgressDialog(mActivity, message);
//        Log.i("Confirmation","bike code : "+ mSerial );
        mActivity.onConnect(params, new CallBack() {
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
                        RentOrderFragment mRentOrder = new RentOrderFragment();
                        Bundle mOrderBundle = new Bundle();
                        mOrderBundle.putString("OrderID",mOrderID);
                        mOrderBundle.putString("BikeID",mBikeID);
                        mOrderBundle.putString("BikeCode",mBikeCode);
                        mOrderBundle.putString("OrderCode",mOrderCode);
                        mOrderBundle.putString("ImagePath",mImagePath);
                        mOrderBundle.putString(OPTION,mInsurance);
                        mOrderBundle.putString("StartTime",mStartTime);
                        mOrderBundle.putBoolean("EndStatus",true);
                        mRentOrder.setArguments(mOrderBundle);
                        FragmentManager mFragmentManager = mActivity.getSupportFragmentManager();
                        FragmentTransaction mTransaction =  mFragmentManager.beginTransaction();
                        mTransaction.add(R.id.base_rent_container,mRentOrder);
                        mTransaction.commit();
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
                                        createOrder();
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
                    Log.e("Confirmation",""+ e.getMessage());
                }

            }

            @Override
            public void onFailure() {
                DialogUtils.dismissProgressDialog();
            }
        });

    }


}
