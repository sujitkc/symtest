package com.symtest.see;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.symtest.cfg.ICFEdge;
import com.symtest.cfg.ICFG;
import com.symtest.cfg.ICFGBasicBlockNode;
import com.symtest.cfg.ICFGNode;
import com.symtest.expression.IExpression;
import com.symtest.expression.IIdentifier;
import com.symtest.expression.Type;
import com.symtest.mycfg.CFGBasicBlockNode;
import com.symtest.mycfg.CFGDecisionNode;
import com.symtest.set.SET;
import com.symtest.set.SETBasicBlockNode;
import com.symtest.set.SETDecisionNode;
import com.symtest.set.SETEdge;
import com.symtest.set.SETExpressionVisitor;
import com.symtest.set.SETNode;
import com.symtest.statement.IStatement;

public class SEE {

	private SET mSET;

	private static final Logger logger = Logger
			.getLogger(SEE.class.getName());

	public SEE(ICFG cfg) throws Exception {
		if (cfg != null) {
			this.mSET = new SET(cfg);
		} else {
			throw new Exception("Null CFG");
		}

	}

	public SET getSET() {
		return mSET;
	}

	/**
	 * The function expands the SET along the specified set of CFG edges
	 * 
	 * @param cfgEdges
	 * @throws Exception
	 */
	public void expandSET(List<ICFEdge> cfgEdges) throws Exception {
		for (ICFEdge edge : cfgEdges) {
			logger.finest("SEE singlestep edge: " + edge.getId());
			singlestep(edge);
		}
	}

	/**
	 * The function performs the addition of a single node to the SET. 
	 * Cases handled: 
	 * case 1 : Leaf node - Basic Block Node & New Node - Basic Block Node 
	 * case 2 : Leaf node - Basic Block Node & New Node - Decision Node
	 * case 3 : Leaf node - Decision Node & New Node - Basic Block Node (3a. New
	 * node belongs to 'Then' Edge 3b. New node belongs to 'Else' edge) 
	 * case 4 : Leaf node - Decision Node & New Node - Decision Node (4a. New node
	 * belongs to 'Then' Edge 4b. New node belongs to 'Else' edge)
	 * 
	 * @param edge
	 */

	private void singlestep(ICFEdge edge) throws Exception {
		boolean valid = false;
		this.mSET.updateLeafNodeSet();
		Set<SETNode> newLeafNodes = new HashSet<SETNode>();
		newLeafNodes = this.mSET.getLeafNodes();
		//System.out.println("SYMTEST DEBUG newLeafNodes: " + newLeafNodes);
		// check for null edge
		if (edge == null) {
			throw new Exception("Null Edge");
		}
		for (SETNode leaf : newLeafNodes) {
			ICFGNode corrCFGNode = leaf.getCFGNode();
			List<ICFEdge> outCFEdges = corrCFGNode.getOutgoingEdgeList();
			if (outCFEdges.contains(edge)) {
				valid = true;
				ICFGNode newNode = edge.getHead();
				// check for dangling edge
				if (newNode == null) {
					throw new Exception("Dangling Edge");
				}
				SETEdge newSETEdge = new SETEdge(mSET, leaf, null);

				if (leaf instanceof SETBasicBlockNode) {
					((SETBasicBlockNode) leaf).setOutgoingEdge(newSETEdge);
					// case 1
					if (newNode instanceof CFGBasicBlockNode) {
						addNewSETBasicBlockNode(newNode, newSETEdge);
					}
					// case 2
					else if (newNode instanceof CFGDecisionNode) {
						addNewSETDecisionNode(newNode, newSETEdge);
					}
				}

				else if (leaf instanceof SETDecisionNode) {
					CFGDecisionNode corrDecisionNode = (CFGDecisionNode) corrCFGNode;
					if (edge.equals(corrDecisionNode.getThenEdge())) {
						// case 3a
						if (newNode instanceof CFGBasicBlockNode) {
							addNewSETBasicBlockNode(newNode, newSETEdge);
							((SETDecisionNode) leaf).setThenEdge(newSETEdge);
						}
						// case 4a
						else if (newNode instanceof CFGDecisionNode) {
							addNewSETDecisionNode(newNode, newSETEdge);
							((SETDecisionNode) leaf).setThenEdge(newSETEdge);
						}
					} else if (edge.equals(corrDecisionNode.getElseEdge())) {
						// case 3b
						if (newNode instanceof CFGBasicBlockNode) {
							addNewSETBasicBlockNode(newNode, newSETEdge);
							((SETDecisionNode) leaf).setElseEdge(newSETEdge);
						}
						// case 4b
						else if (newNode instanceof CFGDecisionNode) {
							addNewSETDecisionNode(newNode, newSETEdge);
							((SETDecisionNode) leaf).setElseEdge(newSETEdge);
						}
					}
				}
			}
			else {
				System.out.println("Invalid : edge = " + edge + " node = " + corrCFGNode);
			}
		}
		if (!valid) {

			throw new Exception("New Node not connected  to Leaf " + edge);
		}
	}

	public void addNewSETDecisionNode(ICFGNode newNode, SETEdge newSETEdge)
			throws Exception {
		CFGDecisionNode decisionNode = (CFGDecisionNode) newNode;
		SETDecisionNode newSETNode = new SETDecisionNode(
				decisionNode.getCondition(), mSET, decisionNode);
		this.mSET.addDecisionNode(newSETNode);
		newSETEdge.setHead(newSETNode);
		newSETNode.setIncomingEdge(newSETEdge);
		this.mSET.addEdge(newSETEdge);
		this.computeExpression(newSETNode);
	}

	private void addNewSETBasicBlockNode(ICFGNode newNode, SETEdge newSETEdge) throws Exception {
		SETBasicBlockNode newSETNode = new SETBasicBlockNode(mSET,
				(CFGBasicBlockNode) newNode);
		this.mSET.addBasicBlockNode(newSETNode);
		newSETEdge.setHead(newSETNode);
		newSETNode.setIncomingEdge(newSETEdge);
		this.mSET.addEdge(newSETEdge);
		this.computeStatementList(newSETNode);
	}

	private void computeStatementList(SETBasicBlockNode node) throws Exception {
		ICFGBasicBlockNode cfgBasicBlockNode = (ICFGBasicBlockNode) node
				.getCFGNode();
		List<IStatement> statements = cfgBasicBlockNode.getStatements();

		for (IStatement statement : statements) {
			//System.out.println("#ComputerSTMNTList: STMNT" + statement);
			SETExpressionVisitor visitor = new SETExpressionVisitor(node,
					statement.getLHS().getType());
			IExpression value = null;

			visitor.visit(statement.getRHS());
			value = visitor.getValue();

			IIdentifier var = statement.getLHS();
			node.setValue(var, value);
		}
	}

	private void computeExpression(SETDecisionNode node) throws Exception {
		SETExpressionVisitor visitor = new SETExpressionVisitor(node,
				Type.BOOLEAN);
		CFGDecisionNode cfgNode = (CFGDecisionNode) node.getCFGNode();
		if (node.getCondition() == null) {
			throw new Exception("Null Expression");
		} else {
			//GreaterThanExpression gt = (GreaterThanExpression)cfgNode.getCondition();
//			System.out.println("SEE DEBUG gt: " + gt.getLHS() + " " + gt.getRHS());
			visitor.visit(cfgNode.getCondition());
			IExpression value = visitor.getValue();
			node.setCondition(value);
		}
	}
}
