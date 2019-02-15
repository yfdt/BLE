package com.blue.elephant.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.blue.elephant.R;
import com.blue.elephant.util.CallBack;
import com.blue.elephant.util.ContentPath;
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

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class BikeMapActivity extends BaseActivity {

    private GoogleMap mGoogleMap;
    private Marker mMarker;
    private SupportMapFragment mMapFragment;
    private LinearLayout mMenuLayout;
    private PopupWindow mMenuPopWindow;
    private TextView actonBarMenu;
    private String rentStatus ="";
    private long latitude,longitude;
    private int page = 1 ;
    private ArrayList<JSONObject> mBikeList = new ArrayList<>();
    private Map<String, JSONObject> mMarkerObject = new HashMap<>();
    private ArrayList<Marker> mMarkerList = new ArrayList<>();

    private OnMapReadyCallback mapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mGoogleMap = googleMap;
            UiSettings mSetting = mGoogleMap.getUiSettings();
            mSetting.setMyLocationButtonEnabled(false);
            if (mGoogleMap != null) {
                if (ActivityCompat.checkSelfPermission(BikeMapActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(BikeMapActivity.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
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

                    Context context = BikeMapActivity.this;
                    View view = LayoutInflater.from(context).inflate(R.layout.map_item,null);
                    TextView mSerialView = view.findViewById(R.id.bike_map_serial);
                    TextView mPowerView = view.findViewById(R.id.bike_map_power);

                    //加载数据
                  final JSONObject mBike = mMarkerObject.get(marker.getId());
                    if (mBike != null) {
                        int power = mBike.optInt("electricity");
                        String number = mBike.optString("bikecode");
                        mPowerView.setText(power + "%");
                        mSerialView.setText(number);
                    }

                    mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

                        @Override
                        public void onInfoWindowClick(Marker marker) {
                            // vehicle info
                            Intent intent = new Intent(BikeMapActivity.this, BikeInfoActivity.class);
                            intent.putExtra("BikeSerial", mBike.optString("bikeid"));
                            startActivity(intent);
                        }
                    });
                    return view;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    return null;
                }
            });


            mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    marker.showInfoWindow();
                    return false;
                }
            });


            //加载数据
            if (mBikeList == null)
                return;
            JSONObject mVehicle = null;
            mMarkerList.clear();
            for (int i = 0; i < mBikeList.size(); i++) {
                mVehicle = mBikeList.get(i);
                double mlatitude = mVehicle.optDouble("lat");
                double mlongitude = mVehicle.optDouble("lng");
                LatLng mLatLng = new LatLng(mlatitude, mlongitude);
                MarkerOptions mMarkerOptions = new MarkerOptions();
                mMarkerOptions.position(mLatLng);// set location
                mMarkerOptions.zIndex(-1);
                int mElectricPower = mVehicle.optInt("electricity");
                BitmapDescriptor mDescriptor = null;
                if (mElectricPower <= 20) {
                    mDescriptor = BitmapDescriptorFactory.fromResource(R.mipmap.bike_map_20);
                } else if (mElectricPower <= 40) {
                    mDescriptor = BitmapDescriptorFactory.fromResource(R.mipmap.bike_map_40);
                } else if (mElectricPower <= 60) {
                    mDescriptor = BitmapDescriptorFactory.fromResource(R.mipmap.bike_map_60);
                } else {
                    mDescriptor = BitmapDescriptorFactory.fromResource(R.mipmap.bike_map_100);
                }
                mMarkerOptions.icon(mDescriptor);
//                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 17));
                mMarker = mGoogleMap.addMarker(mMarkerOptions);
                String markerId = mMarker.getId();
                mMarkerObject.put(markerId, mVehicle);
                mMarkerList.add(mMarker);
            }
        }


    };


    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId())
            {
                case R.id.bike_map_100:

                    break;
                case R.id.bike_map_60:

                    break;
                case R.id.bike_map_40:

                    break;
                case R.id.bike_map_20:

                    break;
                case R.id.actionbar_left:
                    BikeMapActivity.this.finish();
                    break;
            }


        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bike_map);

        ImageView actionBarLeft = findViewById(R.id.actionbar_left);
        TextView actionBarTitle = findViewById(R.id.actionbar_title);
        actonBarMenu = findViewById(R.id.actionbar_sub_menu);
        ImageView actionbarIcon = findViewById(R.id.actionbar_sub_icon);//(SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.info_map);
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().
                findFragmentById(R.id.bike_map);

        actionBarLeft.setImageResource(R.mipmap.back_arraw);
        mMenuLayout = findViewById(R.id.actionbar_right);
        actionBarTitle.setText(R.string.bike_map_title);
        actonBarMenu.setText(R.string.bike_map_menu_all);
        actionbarIcon.setImageResource(R.mipmap.bike_map_down_arrow);
        actionBarLeft.setOnClickListener(onClickListener);
        findViewById(R.id.bike_map_100).setOnClickListener(onClickListener);
        findViewById(R.id.bike_map_60).setOnClickListener(onClickListener);
        findViewById(R.id.bike_map_40).setOnClickListener(onClickListener);
        findViewById(R.id.bike_map_20).setOnClickListener(onClickListener);

        mMenuLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View view = LayoutInflater.from(BikeMapActivity.this).inflate(R.layout.menu_bike,null);
                mMenuPopWindow = DialogUtils.showVehicleMenu(BikeMapActivity.this,mMenuLayout,view);
                TextView tvAll = view.findViewById(R.id.menu_vehicle_all);
                TextView tvNoRent = view.findViewById(R.id.menu_vehicle_no_rent);
                TextView tvRent = view.findViewById(R.id.menu_vehicle_rent);
                tvAll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMenuPopWindow.dismiss();
                        actonBarMenu.setText(R.string.bike_manage_menu_all);
                        rentStatus = "";
                        page = 1;
                        loadData();
                    }
                });
                tvNoRent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMenuPopWindow.dismiss();
                        actonBarMenu.setText(R.string.bike_manage_menu_no_rent);
                        rentStatus = "unoccupied";
                        page = 1;
                        loadData();
                    }
                });
                tvRent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMenuPopWindow.dismiss();
                        actonBarMenu.setText(R.string.bike_manage_menu_rent);
                        rentStatus = "occupied";
                        page = 1;
                        loadData();
                    }
                });




            }
        });

        loadPermission();
        rentStatus = "";
        page = 1;
        loadData();
    }

    private void loadPermission()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        1);
            }
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
            case 2:
                if (grantResults[0] == PERMISSION_GRANTED) {
                    // TODO request success
                    mMapFragment.getMapAsync(mapReadyCallback);
                }
                break;
            case 3:
                break;
        }
    }


    private void loadData()
    {
        if (!NetUtil.hasNet(BikeMapActivity.this)) {
            DialogUtils.showToast(BikeMapActivity.this,R.string.check_network_connect);
            return;
        }
        String url = ContentPath.queryBike ;
        RequestParams params = new RequestParams(url);
        params.addBodyParameter("page", page+ "");
        params.addBodyParameter("pageSize", 40+ "");
        if(!rentStatus.equals(""))
        {
            params.addBodyParameter("filters[0].key", "rentstatus");
            params.addBodyParameter("filters[0].opr", "EQ");
            params.addBodyParameter("filters[0].values[0]", rentStatus);
        }

        String message = getResources().getString(R.string.bike_manage_load);
        DialogUtils.showProgressDialog(BikeMapActivity.this, message);
        onConnect(params, new CallBack() {
            @Override
            public void onResponse(String response) {
                DialogUtils.dismissProgressDialog();
                try{
                    JSONObject mResponse = new JSONObject(response);
//                    Log.i("BikeManageActivity",""+ response);
                    String status = mResponse.optString("status");
                    if(status.equals("1"))
                    {
                        JSONObject mResultObject = mResponse.getJSONObject("result");
                        JSONArray mBikeArray = mResultObject.getJSONArray("rows");
                        mBikeList  = new ArrayList<>();
                        for(int i=0;i< mBikeArray.length();i++ )
                        {
                            JSONObject mObject = mBikeArray.getJSONObject(i);
                            mBikeList.add(mObject);
                        }
                        if (mMarkerList != null) {
                            for (int i = 0; i < mMarkerList.size(); i++) {
                                mMarker = mMarkerList.get(i);
                                mMarker.remove();
                            }
                            mMarkerList.clear();
                        }
                        mMarkerObject = new HashMap<>();
                        mMapFragment.getMapAsync(mapReadyCallback);
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
                                        loadData();
                                    } else {
                                        reLogin(BikeMapActivity.this);
                                    }
                                }
                            });
                        } else {
                            String msg = mResponse.optString("msg");
                            DialogUtils.showToast(BikeMapActivity.this,msg);
                        }
                    }

                }catch(Exception e)
                {
                    Log.e("BikeMapActivity","Exeption ："+ e.getMessage());
                }
            }

            @Override
            public void onFailure() {
                DialogUtils.dismissProgressDialog();
            }
        });

    }

}
