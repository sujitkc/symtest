package frontend;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import cfg.ICFEdge;
import cfg.ICFG;
import cfg.ICFGBasicBlockNode;
import cfg.ICFGDecisionNode;
import expression.IExpression;
import mycfg.CFEdge;
import mycfg.CFG;
import mycfg.CFGBasicBlockNode;
import mycfg.CFGDecisionNode;
import statement.IStatement;

class CFGCreator {
	private ICFG mCFG;
	private Stack<ICFGDecisionNode> mConditionalStack;
	private Stack<ICFGBasicBlockNode> mDanglingBlocks;
	private ICFGBasicBlockNode mCurrBB, mPrevBB;
	private ICFGDecisionNode mCurrDN;
	private ICFGBasicBlockNode mWhileNode, mStartNode;
	public Set<ICFEdge> targets = new HashSet<ICFEdge>();

	private CFGVisualizer visualizer = new CFGVisualizer();
	private int shitId = 0;

	private static final Logger logger = 
			Logger.getLogger(CFGCreator.class.getName());

	public CFGCreator() {
		try {
			mStartNode = new CFGBasicBlockNode("BEGIN", null);
			mWhileNode = new CFGBasicBlockNode("WHILE", null);
			mCFG = new CFG(mStartNode, mWhileNode);
			logger.fine("Starting CFG creation");

			mConditionalStack = new Stack<ICFGDecisionNode>();
			mDanglingBlocks = new Stack<ICFGBasicBlockNode>();

			mCurrBB = mStartNode;
			addBasicBlockNode(true);
		} catch (Exception e) {
			System.out.println(e);
		}
	}


	public String generateId() {
		//return UUID.randomUUID().toString();
		shitId++;
		return Integer.toString(shitId);
	}


	private void addBasicBlockNode(boolean createLink) {
		try {
			mPrevBB = mCurrBB;
			mCurrBB = new CFGBasicBlockNode("BB" + generateId(), mCFG);
			logger.finer("Created new node " + mCurrBB.getId());

			if (createLink) {
				String edgeId = mPrevBB.getId() + "->" + mCurrBB.getId();
				ICFEdge blockEdge = new CFEdge(edgeId, mCFG, mPrevBB, mCurrBB);
				mCFG.addEdge(blockEdge);
				logger.finer("Add edge: " + edgeId); //DOT
				visualizer.addLink(edgeId, false);

			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public ICFG getCFG() {
		return mCFG;
	}

	public void addStatement(IStatement stmnt) {
		mCurrBB.addStatement(stmnt);
		logger.finer("Add statement to " + mCurrBB.getId());
	}

	public void addWhile() {
		try {
			logger.finer("Create while node");
			addBasicBlockNode(true);
			ICFEdge whileEdge = new CFEdge(mWhileNode.getId() + "->" + mCurrBB.getId(), mCFG, mWhileNode, mCurrBB);
			mCFG.addEdge(whileEdge);
			logger.finer("Add edge: " + whileEdge.getId()); //DOT
			visualizer.addLink(whileEdge.getId(), false);
		} catch (Exception e) {
			System.out.println(e);
		}

	}

	public void addConditional(IExpression exp, boolean isTarget) {
		try {
			mCurrDN = new CFGDecisionNode("D" + generateId(), mCFG, exp);
			mConditionalStack.push(mCurrDN);
			logger.finer("Create Decision node " + mCurrDN.getId());
			logger.finest("with condition exp " + mCurrDN.getCondition());

			String edgeId = mCurrBB.getId() + "->" + mCurrDN.getId();
			ICFEdge blockEdge = new CFEdge(edgeId, mCFG, mCurrBB, mCurrDN);
			mCFG.addEdge(blockEdge);
			logger.finer("Add edge: " + edgeId); //DOT
			visualizer.addLink(edgeId, false);


			addBasicBlockNode(false);
			logger.finer("Add then block node");

			ICFEdge decisionThenEdge = new CFEdge(mCurrDN.getId() + "->" + mCurrBB.getId(), mCFG, mCurrDN, mCurrBB);
			mCFG.addEdge(decisionThenEdge);
			mCurrDN.setThenEdge(decisionThenEdge);
			logger.finer("Add Decision-Then edge: " + decisionThenEdge.getId()); //DOT
			visualizer.addLink(decisionThenEdge.getId(), isTarget);

			if (isTarget) {
				targets.add(decisionThenEdge);
				logger.fine("Added edge to target set: " + decisionThenEdge.getId());
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}



	public void setElseBlock(boolean isTarget) {
		try {
			//EXTRA
			mDanglingBlocks.push(mCurrBB);

			addBasicBlockNode(false);
			logger.finer("Add else node");

			ICFEdge decisionElseEdge = new CFEdge(mCurrDN.getId() + "->" + mCurrBB.getId(), mCFG, mCurrDN, mCurrBB);
			mCFG.addEdge(decisionElseEdge);
			mCurrDN.setElseEdge(decisionElseEdge);
			logger.finer("Add Decision-Else edge: " + decisionElseEdge.getId()); //DOT
			visualizer.addLink(decisionElseEdge.getId(), isTarget);
			if (isTarget) {
				targets.add(decisionElseEdge);
				logger.fine("Added edge to target set: " + decisionElseEdge.getId());
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void resetIfBlock() {
		try {
			//EXTRA
			if (mCurrBB != mDanglingBlocks.peek())
				mDanglingBlocks.push(mCurrBB);
			addBasicBlockNode(false);
			ICFGBasicBlockNode thenNode = null, elseNode = null;
			if (mCurrDN.getElseEdge() != null) {
				elseNode = (ICFGBasicBlockNode)mDanglingBlocks.pop();
				thenNode = (ICFGBasicBlockNode)mDanglingBlocks.pop();
			} else {
				thenNode = (ICFGBasicBlockNode)mDanglingBlocks.pop();
			}

			ICFEdge thenEndEdge = new CFEdge(thenNode.getId() + "->" + mCurrBB.getId(), mCFG, thenNode, mCurrBB);
			mCFG.addEdge(thenEndEdge);
			logger.finer("Add Then-EndIf edge: " + thenEndEdge.getId()); //DOT
			visualizer.addLink(thenEndEdge.getId(), false);

			if (mCurrDN.getElseEdge() != null) {
				ICFEdge elseEndEdge = new CFEdge(elseNode.getId() + "->" + mCurrBB.getId(), mCFG, elseNode, mCurrBB);
				mCFG.addEdge(elseEndEdge);
				logger.finer("Add Else-EndIf edge: " + elseEndEdge.getId()); //DOT
				visualizer.addLink(elseEndEdge.getId(), false);
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		mDanglingBlocks.push(mCurrBB);
		mConditionalStack.pop();
		if (!mConditionalStack.empty()) {
			mCurrDN = mConditionalStack.peek();
		}
		logger.finer("End of if");
	}

	public void linkLastNode() {
		try {
			ICFEdge nodeWhileEdge = new CFEdge(mCurrBB.getId() + "->" + mWhileNode.getId(), mCFG, mCurrBB, mWhileNode);
			mCFG.addEdge(nodeWhileEdge);
			logger.finer("Add edge: " + nodeWhileEdge.getId()); //DOT
			visualizer.addLink(nodeWhileEdge.getId(), false);
			logger.fine("CFG creation complete");
			visualizer.execute();

		} catch (Exception e) {
			System.out.println(e);
		}

	}

}