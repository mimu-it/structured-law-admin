package com.ruoyi.web.controller.law.api.domain.inner;

import java.util.List;

/**
 * @author xiao.hu
 * @date 2024-01-15
 * @apiNote
 */
public class TreeNode {

    private String label;
    private String nodeType;
    private TreeNode parent;
    private List<TreeNode> children;
    private Object extra;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public TreeNode getParent() {
        return parent;
    }

    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<TreeNode> children) {
        this.children = children;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public Object getExtra() {
        return extra;
    }

    public void setExtra(Object extra) {
        this.extra = extra;
    }
}
