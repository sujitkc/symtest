package com.symtest.Solver;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import com.symtest.expression.ExpressionPreorderToStringVisitor;
import com.symtest.expression.IExpression;
import com.symtest.expression.IIdentifier;
import com.symtest.expression.Type;


public class Z3Solver implements ISolver {

	IExpression mExpression;
	Set<IIdentifier> mVariables;

	public Z3Solver(Set<IIdentifier> symVars, IExpression exp) {
		this.mVariables = symVars;
		this.mExpression = exp;
	}

	public SolverResult solve() throws Exception {
		String z3Input = Z3Solver.makeZ3Input(this.mVariables,
				this.mExpression);

		//System.out.println("z3 input :\n" + z3Input);

		FileWriter outFile;

		outFile = new FileWriter("resources/input.smt2");
		PrintWriter out = new PrintWriter(outFile);
		out.println(z3Input);
		out.close();
		String command = "z3 resources/input.smt2";
		String output = Z3Solver.cmdExec(command);
		//System.out.println("z3 output :\n" + output);

		SolverResult result = this.parseZ3Output(output);
		System.out.print("solver result = " + result.toString());
		return result;

	}

	/**
	 * Uses Z3 Solver
	 * @param symVars
	 * @param expression
	 * @return
	 * @throws Exception
	 */
	private static String makeZ3Input(Set<IIdentifier> symVars,
			IExpression expression) throws Exception {
		

		ExpressionPreorderToStringVisitor preVisitor = new ExpressionPreorderToStringVisitor();

		preVisitor.visit(expression);
		String formula = preVisitor.getValue();

		String s = "";
		for (IIdentifier v : symVars) {
			s = s + "(declare-fun " + v.getName() + " () "
					+ Z3Solver.getVariableTypeString(v) + ")" + "\n";
		}
		s = s + "(assert " + formula + ")\n";
		s = s + "(check-sat)\n";
		s = s + "(get-model)\n";
		s = s + "(exit)";
		return s;

	}

	private static String getVariableTypeString(IIdentifier var) throws Exception {
		String type = var.getType();
		if (type.equals(Type.BOOLEAN)) {
			return "Bool";
		} else if (type.equals(Type.INT)) {
			return "Int";
		} else {
			Exception e = new Exception(
					"Z3Solver.getVariableTypeString : type of variable '"
							+ var.getName() + "' not handled.");
			throw e;
		}
	}

	private static String cmdExec(String cmdLine) throws IOException {
		String line;
		String output = "";

		Process p = Runtime.getRuntime().exec(cmdLine);
		BufferedReader input = new BufferedReader(new InputStreamReader(
				p.getInputStream()));
		line = input.readLine();
		while (line != null) {
			output += (line + '\n');
			line = input.readLine();
		}
		input.close();

		return output;
	}

	private SolverResult parseZ3Output(String output) throws Exception {
		StringTokenizer tokeniser = new StringTokenizer(output, " )\n", false);
		List<String> tokens = new ArrayList<String>();

		while (tokeniser.hasMoreTokens()) {
			tokens.add(tokeniser.nextToken());
		}
		Boolean isSat = false;
	/*	int m = 0;
		  System.out.print("tokens = "); for(String t : tokens) {
		  System.out.print(m + t + " "); m++;}
	*/ 
		Map<IIdentifier, Object> map = new HashMap<IIdentifier, Object>();
		if (tokens.get(0).equals("sat")) {
			isSat = true;

			for (int i = 3; i < tokens.size(); i = i + 5) {
				String varName = tokens.get(i);
				//System.out.println("Varname-->"+varName);
				IIdentifier var = this.getVariableByName(varName);
				if (var == null) {
					Exception e = new Exception(
							"Z3Solver.parseZ3Output : variable '"
									+ varName + "' not found.");
					throw e;
				}
				//System.out.println("1->"+tokens.get(i + 3));
				
				Object value;
				if(tokens.get(i + 3).equals("(-"))
				{
					//System.out.println("2->"+tokens.get(i + 4));
				    	value = Z3Solver.parseVariableValue(var,
						"-"+tokens.get(i + 4));
					i = i + 1;
				}
				else{
					value = Z3Solver.parseVariableValue(var,
						tokens.get(i + 3));
				}
				
				System.out.println(var+"-->"+value);
				
				map.put(var, value);
			}
		}
		return new SolverResult(isSat, map);
	}

	private static Object parseVariableValue(IIdentifier var, String value)
			throws Exception {
		if (var.getType().equals(Type.INT)) {
			return Integer.parseInt(value);
		} else if (var.getType().equals(Type.BOOLEAN)) {
			return Boolean.parseBoolean(value);
		} else {
			System.out.println(var.getName()+ "not handled");
			Exception e = new Exception("Z3Solver.parseVariableValue : type of variable '"+ var.getName() + "' not handled.");
			throw e;
		}
	}

	private IIdentifier getVariableByName(String name) {
		for (IIdentifier v : this.mVariables) {
			if (v.getName().equals(name)) {
				return v;
			}
		}
		return null;
	}
}

