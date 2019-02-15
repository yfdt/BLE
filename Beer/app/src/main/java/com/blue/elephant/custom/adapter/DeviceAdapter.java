package com.blue.elephant.custom.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.blue.elephant.R;

import org.json.JSONObject;

import java.util.ArrayList;

public class DeviceAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<JSONObject> mDeviceObject = new ArrayList<>();

    public DeviceAdapter(Context context)
    {
        this.mContext = context;
    }

    public void setData(ArrayList<JSONObject> mDevice)
    {
        if(mDevice != null)
        {
            mDeviceObject = mDevice;
        }
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return this.mDeviceObject.size();
    }

    @Override
    public Object getItem(int position) {
        return mDeviceObject.get(position);
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_device,null);
            mViewHolder = new ViewHolder(convertView);
            convertView.setTag(mViewHolder);
        }
        else
        {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        //加载数据
        JSONObject mDevice = mDeviceObject.get(position);
        String mName = mDevice.optString("Name");
        String mAddress = mDevice.optString("Address");
        String mSign = mDevice.optString("Sign");
        mViewHolder.tvName.setText(mName);
        mViewHolder.tvAddress.setText(mAddress);
        mViewHolder.tvSigin.setText(mSign);
        return convertView;
    }

    class ViewHolder{
        private TextView tvName;
        private TextView tvAddress;
        private TextView tvSigin;

        public ViewHolder(View view)
        {
            tvName = view.findViewById(R.id.device_blue_name_text);
            tvAddress = view.findViewById(R.id.device_blue_address);
            tvSigin = view.findViewById(R.id.device_blue_signal);
        }
    }



}
