package com.symtest.backtracking_heuristic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 
 * ComputationTree represents the main class that creates the
 * computation tree from the test runs and computes statistics 
 * about the tree.
 */
public class ComputationTree {

    // Used in weight calculation.
    private static double ALPHA = 1.0;

    Node root;
    // The looping node
    Node loop;

    // Number of paths through a node
    Map<String, Integer> pathsThroughNode;
    // Number of paths through a node pair
    Map<String, Integer> pathsThroughNodePair;
    // Probability of a path through a node pair
    Map<String, Double> pathProbability;
    // Length of a path through a node pair
    Map<String, Integer> pathLength;
    // Weight of a node pair
    Map<String, Double> nodePairWeight;

    // Minimum spanning tree of target edges
    MST mst;


    /**
     * ComputationTree constructor initializes the tree and computes that statistics.
     * @param testFilePath Absolute path of the file containing test runs. 
     * @param rootNode Value of the root node.
     * @param loopNode Value of the looping node.
     * @throws IOException
     */
    public ComputationTree(String testFilePath, String rootNode, String loopNode) throws IOException {
        root = new Node(rootNode, new ArrayList<Node>(), 0);
        loop = new Node(loopNode, new ArrayList<Node>(), 0);
        pathsThroughNode = new HashMap<String, Integer>();
        pathsThroughNodePair = new HashMap<String, Integer>();
        pathProbability = new HashMap<String, Double>();
        pathLength = new HashMap<String, Integer>();
        nodePairWeight = new HashMap<String, Double>();
        initializeComputationTree(testFilePath);
        computeProbability();
        computeLengths();
        computeWeights();
    }

    /**
     * initializeComputationTree loads the file and constructs the computation tree from the 
     * data.
     * @param testRunsFilePath Absolute path of the file containing the test runs.
     * @throws IOException
     */
    private void initializeComputationTree(String testRunsFilePath) throws IOException {
        List<String> runs = Files.readAllLines(Paths.get(testRunsFilePath));
        List<Node> paths = parseRunsToPaths(runs);
        createComputationTreeFromPaths(paths);
    }

    /**
     * parseRunsToPaths parses the file and returns a list with each test run.
     * Path level computation is also performed. 
     */
    private List<Node> parseRunsToPaths(List<String> testPaths) {
        List<Node> paths = new ArrayList<Node>();
        for (String testPath : testPaths) {
            String[] pathNodes = testPath.split(" ");
            computePathStatistics(pathNodes);
            Node path = createPath(pathNodes);
            paths.add(path);
        }
        return paths;
    }

    /**
     * computePathStatistics computes the number of paths through a node and the 
     * number of paths through a node pair.
     * @param nodes Array of values representing a path.
     */
    private void computePathStatistics(String[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            String n1 = nodes[i];
            pathsThroughNode.put(n1, pathsThroughNode.getOrDefault(n1, 0) + 1);
            for (int j = nodes.length-1; j > i; j--) {
                String n2 = nodes[j];
                String np = createNodePair(n1, n2);
                pathsThroughNodePair.put(np, pathsThroughNodePair.getOrDefault(np, 0) + 1);
            }
        }
    }

    /**
     * createNodePair is a convenience fn to create node pair keys.
     */
    private static String createNodePair(String n1, String n2) {
        return n1 + "_" + n2;
    }

    /**
     * createPath creates a path (tree) from a list of nodes. 
     * The process starts at the end and works forward by adding each 
     * node as the child of the next node. 
     */
    private Node createPath(String[] pathNodes) {
        Node current = new Node(pathNodes[pathNodes.length - 1], new ArrayList<Node>(), 0);
        for (int i = pathNodes.length - 2; i >= 0; i--) {
            List<Node> children = new ArrayList<Node>();
            children.add(current);
            current = new Node(pathNodes[i], children, 0);
        }
        return current;
    }


    /**
     * createComputationTreeFromPaths melds the paths to form the computation tree.
     */
    private void createComputationTreeFromPaths(List<Node> paths) {
        for (Node path : paths) {
            traverseAndAttachPath(path, root);
        }
    }

    /**
     * traverseAndAttachPath traverses the current tree starting from the root and patches in the 
     * new path into the tree. 
     */
    private void traverseAndAttachPath(Node path, Node compTree) {
        if (path.value != compTree.value) {
            new Exception("Something's wrong!!!");
        }

        if (path.isLeaf()) {
            return;
        }

        path = path.children.get(0);
        Node child = compTree.getChild(path.value);
        if (child != null) {
            traverseAndAttachPath(path, child);
        } else {
            compTree.children.add(path);
        }
    }

    /**
     * computeProbability computes the node pair probability
     */
    private void computeProbability() {
        Set<String> nodes = pathsThroughNode.keySet();
        for (String n1 : nodes) {
            for (String n2 : nodes) {
                if (!n1.equals(n2)) {
                    String np = createNodePair(n1, n2);
                    double probability = pathsThroughNodePair.getOrDefault(np, 0) / (pathsThroughNode.get(n1) + 1.0);
                    pathProbability.put(np, probability);
                }
            }
        }
    }

