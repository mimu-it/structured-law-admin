package com.ruoyi.web.controller.law.api.domain.resp;

import com.ruoyi.web.controller.law.api.domain.inner.Statistics;

import java.util.HashMap;
import java.util.List;
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
    private Map<String, List<LawWithProvisionsSearchHits>> law = new HashMap<>();

    /**
     * 统计
     */
    private Statistics statistics;

    /**
     *
     * @return
     */
    public Map<String, List<LawWithProvisionsSearchHits>> getLaw() {
        return law;
    }

    public void putLaw(String key, List<LawWithProvisionsSearchHits> hits) {
        this.law.put(key, hits);
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public void setStatistics(Statistics statistics) {
        this.statistics = statistics;
    }
}
