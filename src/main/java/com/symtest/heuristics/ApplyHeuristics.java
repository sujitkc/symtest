package com.symtest.heuristics;

import java.util.Set;

import com.symtest.graph.IGraph;
import com.symtest.graph.IPath;
import com.symtest.tester.CFGToGraphConvertor;
import com.symtest.cfg.ICFEdge;
import com.symtest.cfg.ICFG;
import com.symtest.Solver.SolverResult;

public interface ApplyHeuristics {
	
	public SolverResult performHeuristics(IGraph graph,
			Set<ICFEdge> mTargets, IPath graphpath, ICFG cfg,
			CFGToGraphConvertor mConvertor) throws Exception ;

}
