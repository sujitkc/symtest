package com.symtest.expression;

import com.symtest.program.IProgram;
import com.symtest.visitors.IExprVisitor;

public class SubExpression extends Expression implements IBinaryExpression {

	private IExpression mLHS;
	private IExpression mRHS;
	
	public SubExpression(IProgram program, IExpression lhs, IExpression rhs) throws Exception {
		super(program, lhs.getType());
//		System.out.println(program + " " + lhs + " " + rhs);
		if(lhs.getType() != rhs.getType()) {
			Exception e = new Exception("SubExpression : Type error.");
			throw e;
		}
		this.mLHS = lhs;
		this.mRHS = rhs;
	}

	@Override
	public IExpression getLHS() {
		return this.mLHS;
	}

	@Override
	public IExpression getRHS() {
		return this.mRHS;
	}

	@Override
	public String toString() {
		return "(" + this.mLHS.toString() + " - " + this.mRHS.toString() + ")";
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
