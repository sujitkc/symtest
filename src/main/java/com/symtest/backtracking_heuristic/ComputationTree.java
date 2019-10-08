package com.symtest.backtracking_heuristic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ComputationTree {

    private static double ALPHA = 1.0;

    Node root;
    Node loop;

    Map<String, Integer> totalPaths;
    Map<String, Integer> nodePairPaths;
    Map<String, Double> nodePairProbability;
    Map<String, Integer> nodePairLength;
    Map<String, Double> nodePairWeight;
    Set<Node> nodes;


    public ComputationTree(String testFilePath, String rootNode, String loopNode) throws IOException {
        this.root = new Node(rootNode, new ArrayList<Node>(), 0);
        this.loop = new Node(loopNode, new ArrayList<Node>(), 0);
        this.totalPaths = new HashMap<String, Integer>();
        this.nodePairPaths = new HashMap<String, Integer>();
        this.nodePairProbability = new HashMap<String, Double>();
        this.nodePairLength = new HashMap<String, Integer>();
        this.nodePairWeight = new HashMap<String, Double>();
        initializeComputationTree(testFilePath);
        computeProbability();
        computeLengths();
        computeWeights();
    }

    private void initializeComputationTree(String testFilePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(testFilePath));
        List<Node> trees = processData(lines);
        createComputationTree(trees);
        printTree(trees.get(0), "", "");
    }

    private List<Node> processData(List<String> testPaths) {
        List<Node> trees = new ArrayList<Node>();
        for (String testPath : testPaths) {
            String[] nodes = testPath.split(" ");
            computeNodePairPathCount(nodes);
            Node tree = createTree(nodes);
            trees.add(tree);
        }
        return trees;
    }

    private void computeNodePairPathCount(String[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            String n1 = nodes[i];
            this.totalPaths.put(n1, this.totalPaths.getOrDefault(n1, 0) + 1);
            for (int j = nodes.length-1; j > i; j--) {
                String n2 = nodes[j];
                String np = nodePair(n1, n2);
                this.nodePairPaths.put(np, this.nodePairPaths.getOrDefault(np, 0) + 1);
            }
        }
    }

    private String nodePair(String n1, String n2) {
        return n1 + "_" + n2;
    }

    private Node createTree(String[] nodes) {
        Node current = new Node(nodes[nodes.length - 1], new ArrayList<Node>(), 0);
        for (int i = nodes.length - 2; i >= 0; i--) {
            List<Node> children = new ArrayList<Node>();
            children.add(current);
            current = new Node(nodes[i], children, 0);
        }
        return current;
    }

    // From @VasiliNovikov
    // https://stackoverflow.com/a/8948691/3624795
    private void printTree(Node treeRoot, String prefix, String childrenPrefix) {
        if(treeRoot == null) {
            return;
        }

        System.out.println(prefix + treeRoot.value);
        //for (int i = 0; i < level; i++) System.out.print("*");
        for (Iterator<Node> it = treeRoot.children.iterator(); it.hasNext();) {
            Node next = it.next();
            if (it.hasNext()) {
                printTree(next, childrenPrefix + "├── ", childrenPrefix + "│   ");
            } else {
                printTree(next, childrenPrefix + "└── ", childrenPrefix + "    ");
            }
        }
    }

    private void createComputationTree(List<Node> trees) {
        for (Node treeP : trees) {
            traverseAndAttach(treeP, this.root);
        }
    }

    private void traverseAndAttach(Node treeP, Node compTreeP) {

        if (treeP.value != compTreeP.value) {
            new Exception("Something's wrong!!!");
        }

        if (treeP.children.size() == 0) {
            return;
        }

        treeP = treeP.children.get(0);
        Node childP = compTreeP.getChild(treeP.value);
        if (childP != null) {
            traverseAndAttach(treeP, childP);
        } else {
            compTreeP.children.add(treeP);
        }
    }

    private void computeProbability() {
        Set<String> nodes = totalPaths.keySet();
        for (String n1 : nodes) {
            for (String n2 : nodes) {
                if (!n1.equals(n2)) {
                    String np = nodePair(n1, n2);
                    double probability = this.nodePairPaths.getOrDefault(np, 0) / (this.totalPaths.get(n1) + 1.0);
                    this.nodePairProbability.put(np, probability);
                }
            }
        }
    }

    private void computeLengths() {
        Set<String> nodes = totalPaths.keySet();
        for (String node : nodes) {
            if (!node.equals(this.loop.value)) { // To avoid computation for the looping edge
                computeLengthInternal(this.root, node);
            }
        }

        /* Debugging: uncomment to see computed lengths
        Set<String> ns = nodePairLength.keySet();
        for (String node : ns) {
            System.out.println("Pair: " + node + "Length: " + Integer.toString(nodePairLength.get(node)));
        }
        */
    }

    // length of path passing through n touching target
    private int computeLengthInternal(Node n, String target) {
        int len = Integer.MIN_VALUE;
        if (n.value.equals(target)) {
            len = 0;
        }

        for (Node child : n.children) {
            len = Math.max(computeLengthInternal(child, target), len);
        }

        if (n.value.equals(this.loop.value)) {
            len++;
        } else {
            String np = nodePair(n.value, target);
            nodePairLength.put(np, 
                               Math.max(nodePairLength.getOrDefault(np, Integer.MIN_VALUE), len)); 
        }

        return len;
    }

    private void computeWeights() {
        Set<String> nodePair = nodePairProbability.keySet();
        for (String np : nodePair) {
            double weight = nodePairProbability.get(np) + ALPHA * nodePairLength.getOrDefault(np, 0);
            nodePairWeight.put(np, weight);
        }
    }

    public static void main(String[] args) throws IOException{
        ComputationTree ct = new ComputationTree("/Users/athul/src/symtest/src/main/java/com/symtest/backtracking_heuristic/sample.out", "e1", "e3");
        
    }

}