package com.ruoyi.web.controller.law.srv.impl;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.Page;
import com.ruoyi.system.service.ISlLawProvisionService;
import com.ruoyi.web.controller.elasticsearch.domain.EsFields;
import com.ruoyi.web.controller.elasticsearch.domain.IntegralFields;
import com.ruoyi.web.controller.elasticsearch.domain.IntegralParams;
import com.ruoyi.web.controller.law.srv.AbstractEsSrv;
import com.ruoyi.web.controller.law.srv.PortalSrv;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * @author xiao.hu
 * @date 2024-03-20
 * @apiNote
 */
@Service
public class EsLawProvisionSpecificSrv extends AbstractEsSrv {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    PortalSrv portalSrv;

    @Autowired
    private ISlLawProvisionService slLawProvisionService;

    @Override
    public String getMappingConfig() {
        try {
            return super.readConfig(super.getResourcePathPrefix() + "elasticsearch/index_law_provision_mappings.json");
        } catch (IOException e) {
            logger.error("", e);
            throw new IllegalStateException(e);
        }
    }

    @Override
    public int countData() {
        return slLawProvisionService.count();
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

    @Override
    public SearchSourceBuilder mustConditions(EsFields esFields) {
        IntegralParams integralParams = (IntegralParams) esFields;

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = super.makeCommonBoolQueryBuilder(integralParams);

        List<String> tags = integralParams.getTags();
        if (tags != null) {
            boolQueryBuilder.must(QueryBuilders.termsQuery(IntegralFields.TAGS + ".keyword", tags));
        }

        boolQueryBuilder.must(makeLawNameAndTermTitleCondition(integralParams));

        searchSourceBuilder.query(boolQueryBuilder);
        return searchSourceBuilder;
    }

    /**
     *
     * @param integralParams
     * @return
     */
    private BoolQueryBuilder makeLawNameAndTermTitleCondition(IntegralParams integralParams) {
        BoolQueryBuilder orQuery = QueryBuilders.boolQuery();
        /** 因为在 EsLawProvisionSrv 中， 法律名和条款搜索是或者关系，所以lawName单独从 makeCommonBoolQueryBuilder 中拿出来*/
        String lawName = integralParams.getLawName();
        if (StrUtil.isNotBlank(lawName)) {
            /** 对于输入"婚姻 罪"，这样的词语应该进行分词，不适用于 matchPhraseQuery*/
            orQuery.should(QueryBuilders.termQuery(IntegralFields.LAW_NAME, lawName));
        }

        /** 分词查询 termTitle */
        String[] termTitles = integralParams.getTermTitleArray();
        if (termTitles != null && termTitles.length > 0) {
            BoolQueryBuilder shouldQuery = QueryBuilders.boolQuery();
            for(String termTitle : termTitles) {
                shouldQuery.should(QueryBuilders.matchPhraseQuery(IntegralFields.TERM_TEXT, lawName + " " + termTitle));
            }

            orQuery.should(shouldQuery);
        }
        return orQuery;
    }
}
