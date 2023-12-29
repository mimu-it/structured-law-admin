package com.ruoyi.system.domain;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 法律信息对象 sl_law
 * 
 * @author xiao.hu
 * @date 2023-12-23
 */
public class SlLaw extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    @Excel(name = "id", readConverterExp = "$column.readConverterExp()")
    private Long id;

    /** 目录id */
    @Excel(name = "目录id")
    private Long categoryId;

    /** 法律名称 */
    @Excel(name = "法律名称")
    private String name;

    /** 类型,对标level */
    @Excel(name = "类型,对标level")
    private String lawType;

    /** 文件名称 */
    @Excel(name = "制定机关")
    private String authority;

    /** 公布日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "公布日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date publish;

    /** 状态 */
    @Excel(name = "状态")
    private Integer status;

    /** 排序数字 */
    @Excel(name = "排序数字")
    private Integer lawOrder;

    /** 子标题 */
    @Excel(name = "子标题")
    private String subtitle;

    /** 生效日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "生效日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date validFrom;

    /** 版本 */
    @Excel(name = "版本")
    private Integer ver;

    /** 标签 */
    @Excel(name = "标签")
    private String tags;

    /** 前言 */
    @Excel(name = "前言")
    private String preface;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }

    public void setCategoryId(Long categoryId) 
    {
        this.categoryId = categoryId;
    }

    public Long getCategoryId() 
    {
        return categoryId;
    }
    public void setName(String name) 
    {
        this.name = name;
    }

    public String getName() 
    {
        return name;
    }
    public void setLawType(String lawType) 
    {
        this.lawType = lawType;
    }

    public String getLawType() 
    {
        return lawType;
    }

    public void setPublish(Date publish) 
    {
        this.publish = publish;
    }

    public Date getPublish() 
    {
        return publish;
    }

    public void setLawOrder(Integer lawOrder) 
    {
        this.lawOrder = lawOrder;
    }

    public Integer getLawOrder() 
    {
        return lawOrder;
    }
    public void setSubtitle(String subtitle) 
    {
        this.subtitle = subtitle;
    }

    public String getSubtitle() 
    {
        return subtitle;
    }
    public void setValidFrom(Date validFrom) 
    {
        this.validFrom = validFrom;
    }

    public Date getValidFrom() 
    {
        return validFrom;
    }
    public void setVer(Integer ver) 
    {
        this.ver = ver;
    }

    public Integer getVer() 
    {
        return ver;
    }
    public void setTags(String tags) 
    {
        this.tags = tags;
    }

    public String getTags() 
    {
        return tags;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getPreface() {
        return preface;
    }

    public void setPreface(String preface) {
        this.preface = preface;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("categoryId", getCategoryId())
            .append("name", getName())
            .append("lawType", getLawType())
            .append("authority", getAuthority())
            .append("publish", getPublish())
            .append("status", getStatus())
            .append("lawOrder", getLawOrder())
            .append("subtitle", getSubtitle())
            .append("validFrom", getValidFrom())
            .append("ver", getVer())
            .append("tags", getTags())
                .append("preface", getPreface())
            .toString();
    }

    public static final String ID = "id";
    public static final String CATEGORY_ID = "category_id";
    public static final String NAME = "name";
    public static final String LAW_TYPE = "law_type";
    public static final String AUTHORITY = "authority";
    public static final String PUBLISH = "publish";
    public static final String STATUS = "status";
    public static final String LAW_ORDER = "law_order";
    public static final String SUBTITLE = "subtitle";
    public static final String VALID_FROM = "valid_from";
    public static final String VER = "ver";
    public static final String TAGS = "tags";
    public static final String PREFACE = "preface";


    public static final String C__ID = "id";
    public static final String C__CATEGORY_ID = "categoryId";
    public static final String C__NAME = "name";
    public static final String C__LAW_TYPE = "lawType";
    public static final String C__AUTHORITY = "authority";
    public static final String C__PUBLISH = "publish";
    public static final String C__STATUS = "status";
    public static final String C__LAW_ORDER = "lawOrder";
    public static final String C__SUBTITLE = "subtitle";
    public static final String C__VALID_FROM = "validFrom";
    public static final String C__VER = "ver";
    public static final String C__TAGS = "tags";
    public static final String C__PREFACE = "preface";


    public static void main(String[] args) {
        List<String> list =  BaseEntity.generateConstants(new SlLaw(), true);
        list.forEach(System.out::println);
    }
}
