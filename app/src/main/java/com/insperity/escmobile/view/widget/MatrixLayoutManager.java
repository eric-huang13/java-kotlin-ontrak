package com.insperity.escmobile.view.widget;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.LayoutManager;
import androidx.recyclerview.widget.RecyclerView.LayoutParams;
import androidx.recyclerview.widget.RecyclerView.Recycler;
import android.util.Pair;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;

import com.crashlytics.android.Crashlytics;

public class MatrixLayoutManager extends LayoutManager {
    private SparseIntArray widthPerViewTypeSparseArray = new SparseIntArray();
    private SparseIntArray heightPerViewTypeSparseArray = new SparseIntArray();
    private RecyclerView.Adapter adapter;
    private Recycler recycler;
    private int firstVisiblePosition;
    private HorizontalScrollListener listener;
//    private int scrollAxis;
//    private int dxTrack;
//    private int dyTrack;

    public interface HorizontalScrollListener {
        void scrollHorizontallyBy(int dx);

        void setLeftCoordinate(int left);
    }

    public MatrixLayoutManager(RecyclerView.Adapter adapter) {
        setAutoMeasureEnabled(true);
        this.adapter = adapter;
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
//        scrollAxis = ViewCompat.SCROLL_AXIS_NONE;
//        dxTrack = 0;
//        dyTrack = 0;
    }

