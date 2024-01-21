package com.ruoyi.web.controller.law.api.domain.resp;

import java.util.Map;
import java.util.Objects;

/**
 * @author xiao.hu
 * @date 2024-01-19
 * @apiNote
 */
public class SuggestHits {

    private String text;
    private Map<String, Object> extraData;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Map<String, Object> getExtraData() {
        return extraData;
    }

    public void setExtraData(Map<String, Object> extraData) {
        this.extraData = extraData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SuggestHits that = (SuggestHits) o;
        return text.equals(that.text) &&
                Objects.equals(extraData, that.extraData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, extraData);
    }
}
