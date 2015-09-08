package graph_constructor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import rstar.Data;

public class BuildGraph {
	String cluster_trajectory_data = "./DataSet/Cluster_Trajectory_Data/clusters.txt";
	String graph_path = "./DataSet/Graph/";
	String trajectories_directory = "./DataSet/Trajectories_Directory/";
	
	public HashMap<Integer, Vertex> graph; // vertexID, vertex
	private HashMap<Integer, Integer> mapping; // global_pointID, vertexID
	
	private float[][] probability;
	private int vNumber;
	private double delta = 2000; // meters, for determining score = exp(-dis/delta)
	
	private int destinationID = 12;
	
	public BuildGraph() {
		graph = new HashMap<Integer, Vertex>();
		mapping = new HashMap<Integer, Integer>();
	}
	
	public void build() {
		vNumber = readClusters();
		buildGraph();
		probability = getInitialProbability(graph.get(destinationID));
		writeInitialProbability(probability, destinationID);
		writeGraph();
	}
	
	private int readClusters() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(cluster_trajectory_data)));
			
			String line;
			int size = 0;
			float totalLon = 0, totalLat = 0;
			int vertexID = 0;
			Vertex vertex = new Vertex(vertexID);
			while ((line = reader.readLine()) != null) {
				String[] fields = line.split(",");
				
				if (fields[0].equals("#")) { // start a new cluster
					size = Integer.valueOf(fields[1]);
					vertex = new Vertex(vertexID);
					totalLon = totalLat = 0.0f;
				} else if (fields[0].equals("*")) { // end reading a cluster
					vertex.longitude = totalLon / size;
					vertex.latitude = totalLat / size;
					graph.put(vertexID, vertex);
					vertexID ++;
				} else { // read points
					float lon = Float.valueOf(fields[2]);
					float lat = Float.valueOf(fields[3]);
					
					Data point = new Data();
					point.data[0] = lon;
					point.data[1] = lon;
					point.data[2] = lat;
					point.data[3] = lat;
					point.trajectoryID = Integer.valueOf(fields[0]);
					point.pointID = Integer.valueOf(fields[1]);
					
					vertex.points.add(point);
					mapping.put(Util.getID(point), vertexID);
					totalLon += lon;
					totalLat += lat;
				}
			}
			
			reader.close();
			// print the number of vertices been read
			System.out.println("Read " + vertexID + " clusters done!");
			return vertexID;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	private void buildGraph() {
		HashSet<Integer> trajIDs = new HashSet<Integer>(); // for recording the trajectories that have been processed
		
		Iterator<Entry<Integer, Vertex>> it = graph.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Integer, Vertex> entry = it.next();
			Vertex v1 = entry.getValue();
			ArrayList<Data> points = v1.points;
			trajIDs.clear();
			
			for (Data p1: points) { // check every point in v1
				if (trajIDs.contains(p1.trajectoryID)) { // skip this point
					continue;
				} else {
					trajIDs.add(p1.trajectoryID);
				}
				
				try {
					BufferedReader reader = new BufferedReader(new FileReader(new File(trajectories_directory + p1.trajectoryID + ".txt")));
					
					String line;
					// trace down from point p1 until the next cluster or vertex
					while ((line = reader.readLine()) != null) {
						String[] fields = line.split(",");
						int pointID = Integer.valueOf(fields[1]);
						
						if (pointID <= p1.pointID) continue;
						
						Data p2 = new Data();
						p2.pointID = pointID;
						p2.trajectoryID = p1.trajectoryID;
						
						// encounter another vertex v2
						if (mapping.containsKey(Util.getID(p2)) && mapping.get(Util.getID(p2))!=v1.vertexID) {
							Vertex v2 = graph.get(mapping.get(Util.getID(p2)));
							if (v1.edges.containsKey(v2.vertexID)) { // existed edge
								Edge edge = v1.edges.get(v2.vertexID);
								edge.trajs.add(p1.trajectoryID);
								edge.startPointIDs.add(p1.pointID);
								edge.endPointIDs.add(p2.pointID);
							} else { // new edge
								Edge edge = new Edge(v1.vertexID, v2.vertexID);
								edge.addTrajectory(p1.trajectoryID);
								edge.startPointIDs.add(p1.pointID);
								edge.endPointIDs.add(p2.pointID);
								v1.edges.put(v2.vertexID, edge);
							}
							
							break; // encounter another vertex, so we finish processing the trajectory of p1, 
								   // by get the adjacent edge of p1
						}
					}
					
					reader.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private float[][] getInitialProbability(Vertex target) {
		float[][] transitionProbability = new float[vNumber][vNumber];
		for (int i=0; i<vNumber; i++)
			for (int j=0; j<vNumber; j++)
				transitionProbability[i][j] = 0.0f;
		
		Data targetData = new Data();
		targetData.data = new float[2*2];
		targetData.data[0] = target.longitude;
		targetData.data[1] = target.longitude;
		targetData.data[2] = target.latitude;
		targetData.data[3] = target.latitude;
		
		Iterator<Entry<Integer, Vertex>> it = graph.entrySet().iterator();
		while (it.hasNext()) { // for each vertex in the Graph
			Vertex vertex = it.next().getValue();
			
			HashMap<Integer, Edge> edges = vertex.edges;
			if (edges.size() == 0) { // set as an absorbing state
				transitionProbability[vertex.vertexID][vertex.vertexID] = 1.0f;
				continue;
			}
			
			if (vertex.vertexID == target.vertexID) { // set target node as an absorbing state
				transitionProbability[vertex.vertexID][target.vertexID] = 1.0f;
				continue;
			}
			
			// processing other transient state vertices
			HashMap<Edge, Float> scores = new HashMap<Edge, Float>();
			float totalScore = 0;
			Iterator<Entry<Integer, Edge>> edgeIT = edges.entrySet().iterator();
			while (edgeIT.hasNext()) { // for each outward edge of the vertex
				Edge edge = edgeIT.next().getValue();
				
				float score = 0.0f;
				for (int i=0; i<edge.trajs.size(); i++) {
					int trajID = edge.trajs.get(i);
					int startPointID = edge.startPointIDs.get(i);
					float minDist = minDist(targetData, trajID, startPointID);
					score += score(minDist);
				}
				
				scores.put(edge, score);
				totalScore += score;
			}
			
			Iterator<Entry<Edge, Float>> scoreIT = scores.entrySet().iterator();
			while (scoreIT.hasNext()) {
				Entry<Edge, Float> scoreEntry = scoreIT.next();
				Edge edge = scoreEntry.getKey();
				edge.score = scoreEntry.getValue() / totalScore;
				transitionProbability[edge.startVertexID][edge.endVertexID] = edge.score;
			}
		}
		
		return transitionProbability;
	}
	
	/**
	 * The minimum distance from trajectory (denoted by trajID) to targetData 
	 * since a certain start point (denoted by startPointID)
	 * @param targetData Destination point
	 * @param trajID Trajectory ID
	 * @param startPointID Start Point ID
	 * @return the minimum distance between targetData and trajectory start from startPointID point
	 */
	private float minDist(Data targetData, int trajID, int startPointID) {
		float minDist = Float.MAX_VALUE;
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(trajectories_directory + trajID + ".txt")));
			
			String line;
			while ((line = reader.readLine()) != null) {
				String[] fields = line.split(",");
				int pointID = Integer.valueOf(fields[1]);
				if (pointID <= startPointID) continue; 
				
				Data currentData = new Data();
				currentData.data[0] = currentData.data[1] = Float.valueOf(fields[2]);
				currentData.data[2] = currentData.data[3] = Float.valueOf(fields[3]);
				float distance = (float)Util.distance(currentData, targetData);
				if (distance < minDist) minDist = distance;
			}
			
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return minDist;
	}
	
	private float score(float distance) {
		return (float)Math.exp(-distance/delta);
	}
	
	private void writeInitialProbability(float[][] probability, int destinationVertexID) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(graph_path + "Probability/" + destinationVertexID + ".txt")));
			
			writer.write("#," + vNumber + "\n");
			for (int i=0; i<vNumber; i++) {
				for (int j=0; j<vNumber; j++) {
					if (j == vNumber-1) 
						writer.write(probability[i][j] + "\n");
					else
						writer.write(probability[i][j] + ",");
				}
				writer.flush();
			}
			
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void writeGraph() {
		try {
			BufferedWriter vWriter = new BufferedWriter(new FileWriter(new File(graph_path + "vertices.txt")));
			BufferedWriter eWriter = new BufferedWriter(new FileWriter(new File(graph_path + "edges.txt")));
			
			Iterator<Entry<Integer, Vertex>> vIt = graph.entrySet().iterator();
			while (vIt.hasNext()) {
				Vertex vertex = vIt.next().getValue();
				vWriter.write(vertex.vertexID + "," + vertex.longitude + "," + vertex.latitude + "\n");
				
				Iterator<Entry<Integer, Edge>> eIt = vertex.edges.entrySet().iterator();
				while (eIt.hasNext()) {
					Edge edge = eIt.next().getValue();
					eWriter.write(edge.startVertexID + "," + edge.endVertexID + "," + edge.score + "\n");
				}
			}
			
			vWriter.flush();
			vWriter.close();
			eWriter.flush();
			eWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		BuildGraph buildGraph = new BuildGraph();
		buildGraph.build();
	}
	
}
