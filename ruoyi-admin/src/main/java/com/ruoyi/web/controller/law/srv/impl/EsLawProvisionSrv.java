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

import javax.annotation.Resource;
import java.io.IOException;

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
            return super.readConfig(super.getResourcePathPrefix() + "elasticsearch/index_law_provision_mappings.json");
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
            boolQueryBuilder.must(QueryBuilders.termQuery(IntegralFields.TITLE + ".keyword", title));
        }

        BoolQueryBuilder shouldQuery = QueryBuilders.boolQuery();
        /** 因为在 EsLawProvisionSrv 中， 法律名和条款搜索是或者关系，所以lawName单独从 makeCommonBoolQueryBuilder 中拿出来*/
        String lawName = condition.getLawName();
        if (StrUtil.isNotBlank(lawName)) {
            shouldQuery.should(QueryBuilders.matchPhraseQuery(IntegralFields.LAW_NAME, lawName));
        }

        String termText = condition.getTermText();
        if (StrUtil.isNotBlank(termText)) {
            shouldQuery.should(QueryBuilders.termQuery(IntegralFields.TERM_TEXT, termText));
        }

        boolQueryBuilder.must(shouldQuery);

        searchSourceBuilder.query(boolQueryBuilder);
        return searchSourceBuilder;
    }
}
