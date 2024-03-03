package com.ruoyi.web.controller.law.api.domain.inner;

import java.util.Objects;

/**
 * @author xiao.hu
 * @date 2024-03-03
 * @apiNote
 */
public class LawProvision {

    private String lawName;
    private String termTitle;

    public String getLawName() {
        return lawName;
    }

    public void setLawName(String lawName) {
        this.lawName = lawName;
    }

    public String getTermTitle() {
        return termTitle;
    }

    public void setTermTitle(String termTitle) {
        this.termTitle = termTitle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LawProvision that = (LawProvision) o;
        return lawName.equals(that.lawName) &&
                termTitle.equals(that.termTitle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lawName, termTitle);
    }
}
