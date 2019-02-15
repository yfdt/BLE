package com.blue.elephant.custom.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.blue.elephant.R;
import com.blue.elephant.activity.MainDetailActivity;
import com.blue.elephant.activity.MaintenanceActivity;
import com.blue.elephant.activity.MaintenanceListActivity;
import com.blue.elephant.activity.StopMaintenanceActivity;
import com.blue.elephant.util.DateUtil;

import org.json.JSONObject;

import java.util.ArrayList;

import static com.blue.elephant.activity.MaintenanceActivity.RepairStatus;

public class MainTenAdapter extends BaseAdapter {

    private MaintenanceListActivity mContext;
    private ArrayList<JSONObject> mObjectList = new ArrayList<>();
    private String mServerTime;
    public void setServerTime(String mServerTime)
    {
        if(mServerTime != null)
        {
            this.mServerTime = mServerTime;
        }

    }

    public MainTenAdapter(MaintenanceListActivity context)
    {
        this.mContext = context;
    }

    public void setData(ArrayList<JSONObject> mList)
    {
        if(mList != null)
        {
            mObjectList = mList;
        }
        this.notifyDataSetChanged();
    }

    public void addData(ArrayList<JSONObject> mList)
    {
        if(mList != null)
        {
            mObjectList.addAll(mList);
        }
        this.notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return mObjectList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder mHolder = null;
        if(convertView == null)
        {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_main_list,null);
            mHolder = new ViewHolder(convertView);
            convertView.setTag(mHolder);
        }
        else
        {
            mHolder = (ViewHolder) convertView.getTag();
        }

        String[] spliteTime = null;
        if(mServerTime != null)
        {
            spliteTime = mServerTime.split(",");
        }
        //加载数据
        final JSONObject mBikeObject = mObjectList.get(position);
        String mName = mBikeObject.optString("bikecode");
        String mStart = mBikeObject.optString("starttime");
        String mEnd = mBikeObject.optString("endtime");
        final String status = mBikeObject.optString("maintenancestatus");
        if(spliteTime!= null)
        {
            mStart = DateUtil.getLocalTime(mStart,spliteTime[1]);
        }
        if(!status.equals("completed"))
        {
            if(spliteTime!= null)
            {

                mEnd =spliteTime[0];
            }
        }
        else
        {
            if(spliteTime!= null)
            {
                mEnd = DateUtil.getLocalTime(mEnd,spliteTime[1]);
            }
        }
        long time =  DateUtil.getPeriodTime(mStart,mEnd);
        long hour = time/3600000;
        long min = time/60000;
        long second = time % 60000;
        if(second > 0 )
        {
            min ++;
        }
        if(min > 59)
        {
            min = min %60;
            hour +=1;
        }
        if(!status.equals("completed"))
        {
            min = 30;
            hour =  0;
        }
        String mPeriod = "";
        try {
            mPeriod = mContext.getResources().getString(R.string.order_period,hour,min);
        }catch(Exception e)
        {

        }
        mHolder.tvSerial.setText(mName);
        mHolder.tvStart.setText(mStart);
        mHolder.tvRiding.setText(mPeriod);
        if(status.equals("completed"))
        {
            mHolder.tvStatue.setText("Completed");
            mHolder.tvStatue.setTextColor(mContext.getResources().getColor(R.color.text_red));
        }
        else
        {
            mHolder.tvStatue.setText("In Service");
            mHolder.tvStatue.setTextColor(mContext.getResources().getColor(R.color.text_green));
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if(status.equals("completed"))
                {
                    intent = new Intent(mContext,MainDetailActivity.class);
                    intent.putExtra("MaintenanceID",mBikeObject.optString("maintenanceid"));
                    mContext.startActivityForResult(intent,0X01);
                }
                else
                {
                    //维修中
                    intent = new Intent(mContext,StopMaintenanceActivity.class);
                    intent.putExtra("Maintenance",mBikeObject.toString());
                    mContext.startActivityForResult(intent,0X01);
                }
            }
        });

        return convertView;
    }


    class ViewHolder{

        private TextView tvSerial,tvStart,tvStatue,tvRiding;

        public ViewHolder(View view)
        {
            tvSerial = view.findViewById(R.id.item_main_list_serial);
            tvStart = view.findViewById(R.id.item_main_list_start);
            tvRiding = view.findViewById(R.id.item_main_list_riding);
            tvStatue = view.findViewById(R.id.item_main_list_statue);

        }

    }

}
