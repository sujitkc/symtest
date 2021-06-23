package com.symtest.tester;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


import com.symtest.graph.IEdge;
import com.symtest.graph.IGraph;
import com.symtest.graph.INode;
import com.symtest.mygraph.Edge;
import com.symtest.mygraph.Graph;
import com.symtest.mygraph.Node;
import com.symtest.cfg.ICFEdge;
import com.symtest.cfg.ICFG;
import com.symtest.cfg.ICFGNode;

public class CFGToGraphConvertor {

	private ICFG mCFG;
	private IGraph mGraph;
	private Map<ICFGNode, INode> mNodeMap = new HashMap<ICFGNode, INode>();
	private Map<ICFEdge, IEdge> mEdgeMap = new HashMap<ICFEdge, IEdge>();
	private Set<ICFGNode> mCovered;
	
	public CFGToGraphConvertor(ICFG cfg) {
		this.mCFG = cfg;
	}
	
	
	public IGraph getGraph() throws Exception {
		if(this.mGraph == null) {
			this.convert();
		}
		return this.mGraph;
	}
	
	private void convert() throws Exception {
		mCovered = new HashSet<ICFGNode>();
		ICFGNode cfgStart = this.mCFG.getStartNode();
		INode node = new Node(this.mCFG.getId() + "graph-s", null);
		this.mGraph = new Graph(node);
		this.mNodeMap.put(cfgStart, node);
		this.convertNode(cfgStart);
	}

	private void convertNode(ICFGNode cfgNode) throws Exception {
		if(this.mCovered.contains(cfgNode)) {
			return;
		}
		this.mCovered.add(cfgNode);
		INode node = this.mNodeMap.get(cfgNode);
		for(ICFEdge edge : cfgNode.getOutgoingEdgeList()) {
			if(edge.getHead() != null) {
				INode headNode = null;
				if(this.mNodeMap.containsKey(edge.getHead())) {
					headNode = this.mNodeMap.get(edge.getHead());
				}
				else {
					headNode = new Node("graph-" + edge.getHead().getId(), this.mGraph);
					this.mNodeMap.put(edge.getHead(), headNode);
				}
				IEdge outEdge = new Edge("graph-" + edge.getId(), this.mGraph, node, headNode);
				this.mEdgeMap.put(edge, outEdge);
				this.convertNode(edge.getHead());
			}
		}
	}

	public INode getGraphNode(ICFGNode node) {
		return this.mNodeMap.get(node);
	}
	
	
	public IEdge getGraphEdge(ICFEdge edge) {
		return this.mEdgeMap.get(edge);
	}
	
	public ICFEdge getCFEdge(IEdge edge) {
		for(ICFEdge e : this.mEdgeMap.keySet()) {
			if(this.mEdgeMap.get(e).equals(edge)) {
				return e;
			}
		}
		return null;		
	}
}
