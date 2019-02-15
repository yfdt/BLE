package com.blue.elephant.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.blue.elephant.R;
import com.blue.elephant.custom.adapter.DeductionAdapter;
import com.blue.elephant.custom.listview.XListView;
import com.blue.elephant.util.CallBack;
import com.blue.elephant.util.ContentPath;
import com.blue.elephant.util.DialogUtils;
import com.blue.elephant.util.NetUtil;
import com.blue.elephant.util.QuickLoadCallBack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.http.RequestParams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class DeductionRecordActivity extends BaseActivity {

    private XListView mListView;
    private TextView mMenuText;
    private DeductionAdapter mAdapter;
    private LinearLayout mMenuLayout;
    private int page = 1;
    private String mRecordStatus = "";
    private PopupWindow mMenuPopWindow;


    private XListView.IXListViewListener onListViewListener = new XListView.IXListViewListener() {
        @Override
        public void onRefresh() {
            page = 1;
            loadServer();
        }

        @Override
        public void onLoadMore() {
            page ++;
            loadServer();

        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deducte_record);
        ImageView actionBarLeft = findViewById(R.id.actionbar_left);
        TextView actionBarTitle = findViewById(R.id.actionbar_title);
        mMenuText = findViewById(R.id.actionbar_sub_menu);
        ImageView actionBarIcon = findViewById(R.id.actionbar_sub_icon);
        mMenuLayout = findViewById(R.id.actionbar_right);
        actionBarIcon.setImageResource(R.mipmap.bike_map_down_arrow);
        mMenuText.setText(R.string.bike_map_menu_all);
        actionBarLeft.setImageResource(R.mipmap.back_arraw);
        actionBarTitle.setText(R.string.deduction_record);
        mListView = findViewById(R.id.deducte_record_listview);
        mListView.setPullRefreshEnable(true);
        mListView.setPullLoadEnable(true);
        mListView.setXListViewListener(onListViewListener);
        mAdapter = new DeductionAdapter(DeductionRecordActivity.this);
        mListView.setAdapter(mAdapter);

        mMenuLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View view = LayoutInflater.from(DeductionRecordActivity.this).inflate(R.layout.menu_deduction_record,null);
                mMenuPopWindow = DialogUtils.showVehicleMenu(DeductionRecordActivity.this,mMenuLayout,view);
                TextView tvAll = view.findViewById(R.id.menu_deduction_all);
                TextView tvHistory = view.findViewById(R.id.menu_deduction_wait);
                TextView tvRent = view.findViewById(R.id.menu_deduction_paid);
                tvAll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMenuPopWindow.dismiss();
                        page = 1;
                        mRecordStatus = "";
                        mMenuText.setText(R.string.bike_manage_menu_all);
                        loadServer();
                    }
                });

                tvHistory.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMenuPopWindow.dismiss();
                        mRecordStatus = "WAIT";
                        page = 1;
                        mMenuText.setText(R.string.deduction_menu_wait);
                        loadServer();
                    }
                });

                tvRent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMenuPopWindow.dismiss();
                        page = 1;
                        mRecordStatus = "PAID";
                        mMenuText.setText(R.string.deduction_menu_paid);
                        loadServer();
                    }
                });


            }
        });

        actionBarLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeductionRecordActivity.this.finish();
            }
        });

        loadServer();

    }

    private void loadServer()
    {
        if (!NetUtil.hasNet(DeductionRecordActivity.this)) {
            DialogUtils.showToast(DeductionRecordActivity.this,R.string.check_network_connect);
            return;
        }
        Map<String,String> heads = new HashMap<>();
        heads.put("page",""+ page);
        heads.put("pageSize","40");
        heads.put("appversion","1");
        if(!mRecordStatus.equals(""))
        {
            heads.put("filters[0].key","paystatus");
            heads.put("filters[0].opr","EQ");
            heads.put("filters[0].values[0]",mRecordStatus);
        }
        heads.put("sorts[0].key","createtime");
        heads.put("sorts[0].value","DESC");
        String message = getResources().getString(R.string.deduction_load);
        DialogUtils.showProgressDialog(DeductionRecordActivity.this, message);
        onServerTime(ContentPath.payList, heads, new CallBack() {
            @Override
            public void onResponse(String response) {
                mAdapter.setServerTime(response);
                loadRecord();
            }

            @Override
            public void onFailure() {
                loadRecord();
            }
        });
    }


    private void loadRecord()
    {

        String url = ContentPath.payList ;
        RequestParams params = new RequestParams(url);
        params.addBodyParameter("page", ""+ page);
        params.addBodyParameter("pageSize", "40");
        if(!mRecordStatus.equals(""))
        {
            params.addBodyParameter("filters[0].key", "paystatus");
            params.addBodyParameter("filters[0].opr", "EQ");
            params.addBodyParameter("filters[0].values[0]", mRecordStatus);
        }

        params.addBodyParameter("sorts[0].key", "createtime");
        params.addBodyParameter("sorts[0].value", "DESC");


        onConnect(params, new CallBack() {
            @Override
            public void onResponse(String response) {
                DialogUtils.dismissProgressDialog();
                mListView.stopRefresh();
                mListView.stopLoadMore();
//                Log.i("Record", "record : "+ response);
                //{"errorcode":"90019002","msg":"","status":"0"}
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int status = jsonObject.optInt("status");
                    if(status == 1)
                    {
//                        Log.i("Record",response);
                        JSONObject mResultObject= jsonObject.optJSONObject("result");
                        page =  mResultObject.optInt("page");
                        JSONArray mRecordArray = mResultObject.optJSONArray("rows");
                        ArrayList<JSONObject> mRecordList = new ArrayList<>();
                        for(int i=0;i<mRecordArray.length();i++)
                        {
                            JSONObject mRecord = mRecordArray.getJSONObject(i);
                            mRecordList.add(mRecord);
                        }
                        if(page == 1)
                        {
                            mAdapter.setData(mRecordList);
                        }
                        else
                        {
                            mAdapter.addData(mRecordList);
                        }

                    }
                    else if(status == 0)
                    {

                        String errorCode =  jsonObject.optString("errorcode");
                        if(errorCode.equals("60001"))
                        {
                            quickLogin(new QuickLoadCallBack() {
                                @Override
                                public void doSomeThing(Boolean boolon) {
                                    if (boolon == true) {
                                        loadRecord();
                                    } else {
                                        reLogin(DeductionRecordActivity.this);
                                    }
                                }
                            });
                        }else
                        {
                            String msg = jsonObject.optString("msg");
                            DialogUtils.showToast(DeductionRecordActivity.this,msg);
                        }
                    }
                }catch(Exception e)
                {
                    Log.e("Test"," error: "+ e.getMessage());
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


}
