package com.symtest.program;

import java.util.List;

import com.symtest.statement.IStatement;

public interface IBasicBlockNode extends IProgramNode {
	public List<IStatement> getStatements();
	public void addStatement(IStatement statement);
}
