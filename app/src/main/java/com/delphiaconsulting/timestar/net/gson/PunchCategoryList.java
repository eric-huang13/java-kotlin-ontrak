package com.delphiaconsulting.timestar.net.gson;

import java.util.List;

public class PunchCategoryList {
    public final int allowTransfers;
    public final List<PunchCategory> punchCategories;

    public PunchCategoryList(int allowTransfers, List<PunchCategory> punchCategories) {
        this.allowTransfers = allowTransfers;
        this.punchCategories = punchCategories;
    }

    public class PunchCategory {
        public final String description;
        public final long punchCategory;
        public final int defaultOrgLevelsFlag;
        public final int webPunchFlag;
        public final String name;

        public PunchCategory(String description, long punchCategory, int defaultOrgLevelsFlag, int webPunchFlag, String name) {
            this.description = description;
            this.punchCategory = punchCategory;
            this.defaultOrgLevelsFlag = defaultOrgLevelsFlag;
            this.webPunchFlag = webPunchFlag;
            this.name = name;
        }
    }
}