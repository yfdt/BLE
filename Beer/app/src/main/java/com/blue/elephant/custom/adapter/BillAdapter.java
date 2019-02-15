package com.blue.elephant.custom.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.blue.elephant.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class BillAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<JSONObject> mList = new ArrayList<>();

    private OnBillListener onListener;

    private String mDefaultID ;

    public void setDefauitID(String defaultID)
    {
        this.mDefaultID = defaultID;
    }

    public void setBillListener(OnBillListener onListener)
    {
        this.onListener = onListener;
    }

    public BillAdapter(Context context)
    {
        this.mContext = context;
    }




    public void setData(ArrayList<JSONObject> mList)
    {
        if(mList != null)
        {
            this.mList = mList;
        }
        this.notifyDataSetChanged();
    }

    public void addData(ArrayList<JSONObject> mList)
    {
        if(mList != null)
        {
            this.mList.addAll(mList);
        }
        this.notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return mList.size();
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_bill,null);
            mHolder = new ViewHolder(convertView);
            convertView.setTag(mHolder);
        }
        else
        {
            mHolder = (ViewHolder) convertView.getTag();
        }
        JSONObject mCardObject = mList.get(position);
        String mNumber =  mCardObject.optString("last4");
        final String mCardID = mCardObject.optString("id");

        if(!mDefaultID.isEmpty())
        {
            if(mDefaultID.equals(mCardID))
            {
                mHolder.ivIcon.setVisibility(View.VISIBLE);
                mHolder.tvDefault.setVisibility(View.GONE);
                mHolder.mContainer.setBackgroundResource(R.color.colorPrimary);
            }
            else
            {
            mHolder.ivIcon.setVisibility(View.GONE);
            mHolder.tvDefault.setVisibility(View.VISIBLE);
            mHolder.mContainer.setBackgroundResource(R.color.bill_normal_card);
            }
        }

        mHolder.tvDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onListener!= null)
                {
                    onListener.onDefault(mCardID);
                }
                else
                {
                    Log.e("Bill","please implements the interface of OnBillListener in BillAdapter class ");
                }
            }
        });


        mHolder.tvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onListener!= null)
                {
                    onListener.onDelete(mCardID);
                }
                else
                {
                    Log.e("Bill","please implements the interface of OnBillListener in BillAdapter class ");
                }
            }
        });


        mHolder.tvSerial.setText(mNumber);
        return convertView;
    }


    class ViewHolder{
        private TextView tvSerial;
        private TextView tvDelete;
        private TextView tvDefault;
        private RelativeLayout mContainer;
        private ImageView ivIcon;

        public ViewHolder(View view)
        {
            tvSerial = view.findViewById(R.id.item_bill_serial);
            tvDelete = view.findViewById(R.id.item_bill_delete);
            tvDefault = view.findViewById(R.id.item_bill_default);
            ivIcon = view.findViewById(R.id.item_bill_default_icon);
            mContainer = view.findViewById(R.id.item_bill_back);
        }
    }


    public interface OnBillListener{

        public void onDelete(String cardID);

        public void onDefault(String cardID);
    }


}
