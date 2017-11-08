package frontend;

/**
 * Helper class to handle CFG creation. Deals with creating Basic Block Nodes
 * (BB) & Decision Nodes (DN) and connecting them together. Also generates DOT
 * commands that are used visualized CFGVisualizer.
 * The CFGs are designed in the following way:
 * - All sequential statements are added to BB.
 * - DNs contains only condition expressions.
 * - Each DN is connected to the first BBs in the then and else part.
 * - Each DN ends with the end nodes in the then & else being connected to a BB
 * (endIf BB) i.e each DN unit will have a diamond structure.
 * - Each DN is in the form of If-then-else or just If-then. In the absence of
 * an else part the DN is connected to the endIf BB.
 * - While represents the terminal node.
 * - While node and the node previous to the while is connected to the first BB
 * in the body of the while.
 * - All Edge IDs are in the form of ID->ID to make it easier to generate DOT
 * code.
 * 
 * TODO Do a BFS through the generated CFG and prune empty BBs.
 */

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
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

	/**
	 * Used to keep track of DNs seen so far. Utilized to connect up DNs to
	 * endIf BBs in case the else is missing.
	 */
	private Stack<ICFGDecisionNode> mDecisionNodeStack;

	/**
	 * Each then/else block could consist of multiple BBs/DNs. Dangling block
	 * stack keeps track of the end block in each sections which will later be
	 * connected to the endIf BB. Basically it contains leaves that are produced
	 * during the course of construction of the CFG.
	 */
	private Stack<ICFGBasicBlockNode> mDanglingBlockStack;

	/**
	 * Used to connect up BBs with one another.
	 */
	private ICFGBasicBlockNode mCurrBB, mPrevBB;
	private ICFGDecisionNode mCurrDN;

	/**
	 * While Node a.k.a Terminal Node
	 */
	private ICFGBasicBlockNode mWhileNode, mStartNode;
	public Set<ICFEdge> targets = new HashSet<ICFEdge>();

	// TODO Remove and switch to function before final release
	private int tempId = 0;

	private static final CFGVisualizer visualizer = new CFGVisualizer();
	private static final Logger logger = Logger
			.getLogger(CFGCreator.class.getName());

	/**
	 * Set up the initial & terminal nodes; add first BB.
	 */
	public CFGCreator() {
		try {
			mStartNode = new CFGBasicBlockNode("BEGIN", null);
			mWhileNode = new CFGBasicBlockNode("WHILE", null);
			mCFG = new CFG(mStartNode, mWhileNode);
			logger.fine("Starting CFG creation");

			mDecisionNodeStack = new Stack<ICFGDecisionNode>();
			mDanglingBlockStack = new Stack<ICFGBasicBlockNode>();

			mCurrBB = mStartNode;
			addBasicBlockNode(true);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	/**
	 * Generates a unique ID for each node.
	 * TODO Switch to original implementation
	 * 
	 * @return alpha-numeric string representing ID
	 */
	public String generateId() {
		tempId++;
		return Integer.toString(tempId);
		// return UUID.randomUUID().toString();
	}

	/**
	 * Creates a BB
	 * 
	 * @param createLink
	 *            determines whether newly created BB should be linked
	 *            to last created BB. True for BB->BB connections and
	 *            False for DN->BB connections (done manually).
	 */
	private void addBasicBlockNode(boolean createLink) {
		try {
			mPrevBB = mCurrBB;
			mCurrBB = new CFGBasicBlockNode("BB" + generateId(), mCFG);
			logger.finer("Created new node " + mCurrBB.getId());

			if (createLink) {
				String edgeId = mPrevBB.getId() + "->" + mCurrBB.getId();
				ICFEdge blockEdge = new CFEdge(edgeId, mCFG, mPrevBB, mCurrBB);
				mCFG.addEdge(blockEdge);
				logger.finer("Add edge: " + edgeId);
				visualizer.addLink(edgeId, false);

				if (!mDanglingBlockStack.isEmpty()
						&& mDanglingBlockStack.peek() == mPrevBB) {
					mDanglingBlockStack.pop();
					mDanglingBlockStack.push(mCurrBB);
				}
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

	/**
	 * Connect while node to newly created BB.
	 */
	public void addWhile() {
		try {
			logger.finer("Create while node");
			addBasicBlockNode(true);
			ICFEdge whileEdge = new CFEdge(
					mWhileNode.getId() + "->" + mCurrBB.getId(), mCFG,
					mWhileNode, mCurrBB);
			mCFG.addEdge(whileEdge);
			logger.finer("Add edge: " + whileEdge.getId());
			visualizer.addLink(whileEdge.getId(), false);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	/**
	 * Creates DN, then-BB and connects them up.
	 * 
	 * @param exp
	 *            condition corresponding to DN
	 * @param isTarget
	 *            whether the DN-Then edge is in the target set or not.
	 */
	public void addConditional(IExpression exp, boolean isTarget) {
		try {
			mCurrDN = new CFGDecisionNode("D" + generateId(), mCFG, exp);
			mDecisionNodeStack.push(mCurrDN);
			logger.finer("Create Decision node " + mCurrDN.getId());
			logger.finest("with condition exp " + mCurrDN.getCondition());

			String edgeId = mCurrBB.getId() + "->" + mCurrDN.getId();
			ICFEdge blockEdge = new CFEdge(edgeId, mCFG, mCurrBB, mCurrDN);
			mCFG.addEdge(blockEdge);
			logger.finer("Add edge: " + edgeId);
			visualizer.addLink(edgeId, false);

			// Pop BB since it's no longer a leaf.
			if (!mDanglingBlockStack.isEmpty()
					&& mDanglingBlockStack.peek() == mCurrBB)
				mDanglingBlockStack.pop();

			// add then-BB.
			addBasicBlockNode(false);
			logger.finer("Add then block node");

			ICFEdge decisionThenEdge = new CFEdge(
					mCurrDN.getId() + "->" + mCurrBB.getId(), mCFG, mCurrDN,
					mCurrBB);
			mCFG.addEdge(decisionThenEdge);
			mCurrDN.setThenEdge(decisionThenEdge);
			logger.finer("Add Decision-Then edge: " + decisionThenEdge.getId()); // DOT
			visualizer.addLink(decisionThenEdge.getId(), isTarget);

			if (isTarget) {
				targets.add(decisionThenEdge);
				logger.info("Added edge to target set: "
						+ decisionThenEdge.getId());
			}

			// push then-BB since it's a leaf now.
			mDanglingBlockStack.push(mCurrBB);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	/**
	 * Creates the else-BB and connects it to the corresponding DN.
	 * 
	 * @param isTarget
	 *            whether the DN-else edge is in target set or not.
	 */
	public void setElseBlock(boolean isTarget) {
		try {

			addBasicBlockNode(false);
			logger.finer("Add else node");
			ICFEdge decisionElseEdge = new CFEdge(
					mCurrDN.getId() + "->" + mCurrBB.getId(), mCFG, mCurrDN,
					mCurrBB);
			mCFG.addEdge(decisionElseEdge);
			mCurrDN.setElseEdge(decisionElseEdge);
			logger.finer("Add Decision-Else edge: " + decisionElseEdge.getId()); // DOT
			visualizer.addLink(decisionElseEdge.getId(), isTarget);

			if (isTarget) {
				targets.add(decisionElseEdge);
				logger.info("Added edge to target set: "
						+ decisionElseEdge.getId());
			}

			// push else-BB since it's a leaf now.
			mDanglingBlockStack.push(mCurrBB);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	/**
	 * Called at the end of each if block.
	 */
	public void resetIfBlock() {
		try {
			// Get the relevant DN
			ICFGDecisionNode decNode = mDecisionNodeStack.pop();

			// Add the endIf BB
			addBasicBlockNode(false);
			ICFGBasicBlockNode thenNode = null, elseNode = null;

			if (mCurrDN.getElseEdge() != null) {
				elseNode = (ICFGBasicBlockNode) mDanglingBlockStack.pop();
				thenNode = (ICFGBasicBlockNode) mDanglingBlockStack.pop();
			} else {
				thenNode = (ICFGBasicBlockNode) mDanglingBlockStack.pop();
			}

			// Create then-endIf edges
			ICFEdge thenEndEdge = new CFEdge(
					thenNode.getId() + "->" + mCurrBB.getId(), mCFG, thenNode,
					mCurrBB);
			mCFG.addEdge(thenEndEdge);
			logger.finer("Add Then-EndIf edge: " + thenEndEdge.getId()); // DOT
			visualizer.addLink(thenEndEdge.getId(), false);

			if (mCurrDN.getElseEdge() != null) {
				// Create else-endIf edges
				ICFEdge elseEndEdge = new CFEdge(
						elseNode.getId() + "->" + mCurrBB.getId(), mCFG,
						elseNode, mCurrBB);
				mCFG.addEdge(elseEndEdge);
				logger.finer("Add Else-EndIf edge: " + elseEndEdge.getId()); // DOT
				visualizer.addLink(elseEndEdge.getId(), false);
			} else {
				// If else part is missing then connect up endIf-BB as the else
				// part of DN.
				ICFEdge decEndEdge = new CFEdge(
						decNode.getId() + "->" + mCurrBB.getId(), mCFG, decNode,
						mCurrBB);
				mCFG.addEdge(decEndEdge);
				logger.finer("Add Dec-EndIf edge: " + decNode.getId()); // DOT
				visualizer.addLink(decEndEdge.getId(), false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// push as endIf-BB is now a leaf.
		mDanglingBlockStack.push(mCurrBB);

		// pop since the if has been processed completely.
		if (!mDecisionNodeStack.empty()) {
			mCurrDN = mDecisionNodeStack.peek();
		}
		logger.finer("End of if");
	}

	/**
	 * Called at the end to link last BB to while.
	 */
	public void linkLastNode() {
		try {
			ICFEdge nodeWhileEdge = new CFEdge(
					mCurrBB.getId() + "->" + mWhileNode.getId(), mCFG, mCurrBB,
					mWhileNode);
			mCFG.addEdge(nodeWhileEdge);
			logger.finer("Add edge: " + nodeWhileEdge.getId());
			visualizer.addLink(nodeWhileEdge.getId(), false);

			logger.fine("CFG creation complete");
			visualizer.execute();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}