    private void computeLengths() {
        Set<String> nodes = pathsThroughNode.keySet();
        for (String node : nodes) {
            if (!node.equals(loop.value)) { // To avoid computation for the looping edge
                computeLengthInternal(root, node);
            }
        }
        /* Debugging: uncomment to see computed lengths
        Set<String> ns = nodePairLength.keySet();
        for (String node : ns) {
            System.out.println("Pair: " + node + "Length: " + Integer.toString(nodePairLength.get(node)));
        }
        */
    }

    /**
     * computeLengthInternal computes the length of a path 
     * passing through node, touching target.
     */
    private int computeLengthInternal(Node node, String target) {
        int len = Integer.MIN_VALUE;
        if (node.value.equals(target)) {
            len = 0;
        }

        for (Node child : node.children) {
            len = Math.max(computeLengthInternal(child, target), len);
        }

        if (node.value.equals(loop.value)) {
            len++;
        } else {
            String np = createNodePair(node.value, target);
            pathLength.put(np, 
                           Math.max(pathLength.getOrDefault(np, Integer.MIN_VALUE), len)); 
        }

        return len;
    }

    /**
     * computeWeights computes the weight according to the formula:
     * weight(n1, n2) = Probability(n1, n2) + ALPHA * Length(n1, n2)
     */
    private void computeWeights() {
        Set<String> nodePair = pathProbability.keySet();
        for (String np : nodePair) {
            double weight = pathProbability.get(np) + ALPHA * pathLength.getOrDefault(np, 0);
            nodePairWeight.put(np, weight);
        }
    }

    /**
     * computeOrdering takes a list of target edges and computes 
     * a MST of those edges.
     */
    public void computeOrdering(String targetEdges) {
            String[] targets = targetEdges.split(" ");
            ArrayList<DirectedEdge> targetList = new ArrayList<DirectedEdge>();
            for (int i = 0; i < targets.length; ++i) {
                for (int j = 0; j < targets.length; ++j) {
                    if (i == j) { 
                        continue;
                    }
                    String np = createNodePair(targets[i], targets[j]);
                    DirectedEdge e = new DirectedEdge(targets[i], targets[j], nodePairWeight.getOrDefault(np, Double.MIN_VALUE));
                    if (e.weight - Double.MIN_VALUE > 1) {
                        targetList.add(e);
                    }
                }
            }
            mst = new MST(targetList);
    }

    /**
     * computeOrderingWithBacktrackingDestination takes a list of target edges and computes 
     * a MST of those edges.
     */
    public double computeOrderingWithBacktrackingDestination(String backtrackingDestination, String targetEdges) {
            String[] targets = targetEdges.split(" ");
            ArrayList<DirectedEdge> targetList = new ArrayList<DirectedEdge>();
            // Created a complete graph with all the target edges (which will be nodes in
            // this graph).
            for (int i = 0; i < targets.length; ++i) {
                for (int j = 0; j < targets.length; ++j) {
                    if (i == j) {
                        continue;
                    }
                    String np = createNodePair(targets[i], targets[j]);
                    DirectedEdge e = new DirectedEdge(targets[i], targets[j],
                            nodePairWeight.getOrDefault(np, Double.MIN_VALUE));
                    if (e.weight - Double.MIN_VALUE > 1) {
                        targetList.add(e);
                    }
                }
            }
            // Add weights from backtracking destination to each target edge.
            for (int i = 0; i < targets.length; ++i) {
                String np = createNodePair(backtrackingDestination, targets[i]);
                DirectedEdge e = new DirectedEdge(backtrackingDestination, targets[i],
                        nodePairWeight.getOrDefault(np, Double.MIN_VALUE));
                if (e.weight - Double.MIN_VALUE > 1) {
                    targetList.add(e);
                }
            }
            MST mst = new MST(targetList);
            return mst.computeCumulativeWeight();
    }

    /**
     * computeComposedWeight computes the composite weight for a given backtracking destination.
     * Composite weight = sum of edge weights from backtracking destination to each of the remaining
     *                    target edges.
     */
    public double computeCompositeWeight(String backtrackigDestination, String targetEdges) {
            String[] targets = targetEdges.split(" ");
            double composedWeight = 0;
            for (int i = 0; i < targets.length; ++i) {
                String np = createNodePair(backtrackigDestination, targets[i]);
                composedWeight += nodePairWeight.getOrDefault(np, Double.MIN_VALUE);
            }
            return composedWeight;
    }

    public static void main(String[] args) throws IOException{
        ComputationTree ct = new ComputationTree("/Users/athul/src/symtest/src/main/java/com/symtest/backtracking_heuristic/sample.out", "e1", "e3");
        System.out.println("Computation Tree:");
        System.out.println(ct.root);
        System.out.println("Edges in MST (with inverted weights):");
        ct.computeOrdering("e2 e4 e5 e7");
        System.out.println(ct.mst);
    }

}
