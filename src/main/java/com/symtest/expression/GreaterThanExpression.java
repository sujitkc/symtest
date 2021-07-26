package com.symtest.expression;

import com.symtest.program.IProgram;
import com.symtest.visitors.IExprVisitor;

public class GreaterThanExpression extends Expression implements IBinaryExpression {

	private IExpression mLHS;
	private IExpression mRHS;
	
	public GreaterThanExpression(IProgram program, IExpression lhs, IExpression rhs) throws Exception {
		super(program, Type.BOOLEAN);
//		System.out.println("GT DEBUG: " + lhs + " " + rhs);
		if(lhs.getType() != rhs.getType()) {
			Exception e = new Exception("GreaterThanExpression : Type error " + lhs.getType() + " " + rhs.getType());
			throw e;
		}
		this.mLHS = lhs;
		this.mRHS = rhs;
	}

	public IExpression getLHS() {
		return this.mLHS;
	}

	public IExpression getRHS() {
		return this.mRHS;
	}

	@Override
	public String toString() {
		return "(" + this.mLHS.toString() + " > " + this.mRHS.toString() + ")";
	}

	@Override
	public void accept(IExprVisitor<?> visitor) {
		try {
			visitor.visit(this.mRHS);
			visitor.visit(this.mLHS);
		} catch (Exception e) {
				e.printStackTrace();
		}
	}
}
