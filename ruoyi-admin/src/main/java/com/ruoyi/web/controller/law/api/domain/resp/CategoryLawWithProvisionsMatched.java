package com.ruoyi.web.controller.law.api.domain.resp;

import com.ruoyi.web.controller.law.api.domain.inner.Statistics;

/**
 * @author xiao.hu
 * @date 2024-03-29
 * @apiNote
 */
public class CategoryLawWithProvisionsMatched {

    private LawWithProvisionsMatchedPage lawWithProvisionsMatchedPage;
    /**
     * 统计
     */
    private Statistics statistics;

    public LawWithProvisionsMatchedPage getLawWithProvisionsMatchedPage() {
        return lawWithProvisionsMatchedPage;
    }

    public void setLawWithProvisionsMatchedPage(LawWithProvisionsMatchedPage lawWithProvisionsMatchedPage) {
        this.lawWithProvisionsMatchedPage = lawWithProvisionsMatchedPage;
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public void setStatistics(Statistics statistics) {
        this.statistics = statistics;
    }
}
