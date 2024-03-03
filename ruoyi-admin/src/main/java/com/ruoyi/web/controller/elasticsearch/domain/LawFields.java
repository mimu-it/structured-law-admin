package com.ruoyi.web.controller.elasticsearch.domain;

import java.util.Date;
import java.util.List;

/**
 * @author xiao.hu
 * @date 2024-01-07
 * @apiNote
 */
public class LawFields implements EsFields{
    private Long lawId;
    private String lawName;
    private String subtitle;
    private String lawLevel;
    private String authority;
    private String authorityProvince;
    private String authorityCity;
    private String authorityDistrict;

    private Date publish;
    private String publishRange;

    private Date validFrom;
    private String validFromRange;

    private Integer status;

    private String documentNo;
    private String[] documentNoArray;

    @Override
    public Long getLawId() {
        return lawId;
    }

    public void setLawId(Long lawId) {
        this.lawId = lawId;
    }

    @Override
    public String getLawName() {
        return lawName;
    }

    public void setLawName(String lawName) {
        this.lawName = lawName;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    @Override
    public String getLawLevel() {
        return lawLevel;
    }

    @Override
    public Integer[] getStatusArray() {
        return null;
    }

    @Override
    public String[] getAuthorityArray() {
        return null;
    }

    @Override
    public List<String> getTags() {
        /** law 暂不涉及这个属性 */
        return null;
    }

    public void setLawLevel(String lawLevel) {
        this.lawLevel = lawLevel;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
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

    public void setPublish(Date publish) {
        this.publish = publish;
    }

    public void setPublishRange(String publishRange) {
        this.publishRange = publishRange;
    }

    @Override
    public String getTermText() {
        return null;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    public void setValidFromRange(String validFromRange) {
        this.validFromRange = validFromRange;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String getContentText() {
        return null;
    }

    @Override
    public Long getAssociatedFileId() {
        return null;
    }

    @Override
    public String getDocumentType() {
        return null;
    }

    @Override
    public String getAssociatedFileName() {
        return null;
    }

    public void setDocumentNo(String documentNo) {
        this.documentNo = documentNo;
    }

    @Override
    public String[] getDocumentNoArray() {
        return documentNoArray;
    }

    @Override
    public String[] getPublishRange() {
        return new String[0];
    }

    @Override
    public String[] getValidFromRange() {
        return new String[0];
    }

    public void setDocumentNoArray(String[] documentNoArray) {
        this.documentNoArray = documentNoArray;
    }


}
