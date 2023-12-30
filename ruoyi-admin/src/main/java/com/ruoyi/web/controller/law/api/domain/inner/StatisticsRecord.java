package com.ruoyi.web.controller.law.api.domain.inner;

/**
 * @author xiao.hu
 * @date 2023-12-30
 * @apiNote
 *
 * TODO 这里对应什么数据？
 */
public class StatisticsRecord {

    private Long id;
    private String name;
    private Integer total;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }
}
