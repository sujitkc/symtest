package com.symtest.RL;
import com.symtest.graph.*;
import com.symtest.mygraph.*;
import java.util.*;
public class State{
    private IPath mypath;
    public State(IPath inpath)
    {
        this.mypath = new Path(inpath.getGraph());
        this.mypath.setPath(inpath.getPath());
    }
    public IPath Getpath()
    {
        return this.mypath;
    }

    @Override
    public int hashCode() 
    {
        final int prime = 31;
        int result = 1;
        for(IEdge edge: this.mypath.getPath())
        {
                result = prime * result + (edge.hashCode());
        }
        return result; 
    }

    @Override
    public boolean equals(Object instate)
    {
        if(instate == null)
            return false;

        if(!(instate instanceof State))
            return false; 
        State state = (State)(instate); 

        return this.mypath.getPath().equals(state.Getpath().getPath());
    }


    public static void main(String[] args)
    {
        System.out.println("testing State");
    }
    //*/
}

