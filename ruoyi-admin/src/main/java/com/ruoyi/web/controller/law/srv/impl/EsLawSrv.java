package com.ruoyi.web.controller.law.srv.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.Page;
import com.ruoyi.system.service.ISlLawService;
import com.ruoyi.web.controller.elasticsearch.domain.EsFields;
import com.ruoyi.web.controller.elasticsearch.domain.IntegralFields;
import com.ruoyi.web.controller.elasticsearch.domain.LawFields;
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

/**
 * @author xiao.hu
 * @date 2024-01-05
 * @apiNote
 */
@Service
public class EsLawSrv extends AbstractEsSrv {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    PortalSrv portalSrv;

    @Autowired
    private ISlLawService slLawService;

    /**
     *
     * @return
     */
    @Override
    public String getMappingConfig() {
        try {
            /** 此代码无法在打成jar包的时候使用 File file = ResourceUtils.getFile("classpath:elasticsearch/index_law_mappings.json"); */
            return super.readConfig(super.getResourcePathPrefix() + "elasticsearch/index_law_mappings.json");
        } catch (IOException e) {
            logger.error("", e);
            throw new IllegalStateException(e);
        }
    }

    /**
     *
     * @return
     */
    @Override
    public int countData() {
        return slLawService.count();
    }

    /**
     *
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public Page<IntegralFields> listDataByPage(int pageNum, int pageSize) {
        return portalSrv.listLawByPage(pageNum, pageSize);
    }

    /**
     *
     * @param esFields
     * @return
     */
    @Override
    public SearchSourceBuilder mustConditions(EsFields esFields) {
        IntegralFields integralFields = (IntegralFields) esFields;

        LawFields lawFields = new LawFields();
        BeanUtil.copyProperties(integralFields, lawFields);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = super.makeCommonBoolQueryBuilder(lawFields);

        /** 因为在 EsLawProvisionSrv 中， 法律名和条款搜索是或者关系，所以lawName单独从 makeCommonBoolQueryBuilder 中拿出来*/
        String lawName = lawFields.getLawName();
        if (StrUtil.isNotBlank(lawName)) {
            boolQueryBuilder.must(QueryBuilders.matchPhraseQuery(IntegralFields.LAW_NAME, lawName));
        }

        searchSourceBuilder.query(boolQueryBuilder);
        return searchSourceBuilder;
    }
}
