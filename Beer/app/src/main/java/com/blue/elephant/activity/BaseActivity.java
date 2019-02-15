package com.blue.elephant.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;


import com.blue.elephant.R;
import com.blue.elephant.custom.IApplication;
import com.blue.elephant.util.CallBack;
import com.blue.elephant.util.ContentPath;
import com.blue.elephant.util.DialogUtils;
import com.blue.elephant.util.NetUtil;
import com.blue.elephant.util.QuickLoadCallBack;
import com.blue.elephant.util.ServiceTime;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.http.RequestParams;

import java.util.Map;

public class BaseActivity extends AppCompatActivity {


    protected SharedPreferences mPreference;
    protected SharedPreferences.Editor mEditor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreference = IApplication.getApplication().getPreference();
        mEditor = mPreference.edit();
        mEditor.apply();
    }
    /**
     * 快速登录
     */
    public void quickLogin(final QuickLoadCallBack cb) {
        String url = ContentPath.quickLogin;
        RequestParams params = new RequestParams(url);
        params.addBodyParameter("username",mPreference.getString("username", ""));
        params.addBodyParameter("token", mPreference.getString("token", ""));
        doHttp(params, new NetUtil.CallBack() {
            @Override
            public void onResponse(String response) {
                try {
//                    Log.i("quickLogin",response);
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.optString("status");
                    switch (status) {
                        case "1":
                            JSONObject result = jsonObject.getJSONObject("result");
                            JSONObject mUserObject =  result.getJSONObject("user");
                            String rolecode = mUserObject.optString("rolecode");
                            String token = mUserObject.optString("token");
                            mEditor.putString("rolecode",rolecode);
                            mEditor.putString("token", token);
                            mEditor.apply();
                            cb.doSomeThing(true);
                            break;
                        case "0":
                            cb.doSomeThing(false);
                            break;
                        default:
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });
    }
    protected void reLogin(final Context context){
        DialogUtils.showNoticeDialog(getString(R.string.WaringTokenExpired), context, new DialogUtils.NoticeCallBack() {
            @Override
            public void confirm() {
                mEditor.putString("username", "");
                mEditor.putString("token", "");
                mEditor.apply();
                Intent intent = new Intent(context, LoginActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void cancel() {
                mEditor.putString("username", "");
                mEditor.putString("token", "");
                mEditor.apply();
            }
        });
    }


    public void doHttp(RequestParams params, final NetUtil.CallBack callBack) {
        if (NetUtil.hasNet(this)) {
            NetUtil.doXutilsHttp(BaseActivity.this, params, callBack);
        }
    }

    public void onConnect(RequestParams params, CallBack callBack)
    {
        if (NetUtil.hasNet(this)) {
            NetUtil.doHttp(BaseActivity.this, params, callBack);
        }
    }


    public void onServerTime(String address, Map<String,String> heads,CallBack mCallBack)
    {
        ServiceTime.getInstance(BaseActivity.this)
                .onPost(address,heads,mCallBack);
    }
}
