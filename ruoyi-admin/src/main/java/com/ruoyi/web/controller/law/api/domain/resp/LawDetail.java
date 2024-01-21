package com.ruoyi.web.controller.law.api.domain.resp;

import com.ruoyi.web.controller.elasticsearch.domain.IntegralFields;

import java.util.List;
import java.util.Map;

/**
 * @author xiao.hu
 * @date 2023-12-30
 * @apiNote
 */
public class LawDetail {

    private String id;
    private String level;
    private String authority;
    private String publishAt;
    private String validFrom;
    private String status;
    private String documentNo;
    private String according;
    private String title;
    private List<IntegralFields> provisions;

    private Map<String, List<IntegralFields>> historyMap;
    private Map<Long, List<IntegralFields>> associatedFileMap;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public String getPublishAt() {
        return publishAt;
    }

    public void setPublishAt(String publishAt) {
        this.publishAt = publishAt;
    }

    public String getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(String validFrom) {
        this.validFrom = validFrom;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDocumentNo() {
        return documentNo;
    }

    public void setDocumentNo(String documentNo) {
        this.documentNo = documentNo;
    }

    public String getAccording() {
        return according;
    }

    public void setAccording(String according) {
        this.according = according;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<IntegralFields> getProvisions() {
        return provisions;
    }

    public void setProvisions(List<IntegralFields> provisions) {
        this.provisions = provisions;
    }

    public Map<String, List<IntegralFields>> getHistoryMap() {
        return historyMap;
    }

    public void setHistoryMap(Map<String, List<IntegralFields>> historyMap) {
        this.historyMap = historyMap;
    }

    public Map<Long, List<IntegralFields>> getAssociatedFileMap() {
        return associatedFileMap;
    }

    public void setAssociatedFileMap(Map<Long, List<IntegralFields>> associatedFileMap) {
        this.associatedFileMap = associatedFileMap;
    }
}
