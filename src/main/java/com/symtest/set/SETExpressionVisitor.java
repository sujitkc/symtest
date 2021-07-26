package com.symtest.set;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import com.symtest.program.IProgram;
import com.symtest.visitors.IExprVisitor;
import com.symtest.expression.AddExpression;
import com.symtest.expression.AndExpression;
import com.symtest.expression.BooleanInput;
import com.symtest.expression.BooleanVariable;
import com.symtest.expression.ConcreteConstant;
import com.symtest.expression.DivExpression;
import com.symtest.expression.EqualsExpression;
import com.symtest.expression.False;
import com.symtest.expression.GreaterThanEqualToExpression;
import com.symtest.expression.GreaterThanExpression;
import com.symtest.expression.IExpression;
import com.symtest.expression.IIdentifier;
import com.symtest.expression.Input;
import com.symtest.expression.RealInput;
import com.symtest.expression.LesserThanEqualToExpression;
import com.symtest.expression.LesserThanExpression;
import com.symtest.expression.MulExpression;
import com.symtest.expression.NotExpression;
import com.symtest.expression.OrExpression;
import com.symtest.expression.SubExpression;
import com.symtest.expression.True;
import com.symtest.expression.Variable;

public class SETExpressionVisitor implements IExprVisitor<IExpression> {

	private SETNode mNode;
	private Stack<IExpression> mStack = new Stack<IExpression>();
	private final String mContextType; 

	public SETExpressionVisitor(SETNode node, String type) {
		this.mNode = node;
		this.mContextType = type;		
	}

	@Override
	public void visit(IExpression exp) throws Exception {
		if(exp instanceof ConcreteConstant) {
			this.visit((ConcreteConstant)exp);
		}
		else if(exp instanceof False) {
			this.visit((False)exp);
		}
		else if(exp instanceof GreaterThanExpression) {
			this.visit((GreaterThanExpression)exp);
		}
		else if(exp instanceof True) {
			this.visit((True)exp);
		}
		else if(exp instanceof Variable) {
			this.visit((Variable)exp);
		}
		else if(exp instanceof BooleanVariable) {
			this.visit((BooleanVariable)exp);
		}
		else if (exp instanceof Input) {
			this.visit((Input)exp);
		}
		else if(exp instanceof BooleanInput) {
			this.visit((BooleanInput)exp);
		}
		else if(exp instanceof RealInput) {
			this.visit((RealInput)exp);
		}
		else if(exp instanceof AddExpression) {
			this.visit((AddExpression)exp);
		}

		else if(exp instanceof SubExpression) {
			this.visit((SubExpression)exp);
		}

		else if(exp instanceof MulExpression) {
			this.visit((MulExpression)exp);
		}

		else if(exp instanceof DivExpression) {
			this.visit((DivExpression)exp);
		}

		else if(exp instanceof GreaterThanEqualToExpression) {
			this.visit((GreaterThanEqualToExpression)exp);
		}

		else if(exp instanceof LesserThanExpression) {
			this.visit((LesserThanExpression)exp);
		}

		else if(exp instanceof LesserThanEqualToExpression) {
			this.visit((LesserThanEqualToExpression)exp);
		}
		else if(exp instanceof AndExpression) {
			this.visit((AndExpression)exp);
		}
		else if(exp instanceof OrExpression) {
			this.visit((OrExpression)exp);
		}
		else if(exp instanceof NotExpression) {
			this.visit((NotExpression)exp);
		}
		else if(exp instanceof EqualsExpression) {
			this.visit((EqualsExpression)exp);
		}

		else if(exp == null) {
		}
		else {
			Exception e = new Exception("SETExpressionVisitor : Type '" + exp.getClass().getCanonicalName() + "' of expression not handled.");
			throw e;
		}
	}
	
