package com.symtest.tester;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Logger;
import java.io.*;
import java.util.Random;

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

public class SymTest_RL extends SymTest {

  private double explore_probability = 0.5;
  private double probability_decay = 0.99998;
  private int edges_in_state = 3;
  private int n_backtracks = 0; 
  private Qtable qtable = new Qtable();
  public SymTest_RL(ICFG cfg, Set<ICFEdge> targets) {
    super(cfg, targets);
  }

  
  public SymTest_RL(ICFG cfg, Set<ICFEdge> targets, Set<ApplyHeuristics> heuristics) {
    super(cfg,targets,heuristics);
  }

  public SymTest_RL(ICFG cfg, Set<ICFEdge> targets, Set<ApplyHeuristics> heuristics, Qtable in_table) {
    super(cfg,targets,heuristics);
    qtable = in_table;
  }

  public void UpdateTargets(Set <ICFEdge> ntargets)
  {
    mTargets = new HashSet<>(ntargets);
  }
  
  public TestSequence generateTestSequence() {
    n_backtracks = 0;
    TestSequence testseq = null;
    try {
      
      //EXTRA
      Set<ICFEdge> terminalEdgeSet = new HashSet<ICFEdge>();
      terminalEdgeSet.add(mCFG.getStopNode().getOutgoingEdge());
      Set<IEdge> terminalTarget = convertTargetEdgesToGraphEdges(terminalEdgeSet);
      
      Set<IEdge> targets = convertTargetEdgesToGraphEdges(this.mTargets);
      Stack<Pair<IEdge, Boolean>> stack = new Stack<Pair<IEdge, Boolean>>();
      // Initialise the stack with start edge
      IEdge startEdge = this.mConvertor.getGraphEdge(mCFG.getStartNode()
          .getOutgoingEdgeList().get(0));
      
      stack.push(new Pair<IEdge, Boolean>(startEdge, true));
      ArrayList<IEdge> prefix = new ArrayList<IEdge>();

      IPath completePath = new Path(mGraph);
      Set<IEdge> currentTargets = new HashSet<IEdge>(targets);
      while ((!stack.isEmpty()) && !(stack.peek().getFirst().equals(startEdge) && !stack.peek()
          .getSecond())) {
        // Obtain the path that traverses the targets.
        IPath path;
        if (!hasEncounteredMaximumIterations(completePath)) {
          FindCFPathAlgorithm algorithm = new FindCFPathAlgorithm(
              this.mGraph, currentTargets,
              this.mConvertor.getGraphNode(this.mTarget));

          if (stack.size() != 1) {

            path = algorithm.findCFPath(stack.peek().getFirst()
                .getHead(), currentTargets);
          } else {
            prefix.add(startEdge);
            path = algorithm.findCFPath(stack.peek().getFirst()
                .getHead(), currentTargets);
          }
        } else {
          // If maximum iterations are done, it is only an empty path
          // that gets added
          path = new Path(mGraph);
//          FindCFPathAlgorithm algorithm = new FindCFPathAlgorithm(
//              this.mGraph, terminalTarget,
//              this.mConvertor.getGraphNode(this.mTarget));
//          path = algorithm.findLongestAcyclicPath(stack.peek().getFirst()
//                .getHead(), currentTargets);
//          System.out.println("FINAL PATH : " + path);
        }
        completePath.setPath(addprefix(prefix, path.getPath()));
        ArrayList<ICFEdge> cfPath = convertPathEdgesToCFGEdges(completePath);
        System.out.println("\nDEBUG1 FindCFPath:"+cfPath);
        // Construct the Symbolic Execution Tree
        set = SymTestUtil.getSET(cfPath, this.mCFG);
        // Solve the predicate
        SolverResult solution;
        
//        logger.fine("Complete cfPath: " + cfPath);
//        if (currentTargets.isEmpty()) {
          try {

            solution = SymTestUtil.solveSequence(set);
            return (this.convert(set.getLeafNodes().iterator().next(),
                solution));
          } catch (UnSatisfiableException e) {
            System.out.println("Unsatisfiable");
          }
//        }

        // Add heuristics
        if (this.heuristics != null) {
          for (ApplyHeuristics heuristic : heuristics) {
            try {
              solution = heuristic.performHeuristics(mGraph,
                  mTargets, completePath, mCFG, mConvertor);
              return (this.convert(set.getLeafNodes().iterator()
                  .next(), solution));

            } catch (UnSatisfiableException e) {
              e.printStackTrace();
            }
          }
        }
        if (!hasEncounteredMaximumIterations(completePath)) {
          logger.fine("Finding Longest Viable Prefix");

          // Get Longest Viable Prefix(LVP)
          int satisfiableIndex = SymTestUtil
              .getLongestSatisfiablePrefix(cfPath, mCFG);
          logger.finer("Satisfiable index: " + satisfiableIndex);
          logger.finer("Complete path size: " + completePath.getSize());
          logger.finer("path size: " + path.getSize());
          List<IEdge> satisfiablePrefix = new ArrayList<IEdge>();
          //TODO Figure out what this does
          satisfiablePrefix.addAll(completePath.getPath().subList(
              (completePath.getPath().size() - 1)
                  - path.getPath().size(),
              satisfiableIndex + 2));

          updatestack(stack, satisfiablePrefix);
          prefix.clear();

          prefix.addAll(completePath.getPath().subList(
              0,
              completePath.getPath().indexOf(
                  stack.peek().getFirst())));
        } else {
          prefix.clear();
          prefix.addAll(completePath.getPath().subList(
              0,
              completePath.getPath().lastIndexOf(
                  stack.peek().getFirst())));
          logger.finer("Complete path: " + completePath);
        }

        Stack <Pair <IEdge, Boolean>> mystack = new Stack<>();
        for(IEdge e: prefix)
        {
          mystack.push(new Pair <IEdge, Boolean>(e, false));
        }

        if(mystack.peek().getFirst().getHead().getId().charAt(0) != 'D')
          mystack.push(new Pair <IEdge, Boolean> (stack.peek().getFirst(), false));


        //System.out.println(stack.toString());
        stack = backtrack(mystack);
        //System.out.println(stack.toString());
        if (!stack.isEmpty()) {
          // Add the updated edge
          if (stack.peek().getFirst().getTail().getId() == prefix.get(prefix.size()-1).getHead().getId()) {
            prefix.add(stack.peek().getFirst());
          } else {
            int index = -1;
            for (int i = prefix.size()-1; i >= 0; i--) {
              if (stack.peek().getFirst().getTail().getId() == prefix.get(i).getHead().getId()) {
                index = i;
                break;
              }
            }

            prefix.subList(index+1, prefix.size()).clear();
            prefix.add(stack.peek().getFirst());
          }
        }

        currentTargets.removeAll(prefix);
        
        logger.finest("Stack: " + Arrays.toString(stack.toArray()));

      }
      System.out.println("Unsatisfiable finally");
    } catch (Exception e) {
      e.printStackTrace();
    }

    return testseq;
  }

