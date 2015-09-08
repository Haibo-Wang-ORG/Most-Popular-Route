package graph_constructor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Map.Entry;

import rstar.Data;

/**
 * Clustering the interpolated trajectory data points.
 * 
 * @author uqhwan15
 * @since 2011/04/28
 */
public class Clustering {
	String interpolated_trajectory_data = "./DataSet/Interpolated_Trajectory_Data/interpolated_trajectory.txt";
	String cluster_trajectory_data = "./DataSet/Cluster_Trajectory_Data/clusters.txt";
	
	private double threshold = 0.8f;
	private double delta = 200; // meters
	private double alpha = 2;
	private double beta = 2;
	private double range = Math.pow(-Math.log(threshold), 1/alpha) * delta;
	
	HashMap<Integer, ArrayList<Data>> clusters; // cluster_id, cluster
	HashMap<Integer, Data> points;				// point_id, point
	HashMap<Integer, Integer> pointToCluster;	// point_id, cluster_id
	
	RTreeDB rtreeDB;
	
	public Clustering() {
		rtreeDB = new RTreeDB();
		
		clusters = new HashMap<Integer, ArrayList<Data>>();	// cluster_id, cluster
		points = new HashMap<Integer, Data>();				// point_id, point
		pointToCluster = new HashMap<Integer, Integer>();	// point_id, cluster_id
	}
	
	public void doClustering() {
		retrievePoints();
		cluster();
		writeToFile();
	}
	
	private void retrievePoints() {
		String line;
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(interpolated_trajectory_data)));
			
			while ((line = reader.readLine()) != null) {
				if (line.trim().equals("")) continue; // skip empty line
				
				String[] fields = line.split(",");
				int pointID = Integer.valueOf(fields[1]);
				float lon = Float.valueOf(fields[2]);
				float lat = Float.valueOf(fields[3]);
				float direction = Float.valueOf(fields[4]);
				
				Data point = new Data(2, pointID);
				point.data = new float[2*2];
				point.data[0] = lon; //LX
				point.data[1] = lon; //UX
				point.data[2] = lat; //LY
				point.data[3] = lat; //UY
				point.direction = direction;
				point.trajectoryID = Integer.valueOf(fields[0]);
				point.pointID = pointID;
				
				points.put(Util.getID(point), point);
				pointToCluster.put(Util.getID(point), -1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void cluster() {
		Iterator<Entry<Integer, Data>> it = points.entrySet().iterator();
		int clusterID = 0;
		
		while (it.hasNext()) {
			Entry<Integer, Data> entry = it.next();
			Data point = entry.getValue();
			int pointID = entry.getKey();
			
			if (pointToCluster.get(pointID) == -1) { // not classified yet
				// create a new cluster
				ArrayList<Data> newCluster = new ArrayList<Data>();
				newCluster.add(point);
				clusters.put(clusterID, newCluster);
				
				// setup mapping
				pointToCluster.put(pointID, clusterID++);
				
				expandCluster(point);
			}
		}
	}
	
	private void expandCluster(Data point) {
		Queue<Data> seeds = new LinkedList<Data>();
		seeds.add(point);
		
		while (!seeds.isEmpty()) {
			Data seed = seeds.poll();
			int seedID = Util.getID(seed);
			int clusterID = pointToCluster.get(seedID);
			
			ArrayList<Data> moreSeeds = rtreeDB.rangeQuery(seed.data[0], seed.data[2], (float)range);
			for (Data moreSeed : moreSeeds) {
				int moreSeedID = Util.getID(moreSeed);
				if ((pointToCluster.get(moreSeedID) == -1) && (similarity(moreSeed, seed) > threshold)) {
					// add point to seed's cluster
					ArrayList<Data> cluster = clusters.get(clusterID);
					cluster.add(points.get(moreSeedID));
					
					// setup mapping
					pointToCluster.put(moreSeedID, clusterID);
					
					seeds.add(moreSeed);
				}
			}
		}
	}
	
	private double similarity(Data point, Data point2) {
		if(Util.getID(point) == Util.getID(point2)) return 0.f;
		
		float x1 = point.data[0];
		float y1 = point.data[2];
		float x2 = point2.data[0];
		float y2 = point2.data[2];
		double deltaX = x2 - x1;
		double deltaY = y2 - y1;
		
		double Edis = Math.sqrt(deltaX * deltaX + deltaY * deltaY);				// Euclidian distance
		double Ddis = Math.abs(Math.sin(point2.direction - point.direction));	// Directional distance
		
		return (Math.exp(-Math.pow(Edis/delta, alpha)) * Math.pow(Ddis, beta));
	}
	
	private void writeToFile() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(cluster_trajectory_data)));
			
			Iterator<Entry<Integer, ArrayList<Data>>> it = clusters.entrySet().iterator();
			int num = 0;
			while (it.hasNext()) {
				Entry<Integer, ArrayList<Data>> entry = it.next();
				ArrayList<Data> cluster = entry.getValue();
				
				if (cluster.size() < 3) continue; // skip small cluster
				
				num ++;
				writer.write("#,"+cluster.size()+"\n");
				for (Data point: cluster) {
					// Format for each line: trajecotryID,pointID,longitude,latitude\n
					writer.write(point.trajectoryID + "," + point.pointID + "," + point.data[0] + "," + point.data[2] + "\n");
				}
				writer.write("*,end\n");
				writer.flush();
			}
			
			writer.flush();
			writer.close();
			
			System.out.println("There are total " + clusters.size() + " : " + num + " cluster nodes");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		Clustering cluster = new Clustering();
		cluster.doClustering();
	}
}
