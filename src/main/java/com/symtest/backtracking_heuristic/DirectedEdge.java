package com.symtest.backtracking_heuristic;

/**
 * DirectedEdge represents the directed edges in the target graph
 * that'll be passed to MST.
 */
public class DirectedEdge implements Comparable<DirectedEdge> {

    String from, to;
    double weight;

    public DirectedEdge(String fromNode, String toNode, double weight) {
        from = fromNode;
        to = toNode;
        this.weight = weight;
    }

    public int compareTo(DirectedEdge e) {
        if (weight > e.weight) {
            return 1;
        } else if (weight < e.weight) {
            return -1;
        } else {
            return 0;
        }
    }

    public String toString() {
        return from + "->" + to + "(" + Double.toString(weight) + ")";
    }
}