  /**
   * This function pushes only the outgoing edge of decision node contained in the path into the stack. 
   * @param stack
   * @param path that hets added newly
   */
  /*
  Stack<Pair<IEdge, Boolean>> backtrack(Stack <Pair<IEdge, Boolean>> stack) 
  {
    try
    {
      System.out.println("inside Backtrack");
      
          
      //System.out.println(stack.toString());
      List <Pair <IEdge, Boolean>> computed_path_util = new ArrayList<Pair <IEdge, Boolean>> (stack);
      //Collections.reverse(computed_path_util);
      List <IEdge> computed_path = new ArrayList <IEdge>();
      for (Pair <IEdge, Boolean> it: computed_path_util)
      {
        computed_path.add(it.getFirst());
      }
        
      System.out.println("input stack " + stack);
      
      Utilities my_util = new Utilities(edges_in_state, mGraph, mCFG, this);
      my_util.initialize_table(computed_path, qtable);
      int backtrackpoint = my_util.find_backtrack_point(computed_path, qtable, explore_probability);
      
      System.out.println("\n RL backtrack point:"+ backtrackpoint);
      
      my_util.update_policy(computed_path, qtable, backtrackpoint);
      my_util.stack_update(computed_path, backtrackpoint, stack, backtrackpoint);
      
      System.out.println("output stack " + stack);
      n_backtracks++;
      System.out.println("backtrack number: " + n_backtracks);
      explore_probability = explore_probability * probability_decay;
      
      if(explore_probability<0.2)
        explore_probability = 0.2;
      
      return stack;
    }
    
    catch(Exception e)
    {
      //System.out.println("Backtrack exception");  
      stack.clear();
      e.printStackTrace();
      return stack;
    }
  }
  */
        // dstack    <- removeBasicBlockEdges(stack)
        // states    <- computeStates(dstack)
  // ustates   <- removeDuplicateStates(states) // unique states
  // rewards   <- [ qtable[s] for s in ustates ]
  // btp       <- computeBTP(ustates, rewards)
  // btp'      <- tiebreak(btp, states) // final backtracking point after tiebreaking
  // newstates <- prefix of states till btp'
  // newstack  <- stackify(newstates)
  Stack<Pair<IEdge, Boolean>> backtrack(Stack <Pair<IEdge, Boolean>> stack) 
  {
    try
    {
      System.out.println("stack = " + stack);
      // retain only the decision edges
      // dstack    <- removeBasicBlockEdges(stack)
      Stack <Pair<IEdge, Boolean>> dstack = new Stack <>();
      for(Pair<IEdge, Boolean> el : stack) {
         if(this.isDecisionEdge(el.getFirst())) {
          dstack.push(el);
        }
      }
      System.out.println("dstack = " + dstack);

      // compute state list
      // states    <- computeStates(dstack)
      List<IEdge> edgelist = new ArrayList<>();
      while(!dstack.isEmpty()) {
        edgelist.add(dstack.pop().getFirst());
      }
      Collections.reverse(edgelist);
      System.out.println("edgelist = " + edgelist);
      // ComputeStates<IEdge> C = new ComputeStates<>();
      List<State> states = this.computeStates(edgelist);
      System.out.println("states = " + states);

      // remove duplicates
      // ustates   <- removeDuplicateStates(states) // unique states
      List<State> ustates = new ArrayList<>();
      for(State state : states) {
        if(!ustates.contains(state)) {
          ustates.add(state);
        }
      }
      System.out.println("ustates = " + ustates);

      // compute rewards
      // rewards   <- [ qtable[s] for s in ustates ]
      List<Double> rewards = new ArrayList<>();
      for(State s : ustates) {
        rewards.add(this.qtable.GetValue(s));
      }
      System.out.println(rewards);

      // integerify rewards
      List<Integer> irewards = SymTest_RL.integerify(rewards);
      System.out.println(irewards);

      // compute backtracking point
      State btp = SymTest_RL.computeBTP(ustates, irewards);
      System.out.println(btp);

      // tiebreak
      State finalbtp = SymTest_RL.tiebreak(btp, states);

      this.unwindToBTP(finalbtp, stack);
      System.out.println("unwound stack = " + stack);
      System.exit(1);
      return stack;
    }
    
    catch(Exception e)
    {
      //System.out.println("Backtrack exception");  
      stack.clear();
      e.printStackTrace();
      return stack;
    }
  }

