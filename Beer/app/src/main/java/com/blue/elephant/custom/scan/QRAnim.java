package com.blue.elephant.custom.scan;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.blue.elephant.R;
import com.blue.elephant.util.FormatUtil;


/**
 * Created by song on 2018/6/19.
 */

public class QRAnim extends FrameLayout {

//    private QRFrame mFrameView;
    private Context mContext;



    public QRAnim(@NonNull Context context) {
        super(context);
        init(context);
    }

    public QRAnim(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public QRAnim(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context mContext)
    {
        this.mContext = mContext;
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
//        mFrameView = new QRFrame(mContext);
        //设置控件的大小
        int size = (int) mContext.getResources().getDimension(R.dimen.qr_scan_size);
//        LayoutParams mParams = new LayoutParams(size,size);
//        mParams.gravity = Gravity.CENTER;
//        mFrameView.setLayoutParams(mParams);
//        this.addView(mFrameView);


        //添加一个属性动画
        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation
                .RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT,
                1f);
        animation.setDuration(4500);
        animation.setRepeatCount(-1);
        animation.setRepeatMode(Animation.RESTART);

        ImageView animLine = new ImageView(mContext);
        Drawable mDrawble = mContext.getResources().getDrawable(R.mipmap.scan_line);
        animLine.setImageDrawable(mDrawble);
        int lineHeight = FormatUtil.dpToPix(6,mContext);
        int offset = FormatUtil.dpToPix(4,mContext);
        LayoutParams mLineParams = new LayoutParams(size,lineHeight);

        mLineParams.gravity = Gravity.TOP| Gravity.CENTER_HORIZONTAL;
        mLineParams.setMargins(offset,offset,offset,offset);
        animLine.setLayoutParams(mLineParams);
        animLine.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        this.addView(animLine);
        animLine.startAnimation(animation);
    }

}
