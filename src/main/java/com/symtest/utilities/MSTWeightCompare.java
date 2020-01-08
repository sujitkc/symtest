package com.symtest.utilities;

import java.util.Comparator;
import java.util.Set;

import com.symtest.backtracking_heuristic.ComputationTree;
import com.symtest.backtracking_heuristic.ComputationTreeHandler;
import com.symtest.graph.IEdge;;


public class MSTWeightCompare implements Comparator<Entry> {
    public int compare(Entry a, Entry b) {
        ComputationTree compTree = ComputationTreeHandler.get();
        String aTargets = helper(a.getRemainingTargets());
        String bTargets = helper(b.getRemainingTargets());
        double aScore = compTree.computeOrderingWithBacktrackingDestination(a.getEdge().getId(), aTargets);
        double bScore = compTree.computeOrderingWithBacktrackingDestination(b.getEdge().getId(), bTargets);
        // Descending sort
        return (int)(bScore - aScore);

    }

    // FIX
    private String helper(Set<IEdge> r) {
        if (r.size() > 0) {
            StringBuilder nameBuilder = new StringBuilder();
        
            for (IEdge n : r) {
                nameBuilder.append(n).append(",");
            }
            nameBuilder.deleteCharAt(nameBuilder.length() - 1);
            return nameBuilder.toString();
        } else {
            return "";
        }
    }
}