package com.insperity.escmobile.view.widget

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.FrameLayout

open class StickLastChildLayout : FrameLayout {
    constructor(context: Context) : super(context) {}

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    constructor(context: Context, attributeSet: AttributeSet, i: Int) : super(context, attributeSet, i)

    @TargetApi(21)
    constructor(context: Context, attributeSet: AttributeSet, i: Int, i2: Int) : super(context, attributeSet, i, i2)

    override fun offsetLeftAndRight(i: Int) {
        super.offsetLeftAndRight(i)
        getChildAt(childCount - 1).offsetLeftAndRight(-i)
    }

    override fun onAttachedToWindow() {
        val childAt = getChildAt(childCount - 1)
        childAt.offsetLeftAndRight(-childAt.left - left)
        super.onAttachedToWindow()
    }

    override fun dispatchDraw(canvas: Canvas) {
        val childAt = getChildAt(childCount - 1)
        childAt.offsetLeftAndRight(-childAt.left - left)
        super.dispatchDraw(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), heightMeasureSpec)
    }
}
