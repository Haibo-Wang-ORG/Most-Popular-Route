package network;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Map.Entry;

public class Network {
//	String trajectories_directory = "./DataSet/Trajectories_Directory/";
	String graph_path = "./DataSet/Graph/";
	String dest_prob_file = "./Dataset/Graph/Probability/v139.txt";
	
	private int destination = 139;
	
	public HashMap<Integer, Node> network;
	
	public int mprVistedNodes = 0;
	public int spVistedNodes = 0;
	
	public Network() {
		network = new HashMap<Integer, Node>();
		readNetwork();
	}
	
	public void readNetwork() {
		try {
			BufferedReader vReader = new BufferedReader(new FileReader(new File(graph_path + "vertices.txt")));
			BufferedReader eReader = new BufferedReader(new FileReader(new File(graph_path + "edges.txt")));
			BufferedReader pReader = new BufferedReader(new FileReader(new File(dest_prob_file)));
			
			String line;
			while ((line = vReader.readLine()) != null) {	// read nodes
				String[] fields = line.split(",");
				int nodeID = Integer.valueOf(fields[0]);
				float lon = Float.valueOf(fields[1]);
				float lat = Float.valueOf(fields[2]);
				
				network.put(nodeID, new Node(nodeID, lon, lat));
			}
			
			while ((line = eReader.readLine()) != null) {	// read edges
				String[] fields = line.split(",");
				int startID = Integer.valueOf(fields[0]);
				int endID = Integer.valueOf(fields[1]);
				double probability = Float.valueOf(fields[2]);
				double weight = distance(network.get(startID), network.get(endID));
				
				network.get(startID).addAdjNode(network.get(endID), probability, weight);
			}
			
			int vID = 0;
			while ((line = pReader.readLine()) != null) {	// read transfer probability
				double score = Double.valueOf(line);
				network.get(vID).score = score;
				
				if (vID == destination) network.get(vID).score = 1;
				vID ++;
			}
			
			vReader.close();
			eReader.close();
			pReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<Node> getMPR_old(int startID, int endID) {
		ArrayList<Node> result = new ArrayList<Node>();
		if (startID == endID) {
			result.add(network.get(startID));
			return result;
		}
		
		Comparator<Node> bigRootComparator = new Comparator<Node>() {
			public int compare(Node n1, Node n2) {
				if (n1.label == n2.label) {
					return n1.nodeID - n2.nodeID;
				} else if (n1.label > n2.label) {
					return -1;
				} else {
					return 1;
				}
			}
		};
		PriorityQueue<Node> PQ = new PriorityQueue<Node>(500, bigRootComparator);
		
		Iterator<Entry<Integer, Node>> it = network.entrySet().iterator();
		while (it.hasNext()) {
			Node node = it.next().getValue();
			node.label = 0;
			node.predecessor = null;
		}
		
		Node startNode = network.get(startID);
		Node endNode = network.get(endID);
		startNode.label = 1;
		PQ.add(startNode);
		int numberVisitedNodes = 0;
		while (!PQ.isEmpty()) {
			Node node = PQ.poll();
			node.visited = true;
			numberVisitedNodes ++;
			
			if (node.nodeID == endNode.nodeID) {
				result.add(0, node);
				while (node != startNode) {
					node = node.predecessor;
					result.add(0, node);
				}
				this.mprVistedNodes = numberVisitedNodes;
				return result;
			}
			
			for (Node adjNode : node.adjNodes) {
				if (adjNode.label < node.label*adjNode.score) {
					adjNode.label = node.label * adjNode.score;
					adjNode.predecessor = node;
					PQ.add(adjNode);
				}
			}
		}
		
		return result;
	}
	
	public ArrayList<Node> getMPR(int startID, int endID) {
		ArrayList<Node> result = new ArrayList<Node>();
		if (startID == endID) {
			result.add(network.get(startID));
			return result;
		}
		
		Comparator<Node> smallRootComparator = new Comparator<Node>() {
			public int compare(Node n1, Node n2) {
				if (n1.label == n2.label) {
					return n1.nodeID - n2.nodeID;
				} else if (n1.label > n2.label) {
					return 1;
				} else {
					return -1;
				}
			}
		};
		PriorityQueue<Node> PQ = new PriorityQueue<Node>(500, smallRootComparator);
		
		Iterator<Entry<Integer, Node>> it = network.entrySet().iterator();
		while (it.hasNext()) {
			Node node = it.next().getValue();
			node.label = Double.MAX_VALUE;
			node.predecessor = null;
		}
		
		Node startNode = network.get(startID);
		Node endNode = network.get(endID);
		startNode.label = 0;
		PQ.add(startNode);
		int numberVisitedNodes = 0;
		while (!PQ.isEmpty()) {
			Node node = PQ.poll();
			node.visited = true;
			numberVisitedNodes ++;
			
			if (node.nodeID == endNode.nodeID) {
				result.add(0, node);
				while (node != startNode) {
					node = node.predecessor;
					result.add(0, node);
				}
				this.spVistedNodes = numberVisitedNodes;
				return result;
			}
			
			for (Node adjNode : node.adjNodes) {
				if (adjNode.label > node.label + (int)(1.0/adjNode.score)) {
					adjNode.label = node.label + (int)(1.0/adjNode.score);
					adjNode.predecessor = node;
					PQ.add(adjNode);
				}
			}
		}
		
		return result;
	}
	
	public ArrayList<Node> getShortestPath(int startID, int endID) {
		ArrayList<Node> result = new ArrayList<Node>();
		if (startID == endID) {
			result.add(network.get(startID));
			return result;
		}
		
		Comparator<Node> smallRootComparator = new Comparator<Node>() {
			public int compare(Node n1, Node n2) {
				if (n1.label == n2.label) {
					return n1.nodeID - n2.nodeID;
				} else if (n1.label > n2.label) {
					return 1;
				} else {
					return -1;
				}
			}
		};
		PriorityQueue<Node> PQ = new PriorityQueue<Node>(500, smallRootComparator);
		
		Iterator<Entry<Integer, Node>> it = network.entrySet().iterator();
		while (it.hasNext()) {
			Node node = it.next().getValue();
			node.label = Double.MAX_VALUE;
			node.predecessor = null;
		}
		
		Node startNode = network.get(startID);
		Node endNode = network.get(endID);
		startNode.label = 0;
		PQ.add(startNode);
		int numberVisitedNodes = 0;
		while (!PQ.isEmpty()) {
			Node node = PQ.poll();
			node.visited = true;
			numberVisitedNodes ++;
			
			if (node.nodeID == endNode.nodeID) {
				result.add(0, node);
				while (node != startNode) {
					node = node.predecessor;
					result.add(0, node);
				}
				this.spVistedNodes = numberVisitedNodes;
				return result;
			}
			
			for (Node adjNode : node.adjNodes) {
				if (adjNode.label > node.label + distance(node, adjNode)) {
					adjNode.label = node.label + distance(node, adjNode);
					adjNode.predecessor = node;
					PQ.add(adjNode);
				}
			}
		}
		
		return result;
	}
	
	private double distance(Node n1, Node n2) {
		double deltaX = n1.longitude - n2.longitude;
		double deltaY = n1.latitude - n2.latitude;
		return Math.sqrt(deltaX*deltaX + deltaY*deltaY);
	}
	
	public static void main(String[] args) {
		Network network = new Network();
		int startID = 1199;
		int endID = 139;
		ArrayList<Node> mprResult = network.getMPR(startID, endID);
		ArrayList<Node> spResult = network.getShortestPath(startID, endID);
		
		System.out.println(mprResult.size() + "\t" + spResult.size());
		
		for (Node n: mprResult) 
			System.out.print(n.nodeID + " ");
		System.out.println();
		
		for (Node n: spResult)
			System.out.print(n.nodeID + " ");
		System.out.println();
		
		System.out.println(network.mprVistedNodes + "\t" + network.spVistedNodes);
	}
}
