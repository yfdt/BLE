package com.blue.elephant.custom.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.blue.elephant.R;
import com.blue.elephant.activity.BikeInfoActivity;
import com.blue.elephant.activity.BikeManageActivity;

import org.json.JSONObject;

import java.util.ArrayList;

public class BikeManageAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<JSONObject> mVehicleList = new ArrayList<>();

    public BikeManageAdapter(Context mContext)
    {
        this.mContext = mContext;
    }

    public void setData(ArrayList<JSONObject> mList)
    {
        if(mList != null)
        {
            this.mVehicleList = mList;
        }
        this.notifyDataSetChanged();
    }

    public void addData(ArrayList<JSONObject> mList)
    {
        if(mList != null)
        {
            this.mVehicleList.addAll(mList);
        }
        this.notifyDataSetChanged();
    }

    public JSONObject getData(int position)
    {
        if(position >=0&& position<= mVehicleList.size())
        {
            return mVehicleList.get(position);
        }
        else
        {
            return null;
        }
    }

    @Override
    public int getCount() {
        return this.mVehicleList.size();
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

        ViewHolder mViewHolder = null;
        if(convertView == null)
        {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_bike_manage,null);
            mViewHolder = new ViewHolder(convertView);
            convertView.setTag(mViewHolder);
        }
        else
        {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        final JSONObject mObject = mVehicleList.get(position);
        String mSerial = mObject.optString("bikecode");
        int mPower = mObject.optInt("electricity");
        String rentStatus = mObject.optString("rentstatus");
        mViewHolder.tvSerial.setText(mSerial);
        mViewHolder.tvPower.setText(mPower + "%");
        mViewHolder.tvAction.setText(rentStatus);
        if(mVehicleList.size()< 0)
            return convertView;

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, BikeInfoActivity.class);
                String mSerial = mObject.optString("bikeid");
                intent.putExtra("BikeSerial",mSerial);
                mContext.startActivity(intent);
            }
        });

        return convertView;
    }


    class ViewHolder{

        private TextView tvSerial;
        private TextView tvPower;
        private TextView tvAction;

        public ViewHolder(View view)
        {
            tvSerial = view.findViewById(R.id.item_bike_manage_serial);
            tvPower = view.findViewById(R.id.item_bike_manage_power);
            tvAction = view.findViewById(R.id.item_bike_manage_action);
        }

    }

}
