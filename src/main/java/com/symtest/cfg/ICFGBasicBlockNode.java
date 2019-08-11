package com.symtest.cfg;

import com.symtest.program.IBasicBlockNode;

public interface ICFGBasicBlockNode extends IBasicBlockNode, ICFGNode {
	public ICFEdge getOutgoingEdge();
	public ICFGNode getSuccessorNode();
	public ICFEdge setOutgoingEdge(ICFEdge edge);
	public ICFEdge deleteOutgoingEdge();
}
