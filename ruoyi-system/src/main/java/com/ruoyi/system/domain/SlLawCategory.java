package com.ruoyi.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 法库目录对象 sl_law_category
 * 
 * @author ruoyi
 * @date 2023-12-23
 */
public class SlLawCategory extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    private Long id;

    /** 目录名称 */
    @Excel(name = "目录名称")
    private String name;

    /** 文件夹路径 */
    @Excel(name = "文件夹路径")
    private String folder;

    /** 是否有子目录 */
    @Excel(name = "是否有子目录")
    private Long isSubFolder;

    /** 分类分组 */
    @Excel(name = "分类分组")
    private String categoryGroup;

    /** 排序序号 */
    @Excel(name = "排序序号")
    private Integer categoryOrder;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getName() 
    {
        return name;
    }
    public void setFolder(String folder) 
    {
        this.folder = folder;
    }

    public String getFolder() 
    {
        return folder;
    }
    public void setIsSubFolder(Long isSubFolder) 
    {
        this.isSubFolder = isSubFolder;
    }

    public Long getIsSubFolder() 
    {
        return isSubFolder;
    }
    public void setCategoryGroup(String categoryGroup) 
    {
        this.categoryGroup = categoryGroup;
    }

    public String getCategoryGroup() 
    {
        return categoryGroup;
    }
    public void setCategoryOrder(Integer categoryOrder)
    {
        this.categoryOrder = categoryOrder;
    }

    public Integer getCategoryOrder()
    {
        return categoryOrder;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("name", getName())
            .append("folder", getFolder())
            .append("isSubFolder", getIsSubFolder())
            .append("categoryGroup", getCategoryGroup())
            .append("categoryOrder", getCategoryOrder())
            .toString();
    }


    public static final String NAME = "name";
    public static final String FOLDER = "folder";
    public static final String IS_SUB_FOLDER = "is_sub_folder";
    public static final String CATEGORY_GROUP = "category_group";
    public static final String CATEGORY_ORDER = "category_order";


    /*public static void main(String[] args) {
        List<String> list =  BaseEntity.generateConstants(new SlLawCategory());
        list.forEach((item) -> {
            System.out.println(item);
        });
    }*/
}
