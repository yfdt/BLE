package com.blue.elephant.custom.timepicker;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.blue.elephant.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class TimePickerView extends FrameLayout {

    private final boolean isDebug = true;
    private final String TAG = "WheelView";

    private Context mContext;

    //设置控件的高度
    private int mViewHeight,mTimePanelHeight;

    private WheelView mYearView;
    private WheelView mMonthView;
    private WheelView mDayView;
    private WheelView mHourView;
    private WheelView mMinView;
    private WheelView mSecondView;

    private LinearLayout mViewContainer;
    private RelativeLayout mBottomView;

    private Calendar mCalendar;
    private static boolean isHour = false;
    private SimpleDateFormat mDateFormat,mFormat;

    private ArrayWheelAdapter<String> mMonthAdapter,mDayAdapter;

    public TimePickerView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public TimePickerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TimePickerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private OnPickerListener mCallBack;

    public void setPickerListener(OnPickerListener mCallBack)
    {
        if(mCallBack == null)
            return ;
        if(mCallBack instanceof OnPickerListener)
            this.mCallBack = mCallBack;
    }


    private void init(Context mContext)
    {
        this.mContext = mContext;
        mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        mFormat = new SimpleDateFormat("yyyy-MM-dd");
        //动态添加控件
        mViewHeight = (int) mContext.getResources().getDimension(R.dimen.pick_height);
        //底部控件的尺寸
        LayoutParams mParentParams = new LayoutParams(LayoutParams.MATCH_PARENT,mViewHeight);
        mParentParams.gravity = Gravity.BOTTOM;
        mBottomView = new RelativeLayout(mContext);// 底部容器
        mBottomView.setLayoutParams(mParentParams);
        mBottomView.setBackgroundResource(R.color.white);//底部容器颜色

        int mActionHeight  = (int) mContext.getResources().getDimension(R.dimen.pick_action_height);
        int mActionMargin =  (int) mContext.getResources().getDimension(R.dimen.pick_action_margin);

        RelativeLayout.LayoutParams mActionParamsLeft = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        mActionParamsLeft.addRule(RelativeLayout.CENTER_VERTICAL);
        mActionParamsLeft.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        mActionParamsLeft.leftMargin = mActionMargin;

        RelativeLayout.LayoutParams mActionParamsRight = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        mActionParamsRight.addRule(RelativeLayout.CENTER_VERTICAL);
        mActionParamsRight.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        mActionParamsRight.rightMargin = mActionMargin;

        RelativeLayout.LayoutParams mActionParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,mActionHeight);
        mActionParams.addRule(RelativeLayout.ALIGN_TOP);

        RelativeLayout mActionView = new RelativeLayout(mContext);
        mActionView.setLayoutParams(mActionParams);
        mActionView.setId(R.id.picker_action_container_id);
        mActionView.setBackgroundResource(R.color.light_gray); //底部功能键的背景色

        TextView tvCancel = new TextView(mContext);
        tvCancel.setLayoutParams(mActionParamsLeft);
        tvCancel.setText("Cancel");
        tvCancel.setGravity(Gravity.CENTER|Gravity.LEFT);
        int mTextColor = mContext.getResources().getColor(R.color.cyan);
        tvCancel.setTextColor(mTextColor);
        int mActionPadding = (int) mContext.getResources().getDimension(R.dimen.picker_wheel_action_padding);
        tvCancel.setPadding(mActionPadding,mActionPadding,mActionPadding,mActionPadding);
        tvCancel.setTextSize(TypedValue.COMPLEX_UNIT_DIP,16f);
        tvCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCallBack!= null)
                {
                    mCallBack.onDimiss();
                }
                else
                {
                    throw new NullPointerException("please implements the interface of OnPickerListener");
                }
            }
        });
        TextView tvOK = new TextView(mContext);
        tvOK.setLayoutParams(mActionParamsRight);
        tvOK.setText("OK");
        tvOK.setTextColor(mTextColor);
        tvOK.setGravity(Gravity.CENTER|Gravity.RIGHT);
        tvOK.setTextSize(TypedValue.COMPLEX_UNIT_DIP,16f);
        tvOK.setPadding(mActionPadding,mActionPadding,mActionPadding,mActionPadding);
        tvOK.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int mYear = mYearView.getCurrentItem() + 1950;
                int mMonth = mMonthView.getCurrentItem();
                int mDay = mDayView.getCurrentItem() + 1;
                mCalendar.set(Calendar.YEAR,mYear);
                mCalendar.set(Calendar.MONTH,mMonth);
                mCalendar.set(Calendar.DATE,mDay);
                String mDate;
                if(isHour)
                {
                    int mHour = mHourView.getCurrentItem();
                    int mMin = mMinView.getCurrentItem();
                    int mSecond = mSecondView.getCurrentItem();
                    mCalendar.set(Calendar.HOUR_OF_DAY,mHour);
                    mCalendar.set(Calendar.MINUTE,mMin);
                    mCalendar.set(Calendar.SECOND,mSecond);
                    mDate = mDateFormat.format(mCalendar.getTime());
                }
                else
                {
                    mDate = mFormat.format(mCalendar.getTime());
                }
                if(mCallBack!= null)
                {
                    mCallBack.onDate(mDate);
                }
                else
                {
                    throw new NullPointerException("please implements the interface of OnPickerListener");
                }
            }
        });


        mActionView.addView(tvCancel);
        mActionView.addView(tvOK);
        mBottomView.addView(mActionView);

        //添加时间控件
        mTimePanelHeight = (int) mContext.getResources().getDimension(R.dimen.picker_panel_height);
        RelativeLayout.LayoutParams mViewParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                mTimePanelHeight);
        mViewParams.addRule(RelativeLayout.BELOW,R.id.picker_action_container_id);
        mViewContainer = new LinearLayout(mContext);
        mViewContainer.setLayoutParams(mViewParams);
        int mWheelPadding = (int) mContext.getResources().getDimension(R.dimen.picker_wheel_padding);
        mViewContainer.setPadding(0,mWheelPadding,0,mWheelPadding);
