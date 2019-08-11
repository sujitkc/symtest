package com.symtest.program;

import com.symtest.expression.IExpression;

public interface IDecisionNode extends IProgramNode {
	public IExpression getCondition();
}
