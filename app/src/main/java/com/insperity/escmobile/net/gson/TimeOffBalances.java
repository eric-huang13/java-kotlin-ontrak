package com.insperity.escmobile.net.gson;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import com.crashlytics.android.Crashlytics;

import org.joda.time.DateTime;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import timber.log.Timber;

public class TimeOffBalances {
    public final int balanceType;
    public final List<TimeOffBalance> periodBalances;

    public TimeOffBalances(int balanceType, List<TimeOffBalance> periodBalances) {
        this.balanceType = balanceType;
        this.periodBalances = periodBalances;
    }

    public TimeOffBalance getBalanceByAccrualId(int id) {
        for (TimeOffBalance balance : periodBalances) {
            if (balance.accrualId == id) {
                return balance;
            }
        }
        return null;
    }

    public double getBalance(int id, String date) {
        TimeOffBalance balance = getBalanceByAccrualId(id);
        if (balance != null) {
            for (BalanceDate balanceDate : balance.dates) {
                if (balanceDate.date.equals(date)) {
                    return balanceDate.balance;
                }
            }
        }
        return 0;
    }

    public class TimeOffBalance {
        public final int accrualId;
        public final List<BalanceDate> dates;
        public final String startDate;
        public final String stopDate;

        public TimeOffBalance(int accrualId, List<BalanceDate> dates, String startDate, String stopDate) {
            this.accrualId = accrualId;
            this.dates = dates;
            this.startDate = startDate;
            this.stopDate = stopDate;
        }
    }

    @SuppressLint("SimpleDateFormat")
    public class BalanceDate implements Comparable<BalanceDate> {
        public final String date;
        public final double balance;

        public BalanceDate(String date, double balance) {
            this.date = date;
            this.balance = balance;
        }

        private DateTime getDateTime() {
            try {
                return new DateTime(new SimpleDateFormat("yyyy-MM-dd").parse(date));
            } catch (ParseException e) {
                Timber.e(e, e.getMessage());
                Crashlytics.logException(e);
            }
            return new DateTime();
        }

        public String getFormattedBalance() {
            return new DecimalFormat("0.##").format(balance);
        }

        public int compareTo(@NonNull BalanceDate balanceDate) {
            return this.getDateTime().compareTo(balanceDate.getDateTime());
        }
    }
}