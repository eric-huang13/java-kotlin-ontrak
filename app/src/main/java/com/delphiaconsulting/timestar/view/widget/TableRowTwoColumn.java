package com.delphiaconsulting.timestar.view.widget;

import android.content.Context;
import androidx.annotation.DrawableRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TableRow;
import android.widget.TextView;

import com.delphiaconsulting.timestar.R;

/**
 * A {@link TableRow} containing two text fields
 */
public class TableRowTwoColumn extends TableRow {

    private TextView leftText;
    private TextView rightText;

    public TableRowTwoColumn(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.widget_table_row_two_column, this, true);
        leftText = (TextView) findViewById(R.id.left_text);
        rightText = (TextView) findViewById(R.id.right_text);
        setBackgroundResource(R.color.white);
        rightText.setSingleLine(false);
    }

    public void setText(String leftColumnText, String rightColumnText) {
        leftText.setText(leftColumnText);
        rightText.setText(rightColumnText);
    }

    public void setTextSize(float leftSize, float rightSize) {
        leftText.setTextSize(leftSize);
        rightText.setTextSize(rightSize);
    }

    public void setTextColor(int leftColor, int rightColor) {
        leftText.setTextColor(leftColor);
        rightText.setTextColor(rightColor);
    }

    public void setTextStyle(int leftStyle, int rightStyle) {
        leftText.setTypeface(leftText.getTypeface(), leftStyle);
        rightText.setTypeface(rightText.getTypeface(), rightStyle);
    }

    public void setWeights(float leftWeight, float rightWeight) {
        LayoutParams l_params = (LayoutParams) leftText.getLayoutParams();
        l_params.weight = leftWeight;
        LayoutParams r_params = (LayoutParams) rightText.getLayoutParams();
        r_params.weight = rightWeight;
    }

    public void setTextGravity(int leftGravity, int rightGravity) {
        rightText.setGravity(leftGravity);
        rightText.setGravity(rightGravity);
    }

    public void setBackgroundDrawable(@DrawableRes int res) {
        setBackgroundResource(res);
    }
}
