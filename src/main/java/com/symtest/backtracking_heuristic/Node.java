package com.symtest.backtracking_heuristic;

import java.util.List;

public class Node {

    public String value;
    public List<Node> children;
    public int number;

    public Node(String value, List<Node> children, int number) {
        this.value = value;
        this.children = children;
        this.number = number;
    }

    public Node getChild(String value) {
        for (Node child : this.children) {
            if (child.value.equals(value)) {
                return child;
            }
        }
        return null;
    }

    public boolean isLeaf() {
        if (children == null || 
            children.size() == 0)
            return true;
        return false;
    }
}