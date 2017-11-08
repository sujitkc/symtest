package frontend;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import cfg.ICFG;
import expression.AddExpression;
import expression.AndExpression;
import expression.ConcreteConstant;
import expression.EqualsExpression;
import expression.Expression;
import expression.GreaterThanEqualToExpression;
import expression.GreaterThanExpression;
import expression.IExpression;
import expression.IIdentifier;
import expression.Input;
import expression.LesserThanEqualToExpression;
import expression.LesserThanExpression;
import expression.MulExpression;
import expression.NotExpression;
import expression.OrExpression;
import expression.SubExpression;
import expression.Variable;
import statement.IStatement;
import statement.Statement;
import tester.SymTest;
import tester.TestSequence;


public class CFGVisitor extends CymbolBaseVisitor<Value> {

        private ICFG mCFG;
        private CFGCreator mCreator;
        private HashMap<Integer, String> targetMap;
        
        private static final Logger logger = 
			Logger.getLogger(CFGVisitor.class.getName());

        public CFGVisitor(String targetFileName) {
                targetMap = new HashMap<Integer, String>();
                File targetFile = new File(targetFileName);
                try {
                        BufferedReader br = new BufferedReader(new FileReader(targetFile));
                        String line;
                        logger.info("Target Mapping:");
                        while ((line = br.readLine()) != null) {
                                String[] parts = line.trim().split("-");
                                targetMap.put(Integer.parseInt(parts[0]), parts[1]);
                                logger.info("Line " + parts[0] + " " + parts[1]);
                        }
                        br.close();
                } catch (Exception e) {
                        logger.severe(e.toString());
                }
        }

        @Override 
        public Value visitFile(CymbolParser.FileContext ctx) { 
                mCreator = new CFGCreator();
                mCFG = mCreator.getCFG();
                visitChildren(ctx); 
                mCreator.linkLastNode();
                logger.finest(" ");

				SymTest st = new SymTest(mCFG, mCreator.targets);
				TestSequence seq = st.generateTestSequence();
				Map<IIdentifier, List<Object>> testseq = seq.getTestSequence();
				System.out.println("Test Seq: " + testseq);

                return null;
        }

        @Override 
        public Value visitVarDecl(CymbolParser.VarDeclContext ctx) { 
                logger.finest("DEBUG: VarDECL:- TYPE:" + ctx.type().getText() + " VAR:" + ctx.ID().getText());

                Value val = null;
                try {
                        Variable var = new Variable(ctx.ID().getText(), mCFG);
                        if (ctx.expr() != null) {
                                logger.finest(" EXPR: " +ctx.expr().getText());
                                Value expr = visit(ctx.expr());
                                Statement stmnt = new Statement(mCFG, var, (IExpression)expr.get());
                                mCreator.addStatement(stmnt);
                                val = new Value(stmnt);
                        } else {
                                Statement stmnt = new Statement(mCFG, var, new ConcreteConstant(0, mCFG));
                                logger.finest(" ");
                                mCreator.addStatement(stmnt);
                                val = new Value(stmnt);
                        }
                } catch (Exception e) {
                        logger.severe("VarDECL:" + e);
                }
                return val;
        }

        @Override 
        public Value visitBlock(CymbolParser.BlockContext ctx) { 
                /*
                 *logger.finest("DEBUG: BLOCK Child Count:" + ctx.getChildCount());
                 */
                return visitChildren(ctx); 
        }

        @Override 
        public Value visitStatBlock(CymbolParser.StatBlockContext ctx) { 
                /*
                 *logger.finest("DEBUG: STATEMENT BLOCK");
                 */
                return visitChildren(ctx); 
        }

        @Override 
        public Value visitWhile(CymbolParser.WhileContext ctx) { 
                logger.finest("DEBUG: WHILE");
                mCreator.addWhile();
                visit(ctx.stat());
                return null;
                //return visitChildren(ctx); 
        }

        @Override 
        public Value visitIf(CymbolParser.IfContext ctx) { 
                int lineNo = ctx.getStart().getLine();
                boolean isTargetIf = false;
                boolean isTargetElse = false;
                String targetPath = targetMap.get(lineNo);
                if (targetPath != null) {
                        if (targetPath.equals("I")) {
                                isTargetIf = true;
                        } else if (targetPath.equals("B")) {
                        			isTargetIf = isTargetElse = true;
                        } else {
                                isTargetElse = true;
                        }
                }

                logger.finest("DEBUG: (" + lineNo + ") IF");

                //logger.finest("DEBUG: IF COND text: " + ctx.expr().getText());
                //logger.finest("DEBUG: 0 " + ctx.expr().getChildCount());
                //logger.finest("DEBUG: 1 " + ctx.expr().getChild(0).getText());
                Value vexp = visit(ctx.expr());
                mCreator.addConditional((IExpression) vexp.get(), isTargetIf);
                //mCreator.setThenBlock();
                visit(ctx.stat(0));
                if (ctx.stat(1) != null) {
                        mCreator.setElseBlock(isTargetElse);
                        visit(ctx.stat(1));
                }
                mCreator.resetIfBlock();
                return null;
        }


