package com.ruoyi.web.controller.law.api.domain.inner;

import java.util.List;

/**
 * @author xiao.hu
 * @date 2023-12-30
 * @apiNote
 *
 * TODO 这里对应什么数据？
 */
public class Statistics {

    private Long id;
    private String name;
    private List<StatisticsRecord> list;

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

    public List<StatisticsRecord> getList() {
        return list;
    }

    public void setList(List<StatisticsRecord> list) {
        this.list = list;
    }
}
