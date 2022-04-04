package com.delphiaconsulting.timestar.view.common;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class ViewExpander {
    public static final int ANIMATION_DURATION = 300;

    public static void expandContainer(final ViewGroup container) {
        int widthSpec = View.MeasureSpec.makeMeasureSpec(container.getWidth(), View.MeasureSpec.AT_MOST);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(container.getWidth(), View.MeasureSpec.UNSPECIFIED);
        container.measure(widthSpec, heightSpec);
        final int currentHeight = container.getLayoutParams().height;
        final int difference = container.getMeasuredHeight() - currentHeight;
        if (container.getVisibility() != View.VISIBLE) {
            container.setVisibility(View.VISIBLE);
        }
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                container.getLayoutParams().height = currentHeight + (int) (difference * interpolatedTime);
                container.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration(ANIMATION_DURATION);
        container.startAnimation(a);
    }

    public static void collapseContainer(final ViewGroup container) {
        final int initialHeight = container.getMeasuredHeight();
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    container.setVisibility(View.GONE);
                    return;
                }
                container.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                container.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration(ANIMATION_DURATION);
        container.startAnimation(a);
    }
}
