package com.symtest.mygraph;

import com.symtest.graph.IEdge;
import com.symtest.graph.IGraph;
import com.symtest.graph.IPath;

import java.util.ArrayList;
import java.util.List;

public class Path implements IPath {

	IGraph mGraph;
	List<IEdge> mPath = new ArrayList<IEdge>();

	public Path(IGraph graph) {
		this.mGraph = graph;
	}
	
	public void setPath(List<IEdge> path) {
		this.mPath = path;
	}

	public Path(IPath path) throws Exception {
		this.mGraph = path.getGraph();
		List<IEdge> p = path.getPath();
		for(int i = 0; i < p.size(); i++) {
			if(p.get(i).getClass().equals("Edge")) {
				this.mPath.add(p.get(i));
			}
			else {
				Exception e = new Exception("Couldn't construct path: type mismatch in element number " + i);
				throw(e);
			}
		}
	}
	
	@Override
	public ArrayList<IEdge> getPath() {
		return (ArrayList<IEdge>)this.mPath;
	}

	// Returns the edges on success; null on failure
	@Override
	public IEdge addEdge(IEdge edge) {
		if(!this.mGraph.hasEdge(edge)) {
			return null;
		}
		this.mPath.add(edge);
		return edge;
	}

	@Override
	public IGraph getGraph() {
		return this.mGraph;
	}

	@Override
	public int getSize() {
		return this.mPath.size();
	}

	@Override
	public void concatenate(IPath path) throws Exception {
		if(!this.mGraph.equals(path.getGraph())) {
			Exception e = new Exception("Path.concatenate failed: the 2 paths don't belong to the same graph.");
			throw e;
		}
		List<IEdge> p = path.getPath();
		for(int i = 0; i < p.size(); i++) {
			this.addEdge(p.get(i));
		}
	}

	public void concatenate(IEdge edge) throws Exception {
		if(!this.mGraph.equals(edge.getGraph())) {
			Exception e = new Exception("Path.concatenate failed: the path and edge don't belong to the same graph.");
			throw e;
		}
		this.addEdge(edge);
	}

	@Override
	public boolean hasEdge(IEdge e) {
		return this.mPath.contains(e);
	}

	public String toString() {
		String s = "<";
		for(int i = 0; i < this.getSize(); i++) {
			if(this.mPath.get(i) != null) {
				s = s + this.mPath.get(i).getId();
				s = s + " ";
			}
			else {
				s = s + "null ";
			}
		}
		s = s + ">";
		return s;
	}
	
	//EXTRA
	@Override
	public void removeDups() {
		String s, prev = null;
		ArrayList<Integer> dupIndex = new ArrayList<Integer>();
		for(int i = 0; i < this.getSize(); i++) {
			s = this.mPath.get(i).getId();
			if (s == prev) {
				dupIndex.add(i);
			}
			prev = s;
		}
		for (int i = 0; i < dupIndex.size(); ++i) {
			mPath.remove((int)dupIndex.get(i));
		}
	}
}
