package com.ruoyi.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 增量更新日志对象 sl_incremental_log
 * 
 * @author xiao.hu
 * @date 2024-05-17
 */
public class SlIncrementalLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** id */
    private String id;

    /** 全文 */
    @Excel(name = "全文")
    private String logContent;

    public void setId(String id) 
    {
        this.id = id;
    }

    public String getId() 
    {
        return id;
    }
    public void setLogContent(String logContent) 
    {
        this.logContent = logContent;
    }

    public String getLogContent() 
    {
        return logContent;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("logContent", getLogContent())
            .toString();
    }
}
