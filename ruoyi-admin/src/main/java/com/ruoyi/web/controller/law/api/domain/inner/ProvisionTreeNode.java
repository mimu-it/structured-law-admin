package com.ruoyi.web.controller.law.api.domain.inner;

import java.util.List;

/**
 * @author xiao.hu
 * @date 2024-01-23
 * @apiNote
 */
public class ProvisionTreeNode {

    private String label;
    private String parentPath;
    private String fullPath;
    private String termText;
    private String tags;
    private List<ProvisionTreeNode> children;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getParentPath() {
        return parentPath;
    }

    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

    public String getTermText() {
        return termText;
    }

    public void setTermText(String termText) {
        this.termText = termText;
    }

    public List<ProvisionTreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<ProvisionTreeNode> children) {
        this.children = children;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
}
