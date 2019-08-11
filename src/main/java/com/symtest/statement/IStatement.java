package com.symtest.statement;

import com.symtest.program.IProgram;
import com.symtest.expression.IExpression;
import com.symtest.expression.IIdentifier;

public interface IStatement {
	public IIdentifier getLHS();
	public IExpression getRHS();
	public IProgram getProgram();
}
