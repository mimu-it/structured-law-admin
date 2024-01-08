package com.ruoyi.web.controller.elasticsearch.domain;

import java.util.Date;

/**
 * @author xiao.hu
 * @date 2024-01-07
 * @apiNote
 */
public interface EsFields {

    String getEsDocId();

    Long getLawId();

    Long getProvisionId();

    String getLawName();

    String getSubtitle();

    String getLawLevel();

    String getTags();

    Date getPublish();

    Date getValidFrom();

    String getTitle();

    String getTermText();

    String getAuthority();

    Integer getStatus();

    String getTitleNumber();

    String getPreface();

    String getAuthorityProvince();

    String getAuthorityCity();

    String getAuthorityDistrict();

    String getDocumentNo();

    String getContentText();

    Long getAssociatedFileId();

    String getDocumentType();

    String getAssociatedFileName();

    String[] getDocumentNoArray();

    String getPublishRange();

    String getValidFromRange();
}
