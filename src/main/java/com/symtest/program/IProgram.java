package com.symtest.program;

import java.util.Set;

import com.symtest.expression.IIdentifier;

public interface IProgram {
	public IIdentifier addVariable(IIdentifier var);
	public Set<IIdentifier> getVariables();
	boolean hasVariable(IIdentifier var);
}
