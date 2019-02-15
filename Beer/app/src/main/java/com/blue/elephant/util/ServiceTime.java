package com.blue.elephant.util;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;


public class ServiceTime {

    private static ServiceTime mInstance;
    private Context mContext;
    private Handler mHandler = new Handler();
    private String mServerTime = null;

    public static ServiceTime getInstance(Context mContet)
    {
        if(mInstance == null)
        {
            mInstance = new ServiceTime(mContet);
        }
        return mInstance;
    };

    private ServiceTime(Context mContext)
    {
        this.mContext = mContext;
    }

    public void onPost(final String address,final Map<String,String> input, final CallBack mCallBack)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                HttpClient httpCient = new DefaultHttpClient();
                HttpPost httpGet = new HttpPost(address);
                for(String key: input.keySet())
                {
                    String value = input.get(key);
                    httpGet.addHeader(key,value);
                }
                try {
                    HttpResponse httpResponse = httpCient.execute(httpGet);
                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        Header[] mHeadAll = httpResponse.getAllHeaders();
                        for(int i=0;i<mHeadAll.length;i++)
                        {
                            Header mItemHeader = mHeadAll[i];
                            String key = mItemHeader.getName();
                            if(key.equals("servertime"))
                            {
                                String strDate = mItemHeader.getValue();
                                Long timestamp = Long.parseLong(strDate) * 1000;
                                SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
                                mServerTime = sdf.format(new Date(timestamp));
                            }
                            else if(key.equals("timezone"))
                            {
                                mServerTime +="," + mItemHeader.getValue();
                            }

                        }
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if(mServerTime != null)
                                {
                                    mCallBack.onResponse(mServerTime);
                                }
                                else
                                {
                                    mCallBack.onFailure();
                                }

                            }
                        });
                    }

                } catch (Exception e) {
//            e.printStackTrace();
                    Log.e("ServerTime","" + e.getMessage());
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mCallBack.onFailure();
                        }
                    });
                }

            }
        };
        new Thread(runnable).start();

    }



}
