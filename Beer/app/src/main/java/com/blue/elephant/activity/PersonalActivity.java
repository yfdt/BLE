package com.blue.elephant.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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


public class PersonalActivity extends BaseActivity {

    private TextView mNameView,mEmailView,mPhoneView,mAddressView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);

        ImageView actionBarLeft = findViewById(R.id.actionbar_left);
        TextView actionBarTitle = findViewById(R.id.actionbar_title);

        mNameView = findViewById(R.id.person_name);
        mEmailView = findViewById(R.id.person_email);
        mPhoneView = findViewById(R.id.person_phone);
        mAddressView = findViewById(R.id.person_address);

        actionBarLeft.setImageResource(R.mipmap.back_arraw);
        actionBarTitle.setText(R.string.person_title);
        actionBarLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PersonalActivity.this.finish();
            }
        });

        loadPerson();
    }


    private void loadPerson()
    {
        if (!NetUtil.hasNet(PersonalActivity.this)) {
            DialogUtils.showToast(PersonalActivity.this,R.string.check_network_connect);
            return;
        }
        String url = ContentPath.personInfo ;
        RequestParams params = new RequestParams(url);
        String message = getResources().getString(R.string.person_load);
        DialogUtils.showProgressDialog(PersonalActivity.this, message);
        onConnect(params, new CallBack() {
            @Override
            public void onResponse(String response) {
                DialogUtils.dismissProgressDialog();
//                Log.i("Person","" + response);
                try{
                    JSONObject mResponseObject = new JSONObject(response);
                    String status = mResponseObject.optString("status");
                    if(status.equals("1"))
                    {
//                        Log.i("Person","" + response);
                        JSONObject mResultObject = mResponseObject.optJSONObject("result");
                        JSONObject mInfoObject = mResultObject.optJSONObject("customer");
                        JSONObject mUserObject = mResultObject.optJSONObject("user");
                        String mAddress =  mInfoObject.optString("address");
                        String mName = mUserObject.optString("truename");
                        String mEmail = mUserObject.optString("email");
                        String mPhone = mUserObject.optString("username");
                        mNameView.setText(mName);
                        mEmailView.setText(mEmail);
                        mPhoneView.setText(mPhone);
                        mAddressView.setText(mAddress);
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
                                        loadPerson();
                                    } else {
                                        reLogin(PersonalActivity.this);
                                    }
                                }
                            });
                        }else
                        {
                            String msg = mResponseObject.optString("msg");
                            DialogUtils.showToast(PersonalActivity.this,msg);
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
                DialogUtils.dismissProgressDialog();
            }
        });
    }





}
