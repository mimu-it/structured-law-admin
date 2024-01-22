package com.ruoyi.web.controller.law.api.domain.resp;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xiao.hu
 * @date 2024-01-22
 * @apiNote
 */
public class LawCountHitsGroup {

    private Map<String, Integer> groupMap = new HashMap<>();

    public Map<String, Integer> getGroupMap() {
        return groupMap;
    }

    public void put(String key, Integer count) {
        this.groupMap.put(key, count);
    }
}
