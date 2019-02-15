package com.blue.elephant.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.blue.elephant.R;
import com.blue.elephant.custom.edit.BikeEditText;
import com.blue.elephant.util.CallBack;
import com.blue.elephant.util.ContentPath;
import com.blue.elephant.util.DialogUtils;
import com.blue.elephant.util.NetUtil;
import com.blue.elephant.util.QuickLoadCallBack;

import org.json.JSONObject;
import org.xutils.http.RequestParams;


public class ConfirmFragment extends Fragment {

    private TextView mSerialView,mSubmitView;
    private EditText mFailureView;
    private Spinner mSelectSpinner;

    private String mBikeID;
    private String mBikeCode;
    private String mBluetooth;
    private String mCauseText;

    private int indexTime =1 ;


    private MaintenanceActivity mActivity;

    private MaintenanceActivity.onConnectListener  onConnectListener = new MaintenanceActivity.onConnectListener() {
        @Override
        public void onShow(int status) {
            mSubmitView.setEnabled(true);
//            DialogUtils.dismissProgressDialog();
        }

        @Override
        public void init() {
            //连接设备
            mActivity.onConnectDevice(mBluetooth);
//            Log.i("BlueService","ConfirmFragment 连接蓝牙 "+ mBluetooth);
        }

        @Override
        public void onRent(boolean isOPen) {

            //设置租期
            long time = 24*60 * 60 * 1000;
            mActivity.setRentTime(time);
//            Log.i("BlueService","ConfirmFragment 设置租期");
        }

        @Override
        public void onAction() {

        }
    };



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MaintenanceActivity) getActivity();
        mActivity.setConnectListener(onConnectListener);
        Bundle bundle = getArguments();
        if(bundle == null)
            return ;
        mBikeID = bundle.getString("BikeID");
        mBikeCode = bundle.getString("BikeCode");
        mBluetooth = bundle.getString("Bluetooth");

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.frag_confirm, container, false);

        ImageView actionBarLeft = view.findViewById(R.id.actionbar_left);
        TextView actionBarTitle = view.findViewById(R.id.actionbar_title);

        mSerialView = view.findViewById(R.id.confirm_m_serial);
        mFailureView = view.findViewById(R.id.confirm_m_failure);
        mSelectSpinner = view.findViewById(R.id.confirm_m_time);
        mSubmitView = view.findViewById(R.id.confirm_m_submit);

        actionBarLeft.setImageResource(R.mipmap.back_arraw);
        actionBarTitle.setText(R.string.frag_confirm_title);

        actionBarLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mActivity.disConnectService();
            }
        });

        mSubmitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //切换界面
//                mActivity.onChange();
                //数据请求
                mCauseText = mFailureView.getText().toString();
                if(mCauseText.equals(""))
                {
                    String error = getResources().getString(R.string.mainten_bike_empty);
                    mFailureView.setError(error);
                    return ;
                }
                startMaintenance();
            }
        });

        ArrayAdapter<CharSequence> mTimeAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.frag_confirm_array_time, android.R.layout.simple_spinner_item);
        mTimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSelectSpinner.setAdapter(mTimeAdapter);
        mSelectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                 String item = (String) parent.getItemAtPosition(position);
                 indexTime = position +1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

//        mFailureView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                if ((view.getId() == R.id.confirm_m_failure ) && canVerticalScroll(mFailureView)) {
//                    view.getParent().requestDisallowInterceptTouchEvent(true);
//                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
//                        view.getParent().requestDisallowInterceptTouchEvent(false);
//                    }
//                }
//                return false;
//            }
//        });

        mSerialView.setText(mBikeCode);
        mSubmitView.setEnabled(false);
        try {
            String message = getResources().getString(R.string.mainten_init_load);
//            DialogUtils.showProgressDialog(mActivity, message);
        }catch (Exception e)
        {

        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                try {
//                    DialogUtils.dismissProgressDialog();
//                }catch(Exception e)
//                {
//
//                }
            }
        },5000);
        return view;
    }

    private void startMaintenance()
    {
        if (!NetUtil.hasNet(mActivity)) {
            DialogUtils.showToast(getActivity(), R.string.check_network_connect);
            return;
        }

        String url = ContentPath.startMaintenance ;
        RequestParams params = new RequestParams(url);

        String mCauseText = mFailureView.getText().toString();
        indexTime = indexTime * 30;
        params.addBodyParameter("bikeid", mBikeID);
        params.addBodyParameter("time", indexTime + "");
        params.addBodyParameter("cause", mCauseText);

//        Log.i("Confirm",""+ mBikeCode);
        mActivity.onConnect(params, new CallBack() {
            @Override
            public void onResponse(String response) {

//                Log.i("Confirm","" + response);
                try{
                    JSONObject mResponseObject = new JSONObject(response);
                    String status = mResponseObject.optString("status");
                    if(status.equals("1"))
                    {
                        JSONObject mResult = mResponseObject.getJSONObject("result");
                        String mBike = mResult.getString("maintenance");
//                        intent.putExtra("Maintenance",mBike.toString());
                        Bundle bundle = new Bundle();
                        bundle.putString("Maintenance",mBike);
                        mActivity.onChange(bundle);
//                        String mBluetooth = mBike.optString("bluetooth");
//                        Log.i("Confirm","" + mBike);

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
                                        startMaintenance();
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

}
