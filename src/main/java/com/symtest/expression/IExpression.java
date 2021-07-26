package com.symtest.expression;

import com.symtest.program.IProgram;
import com.symtest.visitors.IAcceptor;

public interface IExpression extends IAcceptor {
	public IProgram getProgram();
	public void setProgram(IProgram program);
	public String getType();
	public String toString();
}
