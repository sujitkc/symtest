package com.symtest.tester;

import java.time.Duration;
import java.time.Instant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Logger;
import java.io.*;

import com.symtest.Solver.SolverResult;
import com.symtest.cfg.ICFEdge;
import com.symtest.cfg.ICFG;
import com.symtest.cfg.ICFGDecisionNode;
import com.symtest.cfg.ICFGNode;
import com.symtest.exceptions.UnSatisfiableException;
import com.symtest.expression.IExpression;
import com.symtest.expression.IIdentifier;
import com.symtest.expression.True;
import com.symtest.expression.Variable;
import com.symtest.graph.IEdge;
import com.symtest.graph.IGraph;
import com.symtest.graph.INode;
import com.symtest.graph.IPath;
import com.symtest.heuristics.ApplyHeuristics;
import com.symtest.mygraph.Path;
import com.symtest.set.SET;
import com.symtest.set.SETBasicBlockNode;
import com.symtest.set.SETNode;
import com.symtest.utilities.Pair;
import com.symtest.RL.*;
import com.symtest.tester.SymTest;
import com.symtest.tester.SymTest_RL;

public class SymTest_RL_trained extends SymTest_basic {

    /*
	private double explore_probability = 0.5;
	private double probability_decay = 0.99998;
	private int edges_in_state = 3;
	private int n_backtracks = 0; 
	*/
    private Qtable my_table = new Qtable();
	public SymTest_RL_trained(ICFG cfg, Set<ICFEdge> targets) {
		super(cfg, targets);
	}

	
	public SymTest_RL_trained(ICFG cfg, Set<ICFEdge> targets, Set<ApplyHeuristics> heuristics) {
		super(cfg,targets,heuristics);
	}

	
	public TestSequence generateTestSequence() {
		TestSequence testseq = null;
		try {
			

                int train_iterations = 5;
				SymTest_RL st = new SymTest_RL(mCFG, mTargets, heuristics, my_table);

				Set <ICFEdge> filtered = new HashSet <>(mCFG.getEdgeSet());

				for(ICFEdge edge: mCFG.getEdgeSet())
				{
					if((edge.getTail().getId().equals("WHILE"))||(edge.getHead().getId().equals("WHILE")))
					{
						filtered.remove(edge);
					}
					if((edge.getTail().getId().equals("BEGIN"))||(edge.getHead().getId().equals("BEGIN")))
					{
						filtered.remove(edge);
					}
				}
				
				List <ICFEdge> list = new ArrayList <ICFEdge> (filtered);

				//System.out.println(list);
				while(train_iterations>0)
                {

					Collections.shuffle(list);
//					System.out.println(list);
					Set <ICFEdge> rTargets = new HashSet <ICFEdge> (list.subList(0, 2));
					st.UpdateTargets(rTargets);

					testseq = st.generateTestSequence();
					
                    System.out.println(testseq);
                    for(State name: my_table.get_table().keySet())
                    {
                        System.out.println(name.Getpath().toString());
                        System.out.println(my_table.GetValue(name));
                        //String s = name.Getpath().toString() + ": "  + my_table.GetValue(name) + "\n";
                        //myfile.append(s);
                    }
                    
                    train_iterations--;
                }

				st.UpdateTargets(mTargets);
				Instant start = Instant.now();
				
				//your code
                testseq = st.generateTestSequence();
				
				Instant end = Instant.now();
				
				Duration timeElapsed = Duration.between(start, end);
				
				System.out.println("Time taken: "+ timeElapsed.getNano()/(1e6) +" milliseconds");

			
			System.out.println("Unsatisfiable finally");
		} 
        catch (Exception e) {
			e.printStackTrace();
		}

		return testseq;
	}

	/**
	 * This function pushes only the outgoing edge of decision node contained in the path into the stack. 
	 * @param stack
	 * @param path that hets added newly
	 */
	

}