	@Override
	public void visit(Input exp) {
		//System.out.println("=================>Input");
		IProgram p = this.mNode.getSET();
		Set<IIdentifier> variables = p.getVariables();
		Set<String> names = new HashSet<String>();
		for(IIdentifier v : variables) {
			names.add(v.getName());
		}
		String name = SETExpressionVisitor.generateNewVariableName(names);
		try {
			this.mStack.push(new Variable(name, this.mContextType, this.mNode.getSET()));
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void visit(RealInput exp) {
		//System.out.println("SETExpressionVisitor===========>RealInput");
		IProgram p = this.mNode.getSET();
		Set<IIdentifier> variables = p.getVariables();
		Set<String> names = new HashSet<String>();
		for(IIdentifier v : variables) {
			names.add(v.getName());
		}
		String name = SETExpressionVisitor.generateNewVariableName(names);
		try {
			this.mStack.push(new Variable(name, this.mContextType, this.mNode.getSET()));
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void visit(BooleanInput exp) {
		IProgram p = this.mNode.getSET();
		Set<IIdentifier> variables = p.getVariables();
		Set<String> names = new HashSet<String>();
		for(IIdentifier v : variables) {
			names.add(v.getName());
		}
		String name = SETExpressionVisitor.generateNewVariableName(names);
		try {
			this.mStack.push(new BooleanVariable(name, p));
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	@Override
	public void visit(ConcreteConstant exp) throws Exception {
		this.mStack.push(new ConcreteConstant(exp.getValue(), this.mNode.getSET()));
	}

	@Override
	public void visit(False exp) throws Exception {
		this.mStack.push(new False(this.mNode.getSET()));
	}

	@Override
	public void visit(GreaterThanExpression exp) throws Exception {
		exp.accept(this);
		IExpression lhs = this.mStack.pop();
		IExpression rhs = this.mStack.pop();
		this.mStack.push(new GreaterThanExpression(this.mNode.getSET(), lhs, rhs));
	}

	@Override
	public void visit(AddExpression exp) throws Exception {
		exp.accept(this);
		IExpression lhs = this.mStack.pop();
		IExpression rhs = this.mStack.pop();
		this.mStack.push(new AddExpression(this.mNode.getSET(), lhs, rhs));
	}

	@Override
	public void visit(SubExpression exp) throws Exception {
		exp.accept(this);
		IExpression lhs = this.mStack.pop();
		IExpression rhs = this.mStack.pop();
		this.mStack.push(new SubExpression(this.mNode.getSET(), lhs, rhs));
	}

	@Override
	public void visit(MulExpression exp) throws Exception {
		exp.accept(this);
		IExpression lhs = this.mStack.pop();
		IExpression rhs = this.mStack.pop();
		this.mStack.push(new MulExpression(this.mNode.getSET(), lhs, rhs));
	}



	@Override
	public void visit(DivExpression exp) throws Exception {
		exp.accept(this);
		IExpression lhs = this.mStack.pop();
		IExpression rhs = this.mStack.pop();
		this.mStack.push(new DivExpression(this.mNode.getSET(), lhs, rhs));
	}
	
	@Override
	public void visit(GreaterThanEqualToExpression exp) throws Exception {
		exp.accept(this);
		IExpression lhs = this.mStack.pop();
		IExpression rhs = this.mStack.pop();
		this.mStack.push(new GreaterThanExpression(this.mNode.getSET(), lhs, rhs));
	}
	
	@Override
	public void visit(LesserThanExpression exp) throws Exception {
		exp.accept(this);
		IExpression lhs = this.mStack.pop();
		IExpression rhs = this.mStack.pop();
		this.mStack.push(new LesserThanExpression(this.mNode.getSET(), lhs, rhs));
	}
	
	@Override
	public void visit(LesserThanEqualToExpression exp) throws Exception {
		exp.accept(this);
		IExpression lhs = this.mStack.pop();
		IExpression rhs = this.mStack.pop();
		this.mStack.push(new LesserThanEqualToExpression(this.mNode.getSET(), lhs, rhs));
	}

	@Override
	public void visit(True exp) throws Exception {
		this.mStack.push(new True(this.mNode.getSET()));
	}

	@Override
	public void visit(Variable exp) {
		this.mStack.push(this.mNode.getLatestValue(exp));
	}
	

	@Override
	public void visit(BooleanVariable exp) {
		this.mStack.push(this.mNode.getLatestValue(exp));
	}

	private static String generateNewVariableName (Set<String> names) {
		
		while (true) {
			Random random = new Random ();
			int integer = random.nextInt();
			if (integer < 0) {
				integer = -1 * integer;
			}
			String name = "symvar" + Integer.toString(integer);
			if (!names.contains(name)) {
				return name;
			}
		}
	}

	@Override
	public void visit(AndExpression exp) throws Exception {
		exp.accept(this);
		IExpression lhs = this.mStack.pop();
		IExpression rhs = this.mStack.pop();
		this.mStack.push(new AndExpression(this.mNode.getSET(), lhs, rhs));
	}


	@Override
	public void visit(OrExpression exp) throws Exception {
		exp.accept(this);
		IExpression lhs = this.mStack.pop();
		IExpression rhs = this.mStack.pop();
		this.mStack.push(new OrExpression(this.mNode.getSET(), lhs, rhs));
	}

	@Override
	public void visit(NotExpression exp) throws Exception {
		exp.accept(this);
		this.mStack.push(new NotExpression(this.mNode.getSET(), this.mStack.pop()));
	}

	@Override
	public void visit(EqualsExpression exp) throws Exception {
		exp.accept(this);
		IExpression lhs = this.mStack.pop();
		IExpression rhs = this.mStack.pop();
		this.mStack.push(new EqualsExpression(this.mNode.getSET(), lhs, rhs));
	}

	@Override
	public IExpression getValue() {
		return this.mStack.peek();
	}
}
