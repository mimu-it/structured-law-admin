package com.ruoyi.web.controller.elasticsearch.domain;

import java.util.Date;
import java.util.List;

/**
 * @author xiao.hu
 * @date 2024-01-07
 * @apiNote
 */
public class LawFields implements EsFields{
    private String esDocId;
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

    @Override
    public Long getProvisionId() {
        return null;
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

    @Override
    public List<String> getTags() {
        /** law 暂不涉及这个属性 */
        return null;
    }

    @Override
    public String getTag(){
        /** law 暂不涉及这个属性 */
        return null;
    }

    public void setLawLevel(String lawLevel) {
        this.lawLevel = lawLevel;
    }

    @Override
    public String getAuthority() {
        return authority;
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

    @Override
    public Date getPublish() {
        return publish;
    }

    public void setPublish(Date publish) {
        this.publish = publish;
    }

    @Override
    public String getPublishRange() {
        return publishRange;
    }

    public void setPublishRange(String publishRange) {
        this.publishRange = publishRange;
    }

    @Override
    public Date getValidFrom() {
        return validFrom;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getTermText() {
        return null;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    @Override
    public String getValidFromRange() {
        return validFromRange;
    }

    public void setValidFromRange(String validFromRange) {
        this.validFromRange = validFromRange;
    }

    @Override
    public Integer getStatus() {
        return status;
    }

    @Override
    public String getTitleNumber() {
        return null;
    }

    @Override
    public String getPreface() {
        return null;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String getDocumentNo() {
        return documentNo;
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

    public void setDocumentNoArray(String[] documentNoArray) {
        this.documentNoArray = documentNoArray;
    }


}
