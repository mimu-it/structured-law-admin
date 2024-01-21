package com.ruoyi.web.controller.law.api.domain.resp;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xiao.hu
 * @date 2024-01-08
 * @apiNote
 */
public class LawSearchHitsGroup {

    /**
     * 效力级别level和命中值的对应关系
     */
    private Map<String, IntegralHits> law = new HashMap<>();

    public Map<String, IntegralHits> getLaw() {
        return law;
    }

    public void putLaw(String key, IntegralHits hits) {
        this.law.put(key, hits);
    }
}
