package com.symtest.backtracking_heuristic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.PriorityQueue;

/**
 * MST represents the Minimum Spanning Tree of all
 * the target edges.
 */

public class MST {
    PriorityQueue<DirectedEdge> pq;
    Set<String> visited;
    List<DirectedEdge> tree;
    
    public MST(List<DirectedEdge> edges) {
        invertEdgeWeight(edges);
        pq = new PriorityQueue<DirectedEdge>(edges);
        visited = new HashSet<String>();
        tree = new ArrayList<DirectedEdge>();
        computeMinimumSpanningTree();
    }

    /**
     * invertEdgeWeight inverts the edge weight to ensure that
     * MST algorithm works as expected with the target weights.
     * @param edges List of edges
     */
    void invertEdgeWeight(List<DirectedEdge> edges) {
        for (DirectedEdge e : edges) {
            e.weight = 1/e.weight;
        }
    }

    /**
     * computeMinimumSpanningTree uses Prim's algorithm to compute 
     * the MST from the target edge graph.
     */
    void computeMinimumSpanningTree() {
        while (!pq.isEmpty()) {
            DirectedEdge e = pq.remove();
            if (visited.contains(e.from) && visited.contains(e.to)) {
                continue;
            }
            visited.add(e.from);
            visited.add(e.to);
            tree.add(e);
        }
    }

    public double computeCumulativeWeight() {
        double sum = 0;
        for (DirectedEdge e : tree) {
            sum += e.weight;
        }
        return sum;
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        for (DirectedEdge e : tree) {
            s.append(e.toString());
            s.append("\n");
        }
        return s.toString();
    }
}

