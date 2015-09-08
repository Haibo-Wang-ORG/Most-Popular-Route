package graph_constructor;

import java.util.ArrayList;
import java.util.HashMap;

import rstar.Data;

public class Vertex {
	public int vertexID;
	public float longitude, latitude;
	public ArrayList<Data> points;
	
	public HashMap<Integer, Edge> edges; // target_vertexID, edges(vertexID->target_vertexID)
	
	public Vertex(int id) {
		this.vertexID = id;
		
		points = new ArrayList<Data>();
		edges = new HashMap<Integer, Edge>();
	}
}
