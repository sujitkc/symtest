package com.symtest.expression;

import com.symtest.program.IProgram;
import com.symtest.visitors.IExprVisitor;

public class RealInput extends Expression {

	public RealInput(IProgram program) throws Exception {
		super(program, Type.REAL);
	}

	
	@Override
	public String toString() {
		return "RealInput";
	}

	@Override
	public void accept(IExprVisitor<?> visitor) {
	}

}
