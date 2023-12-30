package com.ruoyi.web.controller.law.api.domain.inner;

import java.util.List;

/**
 * @author xiao.hu
 * @date 2023-12-30
 * @apiNote
 */
public class Article {
    /**
     * 文章id
     * TODO  对应法律id
     */
    private Long id;
    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章标签
     */
    private String label;

    /**
     * 发布时间
     */
    private String publishAt;

    /**
     * 历史修正记录
     */
    private List<Provision> history;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getPublishAt() {
        return publishAt;
    }

    public void setPublishAt(String publishAt) {
        this.publishAt = publishAt;
    }

    public List<Provision> getHistory() {
        return history;
    }

    public void setHistory(List<Provision> history) {
        this.history = history;
    }
}
