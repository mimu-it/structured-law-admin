package com.ruoyi.web.controller.law.api.domain.resp;

import com.ruoyi.web.controller.law.api.domain.inner.LawWithProvisionsMatched;

import java.util.List;

/**
 * @author xiao.hu
 * @date 2024-03-29
 * @apiNote
 */
public class LawWithProvisionsMatchedPage {

    private Long total;
    private int pageNum;
    private int pageSize;
    private int totalPage;

    private List<LawWithProvisionsMatched> list;

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
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

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public List<LawWithProvisionsMatched> getList() {
        return list;
    }

    public void setList(List<LawWithProvisionsMatched> list) {
        this.list = list;
    }
}