        @Override 
        public Value visitAssign(CymbolParser.AssignContext ctx) { 
                logger.finest("DEBUG: ASSIGN ");
                Value vleft = visit(ctx.expr(0));
                Value vright = visit(ctx.expr(1));
                Value val = null;
                try {
                        Statement assign = new Statement(mCFG, (IIdentifier)vleft.get(), (IExpression)vright.get());
                        mCreator.addStatement(assign);
                        val = new Value(assign);
                } catch (Exception e) {
                        logger.severe("ASSIGN: " + e);
                }
                return val;
        }

        @Override 
        public Value visitExp(CymbolParser.ExpContext ctx) { 
                logger.finest("DEBUG: EXP");
                Value vexp = visit(ctx.expr());
                mCreator.addStatement((IStatement)vexp);
                return vexp;
        }

        @Override 
        public Value visitNot(CymbolParser.NotContext ctx) { 
                logger.finest("DEBUG: NOT EXP");
                Value exp = visit(ctx.expr());
                Value val = null;
                try {
                        NotExpression nexp = new NotExpression(mCFG, (IExpression)exp.get());
                        val = new Value(nexp);
                } catch (Exception e) {
                        logger.severe("NOT: " + e);
                }
                return val;
        }

        @Override 
        public Value visitNotEqual(CymbolParser.NotEqualContext ctx) { 
                logger.finest("DEBUG: NOT EQUALS EXP: " + ctx.expr(0).getText() + " NOT EQUALS? " + ctx.expr(1).getText());
                Value vleft = visit(ctx.expr(0));
                Value vright = visit(ctx.expr(1));
                Value val = null;
                try {
                        EqualsExpression eql = new EqualsExpression(mCFG, (IExpression)vleft.get(), (IExpression)vright.get());
                        NotExpression neql = new NotExpression(mCFG, eql);
                        val = new Value(neql);
                } catch (Exception e) {
                        logger.severe("EQUALS: " + e);
                }
                return val;
        }

        @Override 
        public Value visitGreaterThan(CymbolParser.GreaterThanContext ctx) { 
                logger.finest("DEBUG: GREATER THAN EXP: " + ctx.expr(0).getText() + " GREATER THAN " + ctx.expr(1).getText());
                Value vleft = visit(ctx.expr(0));
                Value vright = visit(ctx.expr(1));
                Value val = null;
                try {
                        GreaterThanExpression grt = new GreaterThanExpression(mCFG, (IExpression)vleft.get(), (IExpression)vright.get());
						//logger.finest("DEBUG: LType - " + ((IExpression)vleft.get()).getType() + "RType - " + ((IExpression)vright.get()).getType());
                        val = new Value(grt);
                } catch (Exception e) {
                        logger.severe("Exception GREATER THAN: " + e);
                }
                return val;
        }

        @Override 
        public Value visitGreaterThanEqual(CymbolParser.GreaterThanEqualContext ctx) { 
                logger.finest("DEBUG: GREATER THAN EQUAL EXP: " + ctx.expr(0).getText() + " GREATER THAN EQUALS " + ctx.expr(1).getText());
                Value vleft = visit(ctx.expr(0));
                Value vright = visit(ctx.expr(1));
                Value val = null;
                try {
                        GreaterThanEqualToExpression grtEq = new GreaterThanEqualToExpression(mCFG, (IExpression)vleft.get(), (IExpression)vright.get());
                        val = new Value(grtEq);
                } catch (Exception e) {
                        logger.severe("GREATER THAN EQ: " + e);
                }
                return val;
        }

        @Override 
        public Value visitLessThan(CymbolParser.LessThanContext ctx) { 
                logger.finest("DEBUG: LESSER THAN EXP: " + ctx.expr(0).getText() + " LESSER THAN " + ctx.expr(1).getText());
                Value vleft = visit(ctx.expr(0));
                Value vright = visit(ctx.expr(1));
                Value val = null;
                try {
                        LesserThanExpression lrt = new LesserThanExpression(mCFG, (IExpression)vleft.get(), (IExpression)vright.get());
                        val = new Value(lrt);
                } catch (Exception e) {
                        logger.severe("LESSER THAN: " + e);
                }
                return val;
        }

        @Override 
        public Value visitLessThanEqual(CymbolParser.LessThanEqualContext ctx) { 
                logger.finest("DEBUG: LESSER THAN EQUAL EXP: " + ctx.expr(0).getText() + " LESSER THAN EQUALS " + ctx.expr(1).getText());
                Value vleft = visit(ctx.expr(0));
                Value vright = visit(ctx.expr(1));
                Value val = null;
                try {
                        LesserThanEqualToExpression lrtEq = new LesserThanEqualToExpression(mCFG, (IExpression)vleft.get(), (IExpression)vright.get());
                        val = new Value(lrtEq);
                } catch (Exception e) {
                        logger.severe("LESSER THAN EQ: " + e);
                }
                return val;
        }

