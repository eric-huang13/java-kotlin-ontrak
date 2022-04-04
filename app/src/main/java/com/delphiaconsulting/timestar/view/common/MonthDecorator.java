package com.delphiaconsulting.timestar.view.common;

import android.content.Context;
import android.graphics.Typeface;
import androidx.core.content.ContextCompat;
import android.util.TypedValue;

import com.delphiaconsulting.timestar.R;
import com.squareup.timessquare.CalendarCellDecorator;
import com.squareup.timessquare.CalendarCellView;

import java.util.Date;

/**
 * Created by qktran on 6/1/15.
 */
public class MonthDecorator implements CalendarCellDecorator {
    private Context context;

    public MonthDecorator(Context context) {
        this.context = context;
    }

    @Override
    public void decorate(CalendarCellView cellView, Date date) {
        cellView.getDayOfMonthTextView().setTypeface(null, Typeface.NORMAL);
        cellView.getDayOfMonthTextView().setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        if (cellView.isSelected()) {
            cellView.setBackgroundResource(R.drawable.circle_green);
            cellView.getDayOfMonthTextView().setTextColor(ContextCompat.getColor(context, R.color.text_white));
            return;
        }
        if (cellView.isToday()) {
            cellView.setBackgroundResource(R.drawable.circle_blue);
            cellView.getDayOfMonthTextView().setTextColor(ContextCompat.getColor(context, R.color.text_white));
            return;
        }
        cellView.setBackgroundResource(R.color.white);
        if (cellView.isSelectable()) {
            cellView.getDayOfMonthTextView().setTextColor(ContextCompat.getColor(context, R.color.text_grey_light));
            return;
        }
        cellView.getDayOfMonthTextView().setTextColor(ContextCompat.getColor(context, R.color.text_grey_lighter));
    }
}
