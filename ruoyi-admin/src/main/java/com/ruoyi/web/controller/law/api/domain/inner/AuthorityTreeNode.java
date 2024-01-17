package com.ruoyi.web.controller.law.api.domain.inner;

import java.util.List;

/**
 * @author xiao.hu
 * @date 2024-01-15
 * @apiNote
 */
public class AuthorityTreeNode {

    private String label;

    private List<AuthorityTreeNode> children;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<AuthorityTreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<AuthorityTreeNode> children) {
        this.children = children;
    }
}
