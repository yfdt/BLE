package com.blue.elephant.activity;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blue.elephant.R;
import com.blue.elephant.util.CallBack;
import com.blue.elephant.util.ContentPath;
import com.blue.elephant.util.DialogUtils;
import com.blue.elephant.util.NetUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.http.RequestParams;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public class TrackActivity extends BaseActivity {

    private TextView mBeginText,mEndText;
    private GoogleMap mGoogleMap;
    private SupportMapFragment mMapFragment;
    private ArrayList<JSONObject> mPotList = new ArrayList<>();
    private ArrayList<Marker> mMarkerList = new ArrayList<>();
    private Marker mMarker;
    private Polyline mMutablePolyline;

    private static final int PATTERN_DASH_LENGTH_PX = 50;
    private static final int PATTERN_GAP_LENGTH_PX = 20;
    private static final Dash DASH = new Dash(PATTERN_DASH_LENGTH_PX);
    private static final Gap GAP = new Gap(PATTERN_GAP_LENGTH_PX);

    private OnMapReadyCallback mReadyCallBack = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {

            mGoogleMap = googleMap;
            //设置路径
            PolylineOptions mPolylineOptions = new PolylineOptions();
            mPolylineOptions.width(4f);
            mPolylineOptions.color(Color.RED);
            mPolylineOptions.geodesic(true);
//            Log.i("Main","map callback "+ mPotList.size() );
            for(int i=0;i<mPotList.size();i++)
            {
                try {
                    JSONObject mPointItem = mPotList.get(i);
                    double mLat =  mPointItem.optDouble("lat");
                    double mLng = mPointItem.optDouble("lng");
                    String mTime = mPointItem.optString("locationtime");
                    LatLng mLaLng = new LatLng(mLat,mLng);
                    mPolylineOptions.add(mLaLng);
                    MarkerOptions mMarkerOptions = new MarkerOptions();
                    mMarkerOptions.position(mLaLng);// set location
                    mMarkerOptions.title(mTime);
                    mMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.gps_peroid));
                    mMarker = mGoogleMap.addMarker(mMarkerOptions);
                    mMarkerList.add(mMarker);

                }catch(Exception e)
                {
                    Log.e("Track","" + e.getMessage());
                    continue;
                }
            }
            if(mPotList.size() > 0 )
            {
                JSONObject mPointItem = mPotList.get(0);
                double mLat =  mPointItem.optDouble("lat");
                double mLng = mPointItem.optDouble("lng");
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLat,mLng), 17F));
            }
            mMutablePolyline = mGoogleMap.addPolyline(mPolylineOptions);
