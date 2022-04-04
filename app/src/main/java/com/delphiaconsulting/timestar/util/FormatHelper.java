package com.delphiaconsulting.timestar.util;

import android.annotation.SuppressLint;

import com.crashlytics.android.Crashlytics;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

@SuppressLint("SimpleDateFormat")
public class FormatHelper {

    public static String amount(String amount) {
        Double amt = 0.0;
        if (amount != null && !amount.equalsIgnoreCase("")) {
            try {
                amt = Double.parseDouble(amount);
            } catch (NumberFormatException e) {
                Timber.e(e, e.getMessage());
                Crashlytics.logException(e);
            }
        }
        return NumberFormat.getCurrencyInstance().format(amt);
    }

    public static String formattedDate(Date date) {
        return new SimpleDateFormat("MM/dd/yyyy").format(date);
    }

    public static DateTime parseStringToDateTime(String dateStr) {
        return DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parseDateTime(dateStr);
    }

    public static String formatPercent(double percent) {
        return formatPercent("%.2f%%", percent);
    }

    public static String formatPercent(String format, double percent) {
        return String.format(Locale.US, format, percent);
    }

    public static String formatAmount(double amount) {
        return NumberFormat.getCurrencyInstance().format(amount);
    }
}
