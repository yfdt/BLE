package com.blue.elephant.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blue.elephant.R;
import com.blue.elephant.custom.adapter.OrderAdapter;
import com.blue.elephant.custom.listview.XListView;
import com.blue.elephant.util.CallBack;
import com.blue.elephant.util.ContentPath;
import com.blue.elephant.util.DialogUtils;
import com.blue.elephant.util.NetUtil;
import com.blue.elephant.util.QuickLoadCallBack;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.http.RequestParams;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class OrderListActivity extends BaseActivity {

    private XListView mListView;
    private OrderAdapter mAdapter;
    private TextView actionBarSubMenu;
    private String rentStatus = "pending";
    private PopupWindow mMenuPopWindow;

    private int page = 1;

    private XListView.IXListViewListener onRefreshListener = new XListView.IXListViewListener() {
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
        setContentView(R.layout.activity_orderlist);

        ImageView mLeftMenu = findViewById(R.id.actionbar_left);
        TextView actionbarTitle = findViewById(R.id.actionbar_title);
        actionBarSubMenu = findViewById(R.id.actionbar_sub_menu);
        ImageView actionMenuIcon = findViewById(R.id.actionbar_sub_icon);
        actionMenuIcon.setImageResource(R.mipmap.bike_map_down_arrow);

        mListView = findViewById(R.id.order_list_view);
        mAdapter = new OrderAdapter(OrderListActivity.this);
        mListView.setPullLoadEnable(true);
        mListView.setPullRefreshEnable(true);
        mListView.setXListViewListener(onRefreshListener);
        mListView.setAdapter(mAdapter);

        mLeftMenu.setImageResource(R.mipmap.back_arraw);
        actionbarTitle.setText(R.string.order_list_title);
        actionBarSubMenu.setText(R.string.order_menu_rent);

        mLeftMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OrderListActivity.this.finish();
            }
        });
        final LinearLayout mClassicView = findViewById(R.id.actionbar_right);
        mClassicView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {

                View view = LayoutInflater.from(OrderListActivity.this).inflate(R.layout.menu_order_list,null);
                mMenuPopWindow = DialogUtils.showModifyVehicleMenu(OrderListActivity.this,mClassicView,view);
                TextView tvAll = view.findViewById(R.id.menu_order_all);
                TextView tvHistory = view.findViewById(R.id.menu_order_history);
                TextView tvRent = view.findViewById(R.id.menu_order_rent);
                tvAll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMenuPopWindow.dismiss();
                        page = 1;
                        rentStatus = "";
                        loadServer();

                    }
                });

                tvHistory.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMenuPopWindow.dismiss();
                        rentStatus = "completed";
                        actionBarSubMenu.setText(R.string.order_menu_history);
                        page = 1;
                        loadServer();
                    }
                });

                tvRent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMenuPopWindow.dismiss();
                        page = 1;
                        rentStatus = "pending";
                        actionBarSubMenu.setText(R.string.order_menu_rent);
                        loadServer();

                    }
                });


            }
        });

//        onLoadOrder();
        loadServer();

    }


    private void loadServer()
    {
        if (!NetUtil.hasNet(OrderListActivity.this)) {
            DialogUtils.showToast(OrderListActivity.this,R.string.check_network_connect);
            return;
        }
        Map<String,String> heads = new HashMap<>();
        heads.put("page",""+ page);
        heads.put("pageSize","40");
        heads.put("appversion","1");
        if(!rentStatus.equals(""))
        {
            heads.put("filters[0].key","orderstatus");
            heads.put("filters[0].opr","EQ");
            heads.put("filters[0].values[0]",rentStatus);
        }
        heads.put("sorts[0].key","starttime");
        heads.put("sorts[0].value","DESC");
        String message = getResources().getString(R.string.upload_load);
        DialogUtils.showProgressDialog(OrderListActivity.this, message);
        onServerTime(ContentPath.orderList, heads, new CallBack() {
            @Override
            public void onResponse(String response) {
                mAdapter.setServerTime(response);
                onLoadOrder();
            }

            @Override
            public void onFailure() {
                onLoadOrder();
            }
        });
    }

    private void onLoadOrder()
    {
        String url = ContentPath.orderList ;
        final RequestParams params = new RequestParams(url);
        params.addBodyParameter("page", page+ "");
        params.addBodyParameter("pageSize", "40");
        if(!rentStatus.equals(""))
        {
            params.addBodyParameter("filters[0].key", "orderstatus");
            params.addBodyParameter("filters[0].opr", "EQ");
            params.addBodyParameter("filters[0].values[0]", rentStatus);
        }
        params.addBodyParameter("sorts[0].key", "starttime");
        params.addBodyParameter("sorts[0].value", "DESC");

        onConnect(params, new CallBack() {
            @Override
            public void onResponse(String response) {
                DialogUtils.dismissProgressDialog();
                mListView.stopLoadMore();
                mListView.stopRefresh();
//                Log.i("OrderListActivity",""+ response);
                try{
                    JSONObject mResponseObject = new JSONObject(response);
                    String status = mResponseObject.optString("status");
                    if(status.equals("1"))
                    {
//                        Log.i("OrderListActivity"," success :"+ response);
                        JSONObject mResultObject = mResponseObject.getJSONObject("result");
                        page = mResultObject.optInt("page");
                        JSONArray mOrderArray = mResultObject.getJSONArray("rows");
                        ArrayList<JSONObject> mOrderList = new ArrayList<>();
                        for(int i=0;i<mOrderArray.length();i++)
                        {
                            mOrderList.add(mOrderArray.optJSONObject(i));
                        }
                        if(page == 1)
                        {
                            mAdapter.setData(mOrderList);
                        }
                        else
                        {
                            mAdapter.addData(mOrderList);
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
                                        onLoadOrder();
                                    } else {
                                        reLogin(OrderListActivity.this);
                                    }
                                }
                            });
                        }else
                        {
                            String msg = mResponseObject.optString("msg");
                            DialogUtils.showToast(OrderListActivity.this,msg);
                        }
                    }
                }catch(Exception e)
                {
                    Log.e("OrderListActivity",""+ e.getMessage());
                }

            }

            @Override
            public void onFailure() {
                DialogUtils.dismissProgressDialog();
                mListView.stopLoadMore();
                mListView.stopRefresh();
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0X02)
        {
            page = 1;
//            rentStatus = "";
            onLoadOrder();
        }
    }
}
