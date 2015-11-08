package us.v4lk.transrock.util;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * ViewPager that allows swiping to be turned on and off.
 */
public class SmartViewPager extends ViewPager {

    private boolean allowSwiping = true;
    public static final int MAP_PAGE = 0, ROUTE_PAGE = 1;

    public SmartViewPager(Context context) {
        super(context);
    }

    public SmartViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return allowSwiping && super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return allowSwiping && super.onTouchEvent(ev);
    }

    public void setAllowSwiping(boolean allow) {
        this.allowSwiping = allow;
    }
}
