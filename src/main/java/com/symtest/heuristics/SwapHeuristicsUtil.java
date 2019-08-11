package com.symtest.heuristics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import com.symtest.exceptions.UnSatisfiableException;
import com.symtest.graph.IEdge;
import com.symtest.graph.IGraph;
import com.symtest.graph.INode;
import com.symtest.graph.IPath;
import com.symtest.mygraph.Path;
import com.symtest.set.SET;
import com.symtest.tester.CFGToGraphConvertor;
import com.symtest.tester.SymTestUtil;
import com.symtest.Solver.SolverResult;
import com.symtest.cfg.ICFEdge;
import com.symtest.cfg.ICFG;

/**
 * Will Handle Only when the unsatisfiable node has same target and source
 * nodes.
 * 
 * @author pavithra
 * 
 */
public class SwapHeuristicsUtil implements ApplyHeuristics {

	
	public SolverResult performHeuristics(IGraph graph,
			Set<ICFEdge> mTargets, IPath graphpath, ICFG cfg,
			CFGToGraphConvertor mConvertor) throws Exception {
		ArrayList<ICFEdge> path = convertPathEdgesToCFGEdges(graphpath,
				mConvertor);
		int index = SymTestUtil.getLongestSatisfiablePrefix(path, cfg);
		int unsatisfiableEdgeIndex = index + 1;
		IEdge newEdge = null;
		IEdge oldEdge = mConvertor.getGraphEdge(path
				.get(unsatisfiableEdgeIndex));
		INode node = oldEdge.getTail();
		
		if ((oldEdge.equals(node.getOutgoingEdgeList().get(0)))
				&& (graphpath.getPath().contains(node.getOutgoingEdgeList()
						.get(1)))) {
			newEdge = node.getOutgoingEdgeList().get(1);
		} else if ((oldEdge.equals(node.getOutgoingEdgeList().get(1)))
				&& (graphpath.getPath().contains(node.getOutgoingEdgeList()
						.get(0)))) {
			newEdge = node.getOutgoingEdgeList().get(0);
		}
		if(newEdge == null) {
			throw new UnSatisfiableException();
		}
		int newIndex = graphpath.getPath().indexOf(newEdge);

		IPath newPath = new Path(graph);
		newPath = graphpath;
		Collections.swap(newPath.getPath(), unsatisfiableEdgeIndex, newIndex);
		Collections.swap(newPath.getPath(), unsatisfiableEdgeIndex + 1,
				newIndex + 1);
		ArrayList<ICFEdge> newCFPath = convertPathEdgesToCFGEdges(newPath,
				mConvertor);
		SET set = SymTestUtil.getSET(newCFPath, cfg);
		// Solve the predicate
		SolverResult solution = SymTestUtil.solveSequence(set);

		return solution;
	}

	public static ArrayList<ICFEdge> convertPathEdgesToCFGEdges(IPath path,
			CFGToGraphConvertor mConvertor) {
		ArrayList<IEdge> list = path.getPath();
		ArrayList<ICFEdge> cfPath = new ArrayList<ICFEdge>();
		// Convert the graph edges to cfg edges.
		for (IEdge e : list) {
			cfPath.add(mConvertor.getCFEdge(e));
		}
		return cfPath;
	}

}
