package expression;

import program.IProgram;
import visitors.IExprVisitor;

public class GreaterThanExpression extends Expression implements IBinaryExpression {

	private IExpression mLHS;
	private IExpression mRHS;
	
	public GreaterThanExpression(IProgram program, IExpression lhs, IExpression rhs) throws Exception {
		super(program, Type.BOOLEAN);
		System.out.println("GT DEBUG: " + lhs + " " + rhs);
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
			System.out.println("GT DEBUG BEFORE VISIT " + mLHS + " " + mRHS);
			visitor.visit(this.mRHS);
			visitor.visit(this.mLHS);
			System.out.println("GT DEBUG after VISIT " + mLHS + " " + mRHS);
		} catch (Exception e) {
				e.printStackTrace();
		}
	}
}
