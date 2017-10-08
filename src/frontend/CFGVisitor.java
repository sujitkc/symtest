import java.util.Stack;
import java.util.UUID;

import cfg.ICFEdge;
import cfg.ICFG;
import cfg.ICFGBasicBlockNode;
import cfg.ICFGDecisionNode;
import expression.AddExpression;
import expression.ConcreteConstant;
import expression.EqualsExpression;
import expression.IExpression;
import expression.IIdentifier;
import expression.MulExpression;
import expression.NotExpression;
import expression.Variable;
import expression.Input;
import mycfg.CFEdge;
import mycfg.CFG;
import mycfg.CFGBasicBlockNode;
import mycfg.CFGDecisionNode;
import statement.IStatement;
import statement.Statement;

class CFGCreator {
        private ICFG mCFG;
        private Stack<ICFGDecisionNode> mConditionalStack;
        private ICFGBasicBlockNode mCurrBB, mPrevBB;
        private ICFGDecisionNode mCurrDN;
        private ICFGBasicBlockNode mWhileNode, mStartNode;

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
                return UUID.randomUUID().toString();
        }

        private void addEdge(ICFEdge edge) {
                mCFG.addEdge(edge);
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
                                addEdge(blockEdge);
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
                        addEdge(whileEdge);
                        System.out.println("DEBUG: Add While edge: " + "WHILE#" + mCurrBB.getId());
                } catch (Exception e) {
                        System.out.println(e);
                }

        }

        public void addConditional(IExpression exp) {

                try {
                        mCurrDN = new CFGDecisionNode(generateId(), mCFG, exp);
                        mConditionalStack.push(mCurrDN);
                        System.out.println("DEBUG: Create Decision node " + mCurrDN.getId());
                        System.out.println("");

                        String edgeId = mCurrBB.getId() + "#" + mCurrDN.getId();
                        ICFEdge blockEdge = new CFEdge(edgeId, mCFG, mCurrBB, mCurrDN);
                        addEdge(blockEdge);
                        System.out.println("DEBUG: Add edge from BB " + mPrevBB.getId() + " to decision node " + mCurrBB.getId() + ": " + edgeId);

                        addBasicBlockNode(false);
                        System.out.println("DEBUG: Add then block node");

                        ICFEdge decisionThenEdge = new CFEdge(mCurrDN.getId() + "#" + mCurrBB.getId(), mCFG, mCurrDN, mCurrBB);
                        addEdge(decisionThenEdge);
                        mCurrDN.setThenEdge(decisionThenEdge);
                        System.out.println("DEBUG: Add Decision-Then edge: " + mCurrDN.getId() + "#" + mCurrBB.getId());
                } catch (Exception e) {
                        System.out.println(e);
                }
        }



        public void setElseBlock() {
                try {
                        addBasicBlockNode(false);
                        System.out.println("DEBUG: Add else node ");

                        ICFEdge decisionElseEdge = new CFEdge(mCurrDN.getId() + "#" + mCurrBB.getId(), mCFG, mCurrDN, mCurrBB);
                        addEdge(decisionElseEdge);
                        mCurrDN.setElseEdge(decisionElseEdge);
                        System.out.println("DEBUG: Add Decision-Else edge: " + mCurrDN.getId() + "#" + mCurrBB.getId());
                } catch (Exception e) {
                        System.out.println(e);
                }
        }

        public void resetIfBlock() {
                try {
                        addBasicBlockNode(false);

                        ICFGBasicBlockNode thenNode = (ICFGBasicBlockNode)mCurrDN.getThenSuccessorNode();
                        ICFEdge thenEndEdge = new CFEdge(thenNode.getId() + "#" + mCurrBB.getId(), mCFG, thenNode, mCurrBB);
                        addEdge(thenEndEdge);
                        System.out.println("DEBUG: Add Then-EndIf edge: " + thenNode.getId() + "#" + mCurrBB.getId());

                        if (mCurrDN.getElseEdge() != null) {
                                ICFGBasicBlockNode elseNode = (ICFGBasicBlockNode)mCurrDN.getElseSuccessorNode();
                                ICFEdge elseEndEdge = new CFEdge(elseNode.getId() + "#" + mCurrBB.getId(), mCFG, elseNode, mCurrBB);
                                addEdge(elseEndEdge);
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
                        ICFEdge decisionElseEdge = new CFEdge("WHILE#" + mCurrBB.getId(), mCFG, mWhileNode, mCurrBB);
                        addEdge(decisionElseEdge);
                        System.out.println("DEBUG: Add Last BB to While edge: " + "WHILE#" + mCurrBB.getId());
                } catch (Exception e) {
                        System.out.println(e);
                }
        }
}

