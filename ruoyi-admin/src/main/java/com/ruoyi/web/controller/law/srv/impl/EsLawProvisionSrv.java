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
public class EsLawProvisionSrv extends AbstractEsSrv {
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
            return super.readConfig("classpath:elasticsearch/index_law_provision_mappings.json");
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
        return portalSrv.listProvisionByPage(pageNum, pageSize);
    }

    /**
     * 构造查询条件
     * @param esFields
     * @return
     */
    @Override
    public SearchSourceBuilder mustConditions(EsFields esFields) {
        IntegralFields condition = (IntegralFields) esFields;

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = super.makeCommonBoolQueryBuilder(condition);

        String title = condition.getTitle();
        if (StrUtil.isNotBlank(title)) {
            boolQueryBuilder.must(QueryBuilders.termQuery(IntegralFields.TITLE, title));
        }

        String termText = condition.getTermText();
        if (StrUtil.isNotBlank(termText)) {
            boolQueryBuilder.must(QueryBuilders.termQuery(IntegralFields.TERM_TEXT, termText));
        }

        searchSourceBuilder.query(boolQueryBuilder);
        return searchSourceBuilder;
    }
}
