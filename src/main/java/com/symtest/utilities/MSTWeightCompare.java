package com.symtest.utilities;

import java.util.Comparator;

import com.symtest.backtracking_heuristic.ComputationTree;
import com.symtest.backtracking_heuristic.ComputationTreeHandler;;


public class MSTWeightCompare implements Comparator<Entry> {
    public int compare(Entry a, Entry b) {
        ComputationTree compTree = ComputationTreeHandler.get();
        String aTargets = String.join(",", a.getRemainingTargets().toArray(new String[0]));
        String bTargets = String.join(",", b.getRemainingTargets().toArray(new String[0]));
        double aScore = compTree.computeOrderingWithBacktrackingDestination(a.getEdge().getId(), aTargets);
        double bScore = compTree.computeOrderingWithBacktrackingDestination(b.getEdge().getId(), bTargets);
        // Descending sort
        return (int)(bScore - aScore);

    }
}