package com.symtest.heuristics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import com.symtest.graph.IEdge;
import com.symtest.graph.IGraph;
import com.symtest.graph.INode;
import com.symtest.graph.IPath;
import com.symtest.mygraph.Path;
import com.symtest.Solver.SolverResult;
import com.symtest.set.SET;
import com.symtest.tester.CFGToGraphConvertor;
import com.symtest.tester.FindCFPathAlgorithm;
import com.symtest.tester.SymTestUtil;
import com.symtest.cfg.ICFEdge;
import com.symtest.cfg.ICFG;

public class RemovingEdgeHeuristicsUtil implements ApplyHeuristics {

	public SolverResult performHeuristics(IGraph graph,
			Set<ICFEdge> mTargets, IPath graphpath, ICFG cfg,
			CFGToGraphConvertor mConvertor) throws Exception {
		System.out.println("Enter number of trials");
		Scanner sc = new Scanner(System.in);
		int num_of_iterations = sc.nextInt();
		SolverResult solution = null;
		for(int i = 0; i< num_of_iterations;i++) {
		ArrayList<ICFEdge> path = convertPathEdgesToCFGEdges(graphpath,
				mConvertor);
		int index = SymTestUtil.getLongestSatisfiablePrefix(path, cfg);
		int unsatisfiableEdgeIndex = index + 1;
		IEdge e = graphpath.getPath().get(unsatisfiableEdgeIndex);
		graph.deleteEdge(e);
		INode newStartNode = graphpath.getPath().get(index).getHead();
		Set<IEdge> uncoveredTargets = new HashSet<IEdge>();
		uncoveredTargets.addAll(convertTargetEdgesToGraphEdges(mTargets, mConvertor));
		uncoveredTargets.removeAll(graphpath.getPath().subList(0,
				unsatisfiableEdgeIndex));
		boolean isEdgeATarget = uncoveredTargets.remove(e);

		FindCFPathAlgorithm algorithm = new FindCFPathAlgorithm(graph,
				uncoveredTargets, mConvertor.getGraphNode(cfg.getStopNode()));
		IPath newPath = algorithm.findLongestAcyclicPath(newStartNode,
				uncoveredTargets);
		IPath completePath = new Path(graph);
		List<IEdge> pathlist = new ArrayList<IEdge>();
		pathlist.addAll(graphpath.getPath().subList(0, unsatisfiableEdgeIndex));
		completePath.setPath(pathlist);
		completePath.getPath().addAll(newPath.getPath());
		uncoveredTargets.removeAll(newPath.getPath());
		//Add the looping edge directly
		completePath.getPath().add(mConvertor.getGraphNode(cfg.getStopNode()).getOutgoingEdgeList().get(0));
		graph.addEdge(e);
		if(isEdgeATarget) uncoveredTargets.add(e);
		FindCFPathAlgorithm nextalgorithm = new FindCFPathAlgorithm(graph,
				uncoveredTargets, mConvertor.getGraphNode(cfg.getStopNode()));
		//IPath nextPath = nextalgorithm.findCFPath();
		//completePath.getPath().addAll(nextPath.getPath());
		ArrayList<ICFEdge> newCFPath = convertPathEdgesToCFGEdges(completePath,
				mConvertor);
		SET set = SymTestUtil.getSET(newCFPath, cfg);
		// Solve the predicate
		solution = SymTestUtil.solveSequence(set);
		if(solution.getResult()) break;
		}
		return solution;
	}

	//Move This to SymTestUtil?
	
	public static ArrayList<ICFEdge> convertPathEdgesToCFGEdges(IPath path,
			CFGToGraphConvertor mConvertor) {
		ArrayList<IEdge> list = path.getPath();
		ArrayList<ICFEdge> cfPath = new ArrayList<ICFEdge>();
		for (IEdge e : list) {
			cfPath.add(mConvertor.getCFEdge(e));
		}
		return cfPath;
	}
	
	public Set<IEdge> convertTargetEdgesToGraphEdges(Set<ICFEdge> targetEdges, CFGToGraphConvertor convertor)
			throws Exception {
		Set<IEdge> targets = new HashSet<IEdge>();
		for (ICFEdge edge : targetEdges) {
			targets.add(convertor.getGraphEdge(edge));
		}
		return targets;
	}

	

}
