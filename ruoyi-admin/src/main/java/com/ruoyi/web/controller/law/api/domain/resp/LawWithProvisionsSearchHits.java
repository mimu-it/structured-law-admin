package com.ruoyi.web.controller.law.api.domain.resp;

import com.ruoyi.web.controller.elasticsearch.domain.IntegralFields;
import com.ruoyi.web.controller.law.api.domain.inner.LawWithProvisionsMatched;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author xiao.hu
 * @date 2024-03-17
 * @apiNote
 */
public class LawWithProvisionsSearchHits {
    private String lawName;
    private String authority;
    private String authorityCity;
    private String authorityDistrict;
    private String authorityProvince;
    private String statusLabel;
    private Integer status;
    private Date publish;
    private Date validFrom;
    private LawSearchHits lawSearchHits;

    private List<LawWithProvisionsMatched> lawWithProvisionsMatched;
    private Map<Long, List<IntegralFields>> associatedFileMap;

    public String getLawName() {
        return lawName;
    }

    public void setLawName(String lawName) {
        this.lawName = lawName;
    }

    public LawSearchHits getLawSearchHits() {
        return lawSearchHits;
    }

    public void setLawSearchHits(LawSearchHits lawSearchHits) {
        List<IntegralFields> hits = lawSearchHits.getSearchHits();
        this.lawSearchHits = lawSearchHits;
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

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public List<LawWithProvisionsMatched> getLawWithProvisionsMatched() {
        return lawWithProvisionsMatched;
    }

    public void setLawWithProvisionsMatched(List<LawWithProvisionsMatched> lawWithProvisionsMatched) {
        this.lawWithProvisionsMatched = lawWithProvisionsMatched;
    }

    public Map<Long, List<IntegralFields>> getAssociatedFileMap() {
        return associatedFileMap;
    }

    public void setAssociatedFileMap(Map<Long, List<IntegralFields>> associatedFileMap) {
        this.associatedFileMap = associatedFileMap;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public Integer getStatus() {
        return status;
    }
}
