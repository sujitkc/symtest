package com.symtest.tester;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Logger;

import com.symtest.Solver.SolverResult;
import com.symtest.cfg.ICFEdge;
import com.symtest.cfg.ICFG;
import com.symtest.cfg.ICFGDecisionNode;
import com.symtest.cfg.ICFGNode;
import com.symtest.exceptions.UnSatisfiableException;
import com.symtest.expression.IExpression;
import com.symtest.expression.IIdentifier;
import com.symtest.expression.Variable;
import com.symtest.graph.IEdge;
import com.symtest.graph.IGraph;
import com.symtest.graph.INode;
import com.symtest.graph.IPath;
import com.symtest.heuristics.ApplyHeuristics;
import com.symtest.mygraph.Path;
import com.symtest.set.SET;
import com.symtest.set.SETBasicBlockNode;
import com.symtest.set.SETNode;
import com.symtest.utilities.Pair;

public abstract class SymTest {

	abstract Stack<Pair<IEdge, Boolean>> backtrack(Stack<Pair<IEdge, Boolean>> stck);

	protected static final int MAXIMUM_ITERATIONS = 10;
	public ICFG mCFG;
	public CFGToGraphConvertor mConvertor;
	public IGraph mGraph;
	public Set<ICFEdge> mTargets;
	public ICFGNode mTarget;
	public SET set;
	public Set<ApplyHeuristics> heuristics;

