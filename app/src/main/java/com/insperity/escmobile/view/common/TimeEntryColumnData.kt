package com.insperity.escmobile.view.common

import android.content.Context
import android.view.Gravity
import com.insperity.escmobile.R

class TimeEntryColumnData(val text: String, width: Int, private val rightAligned: Boolean, var emptyColumn: Boolean = true, var finalWidth: Int = width) {

    var width: Int = width
        set(it) {
            field = it
            finalWidth = it
        }

    fun getGravity(): Int = if (rightAligned) Gravity.CENTER_VERTICAL or Gravity.END else Gravity.CENTER_VERTICAL

    fun getEndPadding(context: Context): Int = if (rightAligned) finalWidth - width + context.resources.getDimension(R.dimen.right_aligned_extra_margin).toInt() else 0
}