package com.ruoyi.web.controller.law.srv.impl;

import com.github.pagehelper.Page;
import com.ruoyi.web.controller.elasticsearch.domain.IntegralFields;
import com.ruoyi.web.controller.law.srv.EsSrv;
import com.ruoyi.web.controller.law.srv.PortalSrv;
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
public class EsLawProvisionSrv extends EsSrv {
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
            File file = ResourceUtils.getFile("classpath:elasticsearch/index_law_provision_mappings.json");
            return new String(Files.readAllBytes(file.toPath()));
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
}
