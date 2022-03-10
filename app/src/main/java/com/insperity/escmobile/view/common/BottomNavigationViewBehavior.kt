package com.insperity.escmobile.view.common

import android.animation.Animator
import android.content.Context
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.View

class BottomNavigationViewBehavior(context: Context, attributeSet: AttributeSet) : CoordinatorLayout.Behavior<BottomNavigationView>() {

    private var height: Int = 0
    private var running = false

    override fun onLayoutChild(parent: CoordinatorLayout, child: BottomNavigationView, layoutDirection: Int): Boolean {
        height = child.height
        return super.onLayoutChild(parent, child, layoutDirection)
    }

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: BottomNavigationView, directTargetChild: View, target: View, axes: Int, type: Int) = false //axes == ViewCompat.SCROLL_AXIS_VERTICAL || axes == 3

    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: BottomNavigationView, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, @ViewCompat.NestedScrollType type: Int) {
        if (dyUnconsumed > 0) {
            slideDown(child)
        } else if (dyUnconsumed < 0) {
            slideUp(child)
        }
    }

    private fun slideUp(child: BottomNavigationView) {
        if (running) return
        child.animate().translationY(0f).setListener(getAnimatorListener())
    }

    private fun slideDown(child: BottomNavigationView) {
        if (running) return
        child.animate().translationY(height.toFloat()).setListener(getAnimatorListener())
    }

    private fun getAnimatorListener(): Animator.AnimatorListener = object : Animator.AnimatorListener {
        override fun onAnimationRepeat(p0: Animator?) {}

        override fun onAnimationCancel(p0: Animator?) {}

        override fun onAnimationStart(p0: Animator?) {
            running = true
        }

        override fun onAnimationEnd(p0: Animator?) {
            running = false
        }
    }
}