        @Override 
        public Value visitOrExp(CymbolParser.OrExpContext ctx) { 
                logger.finest("DEBUG: Or EXP: " + ctx.expr(0).getText() + " || " + ctx.expr(1).getText());
                Value vleft = visit(ctx.expr(0));
                Value vright = visit(ctx.expr(1));
                Value val = null;
                try {
                        OrExpression orExp = new OrExpression(mCFG, (IExpression)vleft.get(), (IExpression)vright.get());
						//logger.finest("DEBUG: LType - " + ((IExpression)vleft.get()).getType() + "RType - " + ((IExpression)vright.get()).getType());
                        val = new Value(orExp);
                } catch (Exception e) {
                        logger.severe("Exception Or: " + e);
                }
                return val;
        }

        @Override 
        public Value visitAndExp(CymbolParser.AndExpContext ctx) { 
                logger.finest("DEBUG: AND EXP: " + ctx.expr(0).getText() + " && " + ctx.expr(1).getText());
                Value vleft = visit(ctx.expr(0).getChild(1));
                Value vright = visit(ctx.expr(1).getChild(1));
                Value val = null;
				logger.finest("DEBUG: LType - " + ((IExpression)vleft.get()).getType() + "RType - " + ((IExpression)vright.get()).getType());
                try {
                        AndExpression andExp = new AndExpression(mCFG, (IExpression)vleft.get(), (IExpression)vright.get());
                        val = new Value(andExp);
                } catch (Exception e) {
                        logger.severe("Exception AND: " + e);
                }
                return val;
        }

        @Override 
        public Value visitMult(CymbolParser.MultContext ctx) { 
                logger.finest("DEBUG: MULT");
                Value vleft = visit(ctx.expr(0));
                Value vright = visit(ctx.expr(1));
                Value val = null;
                try {
                        MulExpression exp = new MulExpression(mCFG, (IExpression)vleft.get(), (IExpression)vright.get());
                        val = new Value(exp);
                } catch (Exception e){
                        logger.severe("MULT:" + e);
                }
                return val;
        }

        @Override 
        public Value visitAddSub(CymbolParser.AddSubContext ctx) { 
                logger.finest("DEBUG: ADDSUB");
                Value vleft = visit(ctx.expr(0));
                Value vright = visit(ctx.expr(1));
                Value val = null;
                try {
                			Expression exp;
                		    if (ctx.getChild(1).getText().equals("-"))
							exp = new SubExpression(mCFG, (IExpression)vleft.get(), (IExpression)vright.get());
                		    else 
							exp = new AddExpression(mCFG, (IExpression)vleft.get(), (IExpression)vright.get());
                        val = new Value(exp);
                } catch (Exception e){
                        logger.severe("ADDSUB:" + e);
                }
                return val;
        }

        @Override 
        public Value visitEqual(CymbolParser.EqualContext ctx) { 
                logger.finest("DEBUG: EQUAL");
                Value val = null;
                Value vleft = visit(ctx.expr(0));
                Value vright = visit(ctx.expr(1));
                try {
                        EqualsExpression exp = new EqualsExpression(mCFG, (IExpression)vleft.get(), (IExpression)vright.get());
                        val = new Value(exp);
                } catch (Exception e) {
                        logger.severe("EQUAL " + e);
                }
                return val;
        }

        @Override 
        public Value visitVar(CymbolParser.VarContext ctx) { 
                logger.finest("DEUBG: VAR:- " + ctx.ID().getText());
                Value val = null;
                try {
                        Variable var = new Variable(ctx.ID().getText(), mCFG);
                        val = new Value(var);
                } catch (Exception e) {
                        logger.severe("VAR:" + e);
                }
                return val;
        }

        @Override 
        public Value visitParens(CymbolParser.ParensContext ctx) { 
                
                logger.finest("DEBUG: Parens STUB ");
//                return visitChildren(ctx.expr()); 
                return visitChildren(ctx.expr()); 
        }

        @Override 
        public Value visitInt(CymbolParser.IntContext ctx) { 
                
                 logger.finest("DEBUG: INT="+ ctx.getText());
                 
                Value val = null; 
                try {
                        Integer intVal = Integer.parseInt(ctx.INT().getText());
                        if (ctx.MINUS() != null)
                        		intVal = intVal * -1;
                        ConcreteConstant constant = new ConcreteConstant(intVal, mCFG);
                        val = new Value(constant);
                } catch (Exception e) {
                        logger.severe("visitInt:"+e);
                }
                return val;
        }

        @Override 
        public Value visitInput(CymbolParser.InputContext ctx) { 
                logger.finest("DEBUG: INPUT");
                Value val = null;
                try {
                        Input inp = new Input(mCFG);
                        val = new Value(inp);
                } catch (Exception e) {
                        logger.severe("INPUT " + e);
                }
                return val;
        }
}

