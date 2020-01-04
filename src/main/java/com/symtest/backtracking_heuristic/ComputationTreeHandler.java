package com.symtest.backtracking_heuristic;

import java.io.IOException;

public class ComputationTreeHandler {
    private static ComputationTree compTree;

    public static void init(String path, String rootNode, String loopNode) throws IOException {
        compTree = new ComputationTree(path, rootNode, loopNode);
    }

    public static boolean isInitialized() {
        if (compTree == null) {
            return false;
        }
        return true;
    }

    public static ComputationTree get() {
        return compTree;
    }

}