public class CFGVisitor extends CymbolBaseVisitor<Value> {

        private ICFG mCFG;
        private CFGCreator mCreator;


        @Override 
        public Value visitFile(CymbolParser.FileContext ctx) { 
                mCreator = new CFGCreator();
                mCFG = mCreator.getCFG();
                visitChildren(ctx); 
                mCreator.linkLastNode();

                return null;
        }

        @Override 
        public Value visitVarDecl(CymbolParser.VarDeclContext ctx) { 
                System.out.print("DEBUG: VarDECL:- TYPE:" + ctx.type().getText() + " VAR:" + ctx.ID().getText());

                Value val = null;
                try {
                        Variable var = new Variable(ctx.ID().getText(), mCFG);
                        if (ctx.expr() != null) {
                                System.out.println(" EXPR: " +ctx.expr().getText());
                                Value expr = visit(ctx.expr());
                                Statement stmnt = new Statement(mCFG, var, (IExpression)expr.get());
                                mCreator.addStatement(stmnt);
                                val = new Value(stmnt);
                        } else {
                                Statement stmnt = new Statement(mCFG, var, new ConcreteConstant(0, mCFG));
                                System.out.println(" ");
                                mCreator.addStatement(stmnt);
                                val = new Value(stmnt);
                        }
                } catch (Exception e) {
                        System.out.println("VarDECL:" + e);
                }
                return val;
        }

        @Override 
        public Value visitBlock(CymbolParser.BlockContext ctx) { 
                /*
                 *System.out.println("DEBUG: BLOCK Child Count:" + ctx.getChildCount());
                 */
                return visitChildren(ctx); 
        }

        @Override 
        public Value visitStatBlock(CymbolParser.StatBlockContext ctx) { 
                /*
                 *System.out.println("DEBUG: STATEMENT BLOCK");
                 */
                return visitChildren(ctx); 
        }

        @Override 
        public Value visitWhile(CymbolParser.WhileContext ctx) { 
                System.out.println("DEBUG: WHILE");
                mCreator.addWhile();
                visit(ctx.stat());
                return null;
                //return visitChildren(ctx); 
        }

        @Override 
        public Value visitIf(CymbolParser.IfContext ctx) { 
                System.out.println("DEBUG: IF");

                Value vexp = visit(ctx.expr());
                mCreator.addConditional((IExpression) vexp);
                //mCreator.setThenBlock();
                visit(ctx.stat(0));
                if (ctx.stat(1) != null) {
                        mCreator.setElseBlock();
                        visit(ctx.stat(1));
                }
                mCreator.resetIfBlock();
                return null;
        }


        @Override 
        public Value visitAssign(CymbolParser.AssignContext ctx) { 
                System.out.println("DEBUG: ASSIGN ");
                Value vleft = visit(ctx.expr(0));
                Value vright = visit(ctx.expr(1));
                Value val = null;
                try {
                        Statement assign = new Statement(mCFG, (IIdentifier)vleft.get(), (IExpression)vright.get());
                        mCreator.addStatement(assign);
                        val = new Value(assign);
                } catch (Exception e) {
                        System.out.println("ASSIGN: " + e);
                }
                return val;
        }

        @Override 
        public Value visitExp(CymbolParser.ExpContext ctx) { 
                System.out.println("DEBUG: EXP");
                Value vexp = visit(ctx.expr());
                mCreator.addStatement((IStatement)vexp);
                return vexp;
        }

