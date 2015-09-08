package network;

import java.util.ArrayList;

public class Node {
	public int nodeID;
	public double longitude, latitude;
	public double score; // transfer probability
	public ArrayList<Node> adjNodes;
	public ArrayList<Double> adjProbs; 
	
	public ArrayList<Double> weights; // Euclidean weights
	
	// for search
	public double label = 0;
	public Node predecessor = null;
	
	public boolean visited = true;
	
	public Node() {
		this.nodeID = -1;
		this.longitude = this.latitude = this.score = 0;
		this.adjNodes = new ArrayList<Node>();
		this.adjProbs = new ArrayList<Double>();
		this.weights = new ArrayList<Double>();
	}
	
	public Node(int nodeID, float longitude, float latitude) {
		this.nodeID = nodeID;
		this.longitude = longitude;
		this.latitude = latitude;
		this.score = 0;
		this.adjNodes = new ArrayList<Node>();
		this.adjProbs = new ArrayList<Double>();
		this.weights = new ArrayList<Double>();
	}
	
	public void addAdjNode(Node node, double prob, double weight) {
		this.adjNodes.add(node);
		this.adjProbs.add(prob);
		this.weights.add(weight);
	}
	
	public Node getLargetProb() {
		if (this.adjNodes.size() == 0) return null;
		
		int index = 0;
		double max = 0;
		for (int i=0; i<adjProbs.size(); i++) {
			if (adjProbs.get(i) > max) {
				index = i;
				max = adjProbs.get(i);
			}
		}
		return adjNodes.get(index);
	}
}
