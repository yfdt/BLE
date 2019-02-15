package com.blue.elephant.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.blue.elephant.R;
import com.blue.elephant.util.ContentPath;
import com.blue.elephant.util.DialogUtils;
import com.blue.elephant.util.NetUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.http.RequestParams;


public class LoginActivity extends BaseActivity {

    private EditText etAccount,etPass;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initActionBar();

    }

    private void initActionBar()
    {
        ImageView actionBarLeft = findViewById(R.id.actionbar_left);
        actionBarLeft.setImageResource(R.mipmap.back_arraw);
        TextView actionBarTitle = findViewById(R.id.actionbar_title);
        etAccount = findViewById(R.id.login_account);
        etPass = findViewById(R.id.login_pass);

        actionBarTitle.setText(R.string.login_title);
        actionBarLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginActivity.this.finish();
            }
        });

        findViewById(R.id.login_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLogin();
            }
        });
        //检测之前是否登陆
        if (!mPreference.getString("token", "").equals("")) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            LoginActivity.this.finish();
        }

        etAccount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String account = etAccount.getText().toString();
                if (account.indexOf("\r")>=0 || account.indexOf("\n")>=0){//发现输入回车符或换行符
                    etAccount.setText(account.replace("\r","").replace("\n",""));//去掉回车符和换行符
                    etPass.requestFocus();//让editText2获取焦点
                }

            }

        });

    }



    private void onLogin()
    {
        String account = etAccount.getText().toString().trim();
        String pass = etPass.getText().toString().trim();

        if(account.equals(""))
        {
            String errorMessage = getResources().getString(R.string.login_account_hint);
            etAccount.setError(errorMessage);
            return ;
        }
        if(pass.equals(""))
        {
            String errorMessage = getResources().getString(R.string.login_pass_hint);
            etPass.setError(errorMessage);
            return ;
        }

        if (!NetUtil.hasNet(LoginActivity.this)) {
            DialogUtils.showToast(LoginActivity.this,R.string.check_network_connect);
            return;
        }
        String url = ContentPath.login ;
        RequestParams params = new RequestParams(url);
        params.addBodyParameter("username", account);
        params.addBodyParameter("password", pass);
        String message = getResources().getString(R.string.login_load);
        DialogUtils.showProgressDialog(LoginActivity.this, message);
        doHttp(params, new NetUtil.CallBack() {
            @Override
            public void onResponse(String response) {
                DialogUtils.dismissProgressDialog();
//                Log.i("Login",""+ response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.optString("status");
//                    Log.i("LoginActivity",response);
                    switch (status) {
                        case "1":
                            JSONObject result = jsonObject.optJSONObject("result");
                            JSONObject user = result.optJSONObject("user");
                            String token = user.optString("token");
                            String username = user.optString("username");
                            String truename = user.optString("truename");
                            String rolecode = user.optString("rolecode");
                            String custId = user.optString("custid");
                            mEditor.putString("token", token);
                            mEditor.putString("username", username);
                            mEditor.putString("truename", truename);
                            mEditor.putString("rolecode", rolecode);
                            mEditor.putString("custid",custId);
                            mEditor.apply();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                            break;
                        default:
                            DialogUtils.showToast(LoginActivity.this,R.string.login_error);
                            break;
                    }

                } catch (JSONException e) {
//                    e.printStackTrace();
                    Log.e("Test","login error: "+ e.getMessage());
                }

            }
        });


    }

}
