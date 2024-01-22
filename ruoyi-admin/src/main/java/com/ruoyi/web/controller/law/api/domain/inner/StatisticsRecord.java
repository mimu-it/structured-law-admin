package com.ruoyi.web.controller.law.api.domain.inner;

/**
 * @author xiao.hu
 * @date 2023-12-30
 * @apiNote
 *
 * TODO 这里对应什么数据？
 */
public class StatisticsRecord {

    private String name;
    private Long total;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }
}
