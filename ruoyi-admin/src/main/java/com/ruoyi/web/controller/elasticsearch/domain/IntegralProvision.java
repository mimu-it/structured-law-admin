package com.ruoyi.web.controller.elasticsearch.domain;

import java.util.Date;

/**
 * @author xiao.hu
 * @date 2023-12-26
 * @apiNote
 */
public class IntegralProvision {

    private Long lawId;
    private Long provisionId;
    private String folder;
    private String lawName;
    private String subtitle;
    private String lawType;
    private String authority;
    private String tags;
    private Date publish;
    private Integer status;
    private Date validFrom;
    private String title;
    private String titleNumber;
    private String preface;
    private String termText;

    public Long getLawId() {
        return lawId;
    }

    public void setLawId(Long lawId) {
        this.lawId = lawId;
    }

    public Long getProvisionId() {
        return provisionId;
    }

    public void setProvisionId(Long provisionId) {
        this.provisionId = provisionId;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public String getLawName() {
        return lawName;
    }

    public void setLawName(String lawName) {
        this.lawName = lawName;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getLawType() {
        return lawType;
    }

    public void setLawType(String lawType) {
        this.lawType = lawType;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Date getPublish() {
        return publish;
    }

    public void setPublish(Date publish) {
        this.publish = publish;
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTermText() {
        return termText;
    }

    public void setTermText(String termText) {
        this.termText = termText;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getTitleNumber() {
        return titleNumber;
    }

    public void setTitleNumber(String titleNumber) {
        this.titleNumber = titleNumber;
    }

    public String getPreface() {
        return preface;
    }

    public void setPreface(String preface) {
        this.preface = preface;
    }

    public static final String LAW_ID = "law_id";
    public static final String PROVISION_ID = "provision_id";
    public static final String FOLDER = "folder";
    public static final String LAW_NAME = "law_name";
    public static final String SUBTITLE = "subtitle";
    public static final String LAW_TYPE = "law_type";
    public static final String AUTHORITY = "authority";
    public static final String TAGS = "tags";
    public static final String PUBLISH = "publish";
    public static final String STATUS = "status";
    public static final String VALID_FROM = "valid_from";
    public static final String TITLE = "title";
    public static final String TITLE_NUMBER = "title_number";
    public static final String TERM_TEXT = "term_text";
    public static final String PREFACE = "preface";

    /*public static void main(String[] args) {
        List<String> list =  BaseEntity.generateConstants(new IntegralProvision());
        list.forEach(System.out::println);
    }*/
}
