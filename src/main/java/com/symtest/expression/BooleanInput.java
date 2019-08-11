package com.symtest.expression;

import com.symtest.program.IProgram;
import com.symtest.visitors.IExprVisitor;

public class BooleanInput extends Expression {

	public BooleanInput(IProgram program) throws Exception {
		super(program, Type.BOOLEAN);
	}

	
	@Override
	public String toString() {
		return "BooleanInput";
	}

	@Override
	public void accept(IExprVisitor<?> visitor) {
	}

}
