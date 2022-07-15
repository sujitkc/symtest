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
import java.io.*;

import com.symtest.Solver.SolverResult;
import com.symtest.cfg.ICFEdge;
import com.symtest.cfg.ICFG;
import com.symtest.cfg.ICFGDecisionNode;
import com.symtest.cfg.ICFGNode;
import com.symtest.exceptions.UnSatisfiableException;
import com.symtest.expression.IExpression;
import com.symtest.expression.IIdentifier;
import com.symtest.expression.True;
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
import com.symtest.RL.*;
import com.symtest.tester.SymTest;

public class SymTest_RL extends SymTest {

	private double explore_probability = 0.5;
	private double probability_decay = 0.99998;
	private int edges_in_state = 3;
	private int n_backtracks = 0; 
	private Qtable my_table = new Qtable();
	public SymTest_RL(ICFG cfg, Set<ICFEdge> targets) {
		super(cfg, targets);
	}

	
	public SymTest_RL(ICFG cfg, Set<ICFEdge> targets, Set<ApplyHeuristics> heuristics) {
		super(cfg,targets,heuristics);
	}

	public SymTest_RL(ICFG cfg, Set<ICFEdge> targets, Set<ApplyHeuristics> heuristics, Qtable in_table) {
		super(cfg,targets,heuristics);
		my_table = in_table;
	}

	public void UpdateTargets(Set <ICFEdge> ntargets)
	{
		mTargets = new HashSet<>(ntargets);
	}
	
	public TestSequence generateTestSequence() {
		n_backtracks = 0;
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

				Stack <Pair <IEdge, Boolean>> mystack = new Stack<>();
				for(IEdge e: prefix)
				{
					mystack.push(new Pair <IEdge, Boolean>(e, false));
				}
				if(mystack.peek().getFirst().getHead().getId().charAt(0) != 'D')
					mystack.push(new Pair <IEdge, Boolean> (stack.peek().getFirst(), false));
				//System.out.println(stack.toString());
				stack = backtrack(mystack);
				//System.out.println(stack.toString());
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
	Stack<Pair<IEdge, Boolean>> backtrack(Stack <Pair<IEdge, Boolean>> stack) 
	{
		try
		{
			System.out.println("inside Backtrack");
			
			/*
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
			//*/
			
			//System.out.println(stack.toString());
			List <Pair <IEdge, Boolean>> computed_path_util = new ArrayList<Pair <IEdge, Boolean>> (stack);
			//Collections.reverse(computed_path_util);
			List <IEdge> computed_path = new ArrayList <IEdge>();
			for (Pair <IEdge, Boolean> it: computed_path_util)
			{
				computed_path.add(it.getFirst());
			}
				
			System.out.println("input stack " + stack);
			
			Utilities my_util = new Utilities(edges_in_state, mGraph, mCFG, this);
			my_util.initialize_table(computed_path, my_table);
			int backtrackpoint = my_util.find_backtrack_point(computed_path, my_table, explore_probability);
			my_util.update_policy(computed_path, my_table, backtrackpoint);
			my_util.stack_update(computed_path, backtrackpoint, stack, backtrackpoint);
			
			System.out.println("output stack " + stack);
			n_backtracks++;
			System.out.println("backtrack number: " + n_backtracks);
			explore_probability = explore_probability * probability_decay;
			
			if(explore_probability<0.2)
				explore_probability = 0.2;
			
			return stack;
		}
		
		catch(Exception e)
		{
			//System.out.println("Backtrack exception");	
			stack.clear();
			e.printStackTrace();
			return stack;
		}
	}

}
