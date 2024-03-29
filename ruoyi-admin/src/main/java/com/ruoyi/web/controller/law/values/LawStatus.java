package com.ruoyi.web.controller.law.values;

/**
 * @author xiao.hu
 * @date 2024-03-17
 * @apiNote
 */
public enum LawStatus {

    /* 可能取值 */
    effective(1, "有效"),
    not_effective(3, "尚未生效"),
    modified(5, "已修改"),
    abolished(9, "已废止"),
    unknown(7, "未知"),
    none(0, "无");

    private final Integer key;
    private final String value;

    LawStatus(Integer key, String value) {
        this.key = key;
        this.value = value;
    }

    public Integer getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