	protected static final Logger logger = Logger
			.getLogger(SymTest.class.getName());

	
	/**
	 * Initialises the cfg and the targets to be processed.
	 * 
	 * @param cfg
	 * @param targets
	 */
	public SymTest(ICFG cfg, Set<ICFEdge> targets) {
		this(cfg, targets, null);
	}

	
	/**
	 * This constructor is to be used to add heuristics.
	 * @param cfg
	 * @param targets
	 * @param heuristics
	 */
	public SymTest(ICFG cfg, Set<ICFEdge> targets, Set<ApplyHeuristics> heuristics) {
		this.mCFG = cfg;
		this.mTargets = targets;
		this.mTarget = this.mCFG.getStopNode();
		this.mConvertor = new CFGToGraphConvertor(this.mCFG);
		if (heuristics != null) {
			this.heuristics = heuristics;
		}
	}

	
	/**
	 * This part holds the core algorithm of SymTest
	 * It performs a series of steps as follows:
	 * Find the shortest path(syntactically)
	 * Construct the SET
	 * Check if it is solvable(semantically feasible), then find a satisfiable solution.
	 * Else if heuristics are available, try the heuristics and return a solution if possible.
	 * Else find the longest viable prefix
	 * 		Update the stack
	 * 		Backtrack to the last decision and change the decision taken (if else edge was taken earlier, try for the other edge;
	 * 																	 If both edges were already explored, repeat the process) 
	 * Repeat the process with the new preferred edge. 
	 * @return
	 */
	public TestSequence generateTestSequence() {
		TestSequence testseq = null;
		try {
			
			//EXTRA
			Set<ICFEdge> terminalEdgeSet = new HashSet<ICFEdge>();
			terminalEdgeSet.add(mCFG.getStopNode().getOutgoingEdge());
			Set<IEdge> terminalTarget = convertTargetEdgesToGraphEdges(terminalEdgeSet);
			
			Set<IEdge> targets = convertTargetEdgesToGraphEdges(this.mTargets);
			Stack<Pair<IEdge, Boolean>> stack = new Stack<Pair<IEdge, Boolean>>();
			// Initialise the stack with start edge
			IEdge startEdge = this.mConvertor.getGraphEdge(mCFG.getStartNode()
					.getOutgoingEdgeList().get(0));
			
			stack.push(new Pair<IEdge, Boolean>(startEdge, true));
			ArrayList<IEdge> prefix = new ArrayList<IEdge>();

			IPath completePath = new Path(mGraph);
			Set<IEdge> currentTargets = new HashSet<IEdge>(targets);
			while ((!stack.isEmpty()) && !(stack.peek().getFirst().equals(startEdge) && !stack.peek()
					.getSecond())) {
				// Obtain the path that traverses the targets.
				IPath path;
				if (!hasEncounteredMaximumIterations(completePath)) {
					FindCFPathAlgorithm algorithm = new FindCFPathAlgorithm(
							this.mGraph, currentTargets,
							this.mConvertor.getGraphNode(this.mTarget));

					if (stack.size() != 1) {

						path = algorithm.findCFPath(stack.peek().getFirst()
								.getHead(), currentTargets);
					} else {
						prefix.add(startEdge);
						path = algorithm.findCFPath(stack.peek().getFirst()
								.getHead(), currentTargets);
					}
				} else {
					// If maximum iterations are done, it is only an empty path
					// that gets added
					path = new Path(mGraph);
//					FindCFPathAlgorithm algorithm = new FindCFPathAlgorithm(
//							this.mGraph, terminalTarget,
//							this.mConvertor.getGraphNode(this.mTarget));
//					path = algorithm.findLongestAcyclicPath(stack.peek().getFirst()
//								.getHead(), currentTargets);
//					System.out.println("FINAL PATH : " + path);
				}
				completePath.setPath(addprefix(prefix, path.getPath()));
				ArrayList<ICFEdge> cfPath = convertPathEdgesToCFGEdges(completePath);

				// Construct the Symbolic Execution Tree
				set = SymTestUtil.getSET(cfPath, this.mCFG);
				// Solve the predicate
				SolverResult solution;
				
				logger.fine("Complete cfPath: " + cfPath);
//				if (currentTargets.isEmpty()) {
					try {

						solution = SymTestUtil.solveSequence(set);
						return (this.convert(set.getLeafNodes().iterator().next(),
								solution));
					} catch (UnSatisfiableException e) {
						System.out.println("Unsatisfiable");
					}
//				}

				// Add heuristics
				if (this.heuristics != null) {
					for (ApplyHeuristics heuristic : heuristics) {
						try {
							solution = heuristic.performHeuristics(mGraph,
									mTargets, completePath, mCFG, mConvertor);
							return (this.convert(set.getLeafNodes().iterator()
									.next(), solution));

						} catch (UnSatisfiableException e) {
							e.printStackTrace();
						}
					}
				}
				if (!hasEncounteredMaximumIterations(completePath)) {
					logger.fine("Finding Longest Viable Prefix");

					// Get Longest Viable Prefix(LVP)
					int satisfiableIndex = SymTestUtil
							.getLongestSatisfiablePrefix(cfPath, mCFG);
					logger.finer("Satisfiable index: " + satisfiableIndex);
					logger.finer("Complete path size: " + completePath.getSize());
					logger.finer("path size: " + path.getSize());
					List<IEdge> satisfiablePrefix = new ArrayList<IEdge>();
					//TODO Figure out what this does
					satisfiablePrefix.addAll(completePath.getPath().subList(
							(completePath.getPath().size() - 1)
									- path.getPath().size(),
							satisfiableIndex + 2));

					updatestack(stack, satisfiablePrefix);
					prefix.clear();

					prefix.addAll(completePath.getPath().subList(
							0,
							completePath.getPath().indexOf(
									stack.peek().getFirst())));
				} else {
					prefix.clear();
					prefix.addAll(completePath.getPath().subList(
							0,
							completePath.getPath().lastIndexOf(
									stack.peek().getFirst())));
					logger.finer("Complete path: " + completePath);
				}

				backtrack(stack);
				if (!stack.isEmpty()) {
					// Add the updated edge
					if (stack.peek().getFirst().getTail().getId() == prefix.get(prefix.size()-1).getHead().getId()) {
						prefix.add(stack.peek().getFirst());
					} else {
						int index = -1;
						for (int i = prefix.size()-1; i >= 0; i--) {
							if (stack.peek().getFirst().getTail().getId() == prefix.get(i).getHead().getId()) {
								index = i;
								break;
							}
						}

						prefix.subList(index+1, prefix.size()).clear();
						prefix.add(stack.peek().getFirst());
					}
				}

				currentTargets.removeAll(prefix);
				
				logger.finest("Stack: " + Arrays.toString(stack.toArray()));

			}
			System.out.println("Unsatisfiable finally");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return testseq;
	}

	/**
	 * This function pushes only the outgoing edge of decision node contained in the path into the stack. 
	 * @param stack
	 * @param path that hets added newly
	 */
	protected void updatestack(Stack<Pair<IEdge, Boolean>> stack, List<IEdge> path) {
		for (IEdge e : path.subList(0, path.size())) {
			// Push only decision node's edges
			if (this.mConvertor.getCFEdge(e).getTail() instanceof ICFGDecisionNode) {
				stack.push(new Pair<IEdge, Boolean>(e, true));
			}
			
			//stack.push(new Pair <IEdge, Boolean> (e, true));
		}

	}

	/**
	 * Checks if the maximum iterations are explored. This avoids infinite loops.
	 * The number of iterations are hardcoded as a constant. 
	 * @param completePath
	 * @return
	 */
	protected boolean hasEncounteredMaximumIterations(IPath completePath) {
		int count = 0;
		if (!mTarget.getOutgoingEdgeList().isEmpty()) {
		count = Collections.frequency(completePath.getPath(),
				mConvertor.getGraphEdge(mTarget.getOutgoingEdgeList().get(0)));
		logger.finer("Count: " + count);
		if (count >= MAXIMUM_ITERATIONS)
			return true;
		else
			return false;
		} return false;
	}

	/**
	 * Concatenates the new path with the prefix to create the complete path, to be fed to the SEE(Symbolic Execution Engine)
	 * @param prefix
	 * @param path
	 * @return complete path
	 */
	protected List<IEdge> addprefix(ArrayList<IEdge> prefix, ArrayList<IEdge> path) {
		List<IEdge> completePath = new ArrayList<IEdge>();
		completePath.addAll(prefix);
		completePath.addAll(path);
		return completePath;
	}

	/**
	 * Backtracks the stack by one step
	 * if the topmost element has (edge, true), It pushes the other edge of the decision node as (otheredge, false)
	 * if the topmost element has (edge, false), it pops the element and backtracks another step.
	 * Continues till only the start edge is in the stack.
	 * @param stack
	 * @return
	 */
/*	public Stack<Pair<IEdge, Boolean>> backtrack(
			Stack<Pair<IEdge, Boolean>> stack) {
		if (!stack.isEmpty()) {
			Pair<IEdge, Boolean> topmostPair = stack.pop();
			if (stack.isEmpty()) {
				return stack;
			}

			// Push the other edge of the node with a false
			if (topmostPair.getSecond()) {
				IEdge newEdge = null;
				IEdge oldEdge = topmostPair.getFirst();
				newEdge = getOtherEdge(oldEdge);
				stack.push(new Pair<IEdge, Boolean>(newEdge, false));
//				System.out.println("PUSH " + newEdge.getId());
				return stack;
			} else
				return backtrack(stack);
		} else
			return stack;
	}
*/
	public IEdge getOtherEdge(IEdge oldEdge) {
		IEdge newEdge = null;
		INode node = oldEdge.getTail();

		if(node.getOutgoingEdgeList().size()<=1)
			return null;

		if (oldEdge.equals(node.getOutgoingEdgeList().get(0))) {
			newEdge = node.getOutgoingEdgeList().get(1);
		} else if (oldEdge.equals(node.getOutgoingEdgeList().get(1))) {
			newEdge = node.getOutgoingEdgeList().get(0);
		}
		return newEdge;
	}

	public Set<IEdge> convertTargetEdgesToGraphEdges(Set<ICFEdge> targetEdges)
			throws Exception {
		this.mGraph = this.mConvertor.getGraph();
		Set<IEdge> targets = new HashSet<IEdge>();
		for (ICFEdge edge : targetEdges) {
			targets.add(this.mConvertor.getGraphEdge(edge));
		}
		return targets;
	}

	public ArrayList<ICFEdge> convertPathEdgesToCFGEdges(IPath path) {
		ArrayList<IEdge> list = path.getPath();
		ArrayList<ICFEdge> cfPath = new ArrayList<ICFEdge>();
		// Convert the graph edges to cfg edges.
		for (IEdge e : list) {
			cfPath.add(this.mConvertor.getCFEdge(e));
		}
		return cfPath;
	}

	/**
	 * Maps the obtained satisfiable solution with symbolic variables to actual variables  
	 * @param leaf
	 * @param solution
	 * @return
	 * @throws Exception
	 */
	protected TestSequence convert(SETNode leaf, SolverResult solution)
			throws Exception {
		TestSequence ts = null;
		if (solution.getResult()) {
			SETNode currNode = leaf;
			Stack<Pair<IIdentifier, IIdentifier>> stack = new Stack<Pair<IIdentifier, IIdentifier>>();
			while (true) {
				if (currNode instanceof SETBasicBlockNode) {
					SETBasicBlockNode bb = (SETBasicBlockNode) currNode;
					Map<IIdentifier, IExpression> values = bb.getValues();
					for (IIdentifier var : values.keySet()) {
						IExpression val = values.get(var);
						if (val instanceof Variable
								&& val.getProgram().equals(set)) {
							stack.push(new Pair<IIdentifier, IIdentifier>(var,
									(IIdentifier) values.get(var)));
						}
					}
				}
				if (currNode.getIncomingEdge() == null) {
					break;
				}
				currNode = currNode.getPredecessorNode();
			}
			Map<IIdentifier, List<Object>> map = new HashMap<IIdentifier, List<Object>>();
			Map<IIdentifier, Object> concValues = solution.getModel();
			while (!stack.isEmpty()) {
				Pair<IIdentifier, IIdentifier> pair = stack.pop();
				IIdentifier var = pair.getFirst();
				IIdentifier symval = pair.getSecond();
				if (!map.keySet().contains(var)) {
					map.put(var, new ArrayList<Object>());
				}
				if (!concValues.keySet().contains(symval)) {
					if (!set.hasVariable(symval)) {
						Exception e = new Exception(
								"SymTest.convert : Symbolic value '"
										+ symval.getName()
										+ "' not present in the solver output.");
						throw e;
					} else {

					}
				}
				Object concreteValue = concValues.get(symval);
				List<Object> values = map.get(var);
				values.add(concreteValue);
			}
			ts = new TestSequence(map.keySet());
			for (IIdentifier v : map.keySet()) {
				for (Object value : map.get(v)) {
					ts.addInputValue(v, value);
				}
			}
		} else {
			throw new UnSatisfiableException();
		}
		return ts;
	}
}
