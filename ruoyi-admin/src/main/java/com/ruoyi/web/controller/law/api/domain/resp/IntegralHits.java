package com.ruoyi.web.controller.law.api.domain.resp;

import com.ruoyi.web.controller.elasticsearch.domain.IntegralFields;
import com.ruoyi.web.controller.law.api.domain.inner.ProvisionHistory;
import com.ruoyi.web.controller.law.api.domain.resp.LawSearchHits;

import java.util.List;
import java.util.Map;

/**
 * @author xiao.hu
 * @date 2024-01-07
 * @apiNote
 */
public class IntegralHits {

    private LawSearchHits hits;
    private Map<String, ProvisionHistory> history;
    private Map<Long, List<IntegralFields>> associatedFileMap;

    public LawSearchHits getHits() {
        return hits;
    }

    public void setHits(LawSearchHits hits) {
        this.hits = hits;
    }

    public Map<String, ProvisionHistory> getHistory() {
        return history;
    }

    public void setHistory(Map<String, ProvisionHistory> history) {
        this.history = history;
    }

    public Map<Long, List<IntegralFields>> getAssociatedFileMap() {
        return associatedFileMap;
    }

    public void setAssociatedFileMap(Map<Long, List<IntegralFields>> associatedFileMap) {
        this.associatedFileMap = associatedFileMap;
    }
}
