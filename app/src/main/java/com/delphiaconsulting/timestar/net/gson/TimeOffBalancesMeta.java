package com.delphiaconsulting.timestar.net.gson;

import java.util.List;

/**
 * Represents metadata pertaining to accrual balances like accrual codes and pay types
 */
public class TimeOffBalancesMeta {
    public final List<AccrualCode> accrualCodes;
    public final List<PayType> accrualPayTypes;
    public final int balanceType;
    public final int allowNegativeBalance;

    public TimeOffBalancesMeta(List<AccrualCode> accrualCodes, List<PayType> accrualPayTypes, int balanceType, int allowNegativeBalance) {
        this.accrualCodes = accrualCodes;
        this.accrualPayTypes = accrualPayTypes;
        this.balanceType = balanceType;
        this.allowNegativeBalance = allowNegativeBalance;
    }

    public String getAccrualNameById(int id) {
        for (AccrualCode accrualCode : accrualCodes) {
            if (accrualCode.accrualId == id) {
                return accrualCode.name;
            }
        }
        return "-";
    }

    public int getAccrualIdByPayTypeCode(String nameCode) {
        for (PayType name : accrualPayTypes) {
            if (name.payType.equals(nameCode)) {
                return name.accrualId;
            }
        }
        return -1;
    }

    public class PayType {
        public final int accrualId;
        public final String payType;

        public PayType(int accrualId, String payType) {
            this.accrualId = accrualId;
            this.payType = payType;
        }
    }

    public class AccrualCode {
        public final int accrualId;
        public final String name;

        public AccrualCode(int accrualId, String name) {
            this.accrualId = accrualId;
            this.name = name;
        }
    }
}