//            List<PatternItem> PATTERN_DASHED = Arrays.asList(DASH, GAP);
//            mMutablePolyline.setPattern(PATTERN_DASHED);
            CustomCap mStartCap = new CustomCap(BitmapDescriptorFactory.fromResource(R.mipmap.gps_green));
            CustomCap mEndCap = new CustomCap(BitmapDescriptorFactory.fromResource(R.mipmap.gps_red));
            mMutablePolyline.setStartCap(mStartCap);
            mMutablePolyline.setEndCap(mEndCap);
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);

        ImageView actionBarLeft = findViewById(R.id.actionbar_left);
        TextView actionBarTitle = findViewById(R.id.actionbar_title);
        mBeginText = findViewById(R.id.track_begin);
        mEndText  = findViewById(R.id.track_end);
        actionBarLeft.setImageResource(R.mipmap.back_arraw);
        actionBarTitle.setText(R.string.track_title);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.track_map);


        actionBarLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TrackActivity.this.finish();
            }
        });


        //默认显示当前的天
        mBeginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TrackActivity.this,TrackTimeDialog.class);
                startActivityForResult(intent,0X01);


            }
        });

        mEndText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TrackActivity.this,TrackTimeDialog.class);
                startActivityForResult(intent,0X01);
            }
        });



        //初始化数据
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar mTime = Calendar.getInstance();
        mTime.set(Calendar.HOUR_OF_DAY,0);
        mTime.set(Calendar.MINUTE,0);
        mTime.set(Calendar.SECOND,0);
        String mStart =  sdf.format(mTime.getTime());
        mTime.set(Calendar.HOUR_OF_DAY,23);
        mTime.set(Calendar.MINUTE,59);
        mTime.set(Calendar.SECOND,59);
        String mEnd =  sdf.format(mTime.getTime());
        mBeginText.setText(mStart);
        mEndText.setText(mEnd);
        //加载数据
        loadTrack();
    }

    private void loadTrack()
    {
        String mBikeId = getIntent().getStringExtra("BikeID");
        String mStart = mBeginText.getText().toString();
        String mEnd = mEndText.getText().toString();
        String mSsource = getResources().getString(R.string.track_start);
        String mEsource = getResources().getString(R.string.track_end);

        if(mStart.equals(mSsource) || mStart.equals(""))
        {
            return ;
        }
        if(mEnd.equals(mEsource) || mEnd.equals(""))
        {
            return ;
        }

        if (!NetUtil.hasNet(TrackActivity.this)) {
            DialogUtils.showToast(TrackActivity.this,R.string.check_network_connect);
            return;
        }
        String url = ContentPath.historyTrack ;
        RequestParams params = new RequestParams(url);
        params.addBodyParameter("bikeid",mBikeId);
        params.addBodyParameter("from",mStart);
        params.addBodyParameter("to",mEnd);
        params.addBodyParameter("page","1");
        params.addBodyParameter("pageSize","40");
        String message = getResources().getString(R.string.track_load);
        DialogUtils.showProgressDialog(TrackActivity.this, message);
        onConnect(params, new CallBack() {
            @Override
            public void onResponse(String response) {
                DialogUtils.dismissProgressDialog();
//                Log.i("Track",response);
//                try {
//                    InputStream inputStream = getAssets().open("track");
//                    InputStreamReader mInputStreamReader = new InputStreamReader(inputStream);
//                    BufferedReader bufferedReader =new BufferedReader(mInputStreamReader);
//                    StringBuilder sb = new StringBuilder();
//                    String line = null;
//                    while ((line = bufferedReader.readLine())!= null)
//                    {
//                        sb.append(line);
//                    }
//                    inputStream.close();
//                    response = sb.toString();
//                    Log.i("Main","response:"+ response);
//                }catch(Exception e)
//                {
//                    Log.i("Main","" + e.getMessage());
//                }

                try{
                    JSONObject mResponseObject = new JSONObject(response);
                    JSONObject mResultObject =  mResponseObject.optJSONObject("result");
                    JSONArray mPotArray = mResultObject.optJSONArray("rows");
//                    Log.i("Main","row size:"+ mPotArray.length());
                    // clear old data     mPotList
                    if (mPotArray.length() == 0) {
                        if (mMarkerList != null) {
                            for (int i = 0; i < mMarkerList.size(); i++) {
                                mMarker = mMarkerList.get(i);
                                mMarker.remove();
                            }
                            mMarkerList.clear();
                        }
                        return;
                    }
                    mPotList.clear();
                    if(mPotArray.length()<0)
                    {
                        DialogUtils.showToast(TrackActivity.this,R.string.track_no_data);
                        mMapFragment.getMapAsync(mReadyCallBack);
                        return ;
                    }
                    else{

                        for(int i=0;i< mPotArray.length();i++ )
                        {
                            JSONObject mTrackObject =  mPotArray.getJSONObject(i);
                            mPotList.add(mTrackObject);
                        }
                    }
                    mMapFragment.getMapAsync(mReadyCallBack);

                }catch(Exception e)
                {
                    Log.e("Track",""+ e.getMessage());
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
            //进行网络请求
            String mStart = data.getStringExtra("start");
            String mEnd = data.getStringExtra("end");
            mBeginText.setText(mStart);
            mEndText.setText(mEnd);
            loadTrack();

        }


    }

    /**
     * 通过起点终点，组合成url
     *
     * @param origin
     * @param dest
     * @return
     */
    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + ","
                + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Travelling Mode
        String mode = "mode=driving";

        //waypoints,116.32885,40.036675
        String waypointLatLng = "waypoints="+"40.036675"+","+"116.32885";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&"
                + mode+"&"+waypointLatLng;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + parameters;
        System.out.println("getDerectionsURL--->: " + url);
        return url;
    }



}
