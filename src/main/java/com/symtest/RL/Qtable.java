package com.symtest.RL;

import com.symtest.RL.State;
import com.symtest.graph.*;
import com.symtest.mygraph.*;
import java.util.*;
public class Qtable{
    public static final double R0 = 0.1;

    private HashMap <State, Double > table = new HashMap<State, Double>();
    public Qtable()
    {
        this.table.clear(); 
    }
    public void AddState(State s, Double v)
    {
        this.table.put(s, v);
    }
    public void UpdateState(State s, Double v)
    {
        this.table.put(s, v);
    }
    public double GetValue(State s)
    {
        if(this.table.containsKey(s))
        {
            return this.table.get(s);
        }   
        return this.R0; 
    }
    public Boolean CheckState(State s)
    {
        return this.table.containsKey(s);
    }
    public HashMap <State, Double> get_table()
    {
        return this.table;
    }
    public HashMap <State, Double> GetTable()
    {
        return table;
    }
    public double GetReward(IPath path, Set <IEdge> intargets, IEdge startedge)
    {
        Set <IEdge> targets = new HashSet <IEdge> (intargets);
        double net_rew;
        Integer rew=0;
        Integer pen=0;
        ArrayList <IEdge> edgelist = path.getPath();
        for(IEdge it: edgelist)
        {
            if(targets.contains(it))
            {
                rew++;
                targets.remove(it);
            }
            if(it==startedge)
            {
                pen++;
            }
        }
        if(startedge == null)
            pen = 0;
        
        net_rew = rew *10 - 2*pen; 
        return net_rew;
    }
    public void main(String[] args) throws Exception
    {

        Node root = new Node (null);
        Graph G = new Graph(root);
        Node n1 = new Node(G);
        Node n2 = new Node(G);
        Node n3 = new Node(G);
        Node n4 = new Node(G);
        Node n5 = new Node(G);
        Node n6 = new Node(G);
        Node n7 = new Node(G);
        Node n8 = new Node(G);


        Edge E0 = new Edge(G, root, n1);
        Edge E1 = new Edge(G, n1, n2);
        Edge E2 = new Edge(G, n2, n3);
        Edge E3 = new Edge(G, n3, n4);
        Edge E4 = new Edge(G, n4, n5);
        Edge E5 = new Edge(G, n5, n6);
        Edge E6 = new Edge(G, n6, n7);
        Edge E7 = new Edge(G, n7, n8);


        Path testpath = new Path(G); 


        testpath.addEdge(E0);
        testpath.addEdge(E1);
        testpath.addEdge(E2);
        testpath.addEdge(E3);

        Path testpath1 = new Path(testpath);
        testpath.addEdge(E4);

        Path testpath2 = new Path(testpath);
        testpath.addEdge(E5);
        
        Path testpath3 = new Path(testpath);
        testpath.addEdge(E6);
        testpath.addEdge(E7);

        State S1 = new State(testpath1);
        State S2 = new State(testpath2);
        State S3 = new State(testpath3);

        //System.out.println(S1.Getpath().toString());
        //System.out.println(S2.Getpath().toString());
        //System.out.println(S3.Getpath().toString());


        IEdge startedge = E0;

        Set <IEdge> testtargets = new HashSet <IEdge>();

        /*testtargets.add(E1);
        testtargets.add(E5);
        System.out.println("reward for s1 path " + Qtable.GetReward(S1.Getpath(), testtargets, startedge));
        System.out.println("reward for s2 path " + Qtable.GetReward(S2.Getpath(), testtargets, startedge));
        System.out.println("reward for s3 path " + Qtable.GetReward(S3.Getpath(), testtargets, startedge));
        //*/
    }
}
