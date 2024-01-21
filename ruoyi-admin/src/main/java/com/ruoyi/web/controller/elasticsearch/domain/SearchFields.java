package com.ruoyi.web.controller.elasticsearch.domain;

/**
 * @author xiao.hu
 * @date 2024-01-19
 * @apiNote
 */
public class SearchFields {

    private String[] lawLevelArray;

    private String publishRange;

    private String validFromRange;

    private String[] documentNoArray;


    public String[] getLawLevelArray() {
        return lawLevelArray;
    }

    public void setLawLevelArray(String[] lawLevelArray) {
        this.lawLevelArray = lawLevelArray;
    }

    public String getPublishRange() {
        return publishRange;
    }

    public void setPublishRange(String publishRange) {
        this.publishRange = publishRange;
    }

    public String getValidFromRange() {
        return validFromRange;
    }

    public void setValidFromRange(String validFromRange) {
        this.validFromRange = validFromRange;
    }

    public String[] getDocumentNoArray() {
        return documentNoArray;
    }

    public void setDocumentNoArray(String[] documentNoArray) {
        this.documentNoArray = documentNoArray;
    }
}