  private boolean isDecisionEdge(IEdge edge) {
    ICFEdge cfgEdge = this.mConvertor.getCFEdge(edge);
    System.out.println(cfgEdge.getTail().getClass());
      if(cfgEdge.getTail() instanceof ICFGDecisionNode) {
        return true;
      }
      return false;
  }

  private static List<State> computeStates(List<IEdge> edgelist) {
    final int k = State.numberOfEdges; 
    List<State> statelist = new ArrayList<>();
    if(!edgelist.isEmpty()) {
      for(int i = -k + 1; i <= edgelist.size() - k; i++) {
        List<IEdge> list = new ArrayList<>();
        for(int j = i; j < i + k; j++) {
          if(j < 0) {
            list.add(null);
          }
          else {
            list.add(edgelist.get(j));
          }
        }
        Path path = new Path(edgelist.get(0).getGraph());
        path.setPath(list);
        State state = new State(path);
        statelist.add(state);
      }
    }
    return statelist;
  }

  // A function that takes a list of values S
  // a list of probability values P.
  // |P| = |S|
  // Select one value from S as per the probability distribution of P.
  // Idea:
  // Create another list S' such that S' has each value s of S copied p times
  // where p is the probability value corresponding to s
  // For example:
  // S = [a, b, c, d, e]; P = [0.1, 0.2, 0.3, 0.2, 0.2 ]
  // then S' = [a, b, b, c, c, c, d, d, e, e]
  //
  // Then pick one value uniformly randomly from S'.
  
