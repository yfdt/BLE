package com.blue.elephant.custom.edit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

@SuppressLint("AppCompatCustomView")
public class BikeEditText extends EditText {

    private int height;

    public BikeEditText(Context context) {
        super(context);
    }

    public BikeEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BikeEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init()
    {

    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
//        return super.dispatchTouchEvent(event);
        getParent().requestDisallowInterceptTouchEvent(true);


        this.scrollTo(0, (int) event.getY());
        return true;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }
}
