package com.symtest.utilities;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import com.symtest.graph.IEdge;
import com.symtest.backtracking_heuristic.ComputationTree;
import com.symtest.backtracking_heuristic.ComputationTreeHandler;;

public class Entry implements Comparable<Entry>{
    private IEdge edge;
    private Boolean branch;
    private Set<IEdge> remainingTargets;

    public Entry(IEdge edge, Boolean branch, Set<IEdge> remainingTargets) {
        this.edge = edge;
        this.branch = branch;
        this.remainingTargets = new HashSet<IEdge>(remainingTargets);
    }

    public Entry(IEdge edge, Boolean branch) {
        this.edge = edge;
        this.branch = branch;
        this.remainingTargets = new HashSet<IEdge>();
    }

    public IEdge getEdge() {
        return this.edge;
    }

    public Boolean getBranch() {
        return this.branch;
    }

    public Set<IEdge> getRemainingTargets() {
        return remainingTargets;
    }

    public void setRemainingTargets(Set<IEdge> remainingTargets) {
        this.remainingTargets = new HashSet<IEdge>(remainingTargets);
    }

    @Override
    public String toString() {
        String s = "(" + edge.toString() + ", " + branch.toString() + ", " + remainingTargets.toString() + ")\n";
        return s;
    }

    @Override
    public int compareTo(Entry o) {
        String targets = String.join(",", remainingTargets.toArray(new String[0]));
        String otherTargets = String.join(",", o.remainingTargets.toArray(new String[0]));

        ComputationTree compTree = ComputationTreeHandler.get();
        double currScore = compTree.computeCompositeWeight(edge.getId(), targets);
        double otherScore = compTree.computeCompositeWeight(o.edge.getId(), otherTargets);

        // This will sort based on score from least to highest. 
        // return (int)(currScore - otherScore);

        // This will sort based on score from highest to least.
        return (int)(otherScore - currScore);
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Entry)) {
            return false;
        }

        Entry e = (Entry) o;
        if (edge == e.edge && branch == e.branch &&
            remainingTargets.equals(e.remainingTargets)) {
            return true;
        }
        return false;
    }

}