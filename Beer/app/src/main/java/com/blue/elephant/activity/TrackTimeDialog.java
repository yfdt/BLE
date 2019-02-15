package com.blue.elephant.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.blue.elephant.R;
import com.blue.elephant.custom.timepicker.OnPickerListener;
import com.blue.elephant.custom.timepicker.TimePickerView;
import com.blue.elephant.util.DialogUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TrackTimeDialog extends Activity {

    private EditText tvStart,tvEnd;
    private SimpleDateFormat mSimpleDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Dialog mPopWindow;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.menu_income_data);
        tvStart = findViewById(R.id.income_menu_start);
        tvEnd = findViewById(R.id.income_menu_end);
        TextView tvOk = findViewById(R.id.income_menu_ok);
        TextView tvCancel = findViewById(R.id.income_menu_cancel);
        tvOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String start = tvStart.getText().toString();
                    String end = tvEnd.getText().toString();
                    String income_s_tip = getResources().getString(R.string.income_menu_start);
                    String income_e_tip = getResources().getString(R.string.income_menu_end);
                    if (start.equals(income_s_tip)) {
                        DialogUtils.showToast(TrackTimeDialog.this,R.string.income_menu_start_t);
                        return;
                    }
                    if (end.equals(income_e_tip)) {
                        DialogUtils.showToast(TrackTimeDialog.this,R.string.income_menu_end_t);
                        return;
                    }
                    try {
                        long mStartMillins = mSimpleDate.parse(start).getTime();
                        long mEndMillins = mSimpleDate.parse(end).getTime();
                        if (mStartMillins > mEndMillins) {
                            DialogUtils.showToast(TrackTimeDialog.this,R.string.income_menu_time_before);
                            return;
                        }
                    } catch (Exception e) {
                        tvStart.setText(R.string.income_menu_start);
                        tvEnd.setText(R.string.income_menu_end);
                        DialogUtils.showToast(TrackTimeDialog.this,R.string.income_menu_time_error);
                    }
                    Intent intent = getIntent();
                    intent.putExtra("start", start);
                    intent.putExtra("end", end);
                    setResult(0X01, intent);
                    TrackTimeDialog.this.finish();

                }catch(Exception e)
                {
                    Log.e("TimeDialogActivity",""+ e.getMessage());
                }
            }
        });
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TrackTimeDialog.this.finish();
            }
        });


        tvStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TimePickerView mView = new TimePickerView(TrackTimeDialog.this);
                mView.setDayAndHour();
                mView.setPickerListener(new OnPickerListener(){
                    @Override
                    public void onDimiss() {
                        if(mPopWindow!= null)
                            mPopWindow.dismiss();
                    }

                    @Override
                    public void onDate(String date) {
                        if(mPopWindow!= null)
                            mPopWindow.dismiss();
                        tvStart.setText(date);
                    }
                });
                mPopWindow =  mView.onShowDialog(mView);

            }
        });

        tvEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerView mView = new TimePickerView(TrackTimeDialog.this);
                mView.setDayAndHour();
                mView.setPickerListener(new OnPickerListener(){
                    @Override
                    public void onDimiss() {
                        if(mPopWindow!= null)
                            mPopWindow.dismiss();
                    }

                    @Override
                    public void onDate(String date) {
                        if(mPopWindow!= null)
                            mPopWindow.dismiss();
                        tvEnd.setText(date);
                    }
                });
                mPopWindow =  mView.onShowDialog(mView);
            }
        });

    }
}
