package com.ruoyi.web.controller.law.srv;

import com.github.pagehelper.Page;
import com.ruoyi.web.controller.elasticsearch.domain.IntegralFields;

/**
 * @author xiao.hu
 * @date 2024-01-05
 * @apiNote
 */
public abstract class EsSrv {

    /**
     * 获取对应类型的索引配置文件
     * @return
     */
    public abstract String getMappingConfig();

    /**
     * 获取对应类型的
     * @return
     */
    public abstract Page<IntegralFields> listDataByPage(int pageNum, int pageSize);
}
