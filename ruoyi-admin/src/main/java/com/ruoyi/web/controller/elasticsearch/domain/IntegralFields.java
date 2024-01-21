package com.ruoyi.web.controller.elasticsearch.domain;

import java.util.Date;

/**
 * @author xiao.hu
 * @date 2023-12-26
 * @apiNote
 * 用于保存到 es 的宽表对象
 */
public class IntegralFields extends SearchFields implements EsFields{

    private String esDocId;
    private Long lawId;
    private Long provisionId;
    private Long associatedFileId;

    private String lawNameOrigin;
    private String lawName;
    private String associatedFileName;
    private String subtitle;
    private String lawLevel;
    private String authority;
    private String authorityProvince;
    private String authorityCity;
    private String authorityDistrict;
    private String tags;
    private Date publish;
    private Date validFrom;
    private Integer status;
    private String documentNo;
    private String documentType;
    private String title;
    private String titleNumber;
    private String preface;
    private String termText;
    private String contentText;

    @Override
    public String getEsDocId() {
        return esDocId;
    }

    public void setEsDocId(String esDocId) {
        this.esDocId = esDocId;
    }

    @Override
    public Long getLawId() {
        return lawId;
    }

    public void setLawId(Long lawId) {
        this.lawId = lawId;
    }

    @Override
    public Long getProvisionId() {
        return provisionId;
    }

    public void setProvisionId(Long provisionId) {
        this.provisionId = provisionId;
    }

    @Override
    public String getLawName() {
        return lawName;
    }

    public void setLawName(String lawName) {
        this.lawName = lawName;
    }

    @Override
    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    @Override
    public String getLawLevel() {
        return lawLevel;
    }

    public void setLawLevel(String lawLevel) {
        this.lawLevel = lawLevel;
    }

    @Override
    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    @Override
    public Date getPublish() {
        return publish;
    }

    public void setPublish(Date publish) {
        this.publish = publish;
    }

    @Override
    public Date getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getTermText() {
        return termText;
    }

    public void setTermText(String termText) {
        this.termText = termText;
    }

    @Override
    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    @Override
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String getTitleNumber() {
        return titleNumber;
    }

    public void setTitleNumber(String titleNumber) {
        this.titleNumber = titleNumber;
    }

    @Override
    public String getPreface() {
        return preface;
    }

    public void setPreface(String preface) {
        this.preface = preface;
    }

    @Override
    public String getAuthorityProvince() {
        return authorityProvince;
    }

    public void setAuthorityProvince(String authorityProvince) {
        this.authorityProvince = authorityProvince;
    }

    @Override
    public String getAuthorityCity() {
        return authorityCity;
    }

    public void setAuthorityCity(String authorityCity) {
        this.authorityCity = authorityCity;
    }

    @Override
    public String getAuthorityDistrict() {
        return authorityDistrict;
    }

    public void setAuthorityDistrict(String authorityDistrict) {
        this.authorityDistrict = authorityDistrict;
    }

    @Override
    public String getDocumentNo() {
        return documentNo;
    }

    public void setDocumentNo(String documentNo) {
        this.documentNo = documentNo;
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
    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    @Override
    public String getAssociatedFileName() {
        return associatedFileName;
    }

    public void setAssociatedFileName(String associatedFileName) {
        this.associatedFileName = associatedFileName;
    }

    public String getLawNameOrigin() {
        return lawNameOrigin;
    }

    public void setLawNameOrigin(String lawNameOrigin) {
        this.lawNameOrigin = lawNameOrigin;
    }

    public static final String LAW_ID = "law_id";
    public static final String PROVISION_ID = "provision_id";
    public static final String ASSOCIATED_FILE_ID = "associated_file_id";
    public static final String LAW_NAME = "law_name";
    public static final String SUBTITLE = "subtitle";
    public static final String LAW_LEVEL = "law_level";
    public static final String AUTHORITY = "authority";
    public static final String AUTHORITY_PROVINCE = "authority_province";
    public static final String AUTHORITY_CITY = "authority_city";
    public static final String AUTHORITY_DISTRICT = "authority_district";
    public static final String TAGS = "tags";
    public static final String PUBLISH = "publish";
    public static final String STATUS = "status";
    public static final String VALID_FROM = "valid_from";
    public static final String DOCUMENT_NO = "document_no";
    public static final String TITLE = "title";
    public static final String TITLE_NUMBER = "title_number";
    public static final String TERM_TEXT = "term_text";
    public static final String PREFACE = "preface";
    public static final String CONTENT_TEXT = "content_text";
    public static final String ASSOCIATED_FILE_NAME = "associated_file_name";
    public static final String DOCUMENT_TYPE = "document_type";


    /*public static void main(String[] args) {
        List<String> list =  BaseEntity.generateConstants(new IntegralProvision());
        list.forEach(System.out::println);
    }*/
}