        @Override 
        public Value visitNot(CymbolParser.NotContext ctx) { 
                System.out.println("DEBUG: NOT EXP");
                Value exp = visit(ctx.expr());
                Value val = null;
                try {
                        NotExpression nexp = new NotExpression(mCFG, (IExpression)exp.get());
                        val = new Value(nexp);
                } catch (Exception e) {
                        System.out.println("NOT: " + e);
                }
                return val;
        }

        @Override 
        public Value visitNotEqual(CymbolParser.NotEqualContext ctx) { 
                System.out.println("DEBUG: NOT EQUALS EXP: " + ctx.expr(0).getText() + " NOT EQUALS? " + ctx.expr(1).getText());
                Value vleft = visit(ctx.expr(0));
                Value vright = visit(ctx.expr(1));
                Value val = null;
                try {
                        EqualsExpression eql = new EqualsExpression(mCFG, (IExpression)vleft.get(), (IExpression)vright.get());
                        NotExpression neql = new NotExpression(mCFG, eql);
                        val = new Value(neql);
                } catch (Exception e) {
                        System.out.println("EQUALS: " + e);
                }
                return val;
        }

        @Override 
        public Value visitMult(CymbolParser.MultContext ctx) { 
                System.out.println("DEBUG: MULT");
                Value vleft = visit(ctx.expr(0));
                Value vright = visit(ctx.expr(1));
                Value val = null;
                try {
                        MulExpression exp = new MulExpression(mCFG, (IExpression)vleft.get(), (IExpression)vright.get());
                        val = new Value(exp);
                } catch (Exception e){
                        System.out.println("MULT:" + e);
                }
                return val;
        }

        @Override 
        public Value visitAddSub(CymbolParser.AddSubContext ctx) { 
                System.out.println("DEBUG: ADDSUB");
                Value vleft = visit(ctx.expr(0));
                Value vright = visit(ctx.expr(1));
                Value val = null;
                try {
                        AddExpression exp = new AddExpression(mCFG, (IExpression)vleft.get(), (IExpression)vright.get());
                        val = new Value(exp);
                } catch (Exception e){
                        System.out.println("ADDSUB:" + e);
                }
                return val;
        }

        @Override 
        public Value visitEqual(CymbolParser.EqualContext ctx) { 
                System.out.println("DEBUG: EQUAL");
                Value val = null;
                Value vleft = visit(ctx.expr(0));
                Value vright = visit(ctx.expr(1));
                try {
                        EqualsExpression exp = new EqualsExpression(mCFG, (IExpression)vleft.get(), (IExpression)vright.get());
                        val = new Value(exp);
                } catch (Exception e) {
                        System.out.println("EQUAL " + e);
                }
                return val;
        }

        @Override 
        public Value visitVar(CymbolParser.VarContext ctx) { 
                System.out.println("DEUBG: VAR:- " + ctx.ID().getText());
                Value val = null;
                try {
                        Variable var = new Variable(ctx.ID().getText(), mCFG);
                        val = new Value(var);
                } catch (Exception e) {
                        System.out.println("VAR:" + e);
                }
                return val;
        }

        @Override 
        public Value visitNegate(CymbolParser.NegateContext ctx) { 
                /*
                 *System.out.println("DEBUG: NEGATE STUB");
                 */
                return visitChildren(ctx); 
        }

        @Override 
        public Value visitInt(CymbolParser.IntContext ctx) { 
                /*
                 *System.out.println("DEBUG: INT="+ ctx.INT().getText());
                 */
                Value val = null; 
                try {
                        Integer intVal = Integer.parseInt(ctx.INT().getText());
                        ConcreteConstant constant = new ConcreteConstant(intVal, mCFG);
                        val = new Value(constant);
                } catch (Exception e) {
                        System.out.println("visitInt:"+e);
                }
                return val;
        }

        @Override 
        public Value visitInput(CymbolParser.InputContext ctx) { 
                System.out.println("DEBUG: INPUT");
                Value val = null;
                try {
                        Input inp = new Input(mCFG);
                        val = new Value(inp);
                } catch (Exception e) {
                        System.out.println("INPUT " + e);
                }
                return val;
        }
}

