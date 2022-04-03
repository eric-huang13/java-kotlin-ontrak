package android.support.design.internal;


import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

@SuppressLint("RestrictedApi")
public class SnackbarContentLayout extends LinearLayout {
    public SnackbarContentLayout(Context context) {
        super(context);
    }

    public SnackbarContentLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