  public static State computeBTP(List<State> values, List<Integer> probabilities) {
    List<State> newvalues = new ArrayList<>();

    for(int i = 0; i < values.size(); i++) {
      for(int j = 0; j < probabilities.get(i); j++) {
        newvalues.add(values.get(i));
      }
    }
    return newvalues.get((new Random()).nextInt(newvalues.size()));
  }

  // [ 0.01, 0.33, 3.435 ]
  // [ 0.1, 3.3, 34.35 ]
  // [ 1, 33, 343.5 ]
  // [ 1, 33, 343 ]
  private static List<Integer> integerify(List<Double> list)  throws Exception {
    List<Boolean> masks = SymTest_RL.maskOut(list);
    while(!SymTest_RL.areAllGEOne(list, masks)) {
      list = into10(list);
    }
    List<Integer> ilist = new ArrayList<>();
    for(double d : list) {
      int i = (int)d;
      ilist.add(i);
    }
    return ilist;
  }

  private static List<Boolean> maskOut(List<Double> list) {
    Double lowerLimit = 0.001;
    List<Boolean> masks = new ArrayList<>();
    for(Double d : list) {
      if(d < lowerLimit) {
        masks.add(false);
      }
      else {
        masks.add(true);
      }
    }
    return masks;
  }

  private static List<Double> into10(List<Double> list) {
    List<Double>  newlist = new ArrayList<>();
    for(Double d : list) {
         newlist.add(10 * d);
    }
    return newlist;
  }

  private static boolean areAllGEOne(List<Double> list, List<Boolean> masks) throws Exception {
    if(list.size() != masks.size()) {
      throw new Exception("areAllGEOne : unequal list size");
    }
    for(int i = 0; i < list.size(); i++) {
      if(masks.get(i) == true && list.get(i) < 1) {
        return false;
      }
    }
    return true;
  }

  private static State tiebreak(State state, List<State> states) {
    return state;
  }

  private void unwindToBTP(State btp, Stack<Pair<IEdge, Boolean>> stack) throws Exception {
    System.out.println("stack = " + stack);
    while(!stack.isEmpty()) {
      Stack<Pair<IEdge, Boolean>> tempStack = new Stack<>();
      for(int i = 0; i < State.numberOfEdges; i++) {
        Pair<IEdge, Boolean> edge;
	if(btp.Getpath().getPath().get(State.numberOfEdges - i - 1) == null) {
          // the state has fewer than complete set of edges. Hence, the match has succeeded.
          while(!tempStack.isEmpty()) {
	    stack.push(tempStack.pop());
	  }
	  return;
	}
	try {
	  do {
            edge = stack.pop();
	  } while(!this.isDecisionEdge(edge.getFirst()));
	}
	catch(Exception e) {
          throw new Exception("Computed backtracking point doesn't exist in the stack.");
	}

	if(edge.getFirst().equals(btp.Getpath().getPath().get(State.numberOfEdges - i - 1))) {
	  tempStack.push(edge);
	}
	else {
	  tempStack.clear();
	  break;
	}
	if(tempStack.size() == State.numberOfEdges) { // success
	  while(!tempStack.isEmpty()) {
	    stack.push(tempStack.pop());
	  }
	  return;
	} // else failure; return to while
      }
    }
    throw new Exception("Computed backtracking point " + btp + "doesn't exist in the stack " + stack + ".");

  }
}
