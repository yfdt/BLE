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
import com.blue.elephant.custom.adapter.BillAdapter;
import com.blue.elephant.custom.listview.XListView;
import com.blue.elephant.util.CallBack;
import com.blue.elephant.util.ContentPath;
import com.blue.elephant.util.DialogUtils;
import com.blue.elephant.util.NetUtil;
import com.blue.elephant.util.QuickLoadCallBack;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.http.RequestParams;

import java.util.ArrayList;


public class BillingActivity extends BaseActivity {

    private XListView mListView;
    private BillAdapter mAdapter;
    private RelativeLayout mEmptyView;

    private int page  = 1;

    private XListView.IXListViewListener onRefreshListener = new XListView.IXListViewListener() {
        @Override
        public void onRefresh() {
            page = 1;
            loadCard();
//            mListView.stopRefresh();
        }

        @Override
        public void onLoadMore() {
            page  = 1 ;
            loadCard();
//            mListView.stopLoadMore();
        }
    };

    private BillAdapter.OnBillListener onBillListener = new BillAdapter.OnBillListener() {
        @Override
        public void onDelete(String cardID) {

            deleteCard(cardID);
        }

        @Override
        public void onDefault(String cardID) {

            setDefault(cardID);

        }
    };



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing);

        ImageView actionBarLeft = findViewById(R.id.actionbar_left);
        TextView actionBarTitle = findViewById(R.id.actionbar_title);
        mListView = findViewById(R.id.bill_listview);
        mEmptyView = findViewById(R.id.bill_empty_container);
        TextView mAddView = findViewById(R.id.bill_add);
        mAdapter = new BillAdapter(BillingActivity.this);
        mAdapter.setBillListener(onBillListener);
        actionBarLeft.setImageResource(R.mipmap.back_arraw);
        actionBarTitle.setText(R.string.bill_title);

        actionBarLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BillingActivity.this.finish();
            }
        });

        mAddView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BillingActivity.this,AddPayActivity.class);
                startActivityForResult(intent,0X01);
            }
        });

        mListView.setPullLoadEnable(true);
        mListView.setPullLoadEnable(true);
        mListView.setXListViewListener(onRefreshListener);
        mListView.setAdapter(mAdapter);

        //当前设置未0数据
        mListView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.GONE);
        loadCard();
    }


    private void loadCard()
    {
        if (!NetUtil.hasNet(BillingActivity.this)) {
            DialogUtils.showToast(BillingActivity.this,R.string.check_network_connect);
            return;
        }
        String url = ContentPath.getCardList ;
        RequestParams params = new RequestParams(url);
//        params.addBodyParameter("sorts[0].key", "funding");
//        params.addBodyParameter("sorts[0].value", "ASC");
        String message = getResources().getString(R.string.bill_list_load);
        DialogUtils.showProgressDialog(BillingActivity.this, message);
        onConnect(params, new CallBack() {
            @Override
            public void onResponse(String response) {
                DialogUtils.dismissProgressDialog();
                mListView.stopRefresh();
                mListView.stopLoadMore();
//                Log.i("Bill","" + response);
                try{
                    JSONObject mResponseObject = new JSONObject(response);
                    String status = mResponseObject.optString("status");
                    if(status.equals("1"))
                    {
//                        Log.i("Bill","" + response);
                        if(mResponseObject.optString("result").equals("{}"))
                        {
                            page = 1;
                            mListView.setVisibility(View.GONE);
                            mEmptyView.setVisibility(View.VISIBLE);
                            return ;
                        }
                        JSONObject mResultObject = mResponseObject.optJSONObject("result");
                        JSONObject mCustomerObject = mResultObject.optJSONObject("customer");
                        String defaultCard = mCustomerObject.optString("defaultSource");
                        JSONObject mSourceObejct = mCustomerObject.optJSONObject("sources");
//                        int total = mSourceObejct.optInt("totalCount");
                        JSONArray mCardArray = mSourceObejct.optJSONArray("data");
                        ArrayList<JSONObject> mCardList = new ArrayList<>();
                        mAdapter.setDefauitID(defaultCard);
                        for(int i=0;i<mCardArray.length();i++)
                        {
                           JSONObject mCard = mCardArray.optJSONObject(i);
                           mCardList.add(mCard);
                        }
                        //更换位置
                        for(int i=0;i<mCardList.size();i++)
                        {
                            JSONObject mObject = mCardList.get(i);
                            String cardID = mObject.optString("id");
                            if(cardID.equals(defaultCard) && i !=0)
                            {
                                JSONObject mOrigin = mCardList.get(0);
                                mCardList.remove(i);//删除原来位置的数据
                                mCardList.set(0,mObject);
                                mCardList.add(mOrigin);
                            }
                        }
                        //判断当前的存在银行卡信息
                        if(mCardList.size() > 0 )
                        {
                            page = 1;
                            mAdapter.setData(mCardList);
                            mListView.setVisibility(View.VISIBLE);
                            mEmptyView.setVisibility(View.GONE);
                        }
                        else
                        {
                            page = 1;
                            mListView.setVisibility(View.GONE);
                            mEmptyView.setVisibility(View.VISIBLE);
                        }



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
                                        loadCard();
                                    } else {
                                        reLogin(BillingActivity.this);
                                    }
                                }
                            });
                        }else
                        {
                            String msg = mResponseObject.optString("msg");
                            DialogUtils.showToast(BillingActivity.this,msg);
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
                mListView.stopRefresh();
                mListView.stopLoadMore();
            }
        });
    }


    private void setDefault(final String cardID)
    {
        if (!NetUtil.hasNet(BillingActivity.this)) {
            DialogUtils.showToast(BillingActivity.this,R.string.check_network_connect);
            return;
        }
        String url = ContentPath.setDefaultCard ;
        RequestParams params = new RequestParams(url);
        params.addBodyParameter("cardid", cardID);
        String message = getResources().getString(R.string.bill_list_default);
        DialogUtils.showProgressDialog(BillingActivity.this, message);
        onConnect(params, new CallBack() {
            @Override
            public void onResponse(String response) {
                DialogUtils.dismissProgressDialog();
//                Log.i("Bill","" + response);
                try{
                    JSONObject mResponseObject = new JSONObject(response);
                    String status = mResponseObject.optString("status");
                    if(status.equals("1"))
                    {
//                        Log.i("Bill","default\t" + response);
                        //{"status":1}
                        loadCard();
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
                                        setDefault(cardID);
                                    } else {
                                        reLogin(BillingActivity.this);
                                    }
                                }
                            });
                        }else
                        {
                            String msg = mResponseObject.optString("msg");
                            DialogUtils.showToast(BillingActivity.this,msg);
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

    private void deleteCard(final String cardID)
    {
        if (!NetUtil.hasNet(BillingActivity.this)) {
            DialogUtils.showToast(BillingActivity.this,R.string.check_network_connect);
            return;
        }
        String url = ContentPath.unBindCard ;
        RequestParams params = new RequestParams(url);
        params.addBodyParameter("cardid", cardID);
        String message = getResources().getString(R.string.bill_list_delete);
        DialogUtils.showProgressDialog(BillingActivity.this, message);
        onConnect(params, new CallBack() {
            @Override
            public void onResponse(String response) {
                DialogUtils.dismissProgressDialog();
//                Log.i("Bill","" + response);
                try{
                    JSONObject mResponseObject = new JSONObject(response);
                    String status = mResponseObject.optString("status");
                    if(status.equals("1"))
                    {
//                        Log.i("Bill","delete\t  " + response);
                        loadCard();
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
                                        deleteCard(cardID);
                                    } else {
                                        reLogin(BillingActivity.this);
                                    }
                                }
                            });
                        }else
                        {
                            String msg = mResponseObject.optString("msg");
                            DialogUtils.showToast(BillingActivity.this,msg);
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



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0X01)
        {
            //重新加载数据
            page = 1;
            loadCard();

        }
    }
}
