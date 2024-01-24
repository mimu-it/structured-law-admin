package com.ruoyi.web.controller.law.srv.impl;

import com.github.pagehelper.Page;
import com.ruoyi.system.service.ISlLawProvisionService;
import com.ruoyi.web.controller.elasticsearch.domain.EsFields;
import com.ruoyi.web.controller.elasticsearch.domain.IntegralFields;
import com.ruoyi.web.controller.law.srv.AbstractEsSrv;
import com.ruoyi.web.controller.law.srv.PortalSrv;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author xiao.hu
 * @date 2024-01-24
 * @apiNote
 */
@Service
public class EsLawProvisionTagsSrv extends AbstractEsSrv {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    PortalSrv portalSrv;

    @Autowired
    private ISlLawProvisionService slLawProvisionService;

    @Override
    public String getMappingConfig() {
        try {
            return super.readConfig(super.getResourcePathPrefix() + "elasticsearch/index_law_provision_tags_mappings.json");
        } catch (IOException e) {
            logger.error("", e);
            throw new IllegalStateException(e);
        }
    }

    @Override
    public int countData() {
        return slLawProvisionService.countTags();
    }

    @Override
    public Page<IntegralFields> listDataByPage(int pageNum, int pageSize) {
        return portalSrv.listProvisionTagsByPage(pageNum, pageSize);
    }

    @Override
    public SearchSourceBuilder mustConditions(EsFields condition) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        return searchSourceBuilder;
    }
}
