package com.blue.elephant.custom.scan;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.blue.elephant.util.FormatUtil;


/**
 * Created by song on 2018/6/19.
 */

public class QRFrame extends View {

    private int height;
    private int width;
    private Paint mPaint;
    private int mColor;
    private Context mContext;

    public QRFrame(Context context) {
        super(context);
        init(context);
    }

    public QRFrame(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public QRFrame(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    private void init(Context mContext)
    {
        this.mContext = mContext;
        mColor = Color.rgb(138,238,251);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(mColor);
        float paintWidth = FormatUtil.dpToPix(5f,mContext);
        mPaint.setStrokeWidth(paintWidth);


    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int offset = FormatUtil.dpToPix(30,mContext);
        int size = Math.min(width,height);
        float[] lines = new float[]{
                0,0,
                0,offset,
                0,0,
                offset,0,
                size-offset,0,
                size,0,
                size,0,
                size,offset,
                0,size - offset,
                0,size,
                0,size,
                offset,size,
                size - offset,size,
                size,size,
                size,size,
                size,size-offset
        };

        //绘制线段
        canvas.drawLines(lines,mPaint);

    }



}
