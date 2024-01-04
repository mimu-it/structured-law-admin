package com.ruoyi.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 关联文件对象 sl_associated_file
 * 
 * @author xiao.hu
 * @date 2024-01-04
 */
public class SlAssociatedFile extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
    private String id;

    /** 关联的法律库官网的法律id */
    private String lawOriginalId;

    /** 关联的法律id */
    @Excel(name = "关联的法律id")
    private String lawId;

    /** 名称 */
    @Excel(name = "名称")
    private String name;

    /** 类型,对标官网的type字段 */
    @Excel(name = "类型,对标官网的type字段")
    private String documentType;

    /** 正文 */
    @Excel(name = "正文")
    private String content;

    public void setId(String id) 
    {
        this.id = id;
    }

    public String getId() 
    {
        return id;
    }
    public void setLawOriginalId(String lawOriginalId) 
    {
        this.lawOriginalId = lawOriginalId;
    }

    public String getLawOriginalId() 
    {
        return lawOriginalId;
    }
    public void setLawId(String lawId) 
    {
        this.lawId = lawId;
    }

    public String getLawId() 
    {
        return lawId;
    }
    public void setName(String name) 
    {
        this.name = name;
    }

    public String getName() 
    {
        return name;
    }
    public void setDocumentType(String documentType) 
    {
        this.documentType = documentType;
    }

    public String getDocumentType() 
    {
        return documentType;
    }
    public void setContent(String content) 
    {
        this.content = content;
    }

    public String getContent() 
    {
        return content;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("lawOriginalId", getLawOriginalId())
            .append("lawId", getLawId())
            .append("name", getName())
            .append("documentType", getDocumentType())
            .append("content", getContent())
            .toString();
    }
}
