package network;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.JFrame;

import sun.security.provider.certpath.AdjacencyList;

public class ShowDistribution extends JFrame{
	private static final long serialVersionUID = -2987784778420338980L;

	String trajectories_directory = "./DataSet/Trajectories_Directory/";
	
	Network network;
	
	BufferedImage img;
	
	int tminLon = 11597552;
	int tminLat = 3952585;
	int tmaxLon = 11725518;
	int tmaxLat = 4063243;
	
	int tdeltaHeight = tmaxLat - tminLat;
	int tdeltaWidth = tmaxLon - tminLon;
	
	int minLon = tminLon + tdeltaWidth/5;
	int minLat = tminLat + tdeltaHeight*1/5;
	int maxLon = tminLon + tdeltaWidth*5/5;
	int maxLat = tminLat + tdeltaHeight*1/2;
	
	int deltaHeight = maxLat - minLat;
	int deltaWidth = maxLon - minLon;
	
	int width, height;
	
//	int startID = 1777;
//	int startID = 99;
//	int startID = 999;
//	int startID = 2757;
	int startID = 2568;
	int endID = 139;
	
	float mean = 0.0052f; // for draw trajectories
	
	public ShowDistribution(int height) {
		network = new Network();
		
		this.height = height;
		this.width = (int) ((float)this.height * ((float)deltaWidth / (float)deltaHeight));
		img = new BufferedImage(this.height, this.width, BufferedImage.TYPE_INT_ARGB);
		Graphics g = img.createGraphics();
		
//		this.drawNetworks(g);
		
		ArrayList<Integer> trajIDs = getTrajIDs(endID, 500);
		this.drawTrajectories(g, trajIDs);
		
		ArrayList<Node> nodes = network.getMPR(startID, endID);
		this.drawSequentialNodes(g, nodes, Color.blue, Color.blue);
		System.out.println(getPathScore(nodes));
		
		nodes = network.getShortestPath(startID, endID);
		this.drawSequentialNodes(g, nodes, Color.black, Color.black);
		System.out.println(getPathScore(nodes));
		
		this.drawNode(g, startID, Color.gray, 24);
		this.drawNode(g, endID, Color.red, 24);
		
		g.dispose();
	}
	
	public void paint(Graphics g) {
		g.drawImage(img, 0, 0, null);
	}
	
	private void drawNetworks(Graphics g) {
		Iterator<Entry<Integer, Node>> it = network.network.entrySet().iterator();
		
		while (it.hasNext()) {
			Node n1 = it.next().getValue();
			int lon1 = (int)n1.longitude;
			int lat1 = (int)n1.latitude;
			for (Node n2: n1.adjNodes) {
				int lon2 = (int)n2.longitude;
				int lat2 = (int)n2.latitude;
				this.drawLine(g, lon1, lat1, lon2, lat2, Color.yellow);
			}
			int size = (int) (n1.score / mean);
			if (size < 1) size = 1;
			else if (size > 30) size = 30;
			this.drawNode(g, n1.nodeID, Color.magenta, size);
			this.drawNode(g, n1.nodeID, Color.gray, n1.adjNodes.size());
		}
	}
	
	private double getPathScore(ArrayList<Node> nodes) {
		double score = 1.0;
		
		System.out.print(nodes.size() + " : ");
		for (Node n : nodes) {
			System.out.print(n.score + " ");
			score *= n.score;
		}
		System.out.println();
		return score;
	}
	
	private void drawNode(Graphics g, int nodeID, Color color, int radius) {
		Node node = network.network.get(nodeID);
		int lon = (int)node.longitude;
		int lat = (int)node.latitude;
		
		g.setColor(color);
		g.fillRect(this.getScreenLon(lon), this.getScreenLat(lat), radius/2, radius/2);
	}
	
	private void drawLine(Graphics g, int lon1, int lat1, int lon2, int lat2, Color color) {
		g.setColor(color);
		g.drawLine(getScreenLon(lon1), getScreenLat(lat1), getScreenLon(lon2), getScreenLat(lat2));
	}
	
	private void drawSequentialNodes(Graphics g, ArrayList<Node> nodes, Color pointColor, Color lineColor) {
		int preLon = -1, preLat = -1;
		for (Node node: nodes) {
			int lon = (int)node.longitude;
			int lat = (int)node.latitude;
			int radius = 14;
			
			g.setColor(pointColor);
			g.fillRect(this.getScreenLon(lon), this.getScreenLat(lat), radius/2, radius/2);
			
			if (preLon == -1 && preLat == -1) {
				preLon = lon;
				preLat = lat;
				continue;
			}
			g.setColor(lineColor);
			g.drawLine(this.getScreenLon(preLon), this.getScreenLat(preLat), this.getScreenLon(lon), this.getScreenLat(lat));
			preLon = lon;
			preLat = lat;
		}
	}
	
	private void drawTrajectories(Graphics g, ArrayList<Integer> trajIDs) {
		try {
			File file = new File(trajectories_directory);
			String[] trajs = file.list();
			
			g.setColor(Color.yellow);
			for (String traj : trajs) {
				int id = Integer.valueOf(traj.split(".txt")[0]);
				if (!trajIDs.contains(id)) continue;
				BufferedReader reader = new BufferedReader(new FileReader(new File(trajectories_directory + traj)));
				String line;
				int preLon = -1, preLat = -1;
				while ((line = reader.readLine()) != null) {
					String[] fields = line.split(",");
					float lon = Float.valueOf(fields[2]);
					float lat = Float.valueOf(fields[3]);
					if (preLon == -1 && preLat == -1) {
						preLon = (int) lon;
						preLat = (int) lat;
						continue;
					}
					g.drawLine(getScreenLon(preLon), getScreenLat(preLat), getScreenLon((int)lon), getScreenLat((int)lat));
					preLon = (int)lon;
					preLat = (int)lat;
				}
				reader.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<Integer> getTrajIDs(int nodeID, float distance) {
		Node node = network.network.get(nodeID);
		ArrayList<Integer> trajIDs = new ArrayList<Integer>();
		
		try {
			File files = new File(trajectories_directory);
			for (String file: files.list()) {
				BufferedReader reader = new BufferedReader(new FileReader(new File(trajectories_directory + file)));
				
				String line;
				while ((line = reader.readLine()) != null) {
					String[] fields = line.split(",");
					float lon = Float.valueOf(fields[2]);
					float lat = Float.valueOf(fields[3]);
					
					if (distance(lon, lat, (float)node.longitude, (float)node.latitude) < distance) {
						trajIDs.add(Integer.valueOf(file.split(".txt")[0]));
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return trajIDs;
	}
	
	private int getScreenLon(int lon) {
		return width*(lon-minLon)/deltaWidth;
	}
	
	private int getScreenLat(int lat) {
		return height - height*(lat-minLat)/deltaHeight;
	}
	
	private float distance(float lon1, float lat1, float lon2, float lat2) {
		float deltaX = lon1 - lon2;
		float deltaY = lat1 - lat2;
		return (float)Math.sqrt(deltaX*deltaX+deltaY*deltaY);
	}
	
	public static void main(String[] args) {
		ShowDistribution show = new ShowDistribution(800);
		
		show.setBounds(50,50,800,800);
		show.setBackground(Color.WHITE);
		show.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		show.setVisible(true);
	}
}
