package com.ruoyi.web.controller.law.api.domain.inner;

import com.ruoyi.web.controller.elasticsearch.domain.IntegralFields;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xiao.hu
 * @date 2024-03-03
 * @apiNote
 */
public class ProvisionHistory {

    private String lawName;
    private Map<String, List<IntegralFields>> termTitleHistory;

    public String getLawName() {
        return lawName;
    }

    public void setLawName(String lawName) {
        this.lawName = lawName;
    }

    public Map<String, List<IntegralFields>> getTermTitleHistory() {
        return termTitleHistory;
    }

    public void addTermTitleHistory(String termTitle, List<IntegralFields> history) {
        if(this.termTitleHistory == null) {
            this.termTitleHistory = new HashMap<>();
        }

        this.termTitleHistory.put(termTitle, history);
    }

    public void addTermTitleHistory(String termTitle, IntegralFields historyItem) {
        if(this.termTitleHistory == null) {
            this.termTitleHistory = new HashMap<>();
        }

        List<IntegralFields> list = this.termTitleHistory.get(termTitle);
        if(list == null) {
            list = new ArrayList<>();
            this.termTitleHistory.put(termTitle, list);
        }

        list.add(historyItem);
    }
}