//        mViewContainer.setBackgroundResource(R.color.blue_light);
        mViewContainer.setGravity(Gravity.CENTER);
        int mWheelHeight =  (int) mContext.getResources().getDimension(R.dimen.picker_panel_view_height);
        LinearLayout.LayoutParams mWheelParams = new LinearLayout.LayoutParams(0,mWheelHeight);
        mWheelParams.weight = 1;

        mYearView = new WheelView(mContext);
//        mYearView.setBackgroundResource(R.color.vehicle_search_actionbar_edit);
        mMonthView =new WheelView(mContext);
        mDayView = new WheelView(mContext);
        mHourView = new WheelView(mContext);
        mMinView = new WheelView(mContext);
        mSecondView = new WheelView(mContext);

        mYearView.setLayoutParams(mWheelParams);
        mMonthView.setLayoutParams(mWheelParams);
        mDayView.setLayoutParams(mWheelParams);
        mHourView.setLayoutParams(mWheelParams);
        mMinView.setLayoutParams(mWheelParams);
        mSecondView.setLayoutParams(mWheelParams);

        mBottomView.addView(mViewContainer);
        this.addView(mBottomView);

    }

    public void setDayAndHour()
    {
        isHour = true;
        //间隔
        int mWheelSpaceWidth = (int) mContext.getResources().getDimension(R.dimen.picker_wheel_space_width);
        View mWheelSpaceView;
        LinearLayout.LayoutParams mSpaceParams =
                new LinearLayout.LayoutParams(mWheelSpaceWidth,
                        50);

        mViewContainer.addView(mYearView);

        mWheelSpaceView = new View(mContext);
        mWheelSpaceView.setLayoutParams(mSpaceParams);
        mWheelSpaceView.setId(R.id.picker_space_1);
        mViewContainer.addView(mWheelSpaceView);

        mViewContainer.addView(mMonthView);

        mWheelSpaceView = new View(mContext);
        mWheelSpaceView.setLayoutParams(mSpaceParams);
        mWheelSpaceView.setId(R.id.picker_space_2);
        mViewContainer.addView(mWheelSpaceView);

        mViewContainer.addView(mDayView);

        mWheelSpaceView = new View(mContext);
        mWheelSpaceView.setLayoutParams(mSpaceParams);
        mWheelSpaceView.setId(R.id.picker_space_3);
        mViewContainer.addView(mWheelSpaceView);

        mViewContainer.addView(mHourView);

        mWheelSpaceView = new View(mContext);
        mWheelSpaceView.setLayoutParams(mSpaceParams);
        mWheelSpaceView.setId(R.id.picker_space_4);
        mViewContainer.addView(mWheelSpaceView);

        mViewContainer.addView(mMinView);

        mWheelSpaceView = new View(mContext);
        mWheelSpaceView.setLayoutParams(mSpaceParams);
        mWheelSpaceView.setId(R.id.picker_space_5);
        mViewContainer.addView(mWheelSpaceView);

        mViewContainer.addView(mSecondView);
        //load data
        mCalendar = Calendar.getInstance();
        mCalendar.setTime(new Date(System.currentTimeMillis()));
        int mYear = mCalendar.get(Calendar.YEAR);
        int mMonth = mCalendar.get(Calendar.MONTH);
        int mDay = mCalendar.get(Calendar.DATE);
        ArrayList<String> mYearList = new ArrayList<>();
        for(int i=1950;i<2150;i++)
        {
            mYearList.add(i + "");
        }
        ArrayWheelAdapter mYearAdapter = new ArrayWheelAdapter(mYearList);
        mYearView.setAdapter(mYearAdapter);
        mYearView.setLabel("");
        mYearView.setCurrentItem(mYear - 1950);

        ArrayList<String> mMonthList = new ArrayList<>();
        for(int i=1;i<13;i++)
        {
            mMonthList.add(i + "");
        }
        mMonthAdapter = new ArrayWheelAdapter(mMonthList);
        mMonthView.setAdapter(mMonthAdapter);
        mMonthView.setLabel("");
        mMonthView.setCurrentItem(mMonth);
        int mMonthOfDay = getDayListener(mYear,mMonth+1);
        ArrayList<String> mDayList = new ArrayList<>();
        for(int i=1;i<= mMonthOfDay;i++)
        {
            mDayList.add(i + "");
        }
        mDayAdapter = new ArrayWheelAdapter(mDayList);
        mDayView.setAdapter(mDayAdapter);
        mDayView.setLabel("");
        mDayView.setCurrentItem(mDay -1);

        ArrayList<String> mHourList = new ArrayList<>();
        for(int i=0;i<24;i++)
        {
            mHourList.add(i+ "");
        }
        ArrayWheelAdapter mHourAdapter = new ArrayWheelAdapter(mHourList);
        mHourView.setAdapter(mHourAdapter);
        mHourView.setLabel("");
        mHourView.setCurrentItem(mCalendar.get(Calendar.HOUR_OF_DAY));
        ArrayList<String> mMinList = new ArrayList<>();
        for(int i=0;i<60;i++)
        {
            mMinList.add(i + "");
        }
        ArrayWheelAdapter mMinAdapter = new ArrayWheelAdapter(mMinList);
        mMinView.setAdapter(mMinAdapter);
        mMinView.setLabel("");
        mMinView.setCurrentItem(mCalendar.get(Calendar.MINUTE));
        final ArrayWheelAdapter mSecondAdapter = new ArrayWheelAdapter(mMinList);
        mSecondView.setAdapter(mSecondAdapter);
        mSecondView.setLabel("");
        mSecondView.setCurrentItem(mCalendar.get(Calendar.SECOND));

        //设置参数
        setParams(mYearView);
        setParams(mMonthView);
        setParams(mDayView);
        setParams(mHourView);
        setParams(mMinView);
        setParams(mSecondView);


        mYearView.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                //更新月份和日期
                int mYear = mYearView.getCurrentItem() + 1950;
                int mMonth = mMonthView.getCurrentItem();
                int mDay = mDayView.getCurrentItem() ;
                int mMaxDay = getDayListener(mYear,mMonth+1);
                ArrayList<String> mDayList = new ArrayList<>();
                for(int i=1;i<= mMaxDay;i++)
                {
                    mDayList.add(i + "");
                }
                if(mDayAdapter == null)
                {
                    mDayAdapter = new ArrayWheelAdapter<>(mDayList);
                    mDayView.setAdapter(mDayAdapter);
                }
                else
                {
                    mDayAdapter.setData(mDayList);
                }
                mDayView.setCurrentItem(mDay);
            }
        });

        mMonthView.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {

                int mYear = mYearView.getCurrentItem() + 1950;
                int mMonth = mMonthView.getCurrentItem();
                int mDay = mDayView.getCurrentItem() ;
                int mMaxDay = getDayListener(mYear,mMonth+1);
                ArrayList<String> mDayList = new ArrayList<>();
                for(int i=1;i<= mMaxDay;i++)
                {
                    mDayList.add(i + "");
                }
                if(mDayAdapter == null)
                {
                    mDayAdapter = new ArrayWheelAdapter<>(mDayList);
                    mDayView.setAdapter(mDayAdapter);
                }
                else
                {
                    mDayAdapter.setData(mDayList);
                }
                mDayView.setCurrentItem(mDay);
            }
        });

    }

    private void setParams(WheelView mWheelView)
    {
        mWheelView.setCyclic(false);
        mWheelView.setDividerColor(0XFFD5D5D5);
        mWheelView.setDividerType(WheelView.DividerType.FILL);
        mWheelView.setLineSpacingMultiplier(1.6F);
        mWheelView.setTextColorOut(0XFFA8A8A8);
        mWheelView.setTextColorCenter(0XFF2A2A2A);
        mWheelView.isCenterLabel(false);
//        int textSize = (int) mContext.getResources().getDimension(R.dimen.pickerview_textsize);
        mWheelView.setTextSize(20F);
    }


    public void setHour()
    {
        //间隔
        int mWheelSpaceWidth = (int) mContext.getResources().getDimension(R.dimen.picker_wheel_space_width);
        LinearLayout.LayoutParams mSpaceParams = new LinearLayout.LayoutParams(mWheelSpaceWidth,LinearLayout.LayoutParams.MATCH_PARENT);
        View mWheelSpaceView;

        mViewContainer.addView(mYearView);

        mWheelSpaceView = new View(mContext);
        mWheelSpaceView.setLayoutParams(mSpaceParams);
        mWheelSpaceView.setId(R.id.picker_space_1);
        mViewContainer.addView(mWheelSpaceView);

        mViewContainer.addView(mMonthView);

        mWheelSpaceView = new View(mContext);
        mWheelSpaceView.setLayoutParams(mSpaceParams);
        mWheelSpaceView.setId(R.id.picker_space_2);
        mViewContainer.addView(mWheelSpaceView);

        mViewContainer.addView(mDayView);
        //load data
        mCalendar = Calendar.getInstance();
        int mYear = mCalendar.get(Calendar.YEAR);
        int mMonth = mCalendar.get(Calendar.MONTH);
        int mDay = mCalendar.get(Calendar.DATE);
        ArrayList<String> mYearList = new ArrayList<>();
        for(int i=1950;i<2150;i++)
        {
            mYearList.add(i + "");
        }
        ArrayWheelAdapter mYearAdapter = new ArrayWheelAdapter(mYearList);
        mYearView.setAdapter(mYearAdapter);
        mYearView.setLabel("");
        mYearView.setCurrentItem(mYear - 1950);

        ArrayList<String> mMonthList = new ArrayList<>();
        for(int i=1;i<13;i++)
        {
            mMonthList.add(i + "");
        }
        mMonthAdapter = new ArrayWheelAdapter(mMonthList);
        mMonthView.setAdapter(mMonthAdapter);
        mMonthView.setLabel("");
        mMonthView.setCurrentItem(mMonth);

        ArrayList<String> mDayList = new ArrayList<>();
        int mMaxDay = getDayListener(mYear,mMonth+ 1);
        for(int i=1;i<= mMaxDay;i++)
        {
            mDayList.add(i + "");
        }
        mDayAdapter = new ArrayWheelAdapter(mDayList);
        mDayView.setAdapter(mDayAdapter);
        mDayView.setLabel("");
        mDayView.setCurrentItem(mDay -1 );

        //设置参数
        setParams(mYearView);
        setParams(mMonthView);
        setParams(mDayView);

        mYearView.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                //更新月份和日期
                int mYear = mYearView.getCurrentItem() + 1950;
                int mMonth = mMonthView.getCurrentItem();
                int mDay = mDayView.getCurrentItem() ;
                int mMaxDay = getDayListener(mYear,mMonth+1);
                ArrayList<String> mDayList = new ArrayList<>();
                for(int i=1;i<= mMaxDay;i++)
                {
                    mDayList.add(i + "");
                }
                if(mDayAdapter == null)
                {
                    mDayAdapter = new ArrayWheelAdapter<>(mDayList);
                    mDayView.setAdapter(mDayAdapter);
                }
                else
                {
                    mDayAdapter.setData(mDayList);
                }
                mDayView.setCurrentItem(mDay);
            }
        });

        mMonthView.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {

                int mYear = mYearView.getCurrentItem() + 1950;
                int mMonth = mMonthView.getCurrentItem();
                int mDay = mDayView.getCurrentItem() ;
                int mMaxDay = getDayListener(mYear,mMonth+1);
                ArrayList<String> mDayList = new ArrayList<>();
                for(int i=1;i<= mMaxDay;i++)
                {
                    mDayList.add(i + "");
                }
                if(mDayAdapter == null)
                {
                    mDayAdapter = new ArrayWheelAdapter<>(mDayList);
                    mDayView.setAdapter(mDayAdapter);
                }
                else
                {
                    mDayAdapter.setData(mDayList);
                }
                mDayView.setCurrentItem(mDay);
            }
        });



    }

    private int getDayListener(int year,int month)
    {
        switch (month)
        {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                return 31;
            case 4:
            case 6:
            case 9:
            case 11:
                return 30;
            case 2:
                if((year%4==0 && year%100!=0) || year%400==0)
                    return 29;
                else
                    return 28;
        }
        return 0;
    }

    public PopupWindow onShowWindow(Context mContext,View view,View parent)
    {
        PopupWindow mPopWindow = new PopupWindow(mContext);
        Drawable mDrawable = mContext.getResources().getDrawable(R.drawable.shape_pop_gray);
        mPopWindow.setBackgroundDrawable(mDrawable);
        mPopWindow.setContentView(view);
        WindowManager mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        int width = mWindowManager.getDefaultDisplay().getWidth();
        int height = mWindowManager.getDefaultDisplay().getHeight();
        mPopWindow.setWidth(width);
        mPopWindow.setHeight(height);
        mPopWindow.setOutsideTouchable(true);
        mPopWindow.showAsDropDown(parent,0,0);
        return mPopWindow;
    }

    public Dialog onShowDialog(View view)
    {
        Dialog mDialog = null;
        mDialog = new Dialog(mContext);
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.setContentView(view);
        Window mDialogWindow = mDialog.getWindow();
        WindowManager.LayoutParams P = mDialogWindow.getAttributes();
        int screenWidth = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
        int offsetHeight = (int) mContext.getResources().getDimension(R.dimen.pick_action_height);
        P.width = screenWidth;
        P.height = mViewHeight+offsetHeight;
        mDialogWindow.setAttributes(P);
        mDialogWindow.setBackgroundDrawableResource(android.R.color.transparent);// 去掉dialog的默认背景
        if(mDialogWindow != null)
        {
            mDialogWindow.setGravity(Gravity.BOTTOM);
        }
        mDialog.show();
        return mDialog;
    }

}
