package com.blue.elephant.custom.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.blue.elephant.R;
import com.blue.elephant.util.DateUtil;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class DeductionAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<JSONObject> mRecordList = new ArrayList<>();


    private String mServerTime;
    public void setServerTime(String mServerTime)
    {
        if(mServerTime != null)
        {
            this.mServerTime = mServerTime;
        }

    }
    public DeductionAdapter(Context context)
    {
        this.mContext = context;
    }

    public void setData(ArrayList<JSONObject> mList)
    {
        if(mList != null)
        {
            mRecordList = mList;
        }
        this.notifyDataSetChanged();
    }

    public void addData(ArrayList<JSONObject> mList)
    {
        if(mList != null)
        {
            mRecordList.addAll(mList);
        }
        this.notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return mRecordList.size();
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

        ViewHolder  mViewHolder = null;
        if(convertView == null)
        {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_deduction,null);
            mViewHolder = new ViewHolder(convertView);
            convertView.setTag(mViewHolder);
        }
        else
        {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        String[] spliteTime = null;
        if(mServerTime != null)
        {
            spliteTime = mServerTime.split(",");
        }
        JSONObject mRecord = mRecordList.get(position);
        String mCreateTime = mRecord.optString("createtime");
        mCreateTime = DateUtil.getLocalTime(mCreateTime,spliteTime[1]);
//        Log.i("Record","createTime "+ mCreateTime);
        String mPayStatus = mRecord.optString("paystatus");
        double mInsurance = mRecord.optDouble("insurancefee");
        double mPrice = mRecord.optDouble("amount");
        double mProfit = mRecord.optDouble("profit");
        mPrice = mPrice  - mProfit;
        String mPayDecimal = new DecimalFormat("$0.00").format(mInsurance);
        String mPriceDecimal = new DecimalFormat("$0.00").format(mPrice);
        mViewHolder.tvTime.setText(mCreateTime);
        if(mPayStatus.equalsIgnoreCase("PAID"))
        {
            int color = mContext.getResources().getColor(R.color.text_green);
            mViewHolder.tvBike.setTextColor(color);
            mViewHolder.tvInsurance.setTextColor(color);

            mViewHolder.tvInsurance.setText(mPayDecimal);
            mViewHolder.tvBike.setText(mPriceDecimal);
        }
        else
        {
            int color = mContext.getResources().getColor(R.color.text_red);
            mViewHolder.tvBike.setTextColor(color);
            mViewHolder.tvInsurance.setTextColor(color);

            mViewHolder.tvInsurance.setText(mPayDecimal);
            mViewHolder.tvBike.setText(mPriceDecimal);
        }
        return convertView;
    }

    class ViewHolder{
        private TextView tvTime;
        private TextView tvInsurance;
        private TextView tvBike;

        public ViewHolder(View view)
        {
            tvTime = view.findViewById(R.id.item_deduction_time);
            tvInsurance = view.findViewById(R.id.item_deduction_price);
            tvBike = view.findViewById(R.id.item_deduction_bike_price);
        }

    }

}
