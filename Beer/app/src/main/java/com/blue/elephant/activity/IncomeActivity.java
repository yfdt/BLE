package com.blue.elephant.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blue.elephant.R;
import com.blue.elephant.util.CallBack;
import com.blue.elephant.util.ContentPath;
import com.blue.elephant.util.DialogUtils;
import com.blue.elephant.util.NetUtil;
import com.blue.elephant.util.QuickLoadCallBack;

import org.json.JSONObject;
import org.xutils.http.RequestParams;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class IncomeActivity extends BaseActivity {


    private TextView mTOrderView,mTAmountView,mTInsuranceView,mTIncomeView,
                    mWOrderView,mWAmountView,mWInsuranceView,mWIncomeView,
                    mMOrderView,mMAmountView,mMInsuranceView,mMIncomeView,
                    mDateView,mSelectView,
                    mSearchOrderView,mSearchAmountView,mSearchInsuranceView,mSearchRealView;


    private RelativeLayout mOverView,mSearchView;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat sdfDay = new SimpleDateFormat("yyyy-MM-dd");

    private String mStart="",mEnd="";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income);

        initView();


    }

    private void initView() {

        mTOrderView = findViewById(R.id.income_t_order);
        mTAmountView = findViewById(R.id.income_t_amount);
        mTInsuranceView = findViewById(R.id.income_t_insurance);
        mTIncomeView  = findViewById(R.id.income_t_real);

        mWOrderView = findViewById(R.id.income_w_order);
        mWAmountView = findViewById(R.id.income_w_amount);
        mWInsuranceView = findViewById(R.id.income_w_insurance);
        mWIncomeView = findViewById(R.id.income_w_real);

        mMOrderView = findViewById(R.id.income_m_order);
        mMAmountView = findViewById(R.id.income_m_amount);
        mMInsuranceView = findViewById(R.id.income_m_insurance);
        mMIncomeView = findViewById(R.id.income_m_real);

        mDateView = findViewById(R.id.income_date);

        mOverView = findViewById(R.id.income_overview);
        mSearchView = findViewById(R.id.income_search);

        mSearchOrderView = findViewById(R.id.income_search_order);
        mSearchAmountView = findViewById(R.id.income_search_amount);
        mSearchInsuranceView = findViewById(R.id.income_search_insurance);
        mSearchRealView = findViewById(R.id.income_search_real);

        mOverView.setVisibility(View.VISIBLE);
        mSearchView.setVisibility(View.GONE);

        ImageView actionBarLef = findViewById(R.id.actionbar_left);
        TextView actionTitle = findViewById(R.id.actionbar_title);
        mSelectView = findViewById(R.id.income_select);

        actionBarLef.setImageResource(R.mipmap.back_arraw);
        actionTitle.setText(R.string.income_title);
        actionBarLef.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IncomeActivity.this.finish();
            }
        });

        mSelectView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                onSelectData();
                Intent intent = new Intent(IncomeActivity.this,TimeDialogActivity.class);
                startActivityForResult(intent,0X01);
            }
        });

        String mCurrentday = sdfDay.format(new Date(System.currentTimeMillis()));
        mDateView.setText(mCurrentday);
        onLoadData();

    }


    private void onLoadData()
    {

        if (!NetUtil.hasNet(IncomeActivity.this)) {
            DialogUtils.showToast(IncomeActivity.this,R.string.check_network_connect);
            return;
        }
        String url = ContentPath.incomPeriod ;
        RequestParams params = new RequestParams(url);
        String message = getResources().getString(R.string.income_load);
        DialogUtils.showProgressDialog(IncomeActivity.this, message);
        onConnect(params, new CallBack() {
            @Override
            public void onResponse(String response) {
                DialogUtils.dismissProgressDialog();
//                Log.i("IncomeActivity",response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.optString("status");
                    if(status.equals("1"))
                    {
                        JSONObject mResult = jsonObject.optJSONObject("result");
                        JSONObject mTodayObject = mResult.optJSONObject("today");
                        JSONObject mWeekObject = mResult.optJSONObject("thisweek");
                        JSONObject mMonthObject = mResult.optJSONObject("thismonth");

                        String mTQuantity = mTodayObject.optString("ordernums");
                        String mTAmount = mTodayObject.optString("amount");
                        String mTInsrucane = mTodayObject.optString("insurancefee");
                        String mTReal = mTodayObject.optString("profit");

                        String mWQuantity = mWeekObject.optString("ordernums");
                        String mWAmount = mWeekObject.optString("amount");
                        String mWInsurance = mWeekObject.optString("insurancefee");
                        String mWReal = mWeekObject.optString("profit");

                        String mMQuantity = mMonthObject.optString("ordernums");
                        String mMAmount = mMonthObject.optString("amount");
                        String mMInsruance = mMonthObject.optString("insurancefee");
                        String mMReal = mMonthObject.optString("profit");

                        mTOrderView.setText(mTQuantity +"Orders");
                        mTAmountView.setText("$" + mTAmount );
                        mTInsuranceView.setText("$"+ mTInsrucane);
                        mTIncomeView.setText("$" + mTReal);

                        mWOrderView.setText(mWQuantity + "Orders");
                        mWAmountView.setText("$" +  mWAmount);
                        mWInsuranceView.setText("$" + mWInsurance);
                        mWIncomeView.setText("$" + mWReal);

                        mMOrderView.setText(mMQuantity + "Orders");
                        mMAmountView.setText("$" + mMAmount );
                        mMInsuranceView.setText("$" +  mMInsruance);
                        mMIncomeView.setText("$" + mMReal);

                        mOverView.setVisibility(View.VISIBLE);
                        mSearchView.setVisibility(View.GONE);
                    }
                    else
                    {
                        String errorCode =  jsonObject.optString("errorcode");
                        if(errorCode.equals("60001"))
                        {
                            quickLogin(new QuickLoadCallBack() {
                                @Override
                                public void doSomeThing(Boolean boolon) {
                                    if (boolon == true) {
                                        onLoadData();
                                    } else {
                                        reLogin(IncomeActivity.this);
                                    }
                                }
                            });
                        }else
                        {
                            String msg = jsonObject.optString("msg");
                            DialogUtils.showToast(IncomeActivity.this,msg);
                        }

                    }
                }catch(Exception e)
                {
                    Log.e("Income","" + e.getMessage());
                }
            }

            @Override
            public void onFailure() {
                DialogUtils.dismissProgressDialog();

            }
        });


    }


    public void onSearchData()
    {
        if (!NetUtil.hasNet(IncomeActivity.this)) {
            DialogUtils.showToast(IncomeActivity.this, R.string.check_network_connect);
            return;
        }
        String url = ContentPath.income ;
        RequestParams params = new RequestParams(url);
        params.addBodyParameter("from", mStart);
        params.addBodyParameter("to", mEnd);
        String message = getResources().getString(R.string.income_load);
        DialogUtils.showProgressDialog(IncomeActivity.this, message);
        onConnect(params, new CallBack() {
            @Override
            public void onResponse(String response) {
                DialogUtils.dismissProgressDialog();
//                Log.i("IncomeActivity",response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.optString("status");
                    if(status.equals("1"))
                    {
                        JSONObject mResult = jsonObject.optJSONObject("result");
                        String mSOrder = mResult.optString("ordernums");
                        String mSAmount = mResult.optString("amount");
                        String mSInsurance = mResult.optString("insurancefee");
                        String mSReal = mResult.optString("profit");

                        mSearchOrderView.setText(mSOrder + "Orders");
                        mSearchAmountView.setText("$" +  mSAmount );
                        mSearchInsuranceView.setText("$" + mSInsurance);
                        mSearchRealView.setText("$" + mSReal);

                        mOverView.setVisibility(View.GONE);
                        mSearchView.setVisibility(View.VISIBLE);

                    }
                    else
                    {
                        String errorCode =  jsonObject.optString("errorcode");
                        if(errorCode.equals("60001"))
                        {
                            quickLogin(new QuickLoadCallBack() {
                                @Override
                                public void doSomeThing(Boolean boolon) {
                                    if (boolon == true) {
                                        onLoadData();
                                    } else {
                                        reLogin(IncomeActivity.this);
                                    }
                                }
                            });
                        }else
                        {
                            String msg = jsonObject.optString("msg");
                            DialogUtils.showToast(IncomeActivity.this,msg);
                        }

                    }
                }catch(Exception e)
                {
                    Log.e("Income","" + e.getMessage());
                }
            }

            @Override
            public void onFailure() {
                DialogUtils.dismissProgressDialog();

            }
        });


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0X01 && resultCode == 0X01)
        {
            //请求数据
//            Log.i("IncomeActivity",""+ data.getStringExtra("start") + "\t" + data.getStringExtra("end"));
            String  mStartTemp = data.getStringExtra("start");
            String mEndTemp = data.getStringExtra("end");
            //处理时间：

            try {
                Calendar mStartTime = Calendar.getInstance();
                mStartTime.setTime( sdfDay.parse(mStartTemp));
                mStartTime.set(Calendar.HOUR_OF_DAY,0);
                mStartTime.set(Calendar.MINUTE,0);
                mStartTime.set(Calendar.SECOND,0);
                mStart = sdf.format(mStartTime.getTime());

                Calendar mEndTime = Calendar.getInstance();
                mEndTime.setTime( sdfDay.parse(mEndTemp));
                mEndTime.set(Calendar.HOUR_OF_DAY,23);
                mEndTime.set(Calendar.MINUTE,59);
                mEndTime.set(Calendar.SECOND,59);
                mEnd = sdf.format(mEndTime.getTime());

                if(mStart== null || mEnd == null)
                {
                    DialogUtils.showToast(IncomeActivity.this,"Setting the query time failed");
                    return ;
                }
                mDateView.setText(mStartTemp +"\n-"+ mEndTemp);

            }catch(Exception e)
            {
                Log.e("Income","" + e.getMessage());

            }

            onSearchData();
        }


    }


}
