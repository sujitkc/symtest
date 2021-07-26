package com.symtest.cfg;

import java.util.List;

import com.symtest.program.IProgramNode;
import com.symtest.graph.INode;


public interface ICFGNode extends IProgramNode {

	public boolean isIncomingEdge(ICFEdge edge);
	public boolean isOutgoingEdge(ICFEdge edge);
	public boolean isCFPredecessor(INode node);
	public boolean isCFSuccessor(INode node);
	public ICFG getCFG();
	public void setCFG(ICFG graph);	
	public List<ICFEdge> getIncomingEdgeList();
	public List<ICFEdge> getOutgoingEdgeList();
	public List<ICFGNode> getCFPredecessorNodeList();
	public List<ICFGNode> getCFSuccessorNodeList();
	public ICFEdge addIncomingEdge(ICFEdge edge);
	public ICFEdge deleteIncomingEdge(ICFEdge edge);
	public ICFEdge addOutgoingEdge(ICFEdge edge);
	public ICFEdge deleteOutgoingEdge(ICFEdge edge);
	public String getId();
}
