package com.ruoyi.web.controller.law.srv.impl;

import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.Page;
import com.ruoyi.web.controller.elasticsearch.domain.EsFields;
import com.ruoyi.web.controller.elasticsearch.domain.IntegralFields;
import com.ruoyi.web.controller.law.srv.AbstractEsSrv;
import com.ruoyi.web.controller.law.srv.PortalSrv;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @author xiao.hu
 * @date 2024-01-05
 * @apiNote
 */
@Service
public class EsLawAssociatedFileSrv extends AbstractEsSrv {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    PortalSrv portalSrv;

    /**
     *
     * @return
     */
    @Override
    public String getMappingConfig() {
        try {
            return super.readConfig(super.getResourcePathPrefix() + "elasticsearch/index_law_associated_file_mappings.json");
        } catch (IOException e) {
            logger.error("", e);
            throw new IllegalStateException(e);
        }
    }

    /**
     *
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public Page<IntegralFields> listDataByPage(int pageNum, int pageSize) {
        return portalSrv.listAssociatedFileByPage(pageNum, pageSize);
    }

    /**
     *
     * @param condition
     * @return
     */
    @Override
    public SearchSourceBuilder mustConditions(EsFields condition) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        String contentText = condition.getContentText();
        if (StrUtil.isNotBlank(contentText)) {
            boolQueryBuilder.must(QueryBuilders.matchQuery(IntegralFields.CONTENT_TEXT, contentText));
        }

        String documentType = condition.getDocumentType();
        if (StrUtil.isNotBlank(documentType)) {
            boolQueryBuilder.must(QueryBuilders.termQuery(IntegralFields.DOCUMENT_TYPE, documentType));
        }

        String associatedFileName = condition.getAssociatedFileName();
        if (StrUtil.isNotBlank(associatedFileName)) {
            boolQueryBuilder.must(QueryBuilders.matchPhraseQuery(IntegralFields.ASSOCIATED_FILE_NAME, associatedFileName));
        }

        Long lawId = condition.getLawId();
        if (lawId != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery(IntegralFields.LAW_ID, String.valueOf(lawId)));
        }

        String termText = condition.getTermText();
        if (StrUtil.isNotBlank(termText)) {
            /** 同时在 content_text 和 associated_file_name 查询内容 */
            boolQueryBuilder.must(QueryBuilders.multiMatchQuery(termText, IntegralFields.CONTENT_TEXT, IntegralFields.ASSOCIATED_FILE_NAME));
        }

        searchSourceBuilder.query(boolQueryBuilder);
        return searchSourceBuilder;
    }
}
