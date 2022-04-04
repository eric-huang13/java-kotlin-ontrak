package com.delphiaconsulting.timestar.net.gson;

import java.util.List;

public class OrgDefaultList {
    public final List<OrgDefault> orgHierarchy;
    public final List<OrgDefault> orgLevelDefaults;

    public OrgDefaultList(List<OrgDefault> orgLevelDefaults, List<OrgDefault> orgHierarchy) {
        this.orgLevelDefaults = orgLevelDefaults;
        this.orgHierarchy = orgHierarchy;
    }

    public class OrgDefault {
        public final long depth;
        public final long orgLevelId;
        public final List<DefaultItem> defaults;

        public OrgDefault(long depth, long orgLevelId, List<DefaultItem> defaults) {
            this.depth = depth;
            this.orgLevelId = orgLevelId;
            this.defaults = defaults;
        }
    }

    public class DefaultItem {
        public final long orgLevelId;
        public final long depth;

        public DefaultItem(long orgLevelId, long depth) {
            this.orgLevelId = orgLevelId;
            this.depth = depth;
        }
    }
}