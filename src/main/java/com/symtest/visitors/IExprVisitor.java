package com.symtest.visitors;

import com.symtest.expression.AddExpression;
import com.symtest.expression.AndExpression;
import com.symtest.expression.BooleanVariable;
import com.symtest.expression.ConcreteConstant;
import com.symtest.expression.DivExpression;
import com.symtest.expression.EqualsExpression;
import com.symtest.expression.False;
import com.symtest.expression.GreaterThanEqualToExpression;
import com.symtest.expression.GreaterThanExpression;
import com.symtest.expression.IExpression;
import com.symtest.expression.Input;
import com.symtest.expression.BooleanInput;
import com.symtest.expression.RealInput;
import com.symtest.expression.LesserThanEqualToExpression;
import com.symtest.expression.LesserThanExpression;
import com.symtest.expression.MulExpression;
import com.symtest.expression.NotExpression;
import com.symtest.expression.OrExpression;
import com.symtest.expression.SubExpression;
import com.symtest.expression.True;
import com.symtest.expression.Variable;

public interface IExprVisitor<T> {
	void visit(Input exp);
	
	void visit(BooleanInput exp);
	
	void visit(RealInput exp) throws Exception;

	void visit(ConcreteConstant exp) throws Exception;

	void visit(False exp) throws Exception;

	void visit(GreaterThanExpression exp) throws Exception;

	void visit(AddExpression exp) throws Exception;

	void visit(SubExpression exp) throws Exception;

	void visit(MulExpression exp) throws Exception;

	void visit(DivExpression exp) throws Exception;

	void visit(LesserThanExpression exp) throws Exception;

	void visit(LesserThanEqualToExpression exp) throws Exception;

	void visit(GreaterThanEqualToExpression exp) throws Exception;

	void visit(AndExpression exp) throws Exception;

	void visit(OrExpression exp) throws Exception;

	void visit(True exp) throws Exception;

	void visit(Variable exp) throws Exception;

	void visit(BooleanVariable exp) throws Exception;

	void visit(NotExpression exp) throws Exception;

	void visit(EqualsExpression exp) throws Exception;

	void visit(IExpression exp) throws Exception;

	public T getValue();

}
