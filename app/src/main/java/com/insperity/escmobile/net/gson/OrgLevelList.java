package com.insperity.escmobile.net.gson;

import java.util.List;

public class OrgLevelList {
    public final List<OrgLevel> orgLevelDepths;

    public OrgLevelList(List<OrgLevel> orgLevelDepths) {
        this.orgLevelDepths = orgLevelDepths;
    }

    public class OrgLevel {
        public final long depth;
        public final String name;
        public final List<OrgItem> items;

        public OrgLevel(long depth, String name, List<OrgItem> items) {
            this.depth = depth;
            this.name = name;
            this.items = items;
        }
    }

    public class OrgItem {
        public final long orgLevelId;
        public final String label;

        public OrgItem(long orgLevelId, String label) {
            this.orgLevelId = orgLevelId;
            this.label = label;
        }
    }
}