package com.symtest.RL;
import com.symtest.graph.*;
import com.symtest.mygraph.*;
import java.util.*;
public class State{
    private IPath path;
    public static final int numberOfEdges = 3;
    
    public State(IPath inpath)
    {
        this.path = new Path(inpath.getGraph());
        this.path.setPath(inpath.getPath());
    }

    public IPath Getpath()
    {
        return this.path;
    }

    /*
    @Override
    public int hashCode() 
    {
        final int prime = 31;
        int result = 1;
        for(IEdge edge: this.path.getPath())
        {
                result = prime * result + (edge.hashCode());
        }
        return result; 
    }
    */
    @Override
    public boolean equals(Object instate)
    {
        if(instate == null)
            return false;

        if(!(instate instanceof State))
            return false; 
        State state = (State)(instate); 

        return this.path.getPath().equals(state.Getpath().getPath());
    }

    public String toString() {
      return "state (" + this.path.toString() + ")";
    }
//    public static void main(String[] args)
//    {
//        System.out.println("testing State");
//    }
    //*/
}

