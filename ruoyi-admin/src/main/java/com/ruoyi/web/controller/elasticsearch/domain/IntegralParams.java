package com.ruoyi.web.controller.elasticsearch.domain;

import java.util.List;

/**
 * @author xiao.hu
 * @date 2024-03-02
 * @apiNote
 */
public class IntegralParams implements EsFields {

    private Long lawId;
    private String lawName;
    private String[] termTitleArray;
    private String termText;
    private List<String> tags;

    private String lawLevel;
    private String[] LawLevelArray;

    private String[] publishRange;
    private String[] validFromRange;

    private String[] documentNoArray;

    private Integer[] statusArray;
    private String[] authorityArray;

    private String[] authorityProvinceArray;

    private String[] authorityCityArray;

    private String authorityDistrict;

    private String contentText;

    private String documentType;

    private Long associatedFileId;
    private String associatedFileName;

    private Integer status;

    @Override
    public String getLawName() {
        return lawName;
    }

    public void setLawName(String lawName) {
        this.lawName = lawName;
    }

    @Override
    public String getTermText() {
        return termText;
    }

    public void setTermText(String termText) {
        this.termText = termText;
    }

    @Override
    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String[] getTermTitleArray() {
        return termTitleArray;
    }

    public void setTermTitleArray(String[] termTitleArray) {
        this.termTitleArray = termTitleArray;
    }

    public String[] getLawLevelArray() {
        return LawLevelArray;
    }

    public void setLawLevelArray(String[] lawLevelArray) {
        LawLevelArray = lawLevelArray;
    }

    @Override
    public String[] getPublishRange() {
        return publishRange;
    }

    public void setPublishRange(String[] publishRange) {
        this.publishRange = publishRange;
    }

    @Override
    public String[] getValidFromRange() {
        return validFromRange;
    }

    public void setValidFromRange(String[] validFromRange) {
        this.validFromRange = validFromRange;
    }

    @Override
    public String[] getDocumentNoArray() {
        return documentNoArray;
    }

    public void setDocumentNoArray(String[] documentNoArray) {
        this.documentNoArray = documentNoArray;
    }

    @Override
    public String getLawLevel() {
        return lawLevel;
    }

    public void setLawLevel(String lawLevel) {
        this.lawLevel = lawLevel;
    }

    @Override
    public Integer[] getStatusArray() {
        return statusArray;
    }

    public void setStatusArray(Integer[] statusArray) {
        this.statusArray = statusArray;
    }

    @Override
    public String[] getAuthorityArray() {
        return authorityArray;
    }

    public void setAuthorityArray(String[] authorityArray) {
        this.authorityArray = authorityArray;
    }

    @Override
    public String getAuthorityDistrict() {
        return authorityDistrict;
    }

    public void setAuthorityDistrict(String authorityDistrict) {
        this.authorityDistrict = authorityDistrict;
    }

    @Override
    public Long getLawId() {
        return lawId;
    }

    public void setLawId(Long lawId) {
        this.lawId = lawId;
    }

    @Override
    public String getContentText() {
        return contentText;
    }

    public void setContentText(String contentText) {
        this.contentText = contentText;
    }

    @Override
    public Long getAssociatedFileId() {
        return associatedFileId;
    }

    public void setAssociatedFileId(Long associatedFileId) {
        this.associatedFileId = associatedFileId;
    }

    @Override
    public String getAssociatedFileName() {
        return associatedFileName;
    }

    public void setAssociatedFileName(String associatedFileName) {
        this.associatedFileName = associatedFileName;
    }

    @Override
    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    @Override
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String[] getAuthorityProvinceArray() {
        return authorityProvinceArray;
    }

    public void setAuthorityProvinceArray(String[] authorityProvinceArray) {
        this.authorityProvinceArray = authorityProvinceArray;
    }

    @Override
    public String[] getAuthorityCityArray() {
        return authorityCityArray;
    }

    public void setAuthorityCityArray(String[] authorityCityArray) {
        this.authorityCityArray = authorityCityArray;
    }
}
