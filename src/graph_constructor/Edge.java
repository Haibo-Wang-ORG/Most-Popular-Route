package graph_constructor;

import java.util.ArrayList;

public class Edge {
	public int startVertexID, endVertexID;
	public float score;
	
	public ArrayList<Integer> trajs;
	public ArrayList<Integer> startPointIDs; // Indicate where the trajectory starts.
	public ArrayList<Integer> endPointIDs;
	
	public Edge(int s_id, int e_id) {
		this.startVertexID = s_id;
		this.endVertexID = e_id;
		
		this.trajs = new ArrayList<Integer>();
		this.startPointIDs = new ArrayList<Integer>();
		this.endPointIDs = new ArrayList<Integer>();
	}
	
	public void addTrajectory(int trajID) { // Attention: if add a trajectory, the startPointIDs and endPointIDs are also need to be updated
		trajs.add(trajID);
	}
}
