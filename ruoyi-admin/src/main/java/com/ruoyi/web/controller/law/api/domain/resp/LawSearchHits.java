package com.ruoyi.web.controller.law.api.domain.resp;

import com.ruoyi.web.controller.elasticsearch.domain.IntegralFields;
import com.ruoyi.web.controller.law.api.domain.inner.Article;
import com.ruoyi.web.controller.law.api.domain.inner.Statistics;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Map;

/**
 * @author xiao.hu
 * @date 2023-12-30
 * @apiNote
 */
@ApiModel(value="查询命中数据")
public class LawSearchHits {

    /**
     * 总条数
     */
    @ApiModelProperty(value="命中总条数")
    private Long total;

    @ApiModelProperty(value="页码")
    private int pageNum;

    @ApiModelProperty(value="每页条数")
    private int pageSize;

    /**
     * 分类名称
     */
    @ApiModelProperty(value="分类名称")
    private Integer category;

    /**
     * 命中的数据行
     */
    @ApiModelProperty(value="命中的数据行")
    private List<IntegralFields> searchHits;

    /**
     * 关联的文章
     */
    @ApiModelProperty(value="关联的文章")
    private Article article;

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public List<IntegralFields> getSearchHits() {
        return searchHits;
    }

    public void setSearchHits(List<IntegralFields> searchHits) {
        this.searchHits = searchHits;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

}
