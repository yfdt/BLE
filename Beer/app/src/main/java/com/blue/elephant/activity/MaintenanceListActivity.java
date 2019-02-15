package com.blue.elephant.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.blue.elephant.R;
import com.blue.elephant.custom.adapter.MainTenAdapter;
import com.blue.elephant.custom.listview.XListView;
import com.blue.elephant.util.AppUtil;
import com.blue.elephant.util.CallBack;
import com.blue.elephant.util.ContentPath;
import com.blue.elephant.util.DateUtil;
import com.blue.elephant.util.DialogUtils;
import com.blue.elephant.util.NetUtil;
import com.blue.elephant.util.QuickLoadCallBack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.util.KeyValue;
import org.xutils.http.RequestParams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/****
 * 维修管理列表
 */
public class MaintenanceListActivity extends BaseActivity {

    private XListView mListView;
    private TextView mMenuView;
    private MainTenAdapter mAdapter;
    private LinearLayout mMenuRight;
    private PopupWindow mMenuPopWindow;
    private int page = 1;
    private String maintenanceStatus="pending";


    private XListView.IXListViewListener mListViewListener = new XListView.IXListViewListener() {
        @Override
        public void onRefresh() {
            page = 1;
            loadData();
        }

        @Override
        public void onLoadMore() {
            page ++;
            loadData();
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainten_list);

        ImageView actionBarLeft = findViewById(R.id.actionbar_left);
        TextView actionBarTitle = findViewById(R.id.actionbar_title);
        mMenuView = findViewById(R.id.actionbar_sub_menu);
        ImageView actionBarIcon = findViewById(R.id.actionbar_sub_icon);

        mListView = findViewById(R.id.mainten_list_listview);
        mAdapter = new MainTenAdapter(MaintenanceListActivity.this);
        actionBarLeft.setImageResource(R.mipmap.back_arraw);
        actionBarTitle.setText(R.string.mainten_list_title);
        mMenuRight = findViewById(R.id.actionbar_right);
        mMenuView.setText("In service");
        actionBarIcon.setImageResource(R.mipmap.bike_map_down_arrow);

        actionBarLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaintenanceListActivity.this.finish();
            }
        });

        mMenuRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //搜索当前的数据
                View view = LayoutInflater.from(MaintenanceListActivity.this).inflate(R.layout.menu_maintenance,null);
                mMenuPopWindow = DialogUtils.showModifyVehicleMenu(MaintenanceListActivity.this,mMenuRight,view);
                TextView tvAll = view.findViewById(R.id.menu_maintenance_all);
                TextView tvNoRent = view.findViewById(R.id.menu_maintenance_in);
                TextView tvRent = view.findViewById(R.id.menu_maintenance_stop);
                tvAll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMenuPopWindow.dismiss();
                        mMenuView.setText(R.string.bike_manage_menu_all);
                        maintenanceStatus = "";
                        page = 1;
                        loadData();
                    }
                });
                tvNoRent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMenuPopWindow.dismiss();
                        mMenuView.setText(R.string.mainten_list_statue_service);
                        maintenanceStatus = "pending";
                        page = 1;
                        loadData();
                    }
                });
                tvRent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMenuPopWindow.dismiss();
                        mMenuView.setText(R.string.mainten_list_statue_completed);
                        maintenanceStatus = "completed";
                        page = 1;
                        loadData();
                    }
                });



            }
        });

        mListView.setPullRefreshEnable(true);
        mListView.setPullLoadEnable(true);
        mListView.setXListViewListener(mListViewListener);
        mListView.setAdapter(mAdapter);

        loadData();
    }

    private void loadData()
    {
        if (!NetUtil.hasNet(MaintenanceListActivity.this)) {
            DialogUtils.showToast(MaintenanceListActivity.this,R.string.check_network_connect);
            return;
        }
        Map<String,String> heads = new HashMap<>();
        heads.put("page",""+ page);
        heads.put("pageSize","40");
        heads.put("appversion","1");
        String message = getResources().getString(R.string.mainten_list_load);
        DialogUtils.showProgressDialog(MaintenanceListActivity.this, message);
        onServerTime(ContentPath.queryMaintenance, heads, new CallBack() {
            @Override
            public void onResponse(String response) {
                loadMaintenance();
                mAdapter.setServerTime(response);
//                Log.i("Maintenance","----->" + response);
            }

            @Override
            public void onFailure() {
                loadMaintenance();
            }
        });

    }


    private void loadMaintenance()
    {
        String url = ContentPath.queryMaintenance ;
        final RequestParams params = new RequestParams(url);
        params.addBodyParameter("page", page+ "");
        params.addBodyParameter("pageSize", 40+"");
        if(!maintenanceStatus.equals(""))
        {
            params.addBodyParameter("filters[0].key", "maintenancestatus");
            params.addBodyParameter("filters[0].opr", "EQ");
            params.addBodyParameter("filters[0].values[0]", maintenanceStatus);
        }

        params.addBodyParameter("sorts[0].key", "starttime");
        params.addBodyParameter("sorts[0].value", "DESC");

        onConnect(params, new CallBack() {
            @Override
            public void onResponse(String response) {
                DialogUtils.dismissProgressDialog();
//                Log.i("MaintenanceList","" + sb.toString());
                mListView.stopRefresh();
                mListView.stopLoadMore();
                try {
                    JSONObject mResponseObject = new JSONObject(response);
                    String status = mResponseObject.optString("status");
                    if(status.equals("1"))
                    {
//                        Log.i("MaintenanceList","" + response);
                        JSONObject mBikeObject = mResponseObject.optJSONObject("result");
                        page = mBikeObject.optInt("page");
                        JSONArray mBikeArray = mBikeObject.optJSONArray("rows");
                        ArrayList<JSONObject> mBikeList = new ArrayList<>();
                        for(int i=0;i<mBikeArray.length();i++)
                        {
                            JSONObject mBikeItem =  mBikeArray.optJSONObject(i);
                            mBikeList.add(mBikeItem);
                        }
                        if(page == 1)
                        {
                            mAdapter.setData(mBikeList);
                        }
                        else if(page > 1)
                        {
                            mAdapter.addData(mBikeList);
                        }

                    }
                    else if(status.equals("0"))
                    {
                        String errorcode =  mResponseObject.optString("errorcode");
                        if (errorcode.equals("60001")) {
                            quickLogin(new QuickLoadCallBack() {
                                @Override
                                public void doSomeThing(Boolean boolon) {
                                    if (boolon == true) {
                                        loadMaintenance();
                                    } else {
                                        reLogin(MaintenanceListActivity.this);
                                    }
                                }
                            });
                        } else {
                            String msg = mResponseObject.optString("msg");
                            DialogUtils.showToast(MaintenanceListActivity.this,msg);
                        }
                    }


                }catch(Exception e)
                {
                    Log.e("MaintenanceListActivity","" + e.getMessage());
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0X01)
        {
            page = 1;
            loadMaintenance();
        }
    }
}
