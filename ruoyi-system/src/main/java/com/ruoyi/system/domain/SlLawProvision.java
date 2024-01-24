package com.ruoyi.system.domain;


import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 法律条款对象 sl_law_provision
 * 
 * @author xiao.hu
 * @date 2023-12-23
 */
public class SlLawProvision extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** id */
    private Long id;

    /** 所属法律 */
    @Excel(name = "所属法律")
    private Long lawId;

    /** 条款标题 */
    @Excel(name = "条款标题")
    private String title;

    /** 数字条款标题 */
    @Excel(name = "数字条款标题")
    private String titleNumber;

    @Excel(name = "标签，对于刑法来说，就是'组织诈骗罪'之类的")
    private String tags;

    /** 条款正文 */
    @Excel(name = "条款正文")
    private String termText;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setLawId(Long lawId) 
    {
        this.lawId = lawId;
    }

    public Long getLawId() 
    {
        return lawId;
    }
    public void setTitle(String title) 
    {
        this.title = title;
    }

    public String getTitle() 
    {
        return title;
    }
    public void setTermText(String termText) 
    {
        this.termText = termText;
    }

    public String getTermText() 
    {
        return termText;
    }

    public String getTitleNumber() {
        return titleNumber;
    }

    public void setTitleNumber(String titleNumber) {
        this.titleNumber = titleNumber;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("lawId", getLawId())
            .append("title", getTitle())
                .append("titleNumber", getTitleNumber())
                .append("tags", getTags())
            .append("termText", getTermText())
            .toString();
    }


    public static final String ID = "id";
    public static final String LAW_ID = "law_id";
    public static final String TITLE = "title";
    public static final String TITLE_NUMBER = "title_number";
    public static final String TAGS = "tags";
    public static final String TERM_TEXT = "term_text";

    /*public static void main(String[] args) {
        List<String> list =  BaseEntity.generateConstants(new SlLawProvision());
        list.forEach(System.out::println);
    }*/
}
