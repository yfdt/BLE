package com.blue.elephant.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blue.elephant.R;
import com.blue.elephant.custom.adapter.BikeManageAdapter;
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


public class BikeManageActivity extends BaseActivity {

    private TextView mClassicMenu;//分类的标签提示
    private XListView mListView;
    private EditText mBikeSerialView;
    private RelativeLayout mTitleContainer,mSearchContainer;
    private BikeManageAdapter mAdapter;
    private PopupWindow mMenuPopWindow;

    private String mVehicleNumber="";
    private String mBikeDisable = "";
    private int page = 1;

    private String rentStatus = "";

    private XListView.IXListViewListener onListListener = new XListView.IXListViewListener() {
        @Override
        public void onRefresh() {
            mVehicleNumber= "";
            page = 1;
            onLoadBike();
//            Log.i("BikeManageActivity","下拉刷新");

        }

        @Override
        public void onLoadMore() {
            mVehicleNumber= "";
            page ++;
            onLoadBike();
//            Log.i("BikeManageActivity","上拉加载");
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bike_manage);

        mTitleContainer = findViewById(R.id.bike_manage_top);
        mSearchContainer = findViewById(R.id.bike_manage_second);
        mListView = findViewById(R.id.bike_manage_listview);
        mClassicMenu = findViewById(R.id.bike_manage_classic);// 当前的车辆类型
        mBikeSerialView = findViewById(R.id.bike_manage_edit);

        mAdapter = new BikeManageAdapter(BikeManageActivity.this);

        //返回
        findViewById(R.id.actionbar_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BikeManageActivity.this.finish();
            }
        });
        //搜索界面唤出
        findViewById(R.id.bike_manage_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mTitleContainer.setVisibility(View.GONE);
                mSearchContainer.setVisibility(View.VISIBLE);
            }
        });

        //切换到当前的标题
        findViewById(R.id.bike_manage_menu_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTitleContainer.setVisibility(View.VISIBLE);
                mSearchContainer.setVisibility(View.GONE);
                //搜索当前的数据
                mVehicleNumber = "";
                mBikeDisable = "";
                page = 1;
                onLoadBike();


            }
        });

        //分类搜索
        final RelativeLayout mClassicView = findViewById(R.id.bike_manage_action);
        mClassicView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                //搜索当前的数据
                View view = LayoutInflater.from(BikeManageActivity.this).inflate(R.layout.menu_bike,null);
                mMenuPopWindow = DialogUtils.showBikeMenu(BikeManageActivity.this,mClassicView,view);
                TextView tvAll = view.findViewById(R.id.menu_vehicle_all);
                TextView tvNoRent = view.findViewById(R.id.menu_vehicle_no_rent);
                TextView tvRent = view.findViewById(R.id.menu_vehicle_rent);
                TextView tvDisable = view.findViewById(R.id.menu_vehicle_disable);
                tvAll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMenuPopWindow.dismiss();
                        mClassicMenu.setText(R.string.bike_manage_menu_all);
                        rentStatus = "";
                        mVehicleNumber= "";
                        mBikeDisable = "";
                        page = 1;
                        onLoadBike();
                    }
                });
                tvNoRent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMenuPopWindow.dismiss();
                        mClassicMenu.setText(R.string.bike_manage_menu_no_rent);
                        rentStatus = "unoccupied";
                        mVehicleNumber= "";
                        mBikeDisable = "";
                        page = 1;
                        onLoadBike();
                    }
                });
                tvRent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMenuPopWindow.dismiss();
                        mClassicMenu.setText(R.string.bike_manage_menu_rent);
                        rentStatus = "occupied";
                        mVehicleNumber= "";
                        mBikeDisable = "";
                        page = 1;
                        onLoadBike();
                    }
                });

                tvDisable.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMenuPopWindow.dismiss();
                        mClassicMenu.setText(R.string.bike_manage_menu_disable);
                        rentStatus = "";
                        mVehicleNumber= "";
                        mBikeDisable = "1";
                        page = 1;
                        onLoadBike();
                    }
                });

            }
        });

        findViewById(R.id.bike_manage_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BikeManageActivity.this,AddBikeActivity.class);
                startActivityForResult(intent,0X01);
            }
        });

        mBikeSerialView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // if(actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE)
                //                {
                if(actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_SEND
                        || actionId == EditorInfo.IME_ACTION_DONE)
                {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

                    //Search vehicle
                       mVehicleNumber = mBikeSerialView.getText().toString();
                    //搜索车辆的数据
                    page = 1;
                    onLoadBike();
                    return true;
                }
                return false;
            }
        });

        //切换当前的菜单栏状态
        mTitleContainer.setVisibility(View.VISIBLE);
        mSearchContainer.setVisibility(View.GONE);

        mListView.setPullLoadEnable(true);
        mListView.setPullRefreshEnable(true);
        mListView.setXListViewListener(onListListener);
        mListView.setAdapter(mAdapter);

        onLoadBike();
    }


    private void onLoadBike()
    {
        if (!NetUtil.hasNet(BikeManageActivity.this)) {
            DialogUtils.showToast(BikeManageActivity.this,R.string.check_network_connect);
            return;
        }
        String custId = mPreference.getString("custid","");
        String url = ContentPath.queryBike ;
        RequestParams params = new RequestParams(url);
        params.addBodyParameter("page", page+ "");
        params.addBodyParameter("pageSize", 40+ "");
        if(!custId.equals(""))
        {
            params.addBodyParameter("filters[0].key", "custid");
            params.addBodyParameter("filters[0].opr", "EQ");
            params.addBodyParameter("filters[0].values[0]", custId);
        }
        if(!rentStatus.equals(""))
        {
            params.addBodyParameter("filters[1].key", "rentstatus");
            params.addBodyParameter("filters[1].opr", "EQ");
            params.addBodyParameter("filters[1].values[0]", rentStatus);
            if(!mVehicleNumber.equals(""))
            {
                params.addBodyParameter("filters[2].key", "bikecode");
                params.addBodyParameter("filters[2].opr", "EQ");
                params.addBodyParameter("filters[2].values[0]", mVehicleNumber);
            }
        }
        else
        {
            if(!mVehicleNumber.equals(""))
            {
                params.addBodyParameter("filters[1].key", "bikecode");
                params.addBodyParameter("filters[1].opr", "LIKE");
                params.addBodyParameter("filters[1].values[0]", mVehicleNumber);
            }
        }
        if(!mBikeDisable.equals(""))
        {
            params.addBodyParameter("filters[1].key", "disabled");
            params.addBodyParameter("filters[1].opr", "EQ");
            params.addBodyParameter("filters[1].values[0]", mBikeDisable);
        }
        params.addBodyParameter("sorts[0].key", "electricity");
        params.addBodyParameter("sorts[0].value", "ASC");
        String message = getResources().getString(R.string.bike_manage_load);
        DialogUtils.showProgressDialog(BikeManageActivity.this, message);
        onConnect(params, new CallBack() {
            @Override
            public void onResponse(String response) {
                DialogUtils.dismissProgressDialog();
                mListView.stopRefresh();
                mListView.stopLoadMore();
                try{
                    JSONObject mResponse = new JSONObject(response);
//                    Log.i("BikeManageActivity",""+ response);
                    String status = mResponse.optString("status");
                    if(status.equals("1"))
                    {
                        JSONObject mResultObject = mResponse.getJSONObject("result");
                        JSONArray mBikeArray = mResultObject.getJSONArray("rows");
                        page = mResultObject.getInt("page");
                        ArrayList<JSONObject> mBikeList  = new ArrayList<>();
                        for(int i=0;i< mBikeArray.length();i++ )
                        {
                            JSONObject mObject = mBikeArray.getJSONObject(i);
                            mBikeList.add(mObject);
                        }
                        if(page ==1)
                        {
                            mAdapter.setData(mBikeList);
                        }
                        else
                        {
                            mAdapter.addData(mBikeList);
                        }
                    }
                    else
                    {
//                        Log.i("BikeManageActivity","statue:"+status );
                        String errorcode =  mResponse.optString("errorcode");
                        if (errorcode.equals("60001")) {
                            quickLogin(new QuickLoadCallBack() {
                                @Override
                                public void doSomeThing(Boolean boolon) {
                                    if (boolon == true) {
                                        onLoadBike();
                                    } else {
                                        reLogin(BikeManageActivity.this);
                                    }
                                }
                            });
                        } else {
                            String msg = mResponse.optString("msg");
                            DialogUtils.showToast(BikeManageActivity.this,msg);
                        }
                    }

                }catch(Exception e)
                {
                    Log.e("BikeManageActivity","Exeption ："+ e.getMessage());
                }
            }

            @Override
            public void onFailure() {
                DialogUtils.dismissProgressDialog();
                mListView.stopRefresh();
                mListView.stopLoadMore();
//                Log.i("BikeManageActivity","onFailure");

            }
        });


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 0X01)
        {
            page = 1;
            onLoadBike();
        }
    }
}
