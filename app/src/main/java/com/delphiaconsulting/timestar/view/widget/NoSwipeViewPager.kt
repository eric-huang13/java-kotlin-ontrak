package com.delphiaconsulting.timestar.view.widget

import android.content.Context
import androidx.viewpager.widget.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent


class NoSwipeViewPager(context: Context, attrs: AttributeSet?) : ViewPager(context, attrs) {

    override fun onTouchEvent(ev: MotionEvent) = false

    override fun onInterceptTouchEvent(ev: MotionEvent) = false
}