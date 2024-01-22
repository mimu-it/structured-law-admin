package com.ruoyi.web.controller.law.api.domain.inner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xiao.hu
 * @date 2023-12-30
 * @apiNote
 *
 */
public class Statistics {

    private Map<String, List<StatisticsRecord>> statisticsMap = new HashMap<>();

    public Map<String, List<StatisticsRecord>> getStatisticsMap() {
        return statisticsMap;
    }

    public void put(String statType, List<StatisticsRecord> list) {
        this.statisticsMap.put(statType, list);
    }
}