    @Override
    public LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter newAdapter) {
        super.onAdapterChanged(oldAdapter, newAdapter);
        this.adapter = newAdapter;
    }

    @Override
    public void onLayoutChildren(Recycler recycler, RecyclerView.State state) {
        this.recycler = recycler;
        if (getItemCount() == 0) {
            detachAndScrapAttachedViews(recycler);
            return;
        }
        int left = 0;
        int top = 0;
        calculateViewTypesDecoratedMeasures(recycler);
        if (getChildCount() == 0 || this.adapter == null) {
            this.firstVisiblePosition = 0;
        } else {
            View view = getChildAt(0);
            int decoratedTop = getDecoratedTop(view);
            int decoratedLeft = getDecoratedLeft(view);
            while (this.firstVisiblePosition > 0 && canVerticallyScroll()) {
                this.firstVisiblePosition--;
                top = 1;
            }
            if (top != 0) {
                decoratedTop = getVerticalSpace();
                for (top = getItemCount() - 1; top >= this.firstVisiblePosition; top--) {
                    decoratedTop -= this.heightPerViewTypeSparseArray.get(this.adapter.getItemViewType(top));
                }
                if (this.firstVisiblePosition == 0) {
                    decoratedTop = Math.min(decoratedTop, 0);
                }
            }
            if (decoratedLeft < 0) {
                top = getWiderPositionPair().second;
                int horizontalSpace = getHorizontalSpace();
                if (top <= horizontalSpace) {
                    top = decoratedTop;
                } else {
                    left = Math.max(decoratedLeft, horizontalSpace - top);
                    top = decoratedTop;
                }
            } else {
                left = decoratedLeft;
                top = decoratedTop;
            }
        }
        removeAndRecycleAllViews(recycler);
        fillViews(left, top, recycler);
        if (this.listener != null) {
            this.listener.setLeftCoordinate(left);
        }
    }

    @Override
    public void scrollToPosition(int position) {
        if (position >= 0 && position < getItemCount() && this.recycler != null) {
            int left = 0;
            this.firstVisiblePosition = position;
            if (getChildCount() > 0) {
                left = getDecoratedLeft(getChildAt(0));
                offsetChildrenVertical(-getDecoratedTop(getChildAt(0)));
            }
            while (position > 0 && canVerticallyScroll()) {
                this.firstVisiblePosition--;
            }
            removeAndRecycleAllViews(this.recycler);
            fillViews(left, 0, this.recycler);
        }
    }

    private void calculateViewTypesDecoratedMeasures(Recycler recycler) {
        this.widthPerViewTypeSparseArray.clear();
        this.heightPerViewTypeSparseArray.clear();
        if (this.adapter != null) {
            for (int i = 0; i < getItemCount(); i++) {
                int itemViewType = this.adapter.getItemViewType(i);
                if (this.widthPerViewTypeSparseArray.get(itemViewType) == 0) {
                    View view = recycler.getViewForPosition(i);
                    addView(view);
                    measureChildWithMargins(view, 0, 0);
                    detachAndScrapView(view, recycler);
                    this.widthPerViewTypeSparseArray.put(itemViewType, getDecoratedMeasuredWidth(view));
                    this.heightPerViewTypeSparseArray.put(itemViewType, getDecoratedMeasuredHeight(view));
                }
            }
        }
    }

    private int getLastVisiblePosition() {
        if (this.adapter == null) {
            return 0;
        }
        int verticalSpaceSum = 0;
        int verticalSpace = getVerticalSpace();
        int lastVisiblePosition = 0;
        for (int index = this.firstVisiblePosition; index < getItemCount(); index++) {
            lastVisiblePosition++;
            verticalSpaceSum += this.heightPerViewTypeSparseArray.get(this.adapter.getItemViewType(index));
            if (verticalSpaceSum >= verticalSpace) {
                return lastVisiblePosition;
            }
        }
        return lastVisiblePosition;
    }

    private boolean canVerticallyScroll() {
        if (this.adapter != null) {
            int verticalSpace = getVerticalSpace();
            int verticalSpaceSum = 0;
            for (int position = 0; position < getItemCount(); position++) {
                verticalSpaceSum += this.heightPerViewTypeSparseArray.get(this.adapter.getItemViewType(position));
                if (verticalSpaceSum >= verticalSpace) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int scrollHorizontallyBy(int dx, Recycler recycler, RecyclerView.State state) {
        if (getChildCount() == 0) {// || this.scrollAxis == ViewCompat.SCROLL_AXIS_VERTICAL) {
            return 0;
        }
//        dxTrack += dx;
//        if (scrollAxis != ViewCompat.SCROLL_AXIS_HORIZONTAL && Math.abs(dxTrack) > 20) {
//            scrollAxis = ViewCompat.SCROLL_AXIS_HORIZONTAL;
//        }
        View view = getChildAt(getWiderPositionPair().first);
        if (getDecoratedRight(view) - getDecoratedLeft(view) <= getHorizontalSpace()) {
            return 0;
        }
        int max;
        if (dx > 0) {
            max = Math.max(-dx, (getHorizontalSpace() - getDecoratedRight(view)) + getPaddingRight());
        } else {
            max = Math.min(-dx, (-getDecoratedLeft(view)) + getPaddingLeft());
        }
        offsetChildrenHorizontal(max);
        if (Math.abs(max) != Math.abs(dx)) {
            return Math.abs(max);
        }
        return dx;
    }

    @Override
    public int scrollVerticallyBy(int dy, Recycler recycler, RecyclerView.State state) {
        if (getChildCount() == 0) {// || this.scrollAxis == ViewCompat.SCROLL_AXIS_HORIZONTAL) {
            return 0;
        }
//        dyTrack += dy;
//        if (scrollAxis != ViewCompat.SCROLL_AXIS_VERTICAL && Math.abs(dyTrack) > 20) {
//            scrollAxis = ViewCompat.SCROLL_AXIS_VERTICAL;
//        }
        if (getDecoratedBottom(getChildAt(getChildCount() - 1)) - getDecoratedTop(getChildAt(0)) <= getVerticalSpace()) {
            return 0;
        }
        int b = dy > 0 ? scrollUp(dy, recycler) : scrollDown(dy, recycler);
        offsetChildrenVertical(b);
        return -b;
    }

    private int scrollUp(int dy, Recycler recycler) {
        int paddingBottom = getHeight() - getPaddingBottom();
        View view = getChildAt(getChildCount() - 1);
        int decoratedBottom = getDecoratedBottom(view);
        if (decoratedBottom - dy >= paddingBottom) {
            return -dy;
        }
        int decoratedLeft = getDecoratedLeft(view);
        while (this.firstVisiblePosition + getChildCount() < getItemCount()) {
            view = recycler.getViewForPosition(this.firstVisiblePosition + getChildCount());
            addView(view);
            measureChild(view, 0, 0);
            int bottom = decoratedBottom + getDecoratedMeasuredHeight(view);
            layoutDecorated(view, decoratedLeft, decoratedBottom, decoratedLeft + getDecoratedMeasuredWidth(view), bottom);
            if (bottom - dy >= paddingBottom) {
                break;
            }
            decoratedBottom = bottom;
        }
        paddingBottom = paddingBottom - decoratedBottom;
        int height = getPaddingTop() - paddingBottom;
        while (true) {
            view = getChildAt(0);
            if (getDecoratedBottom(view) >= height) {
                return paddingBottom;
            }
            detachAndScrapView(view, recycler);
            this.firstVisiblePosition++;
        }
    }

    private int scrollDown(int dy, Recycler recycler) {
        int paddingTop = getPaddingTop();
        View view = getChildAt(0);
        int decoratedTop = getDecoratedTop(view);
        if (decoratedTop - dy <= paddingTop) {
            return -dy;
        }
        int decoratedLeft = getDecoratedLeft(view);
        while (this.firstVisiblePosition != 0) {
            this.firstVisiblePosition--;
            view = recycler.getViewForPosition(this.firstVisiblePosition);
            addView(view, 0);
            measureChild(view, 0, 0);
            int top = decoratedTop - getDecoratedMeasuredHeight(view);
            layoutDecorated(view, decoratedLeft, top, decoratedLeft + getDecoratedMeasuredWidth(view), decoratedTop);
            if (top - dy <= paddingTop) {
                break;
            }
            decoratedTop = top;
        }
        paddingTop = paddingTop - decoratedTop;
        int height = (getHeight() - getPaddingBottom()) + paddingTop;
        while (true) {
            view = getChildAt(getChildCount() - 1);
            if (getDecoratedTop(view) <= height) {
                return paddingTop;
            }
            detachAndScrapView(view, recycler);
        }
    }


    private void fillViews(int left, int top, Recycler recycler) {
        int topSum = 0;
        this.firstVisiblePosition = Math.max(0, Math.min(getItemCount() - 1, this.firstVisiblePosition));
        int nextPosition = this.firstVisiblePosition + 1;
        View child = recycler.getViewForPosition(this.firstVisiblePosition);
        addView(child);
        measureChild(child, 0, 0);
        int decoratedMeasuredHeight = getDecoratedMeasuredHeight(child);
        if (decoratedMeasuredHeight + top >= 0) {
            topSum = top;
        } else if (this.firstVisiblePosition < getItemCount() - 1) {
            this.firstVisiblePosition++;
            detachAndScrapView(child, recycler);
            child = null;
        }
        if (child != null) {
            layoutDecorated(child, left, topSum, left + getDecoratedMeasuredWidth(child), topSum + decoratedMeasuredHeight);
            topSum += decoratedMeasuredHeight;
        }
        decoratedMeasuredHeight = getHeight() - getPaddingBottom();
        while (nextPosition < getItemCount()) {
            try {
                child = recycler.getViewForPosition(nextPosition);
                addView(child);
                measureChild(child, 0, 0);
                int decoratedMeasuredHeight2 = getDecoratedMeasuredHeight(child);
                layoutDecorated(child, left, topSum, left + getDecoratedMeasuredWidth(child), topSum + decoratedMeasuredHeight2);
                topSum += decoratedMeasuredHeight2;
                if (topSum >= decoratedMeasuredHeight) {
                    break;
                }
                nextPosition++;
            } catch (Throwable e) {
                Crashlytics.logException(e);
            }
        }
        if (topSum < decoratedMeasuredHeight) {
            int heightDifference = decoratedMeasuredHeight - topSum;
            int decoratedTop = getDecoratedTop(getChildAt(0));
            while (this.firstVisiblePosition > 0) {
                this.firstVisiblePosition--;
                child = recycler.getViewForPosition(this.firstVisiblePosition);
                addView(child, 0);
                measureChild(child, 0, 0);
                decoratedMeasuredHeight = getDecoratedMeasuredHeight(child);
                layoutDecorated(child, left, decoratedTop - decoratedMeasuredHeight, left + getDecoratedMeasuredWidth(child), decoratedTop);
                decoratedTop -= decoratedMeasuredHeight;
                if ((-decoratedTop) >= heightDifference) {
                    break;
                }
            }
            if ((-decoratedTop) < heightDifference) {
                offsetChildrenVertical(-getDecoratedTop(getChildAt(0)));
            } else {
                offsetChildrenVertical(heightDifference);
            }
        }
    }

    private int getHorizontalSpace() {
        return (getWidth() - getPaddingRight()) - getPaddingLeft();
    }

    private int getVerticalSpace() {
        return (getHeight() - getPaddingBottom()) - getPaddingTop();
    }

    private Pair<Integer, Integer> getWiderPositionPair() {
        int finalWidth = 0;
        int topPosition = Math.min(getItemCount(), this.firstVisiblePosition + getLastVisiblePosition()) - 1;
        int position = this.firstVisiblePosition;
        int finalPosition = 0;
        while (position <= topPosition) {
            int width = this.widthPerViewTypeSparseArray.get(this.adapter.getItemViewType(position));
            if (width > finalWidth) {
                finalPosition = position - this.firstVisiblePosition;
                finalWidth = width;
            }
            position++;
        }
        return new Pair<>(finalPosition, finalWidth);
    }

    @Override
    public void offsetChildrenHorizontal(int dx) {
        super.offsetChildrenHorizontal(dx);
        if (this.listener != null) {
            this.listener.scrollHorizontallyBy(dx);
        }
    }

    public MatrixLayoutManager setHorizontalScrollListener(HorizontalScrollListener listener) {
        this.listener = listener;
        return this;
    }
}
