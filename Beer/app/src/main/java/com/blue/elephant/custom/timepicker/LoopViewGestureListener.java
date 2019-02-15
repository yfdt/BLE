package com.blue.elephant.custom.timepicker;

import android.view.MotionEvent;

public class LoopViewGestureListener extends android.view.GestureDetector.SimpleOnGestureListener{

    private final WheelView wheelView;


    public LoopViewGestureListener(WheelView wheelView) {
        this.wheelView = wheelView;
    }

    @Override
    public final boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        wheelView.scrollBy(velocityY);
        return true;
    }

}
