package com.ruoyi.web.controller.law.srv.impl;

import cn.hutool.core.bean.BeanUtil;
import com.github.pagehelper.Page;
import com.ruoyi.web.controller.elasticsearch.domain.EsFields;
import com.ruoyi.web.controller.elasticsearch.domain.IntegralFields;
import com.ruoyi.web.controller.elasticsearch.domain.LawFields;
import com.ruoyi.web.controller.law.srv.AbstractEsSrv;
import com.ruoyi.web.controller.law.srv.PortalSrv;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;

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

        searchSourceBuilder.query(boolQueryBuilder);
        return searchSourceBuilder;
    }
}
