package com.symtest.frontend;

/**
 * The ANTLR visitor pattern. Each function defines a set of operations to be
 * done whenever a grammar clause is identified.
 * The visitor recursively traverses each node in the AST and invokes functions
 * corresponding to each grammar rule.
 * Each function is expected to have the same return type. Here Value type is
 * chosen as a wrapper for any specific type object that each function might
 * return.
 * There's a function corresponding to each rule in the grammar. In case there's
 * nothing special to be done, the function just visits its children or is
 * omitted (the default definition does the same thing).
 * 
 * For more info about ANTLR and and its functions, please refer "The Definitive
 * ANTLR 4 Reference". It's an amazing book!
 * 
 * TODO 
 * Add rules for Booleans & Division
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.symtest.cfg.ICFG;
import com.symtest.cymbol.CymbolBaseVisitor;
import com.symtest.cymbol.CymbolParser;
import com.symtest.expression.AddExpression;
import com.symtest.expression.AndExpression;
import com.symtest.expression.ConcreteConstant;
import com.symtest.expression.EqualsExpression;
import com.symtest.expression.Expression;
import com.symtest.expression.False;
import com.symtest.expression.GreaterThanEqualToExpression;
import com.symtest.expression.GreaterThanExpression;
import com.symtest.expression.IExpression;
import com.symtest.expression.IIdentifier;
import com.symtest.expression.Input;
import com.symtest.expression.LesserThanEqualToExpression;
import com.symtest.expression.LesserThanExpression;
import com.symtest.expression.MulExpression;
import com.symtest.expression.NotExpression;
import com.symtest.expression.OrExpression;
import com.symtest.expression.SubExpression;
import com.symtest.expression.Type;
import com.symtest.expression.Variable;
import com.symtest.statement.IStatement;
import com.symtest.statement.Statement;
import com.symtest.tester.SymTest;
import com.symtest.tester.TestSequence;


public class CFGVisitor extends CymbolBaseVisitor<Value> {

	private ICFG mCFG;
	/**
	 * The main object used for CFG creation. Provides convenience function to
	 * add program elements to the CFG. Please refer the source file for
	 * detailed
	 * comments.
	 */
	private CFGCreator mCreator;
	/**
	 * Mapping of Line Number to Coverage type. Coverage applies only to
	 * conditional nodes and their then/else edges.
	 * Coverage type could be
	 * - I : Cover only the then part of the condition.
	 * - E : Cover only the else part of the condition.
	 * - B : Cover both the parts of the condition.
	 * Target edges are highlighted in red in the visualized CFG.
	 */
	private Map<Integer, String> targetMap;

	private static final Logger logger = Logger
			.getLogger(CFGVisitor.class.getName());

	/**
	 * Read the target edges and coverage type.
	 * Target file format is:
	 * LINE_NO - COVERAGE_TYPE
	 * 
	 * @param targetFileName
	 *            Contains the line number - coverage type mapping.
	 */
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

	/**
	 * The first method invoked by the visitor at the beginning of the source
	 * file.
	 * Sets up initial values and initiates traversal.
	 */
	@Override
	public Value visitFile(CymbolParser.FileContext ctx) {
		mCreator = new CFGCreator();
		mCFG = mCreator.getCFG();
		// visit the nodes in the AST
		visitChildren(ctx);
		mCreator.linkLastNode();

		// TODO Add more documentation about what's going on here.
		// TODO Move this out of here into Driver
		SymTest st = new SymTest(mCFG, mCreator.targets);
		TestSequence seq = st.generateTestSequence();
		Map<IIdentifier, List<Object>> testseq = seq.getTestSequence();
		System.out.println("Test Seq: " + testseq);

		return null;
	}

	/**
	 * Grammar rule:
	 * varDecl : type ID ('=' expr)? ';'
	 * Ex:
	 * int v1 = 22;
	 * boolean isEmpty = !isFull;
	 */
	@Override
	public Value visitVarDecl(CymbolParser.VarDeclContext ctx) {
		logger.finest("Type-" + ctx.type().getText() + " varId-"
				+ ctx.ID().getText());

		Value val = null;
		try {
			String varType = ctx.ID().getText();
			Variable var = new Variable(varType, mCFG);
			if (ctx.expr() != null) {
				logger.finest("Expr-" + ctx.expr().getText());
				Value expr = visit(ctx.expr());
				Statement stmnt = new Statement(mCFG, var,
						(IExpression) expr.get());
				mCreator.addStatement(stmnt);
				val = new Value(stmnt);
			} else {
				// all variables are given a default value of 0/False if default
				// value is absent.
				Statement stmnt = null;
				if (var.getType() == Type.BOOLEAN) {
					stmnt = new Statement(mCFG, var, new False(mCFG));
				} else {
					stmnt = new Statement(mCFG, var,
							new ConcreteConstant(0, mCFG));
				}
				mCreator.addStatement(stmnt);
				val = new Value(stmnt);
			}
		} catch (Exception e) {
			logger.severe("VarDECL:" + e);
		}
		return val;
	}

	/*
	 * @Override
	 * public Value visitBlock(CymbolParser.BlockContext ctx) {
	 * return visitChildren(ctx);
	 * }
	 * 
	 * @Override
	 * public Value visitStatBlock(CymbolParser.StatBlockContext ctx) {
	 * return visitChildren(ctx);
	 * }
	 */

	/**
	 * Grammar rule:
	 * 'while' expr stat
	 * 
	 * The expression is ignored for now. It is assumed that there will only be
	 * one loops that operates infinitely.
	 */
	@Override
	public Value visitWhile(CymbolParser.WhileContext ctx) {
		mCreator.addWhile();
		visit(ctx.stat());
		return null;
	}

	/**
	 * Grammar rule:
	 * 'if' '('expr')' stat ('else' stat)?
	 * 
	 */
	@Override
	public Value visitIf(CymbolParser.IfContext ctx) {
		int lineNo = ctx.getStart().getLine();
		logger.finest("If Line#" + lineNo);

		boolean isTargetIf = false;
		boolean isTargetElse = false;
		// Check whether an if edge is in the target set.
		String targetPath = targetMap.get(lineNo);

		if (targetPath != null) {
			if (targetPath.equals("I")) {
				isTargetIf = true;
			} else if (targetPath.equals("E")) {
				isTargetElse = true;
			} else {
				isTargetIf = isTargetElse = true;
			}
		}

		// Get the if condition.
		Value vexp = visit(ctx.expr());
		// Add the then block.
		mCreator.addConditional((IExpression) vexp.get(), isTargetIf);
		visit(ctx.stat(0));
		// Check if an else part exists.
		// TODO Clean up. Find better logic to check the presence of else.
		if (ctx.stat().size() > 1) {
			// Add else block.
			mCreator.setElseBlock(isTargetElse);
			visit(ctx.stat(1));
		}
		mCreator.resetIfBlock();
		return null;
	}

	/**
	 * Grammar rule:
	 * expr '=' expr ';'
	 */
	@Override
	public Value visitAssign(CymbolParser.AssignContext ctx) {
		logger.finest("Assign");
		// visit lhs
		Value vleft = visit(ctx.expr(0));
		// visit rhs
		Value vright = visit(ctx.expr(1));
		Value val = null;
		try {
			Statement assign = new Statement(mCFG, (IIdentifier) vleft.get(),
					(IExpression) vright.get());
			mCreator.addStatement(assign);
			val = new Value(assign);
		} catch (Exception e) {
			logger.severe("Assign: " + e);
		}
		return val;
	}

	/**
	 * Grammar rule:
	 * expr ';'
	 */
	@Override
	public Value visitExp(CymbolParser.ExpContext ctx) {
		logger.finest("Exp");
		Value vexp = visit(ctx.expr());
		mCreator.addStatement((IStatement) vexp);
		return vexp;
	}

	/**
	 * Grammar rule:
	 * '!' expr
	 */
	@Override
	public Value visitNot(CymbolParser.NotContext ctx) {
		logger.finest("Not exp");
		Value exp = visit(ctx.expr());
		Value val = null;
		try {
			NotExpression nexp = new NotExpression(mCFG,
					(IExpression) exp.get());
			val = new Value(nexp);
		} catch (Exception e) {
			logger.severe("Not: " + e);
		}
		return val;
	}

	/**
	 * Grammar rule:
	 * expr '!=' expr
	 */
	@Override
	public Value visitNotEqual(CymbolParser.NotEqualContext ctx) {
		logger.finest("Not equals");
		Value vleft = visit(ctx.expr(0));
		Value vright = visit(ctx.expr(1));
		Value val = null;
		try {
			EqualsExpression eql = new EqualsExpression(mCFG,
					(IExpression) vleft.get(), (IExpression) vright.get());
			NotExpression neql = new NotExpression(mCFG, eql);
			val = new Value(neql);
		} catch (Exception e) {
			logger.severe("Not equals: " + e);
		}
		return val;
	}

	/**
	 * Grammar rule:
	 * expr '>' expr
	 */
	@Override
	public Value visitGreaterThan(CymbolParser.GreaterThanContext ctx) {
		logger.finest("Greater than");
		Value vleft = visit(ctx.expr(0));
		Value vright = visit(ctx.expr(1));
		Value val = null;
		try {
			GreaterThanExpression grt = new GreaterThanExpression(mCFG,
					(IExpression) vleft.get(), (IExpression) vright.get());
			val = new Value(grt);
		} catch (Exception e) {
			logger.severe("Greater than: " + e);
		}
		return val;
	}

	/**
	 * Grammar rule:
	 * expr '>=' expr
	 */
	@Override
	public Value visitGreaterThanEqual(
			CymbolParser.GreaterThanEqualContext ctx) {
		logger.finest("Greater than or equal");
		Value vleft = visit(ctx.expr(0));
		Value vright = visit(ctx.expr(1));
		Value val = null;
		try {
			GreaterThanEqualToExpression grtEq = new GreaterThanEqualToExpression(
					mCFG, (IExpression) vleft.get(),
					(IExpression) vright.get());
			val = new Value(grtEq);
		} catch (Exception e) {
			logger.severe("Greater than or equal: " + e);
		}
		return val;
	}

	/**
	 * Grammar rule:
	 * expr '<' expr
	 */
	@Override
	public Value visitLessThan(CymbolParser.LessThanContext ctx) {
		logger.finest("Lesser than");
		Value vleft = visit(ctx.expr(0));
		Value vright = visit(ctx.expr(1));
		Value val = null;
		try {
			LesserThanExpression lrt = new LesserThanExpression(mCFG,
					(IExpression) vleft.get(), (IExpression) vright.get());
			val = new Value(lrt);
		} catch (Exception e) {
			logger.severe("Lesser than: " + e);
		}
		return val;
	}

	/**
	 * Grammar rule:
	 * expr '<=' expr
	 */
	@Override
	public Value visitLessThanEqual(CymbolParser.LessThanEqualContext ctx) {
		logger.finest("Lesser than or equal");
		Value vleft = visit(ctx.expr(0));
		Value vright = visit(ctx.expr(1));
		Value val = null;
		try {
			LesserThanEqualToExpression lrtEq = new LesserThanEqualToExpression(
					mCFG, (IExpression) vleft.get(),
					(IExpression) vright.get());
			val = new Value(lrtEq);
		} catch (Exception e) {
			logger.severe("Lesser than or equal: " + e);
		}
		return val;
	}

	/**
	 * Grammar rule:
	 * expr '||' expr
	 * 
	 * Caveat: Or exps must be parenthesized.
	 * TODO : Fix the caveat.
	 */
	@Override
	public Value visitOrExp(CymbolParser.OrExpContext ctx) {
		logger.finest("Or exp");
		Value vleft = visit(ctx.expr(0).getChild(1));
		Value vright = visit(ctx.expr(1).getChild(1));
		Value val = null;
		try {
			OrExpression orExp = new OrExpression(mCFG,
					(IExpression) vleft.get(), (IExpression) vright.get());
			val = new Value(orExp);
		} catch (Exception e) {
			logger.severe("Or exp: " + e);
		}
		return val;
	}

	/**
	 * Grammar rule:
	 * expr '&&' expr
	 * 
	 * Caveat: And exps must be parenthesized.
	 * TODO : Fix the caveat.
	 */
	@Override
	public Value visitAndExp(CymbolParser.AndExpContext ctx) {
		logger.finest("And Exp");
		Value vleft = visit(ctx.expr(0).getChild(1));
		Value vright = visit(ctx.expr(1).getChild(1));
		Value val = null;
		try {
			AndExpression andExp = new AndExpression(mCFG,
					(IExpression) vleft.get(), (IExpression) vright.get());
			val = new Value(andExp);
		} catch (Exception e) {
			logger.severe("And Exp: " + e);
		}
		return val;
	}

	/**
	 * Grammar rule:
	 * expr '*' expr
	 */
	@Override
	public Value visitMult(CymbolParser.MultContext ctx) {
		logger.finest("Mult exp");
		Value vleft = visit(ctx.expr(0));
		Value vright = visit(ctx.expr(1));
		Value val = null;
		try {
			MulExpression exp = new MulExpression(mCFG,
					(IExpression) vleft.get(), (IExpression) vright.get());
			val = new Value(exp);
		} catch (Exception e) {
			logger.severe("Mult exp: " + e);
		}
		return val;
	}

	/**
	 * Grammar rule:
	 * expr ('+' | '-') expr
	 */
	@Override
	public Value visitAddSub(CymbolParser.AddSubContext ctx) {
		logger.finest("Add/Sub exp");
		Value vleft = visit(ctx.expr(0));
		Value vright = visit(ctx.expr(1));
		Value val = null;
		try {
			Expression exp;
			if (ctx.getChild(1).getText().equals("-")) {
				exp = new SubExpression(mCFG, (IExpression) vleft.get(),
						(IExpression) vright.get());
			} else {
				exp = new AddExpression(mCFG, (IExpression) vleft.get(),
						(IExpression) vright.get());
			}
			val = new Value(exp);
		} catch (Exception e) {
			logger.severe("Add/Sub exp:" + e);
		}
		return val;
	}

	/**
	 * Grammar rule:
	 * expr '==' expr
	 */
	@Override
	public Value visitEqual(CymbolParser.EqualContext ctx) {
		logger.finest("Equals exp");
		Value val = null;
		Value vleft = visit(ctx.expr(0));
		Value vright = visit(ctx.expr(1));
		try {
			EqualsExpression exp = new EqualsExpression(mCFG,
					(IExpression) vleft.get(), (IExpression) vright.get());
			val = new Value(exp);
		} catch (Exception e) {
			logger.severe("Equals exp: " + e);
		}
		return val;
	}

	/**
	 * Grammar rule:
	 * ID
	 */
	@Override
	public Value visitVar(CymbolParser.VarContext ctx) {
		logger.finest("varName: " + ctx.ID().getText());
		Value val = null;
		try {
			Variable var = new Variable(ctx.ID().getText(), mCFG);
			val = new Value(var);
		} catch (Exception e) {
			logger.severe("Var exp:" + e);
		}
		return val;
	}

	/*
	 * @Override
	 * public Value visitParens(CymbolParser.ParensContext ctx) {
	 * return visitChildren(ctx.expr());
	 * }
	 */

	@Override
	public Value visitInt(CymbolParser.IntContext ctx) {
		logger.finest("Int: " + ctx.getText());
		Value val = null;
		try {
			Integer intVal = Integer.parseInt(ctx.INT().getText());
			if (ctx.MINUS() != null)
				intVal = intVal * -1;
			ConcreteConstant constant = new ConcreteConstant(intVal, mCFG);
			val = new Value(constant);
		} catch (Exception e) {
			logger.severe("Int: " + e);
		}
		return val;
	}

	/**
	 * Grammar rule:
	 * 'input()'
	 */
	@Override
	public Value visitInput(CymbolParser.InputContext ctx) {
		logger.finest("Input");
		Value val = null;
		try {
			Input inp = new Input(mCFG);
			val = new Value(inp);
		} catch (Exception e) {
			logger.severe("Input: " + e);
		}
		return val;
	}
}
