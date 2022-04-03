package com.insperity.escmobile.util

import android.content.Context
import android.content.res.Resources
import androidx.core.content.res.ResourcesCompat
import android.text.TextPaint
import android.util.DisplayMetrics
import com.insperity.escmobile.BuildConfig
import com.insperity.escmobile.R
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AppUtil @Inject constructor(val context: Context) {

    val versionInfo: String
        get() {
            val buildInfoStr = StringBuilder("Version ").append(BuildConfig.VERSION_NAME)
            return if (BuildConfig.BUILD_TYPE == "release") {
                buildInfoStr.toString()
            } else buildInfoStr
                    .append(" ")
                    .append("Build ")
                    .append(BuildConfig.BUILD_ID ?: "null")
                    .toString()
        }

    val versionInfoFull: String
        get() = String.format(Locale.US, "Version %s\nBuild %d", BuildConfig.VERSION_NAME, BuildConfig.BUILD_ID)

    fun dpToPx(dp: Float): Int {
        val metrics = Resources.getSystem().displayMetrics
        return Math.round(dp * (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT))
    }

    fun pxToDp(px: Float): Float {
        val metrics = Resources.getSystem().displayMetrics
        return px / (metrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun getTextWidth(text: String, spSize: Float = 14f, padding: Float = context.resources.getDimension(R.dimen.full_margin)): Int {
        val paint = TextPaint()
        paint.textSize = spSize * Resources.getSystem().displayMetrics.scaledDensity
        paint.typeface = ResourcesCompat.getFont(context, R.font.noto_sans_bold)
        return (paint.measureText(text) + padding).toInt()
    }
}
