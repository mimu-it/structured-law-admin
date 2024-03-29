package com.ruoyi.web.controller.law.api.domain.inner;

import com.ruoyi.web.controller.elasticsearch.domain.IntegralFields;

import java.util.*;

/**
 * @author xiao.hu
 * @date 2024-03-27
 * @apiNote
 */
public class LawWithProvisionsMatched {

    private Long id;
    private String level;
    private String lawName;
    private String documentNo;
    private String lawNameOrigin;
    private String authority;
    private String authorityCity;
    private String authorityDistrict;
    private String authorityProvince;
    private String statusLabel;
    private Integer status;
    private Date publish;
    private Date validFrom;
    private List<IntegralFields> provisionList = new ArrayList<>();

    public void addProvision(IntegralFields item) {
        this.provisionList.add(item);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getLawNameOrigin() {
        return lawNameOrigin;
    }

    public void setLawNameOrigin(String lawNameOrigin) {
        this.lawNameOrigin = lawNameOrigin;
    }

    public String getLawName() {
        return lawName;
    }

    public void setLawName(String lawName) {
        this.lawName = lawName;
    }

    public String getDocumentNo() {
        return documentNo;
    }

    public void setDocumentNo(String documentNo) {
        this.documentNo = documentNo;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public String getAuthorityCity() {
        return authorityCity;
    }

    public void setAuthorityCity(String authorityCity) {
        this.authorityCity = authorityCity;
    }

    public String getAuthorityDistrict() {
        return authorityDistrict;
    }

    public void setAuthorityDistrict(String authorityDistrict) {
        this.authorityDistrict = authorityDistrict;
    }

    public String getAuthorityProvince() {
        return authorityProvince;
    }

    public void setAuthorityProvince(String authorityProvince) {
        this.authorityProvince = authorityProvince;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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

    public List<IntegralFields> getProvisionList() {
        return provisionList;
    }

    public void setProvisionList(List<IntegralFields> provisionList) {
        this.provisionList = provisionList;
    }
}
