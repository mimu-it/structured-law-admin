package com.ruoyi.web.controller.law.api.domain.resp;

import com.ruoyi.web.controller.law.api.domain.inner.TreeNode;

import java.util.List;

/**
 * @author xiao.hu
 * @date 2023-12-30
 * @apiNote
 */
public class LawSearchConditionOptions {

    private List<String> authorityOptions;
    private List<TreeNode> authorityTree;
    private List<String> levelOptions;
    /**
     * 立法资料类别的id
     * TODO 数据从哪来？  爬虫爬取过程中新增而来？ 爬取哪个值？
     */
    private List<Long> classificationOptions;
    private List<String> statusOptions;
    private List<String> collectStatusOptions;

    public List<String> getAuthorityOptions() {
        return authorityOptions;
    }

    public void setAuthorityOptions(List<String> authorityOptions) {
        this.authorityOptions = authorityOptions;
    }

    public List<String> getLevelOptions() {
        return levelOptions;
    }

    public void setLevelOptions(List<String> levelOptions) {
        this.levelOptions = levelOptions;
    }

    public List<Long> getClassificationOptions() {
        return classificationOptions;
    }

    public void setClassificationOptions(List<Long> classificationOptions) {
        this.classificationOptions = classificationOptions;
    }

    public List<String> getStatusOptions() {
        return statusOptions;
    }

    public void setStatusOptions(List<String> statusOptions) {
        this.statusOptions = statusOptions;
    }

    public List<String> getCollectStatusOptions() {
        return collectStatusOptions;
    }

    public void setCollectStatusOptions(List<String> collectStatusOptions) {
        this.collectStatusOptions = collectStatusOptions;
    }

    public List<TreeNode> getAuthorityTree() {
        return authorityTree;
    }

    public void setAuthorityTree(List<TreeNode> authorityTree) {
        this.authorityTree = authorityTree;
    }
}
