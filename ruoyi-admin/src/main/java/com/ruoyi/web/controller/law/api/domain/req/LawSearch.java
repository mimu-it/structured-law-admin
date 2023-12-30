package com.ruoyi.web.controller.law.api.domain.req;

/**
 * @author xiao.hu
 * @date 2023-12-30
 * @apiNote
 */
public class LawSearch {

    private String title;
    private String content;
    private String documentNo;
    private String[] publishRange;
    private String[] validFromRange;
    /**
     * "部分失效","已被修改","失效","现行有效","尚未生效"
     */
    private String status;
    /**
     * ["法律","有关法律问题和重大问题的决定","法律解释","工作答复","工作文件","行政法规解释"]
     */
    private String level;

    /**
     * ["征集完","征集中"]
     * TODO 数据从哪来？
     */
    private String collectStatus;

    /**
     * 页码
     */
    private Integer pageNo;

    /**
     * 每页条数
     */
    private Integer pageSize;

    /**
     * 立法资料类别的id
     * TODO 数据从哪来？  爬虫爬取过程中新增而来？ 爬取哪个值？
     */
    private Long[] classification;

    /**
     * 发布机关
     */
    private String[] authority;

    /**
     * 分类
     */
    private String[] category;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDocumentNo() {
        return documentNo;
    }

    public void setDocumentNo(String documentNo) {
        this.documentNo = documentNo;
    }

    public String[] getPublishRange() {
        return publishRange;
    }

    public void setPublishRange(String[] publishRange) {
        this.publishRange = publishRange;
    }

    public String[] getValidFromRange() {
        return validFromRange;
    }

    public void setValidFromRange(String[] validFromRange) {
        this.validFromRange = validFromRange;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getCollectStatus() {
        return collectStatus;
    }

    public void setCollectStatus(String collectStatus) {
        this.collectStatus = collectStatus;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Long[] getClassification() {
        return classification;
    }

    public void setClassification(Long[] classification) {
        this.classification = classification;
    }

    public String[] getAuthority() {
        return authority;
    }

    public void setAuthority(String[] authority) {
        this.authority = authority;
    }

    public String[] getCategory() {
        return category;
    }

    public void setCategory(String[] category) {
        this.category = category;
    }
}
