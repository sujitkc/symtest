package frontend;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cfg.ICFG;
import expression.AddExpression;
import expression.ConcreteConstant;
import expression.EqualsExpression;
import expression.IExpression;
import expression.IIdentifier;
import expression.Input;
import expression.MulExpression;
import expression.NotExpression;
import expression.Variable;
import statement.IStatement;
import statement.Statement;
import tester.SymTest;
import tester.TestSequence;


public class CFGVisitor extends CymbolBaseVisitor<Value> {

        private ICFG mCFG;
        private CFGCreator mCreator;
        private HashMap<Integer, String> targetMap;

        public CFGVisitor(String targetFileName) {
                targetMap = new HashMap<Integer, String>();
                File targetFile = new File(targetFileName);
                try {
                        BufferedReader br = new BufferedReader(new FileReader(targetFile));
                        String line;
                        System.out.println("DEBUG: Target Mapping");
                        while ((line = br.readLine()) != null) {
                                String[] parts = line.trim().split("-");
                                targetMap.put(Integer.parseInt(parts[0]), parts[1]);
                                System.out.println("Line " + parts[0] + " " + parts[1]);
                        }
                } catch (Exception e) {
                        System.out.println(e);
                }
                System.out.println();
        }


        @Override 
        public Value visitFile(CymbolParser.FileContext ctx) { 
                mCreator = new CFGCreator();
                mCFG = mCreator.getCFG();
                visitChildren(ctx); 
                mCreator.linkLastNode();

                /*
				SymTest st = new SymTest(mCFG, mCreator.targets);
				TestSequence seq = st.generateTestSequence();
				System.out.println(seq);
				Map<IIdentifier, List<Object>> testseq = seq.getTestSequence();
				System.out.println(testseq);
				*/

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
                int lineNo = ctx.getStart().getLine();
                boolean isTargetIf = false;
                boolean isTargetElse = false;
                String targetPath = targetMap.get(lineNo);
                if (targetPath != null) {
                        if (targetPath.equals("I")) 
                                isTargetIf = true;
                        else 
                                isTargetElse = true;
                }


                System.out.println("DEBUG: (" + lineNo + ") IF");

                Value vexp = visit(ctx.expr());
                mCreator.addConditional((IExpression) vexp, isTargetIf);
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

