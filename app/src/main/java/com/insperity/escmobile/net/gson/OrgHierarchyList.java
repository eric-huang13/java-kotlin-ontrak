package com.insperity.escmobile.net.gson;

import java.util.List;
import java.util.Map;

public class OrgHierarchyList {
    public final List<ListItem> lists;
    public final List<MapItem> map;

    public OrgHierarchyList(List<ListItem> lists, List<MapItem> map) {
        this.lists = lists;
        this.map = map;
    }

    public class ListItem {
        public final List<Integer> list;
        public final String hash;

        public ListItem(List<Integer> list, String hash) {
            this.list = list;
            this.hash = hash;
        }
    }

    public class MapItem {
        public final long depth;
        public final Map<String, String> items;

        public MapItem(long depth, Map<String, String> items) {
            this.depth = depth;
            this.items = items;
        }
    }
}