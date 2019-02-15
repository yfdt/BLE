package com.blue.elephant.custom.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.blue.elephant.R;
import com.blue.elephant.activity.ConfirmationRentActivity;
import com.blue.elephant.activity.EndOfOrderActivity;
import com.blue.elephant.activity.InsuranceActivity;
import com.blue.elephant.activity.OrderDetailActivity;
import com.blue.elephant.activity.OrderListActivity;
import com.blue.elephant.util.DateUtil;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class OrderAdapter extends BaseAdapter {

    private OrderListActivity mContext;
    private ArrayList<JSONObject> mOrderList = new ArrayList<>();

    private String mServerTime;
    public void setServerTime(String mServerTime)
    {
        if(mServerTime != null)
        {
            this.mServerTime = mServerTime;
        }

    }


    public OrderAdapter(OrderListActivity mContext)
    {
        this.mContext = mContext;
    }

    public void setData(ArrayList<JSONObject> mList)
    {
        if(mList != null)
        {
            this.mOrderList = new ArrayList<>();
            this.mOrderList.addAll(mList);
        }
//        Log.i("OrderListActivity","添加了数据" + mOrderList.size() + "\t orgin :"+ mList.size());
        this.notifyDataSetChanged();
    }

    public void addData(ArrayList<JSONObject> mList)
    {
        if(mList != null)
        {
            this.mOrderList.addAll(mList);
        }
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return this.mOrderList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder mViewHolder = null;
        if (convertView == null)
        {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_order_list,null);
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
        final JSONObject mOrderObject = mOrderList.get(position);
        String mSerial = mOrderObject.optString("bikecode");
        String mStartTime = mOrderObject.optString("starttime");
        String mEndTime = mOrderObject.optString("endtime");
        String price = mOrderObject.optString("amount");
        final String mOrderStatus = mOrderObject.optString("orderstatus");
        if(spliteTime!= null)
        {
            mStartTime = DateUtil.getLocalTime(mStartTime,spliteTime[1]);
        }
        if(!mOrderStatus.equals("completed"))
        {
            if(spliteTime!= null)
            {
                mEndTime =spliteTime[0];
                mEndTime = DateUtil.getLocalTime(mEndTime,spliteTime[1]);
            }
        }
        else
        {
            if(spliteTime!= null)
            {
                mEndTime = DateUtil.getLocalTime(mEndTime,spliteTime[1]);
            }
        }
        long time =  DateUtil.getPeriodTime(mStartTime,mEndTime);
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
        String mPeriod = "";
        try {
            mPeriod = mContext.getResources().getString(R.string.order_period,hour,min);
        }catch(Exception e)
        {

        }
        mViewHolder.tvSerial.setText(mSerial);
        mViewHolder.tvStart.setText(mStartTime);
        mViewHolder.tvRiding.setText(mPeriod);
        if(mOrderStatus.equals("completed"))
        {
            price = price.equals("")? "": "$" + price;
            mViewHolder.tvPrice.setText(price);
            int color = mContext.getResources().getColor(R.color.text_red);
            mViewHolder.tvPrice.setTextColor(color);
        }
        else
        {
            mViewHolder.tvPrice.setText(R.string.order_menu_rent);
            int color = mContext.getResources().getColor(R.color.text_green);
            mViewHolder.tvPrice.setTextColor(color);
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOrderStatus.equals("completed")) //查看订单详情
                {
                    Intent intent = new Intent(mContext, OrderDetailActivity.class);
                    intent.putExtra("OrderID",mOrderObject.optString("orderid"));
                    mContext.startActivityForResult(intent,0X02);
                }
                else //正在租用中 ，可以还车
                {
                    Intent intent = new Intent(mContext,EndOfOrderActivity.class);
                    intent.putExtra("BikeID",mOrderObject.optString("bikeid"));
                    intent.putExtra("BikeCode",mOrderObject.optString("bikecode"));
                    intent.putExtra("OrderID",mOrderObject.optString("orderid"));
                    intent.putExtra("OrderCode",mOrderObject.optString("orderno"));
                    intent.putExtra("ImagePath",mOrderObject.optString("contractpath"));
                    intent.putExtra("Insurance",mOrderObject.optString("buyinsurance"));
                    intent.putExtra("StartTime",mOrderObject.optString("starttime"));
                    mContext.startActivityForResult(intent,0X02);


                }
            }
        });

        return convertView;
    }



    class ViewHolder {

        private TextView tvSerial;
        private TextView tvStart;
        private TextView tvRiding;
        private TextView tvPrice;

        public ViewHolder(View view)
        {
            tvSerial = view.findViewById(R.id.item_order_list_serial);
            tvStart = view.findViewById(R.id.item_order_list_start);
            tvPrice = view.findViewById(R.id.item_order_list_price);
            tvRiding = view.findViewById(R.id.item_order_list_riding);
        }

    }


}
