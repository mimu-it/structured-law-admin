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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    @Autowired
    private ISlLawProvisionService slLawProvisionService;

    private String urlRegex = "[\u4e00-\u9fa5]+法";
    private Pattern pattern = Pattern.compile(urlRegex);


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

    /**
     * 构造查询条件
     * @param esFields
     * @return
     */
    @Override
    public SearchSourceBuilder mustConditions(EsFields esFields) {
        IntegralParams integralParams = (IntegralParams) esFields;

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = super.makeCommonBoolQueryBuilder(integralParams);

        String[] termTitleArray = integralParams.getTermTitleArray();
        if (ArrayUtil.isNotEmpty(termTitleArray)) {
            BoolQueryBuilder boolQueryBuilderShould = QueryBuilders.boolQuery();
            for(String termTitle : termTitleArray) {
                boolQueryBuilderShould.should(QueryBuilders.matchPhraseQuery(IntegralFields.TITLE, termTitle));
            }

            boolQueryBuilder.must(boolQueryBuilderShould);
        }

        List<String> tags = integralParams.getTags();
        if (tags != null) {
            boolQueryBuilder.must(QueryBuilders.termsQuery(IntegralFields.TAGS + ".keyword", tags));
        }

        boolQueryBuilder.must(makeLawNameOrTermTextCondition(integralParams));

        searchSourceBuilder.query(boolQueryBuilder);
        return searchSourceBuilder;
    }

    /**
     *
     * @param integralParams
     * @return
     */
    private BoolQueryBuilder makeLawNameOrTermTextCondition(IntegralParams integralParams) {
        BoolQueryBuilder shouldQuery = QueryBuilders.boolQuery();
        /** 因为在 EsLawProvisionSrv 中， 法律名和条款搜索是或者关系，所以lawName单独从 makeCommonBoolQueryBuilder 中拿出来*/
        String lawName = integralParams.getLawName();
        if (StrUtil.isNotBlank(lawName)) {
            /** 对于输入"婚姻 罪"，这样的词语应该进行分词，不适用于 matchPhraseQuery*/
            shouldQuery.should(QueryBuilders.matchQuery(IntegralFields.LAW_NAME, lawName));
        }

        /** 分词查询 termText */
        String termText = integralParams.getTermText();
        if (StrUtil.isNotBlank(termText)) {
            /** 如果遇到"刑法"，则LAW_NAME去找刑法  */
            Matcher matcher = pattern.matcher(termText);
            if(matcher.find()) {
                String lawNameText = matcher.group(0);
                String termTextPured = termText.replace(lawNameText, "");
                /** 拆分输入，遇到空格拆分多个关键字用于匹配 */
                String[] preciseKeywords = termTextPured.split("\\s+");

                BoolQueryBuilder mustQuery = QueryBuilders.boolQuery();
                mustQuery.must(QueryBuilders.matchQuery(IntegralFields.LAW_NAME, lawNameText));
                for(String preciseKeyword : preciseKeywords) {
                    mustQuery.must(QueryBuilders.matchPhraseQuery(IntegralFields.TERM_TEXT, preciseKeyword));
                }

                shouldQuery.should(mustQuery);
            }
            else {
                /** 拆分输入，遇到空格拆分多个关键字用于匹配 */
                String[] preciseKeywords = termText.split("\\s+");
                BoolQueryBuilder mustQuery = QueryBuilders.boolQuery();
                for(String preciseKeyword : preciseKeywords) {
                    mustQuery.must(QueryBuilders.matchPhraseQuery(IntegralFields.TERM_TEXT, preciseKeyword));
                }

                shouldQuery.should(mustQuery);
            }
        }
        return shouldQuery;
    }
}
