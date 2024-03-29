package com.ruoyi.web.controller.elasticsearch.domain;

import java.util.List;

/**
 * @author xiao.hu
 * @date 2024-01-07
 * @apiNote
 */
public interface EsFields {

    Long getLawId();

    String getLawName();

    String getLawLevel();

    Integer[] getStatusArray();

    String[] getAuthorityArray();

    List<String> getTags();

    String getTermText();

    String[] getAuthorityProvinceArray();

    String[] getAuthorityCityArray();

    String getAuthorityDistrict();

    String getContentText();

    Long getAssociatedFileId();

    String getDocumentType();

    String getAssociatedFileName();

    String[] getDocumentNoArray();

    String[] getPublishRange();

    String[] getValidFromRange();

    Integer getStatus();
}
