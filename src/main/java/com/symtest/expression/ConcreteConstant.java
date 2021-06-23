package com.symtest.expression;

import com.symtest.program.IProgram;
import com.symtest.visitors.IExprVisitor;

public class ConcreteConstant  extends Expression {

	private final int mValue;
	//private final float mValue1;

	public ConcreteConstant(int value, IProgram program) throws Exception {
		super(program, Type.INT);
		this.mValue = value;
	}
	
	/*public ConcreteConstant(float value, IProgram program) throws Exception {
		super(program, Type.REAL);
		this.mValue1 = value;
	}*/

	public int getValue() {
		return this.mValue;
	}
	
	/*public float getValue1() {
		return this.mValue1;
	}*/
	
	@Override
	public String toString() {
		Integer i = new Integer(this.mValue);
		return i.toString();
	}

	@Override
	public void accept(IExprVisitor<?> visitor) {
	}
}
