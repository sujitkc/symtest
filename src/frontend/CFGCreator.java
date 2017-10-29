package frontend;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

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
        private ICFGBasicBlockNode mCurrBB, mPrevBB;
        private ICFGDecisionNode mCurrDN;
        private ICFGBasicBlockNode mWhileNode, mStartNode;
        public Set<ICFEdge> targets = new HashSet<ICFEdge>();
        
        private int shitId = 0;

        public CFGCreator() {
                try {
                        mStartNode = new CFGBasicBlockNode("BEGIN", null);
                        mWhileNode = new CFGBasicBlockNode("WHILE", null);
                        mCFG = new CFG(mStartNode, mWhileNode);
                        System.out.println("DEBUG: Create CFG");

                        mConditionalStack = new Stack<ICFGDecisionNode>();

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
                        mCurrBB = new CFGBasicBlockNode(generateId(), mCFG);
                        System.out.println("DEBUG: Created new node " + mCurrBB.getId());
                        System.out.println("");

                        if (createLink) {
                                String edgeId = mPrevBB.getId() + "#" + mCurrBB.getId();
                                ICFEdge blockEdge = new CFEdge(edgeId, mCFG, mPrevBB, mCurrBB);
                                mCFG.addEdge(blockEdge);
                                System.out.println("DEBUG: Add edge from " + mPrevBB.getId() + " to " + mCurrBB.getId() + ": " + edgeId);
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
                System.out.println("DEBUG: Add statement to node " + mCurrBB.getId());
                System.out.println("");
        }

        public void addWhile() {
                try {
                        System.out.println("DEBUG: Create while node");
                        System.out.println("");

                        addBasicBlockNode(true);

                        ICFEdge whileEdge = new CFEdge("WHILE#" + mCurrBB.getId(), mCFG, mWhileNode, mCurrBB);
                        mCFG.addEdge(whileEdge);
                        System.out.println("DEBUG: Add While edge: " + "WHILE#" + mCurrBB.getId());
                } catch (Exception e) {
                        System.out.println(e);
                }

        }

        public void addConditional(IExpression exp, boolean isTarget) {

                try {
                        mCurrDN = new CFGDecisionNode(generateId(), mCFG, exp);
                        mConditionalStack.push(mCurrDN);
                        System.out.println("DEBUG: Create Decision node " + mCurrDN.getId());
                        System.out.println("DEBUG: with condition exp " + mCurrDN.getCondition());
                        System.out.println("");

                        String edgeId = mCurrBB.getId() + "#" + mCurrDN.getId();
                        ICFEdge blockEdge = new CFEdge(edgeId, mCFG, mCurrBB, mCurrDN);
                        mCFG.addEdge(blockEdge);
                        System.out.println("DEBUG: Add edge from BB " + mCurrBB.getId() + " to decision node " + mCurrDN.getId() + ": " + edgeId);

                        addBasicBlockNode(false);
                        System.out.println("DEBUG: Add then block node");

                        ICFEdge decisionThenEdge = new CFEdge(mCurrDN.getId() + "#" + mCurrBB.getId(), mCFG, mCurrDN, mCurrBB);
                        mCFG.addEdge(decisionThenEdge);
                        mCurrDN.setThenEdge(decisionThenEdge);
                        System.out.println("DEBUG: Add Decision-Then edge: " + mCurrDN.getId() + "#" + mCurrBB.getId());
                        if (isTarget) {
                                targets.add(decisionThenEdge);
                                System.out.println("DEBUG: Add Decision-Then edge to target set");
                        }
                } catch (Exception e) {
                        System.out.println(e);
                }
        }



        public void setElseBlock(boolean isTarget) {
                try {
                        addBasicBlockNode(false);
                        System.out.println("DEBUG: Add else node ");

                        ICFEdge decisionElseEdge = new CFEdge(mCurrDN.getId() + "#" + mCurrBB.getId(), mCFG, mCurrDN, mCurrBB);
                        mCFG.addEdge(decisionElseEdge);
                        mCurrDN.setElseEdge(decisionElseEdge);
                        System.out.println("DEBUG: Add Decision-Else edge: " + mCurrDN.getId() + "#" + mCurrBB.getId());
                        if (isTarget) {
                                targets.add(decisionElseEdge);
                                System.out.println("DEBUG: Add Decision-Else edge to target set");
                        }
                } catch (Exception e) {
                        System.out.println(e);
                }
        }

        public void resetIfBlock() {
                try {
                        addBasicBlockNode(false);

                        ICFGBasicBlockNode thenNode = (ICFGBasicBlockNode)mCurrDN.getThenSuccessorNode();
                        ICFEdge thenEndEdge = new CFEdge(thenNode.getId() + "#" + mCurrBB.getId(), mCFG, thenNode, mCurrBB);
                        mCFG.addEdge(thenEndEdge);
                        System.out.println("DEBUG: Add Then-EndIf edge: " + thenNode.getId() + "#" + mCurrBB.getId());

                        if (mCurrDN.getElseEdge() != null) {
                                ICFGBasicBlockNode elseNode = (ICFGBasicBlockNode)mCurrDN.getElseSuccessorNode();
                                ICFEdge elseEndEdge = new CFEdge(elseNode.getId() + "#" + mCurrBB.getId(), mCFG, elseNode, mCurrBB);
                                mCFG.addEdge(elseEndEdge);
                                System.out.println("DEBUG: Add Else-EndIf edge: " + elseNode.getId() + "#" + mCurrBB.getId());
                        }
                } catch (Exception e) {
                        System.out.println(e);
                }

                mConditionalStack.pop();
                if (!mConditionalStack.empty()) {
                        mCurrDN = mConditionalStack.peek();
                }

                System.out.println("DEGUB: End of if");
                System.out.println("");
        }

        public void linkLastNode() {
                try {
                        ICFEdge nodeWhileEdge = new CFEdge(mCurrBB.getId() + "#WHILE", mCFG, mCurrBB, mWhileNode);
                        mCFG.addEdge(nodeWhileEdge);
                        System.out.println("DEBUG: Add Last BB to While edge: " + nodeWhileEdge.getId());
//                        ICFEdge whileStart = new CFEdge("WHILE#" + mStartNode.getId(), mCFG, mWhileNode, mStartNode);
//                        addEdge(whileStart);
                } catch (Exception e) {
                        System.out.println(e);
                }
        }
        
        public void debugCFG() {
        			System.out.println("----DEBUG CFG-----");
        			System.out.println("----DEBUG CFG-----");

        }
}