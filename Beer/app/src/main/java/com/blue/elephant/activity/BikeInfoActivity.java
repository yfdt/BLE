package com.blue.elephant.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.blue.elephant.R;
import com.blue.elephant.util.CallBack;
import com.blue.elephant.util.ContentPath;
import com.blue.elephant.util.DateUtil;
import com.blue.elephant.util.DialogUtils;
import com.blue.elephant.util.NetUtil;
import com.blue.elephant.util.QuickLoadCallBack;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.http.RequestParams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BikeInfoActivity extends BaseActivity {

    private GoogleMap mGoogleMap;
    private SupportMapFragment mMapFragment;
    private Marker mMarker;
    private double latitude;
    private double longitude;

    private JSONObject mBikeObject;
    private String mTimeZone;

    private OnMapReadyCallback mMapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        UiSettings mSetting = mGoogleMap.getUiSettings();
        mSetting.setMyLocationButtonEnabled(false);
        if (mGoogleMap != null) {
            if (ActivityCompat.checkSelfPermission(BikeInfoActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(BikeInfoActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mGoogleMap.setMyLocationEnabled(true);
        }

        mGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                Context context = BikeInfoActivity.this;
                View view = LayoutInflater.from(context).inflate(R.layout.map_bike,null);
                TextView tvFrameNumber = view.findViewById(R.id.item_info_frame);//frame
                TextView tvSerialNumber = view.findViewById(R.id.item_info_number);//bike no
                TextView tvBluetooth = view.findViewById(R.id.item_info_bluetooth);//bluetooth
                TextView tvGPS = view.findViewById(R.id.item_info_imei); // gps
                TextView tvTermal = view.findViewById(R.id.item_info_terminal); //terminal status
                TextView tvRentStatus = view.findViewById(R.id.item_info_status); //bike status
                TextView tvGPSStatus = view.findViewById(R.id.item_info_gps); //gps status
                TextView tvTime = view.findViewById(R.id.item_info_last); //gps location time


                //加载数据
                 if(mBikeObject == null)
                     return view;
                 String mFrame = mBikeObject.optString("vin");//?
                 String mSerial= mBikeObject.optString("bikecode");
                 String mBluetooth = mBikeObject.optString("bluetooth");
                 String mGPS = mBikeObject.optString("imei");//?
                 int mTerminal = mBikeObject.optInt("terminalstatus");
                 String mRentStatus = mBikeObject.optString("rentstatus");
                 int mDisable = mBikeObject.optInt("disabled");
                 int mGPSStatus = mBikeObject.optInt("status");
                 String time = mBikeObject.optString("updatetime");
                 if(mTimeZone!= null)
                 {
                     time = DateUtil.getLocalTime(time,mTimeZone);
                 }
                String gpsStatus ;
                if(mGPSStatus == 3 || mGPSStatus == 0 )
                    gpsStatus =  context.getResources().getString(R.string.bike_info_gps_offline);
                else
                    gpsStatus =  context.getResources().getString(R.string.bike_info_gps_online);
                String mTermalStatus = "";
                if(mGPSStatus !=3 && mGPSStatus == 1)
                {
                    mTermalStatus = getResources().getString(R.string.bike_info_statue_r);
                }
                else
                {
                    mTermalStatus = getResources().getString(R.string.bike_info_statue_ur);
                }

                tvFrameNumber.setText(mFrame);
                tvSerialNumber.setText(mSerial);
                tvBluetooth.setText(mBluetooth);
                tvGPS.setText(mGPS);
                tvTermal.setText(""+mTermalStatus );
                tvRentStatus.setText(mRentStatus);
                tvGPSStatus.setText("" +gpsStatus);
                tvTime.setText(time);
                return view;
            }

            @Override
            public View getInfoContents(Marker marker) {
                return null;
            }
        });

            latitude = mBikeObject.optDouble("lat");
            longitude = mBikeObject.optDouble("lng");
            LatLng mLatLng = new LatLng(latitude,longitude);
            MarkerOptions mMarkerOptions = new MarkerOptions();
            mMarkerOptions.position(mLatLng);
            mMarkerOptions.zIndex(-1);
            BitmapDescriptor mBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.mipmap.bike_info_location);
            mMarkerOptions.icon(mBitmapDescriptor);
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatLng,17));
            mMarker = mGoogleMap.addMarker(mMarkerOptions);
            mMarker.showInfoWindow();

        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bike_info);
        mMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.bike_info_map);

        findViewById(R.id.bike_info_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BikeInfoActivity.this.finish();
            }
        });
        final View mMenuView = findViewById(R.id.bike_info_menu);
        mMenuView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            View mLayoutView = LayoutInflater.from(BikeInfoActivity.this).
                    inflate(R.layout.menu_bike_info,null);
            TextView tvForbidden = mLayoutView.findViewById(R.id.bike_info_menu_forbidden);
            TextView tvActive = mLayoutView.findViewById(R.id.bike_info_menu_active);
            TextView tvEdit = mLayoutView.findViewById(R.id.bike_info_menu_edit);
            TextView tvTrack = mLayoutView.findViewById(R.id.bike_info_menu_track);
            TextView tvLocl = mLayoutView.findViewById(R.id.bike_info_menu_lock);
            TextView tvUnlocl = mLayoutView.findViewById(R.id.bike_info_menu_unlock);
            TextView tvBluetooth = mLayoutView.findViewById(R.id.bike_info_menu_bluetooth);
            TextView tvCancle = mLayoutView.findViewById(R.id.bike_info_mwnu_cancel);
            String disable = mBikeObject.optString("disabled");
            String mRentStatus = mBikeObject.optString("rentstatus");
            if(!mRentStatus.equals("occupied"))
            {
                tvLocl.setVisibility(View.GONE);
                tvUnlocl.setVisibility(View.GONE);
                mLayoutView.findViewById(R.id.bike_info_menu_line1).setVisibility(View.GONE);
                mLayoutView.findViewById(R.id.bike_info_menu_line2).setVisibility(View.GONE);
            }
            if(disable.equals("1"))
            {
                tvActive.setVisibility(View.VISIBLE);
                tvForbidden.setVisibility(View.GONE);
            }
            else
            {
                tvActive.setVisibility(View.GONE);
                tvForbidden.setVisibility(View.VISIBLE);
            }

            final PopupWindow mPopWindow = DialogUtils.
                    showAllMenu(BikeInfoActivity.this,mLayoutView,mMenuView);
            tvForbidden.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPopWindow.dismiss();
                    onForbidden();
                }
            });
                tvActive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mPopWindow.dismiss();
                    onActive();
                    }
                });
                tvEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mPopWindow.dismiss();
                        onEdit();
                    }
                });
                tvTrack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mPopWindow.dismiss();
                        onTrack();
                    }
                });
                tvLocl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mPopWindow.dismiss();
                        onLock();
                    }
                });
                tvUnlocl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mPopWindow.dismiss();
                        onUnLock();
                    }
                });
                tvBluetooth.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mPopWindow.dismiss();
                        onUpdate();
                    }
                });

                tvCancle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mPopWindow.dismiss();
                    }
                });

            }
        });
        loadServer();
    }

    private void loadServer()
    {
        if (!NetUtil.hasNet(BikeInfoActivity.this)) {
            DialogUtils.showToast(BikeInfoActivity.this,R.string.check_network_connect);
            return;
        }
        Map<String,String> heads = new HashMap<>();
        String mSerial = getIntent().getStringExtra("BikeSerial");
        heads.put("bikeid",mSerial);
        heads.put("appversion","1");
        String message = getResources().getString(R.string.bike_manage_load);
        DialogUtils.showProgressDialog(BikeInfoActivity.this, message);
        onServerTime( ContentPath.bikeInfo,heads,new CallBack(){

            @Override
            public void onResponse(String response) {
                String mServerTime = response;
//                Log.i("OrderDetailActivity","" + mServerTime);
                String[] mSplit = null;
                if(mServerTime!= null)
                {
                    mSplit = mServerTime.split(",");
                    if(mSplit != null)
                    {
                        mTimeZone = mSplit[1];
                    }
                }
                onLoadBike();
            }

            @Override
            public void onFailure() {
                onLoadBike();
            }
        });
    }


    private void onLoadBike()
    {
        Intent intent = getIntent();
        String mSerial = intent.getStringExtra("BikeSerial");

        String url = ContentPath.bikeInfo ;
        RequestParams params = new RequestParams(url);
        params.addBodyParameter("bikeid", mSerial);

        onConnect(params, new CallBack() {
            @Override
            public void onResponse(String response) {
                DialogUtils.dismissProgressDialog();
                try{
                    JSONObject mResponse = new JSONObject(response);
                    JSONObject mResultObject = mResponse.getJSONObject("result");
                    String status = mResponse.optString("status");
//                    Log.i("BikeInfoActivity",""+ response);
                    if(status.equals("1"))
                    {
                        mBikeObject = mResultObject.getJSONObject("bike");
                        mMapFragment.getMapAsync(mMapReadyCallback);
                    }
                    else
                    {
                       String errorcode =  mResponse.optString("errorcode");
                        if (errorcode.equals("60001")) {
                            quickLogin(new QuickLoadCallBack() {
                                @Override
                                public void doSomeThing(Boolean boolon) {
                                    if (boolon == true) {
                                       onLoadBike();
                                    } else {
                                        reLogin(BikeInfoActivity.this);
                                    }
                                }
                            });
                        } else {
                            String msg = mResponse.optString("msg");
                            DialogUtils.showToast(BikeInfoActivity.this,msg);
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
            }
        });
    }



    private void onForbidden()
    {
        if (!NetUtil.hasNet(BikeInfoActivity.this)) {
            DialogUtils.showToast(BikeInfoActivity.this,R.string.check_network_connect);
            return;
        }
        Intent intent = getIntent();
        String mSerial = intent.getStringExtra("BikeSerial");
        String url = ContentPath.disableBike ;
        RequestParams params = new RequestParams(url);
        params.addBodyParameter("bikeid", mSerial);
        String tip = getResources().getString(R.string.bike_info_tip);
        DialogUtils.showProgressDialog(BikeInfoActivity.this,tip);
        onConnect(params, new CallBack() {
            @Override
            public void onResponse(String response) {
                DialogUtils.dismissProgressDialog();
                try{
                    JSONObject mResponseObject = new JSONObject(response);
                    String status = mResponseObject.getString("status");
                    switch (status)
                    {
                        case "1":
//                        Log.i("BikeInfo","disable: \t"+ response);
                            DialogUtils.showToast(BikeInfoActivity.this,R.string.bike_info_disable_message);
                            //refresh data
                            onLoadBike();
                            break;
                        case "0":
                            String errorcode = mResponseObject.optString("errorcode");
                            if (errorcode.equals("60001")) {
                                quickLogin(new QuickLoadCallBack() {
                                    @Override
                                    public void doSomeThing(Boolean boolon) {
                                        if (boolon == true) {
                                            onForbidden();
                                        } else {
                                            reLogin(BikeInfoActivity.this);
                                        }
                                    }
                                });
                            }
                            break;
                    }

                }catch(Exception e)
                {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure() {
                DialogUtils.dismissProgressDialog();
            }
        });

    }

    private void onActive()
    {
        if (!NetUtil.hasNet(BikeInfoActivity.this)) {
            DialogUtils.showToast(BikeInfoActivity.this,R.string.check_network_connect);
            return;
        }
        Intent intent = getIntent();
        String mSerial = intent.getStringExtra("BikeSerial");
        String url = ContentPath.activeBike ;
        RequestParams params = new RequestParams(url);
        params.addBodyParameter("bikeid", mSerial);
        String tip = getResources().getString(R.string.bike_info_tip);
        DialogUtils.showProgressDialog(BikeInfoActivity.this,tip);
        onConnect(params, new CallBack() {
            @Override
            public void onResponse(String response) {
                DialogUtils.dismissProgressDialog();
                try{
                    JSONObject mResponseObject = new JSONObject(response);
                    String status = mResponseObject.getString("status");
                    switch (status)
                    {
                        case "1":
//                            Log.i("BikeInfo","active: \t"+ response);
                            DialogUtils.showToast(BikeInfoActivity.this,R.string.bike_info_active_message);
                            //refresh data
                            onLoadBike();
                            break;
                        case "0":
                            String errorcode = mResponseObject.optString("errorcode");
                            if (errorcode.equals("60001")) {
                                quickLogin(new QuickLoadCallBack() {
                                    @Override
                                    public void doSomeThing(Boolean boolon) {
                                        if (boolon == true) {
                                            onForbidden();
                                        } else {
                                            reLogin(BikeInfoActivity.this);
                                        }
                                    }
                                });
                            }
                            break;
                    }

                }catch(Exception e)
                {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure() {
                DialogUtils.dismissProgressDialog();
            }
        });
    }

    private void onEdit()
    {
        Intent intent = new Intent(BikeInfoActivity.this,AddBikeActivity.class);
        intent.putExtra("Serial",mBikeObject.optString("bikecode"));
        intent.putExtra("Frame",mBikeObject.optString("vin"));
        intent.putExtra("IMEI",mBikeObject.optString("imei"));
        intent.putExtra("Bluetooth",mBikeObject.optString("bluetooth"));
        startActivityForResult(intent,0X01);
    }

    private void onTrack()
    {
        String mSerial= mBikeObject.optString("bikeid");
        Intent intent = new Intent(BikeInfoActivity.this,TrackActivity.class);
        intent.putExtra("BikeID",mSerial);
        startActivity(intent);
    }

    private void onLock()
    {
        if (!NetUtil.hasNet(BikeInfoActivity.this)) {
            DialogUtils.showToast(BikeInfoActivity.this,R.string.check_network_connect);
            return;
        }
        Intent intent = getIntent();
        String mSerial = intent.getStringExtra("BikeSerial");
        String url = ContentPath.lockBike ;
        RequestParams params = new RequestParams(url);
        params.addBodyParameter("bikeid", mSerial);
        String tip = getResources().getString(R.string.bike_info_tip);
        DialogUtils.showProgressDialog(BikeInfoActivity.this,tip);
        onConnect(params, new CallBack() {
            @Override
            public void onResponse(String response) {
                DialogUtils.dismissProgressDialog();
                try{
                    JSONObject mResponseObject = new JSONObject(response);
                    String status = mResponseObject.getString("status");
                    switch (status)
                    {
                        case "1":
//                            Log.i("BikeInfo","lock: \t"+ response);
                            DialogUtils.showToast(BikeInfoActivity.this,R.string.bike_info_lock_message);
                            //refresh data
                            onLoadBike();
                            break;
                        case "0":
                            String errorcode = mResponseObject.optString("errorcode");
                            if (errorcode.equals("60001")) {
                                quickLogin(new QuickLoadCallBack() {
                                    @Override
                                    public void doSomeThing(Boolean boolon) {
                                        if (boolon == true) {
                                            onForbidden();
                                        } else {
                                            reLogin(BikeInfoActivity.this);
                                        }
                                    }
                                });
                            }
                            break;
                    }

                }catch(Exception e)
                {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure() {
                DialogUtils.dismissProgressDialog();
            }
        });
    }

    private void onUnLock()
    {
        if (!NetUtil.hasNet(BikeInfoActivity.this)) {
            DialogUtils.showToast(BikeInfoActivity.this,R.string.check_network_connect);
            return;
        }
        Intent intent = getIntent();
        String mSerial = intent.getStringExtra("BikeSerial");
        String url = ContentPath.unlockBike ;
        RequestParams params = new RequestParams(url);
        params.addBodyParameter("bikeid", mSerial);
        String tip = getResources().getString(R.string.bike_info_tip);
        DialogUtils.showProgressDialog(BikeInfoActivity.this,tip);
        onConnect(params, new CallBack() {
            @Override
            public void onResponse(String response) {
                DialogUtils.dismissProgressDialog();
                try{
                    JSONObject mResponseObject = new JSONObject(response);
                    String status = mResponseObject.getString("status");
                    switch (status)
                    {
                        case "1":
//                            Log.i("BikeInfo","unlock: \t"+ response);
                            DialogUtils.showToast(BikeInfoActivity.this,R.string.bike_info_unlock_message);
                            //refresh data
                            onLoadBike();
                            break;
                        case "0":
                            String errorcode = mResponseObject.optString("errorcode");
                            if (errorcode.equals("60001")) {
                                quickLogin(new QuickLoadCallBack() {
                                    @Override
                                    public void doSomeThing(Boolean boolon) {
                                        if (boolon == true) {
                                            onForbidden();
                                        } else {
                                            reLogin(BikeInfoActivity.this);
                                        }
                                    }
                                });
                            }
                            break;
                    }

                }catch(Exception e)
                {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure() {
                DialogUtils.dismissProgressDialog();
            }
        });
    }

    private void onUpdate()
    {
        //更新蓝牙：
        String mBluetooth = mBikeObject.optString("bluetooth");
        Intent intent = new Intent(BikeInfoActivity.this,UpdatBluetooth.class);
        intent.putExtra("Bluetooth",mBluetooth);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 0X01)
        {
            onLoadBike();
        }
    }
}
