package com.ruoyi.web.controller.law.api.domain.resp;

import com.ruoyi.web.controller.law.api.domain.inner.Article;
import com.ruoyi.web.controller.law.api.domain.inner.Statistics;

import java.util.Map;

/**
 * @author xiao.hu
 * @date 2023-12-30
 * @apiNote
 */
public class LawHits {

    /**
     * 总条数
     */
    private Integer total;


    /**
     * 分类名称
     */
    private Integer category;

    /**
     * 词条知识图谱，树型结构
     * TODO 数据从哪来？
     */
    private Map<String, Object> wordsMap;

    /**
     * 统计
     */
    private Statistics statistics;

    /**
     * 关联的文章
     */
    private Article article;

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public Map<String, Object> getWordsMap() {
        return wordsMap;
    }

    public void setWordsMap(Map<String, Object> wordsMap) {
        this.wordsMap = wordsMap;
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public void setStatistics(Statistics statistics) {
        this.statistics = statistics;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }
}
