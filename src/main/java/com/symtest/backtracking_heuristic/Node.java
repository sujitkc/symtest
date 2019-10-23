package com.symtest.backtracking_heuristic;

import java.util.Iterator;
import java.util.List;

/**
 * Node represents a node in the computation tree.
 */
public class Node {

    public String value;
    public List<Node> children;
    public int number;

    public Node(String value, List<Node> children, int number) {
        this.value = value;
        this.children = children;
        this.number = number;
    }

    /**
     * getChild is primarily called during tree construction to 
     * find the path along which the traversal / construction should happen.
     * @param value The value of the node
     * @return Node  The child node who's value corresponds to the input value (if any)
     */
    public Node getChild(String value) {
        for (Node child : children) {
            if (child.value.equals(value)) {
                return child;
            }
        }
        return null;
    }

    /**
     * isLeaf is a convenience function called during traversal.
     * @return boolean Indicates if the node is a leaf node or not.
     */
    public boolean isLeaf() {
        if (children == null || 
            children.size() == 0)
            return true;
        return false;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder(50);
        printTree(buffer, "", "");
        return buffer.toString();
    }

    /**
     * printTree is internally called by toString to recursively descent through the 
     * child nodes to construct the tree representation.
     * From @VasiliNovikov
     * https://stackoverflow.com/a/8948691/3624795
     */
    private void printTree(StringBuilder buffer, String prefix, String childrenPrefix) {
        buffer.append(prefix);
        buffer.append(value);
        buffer.append('\n');
        //for (int i = 0; i < level; i++) System.out.print("*");
        for (Iterator<Node> it = children.iterator(); it.hasNext();) {
            Node next = it.next();
            if (it.hasNext()) {
                next.printTree(buffer, childrenPrefix + "├── ", childrenPrefix + "│   ");
            } else {
                next.printTree(buffer, childrenPrefix + "└── ", childrenPrefix + "    ");
            }
        }
    }
}