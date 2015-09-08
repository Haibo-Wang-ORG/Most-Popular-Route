package graph_constructor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import rstar.Data;
import rstar.PPoint;
import rstar.RTree;
import rstar.SortedLinList;

/**
 * Build a rstar tree on all trajecotries's points. Since there are about 28610766 points,
 * it will take quite a long time to build the r*-tree up.
 * @author uqhwan15
 * @since 2011/04/27
 */
public class RTreeDB {
	public RTree rtree;
	
    private static int DIMENSION = 2;
    private static int BLOCKLENGTH = 512;
    private static int CACHESIZE = 128;
    
    String rtree_path="./DataSet/RTree/rtree.dat";
    String interpolated_trajectory_data = "./DataSet/Interpolated_Trajectory_Data/interpolated_trajectory.txt";
    
    public RTreeDB() {
    	File file = new File(rtree_path);
    	if (file.exists()) {
    		System.out.println("Loading in r-tree from " + rtree_path);
    		rtree = new RTree(rtree_path, CACHESIZE);
    		System.out.println("Number of data = " + rtree.num_of_data);
    	} else {
    		System.out.println("Creating an r-tree at " + rtree_path);
    		rtree = new RTree(rtree_path, BLOCKLENGTH, CACHESIZE, DIMENSION);
    		buildRtree();
    	}
    }
    
    private void buildRtree() {
    	try {
    		long start = System.currentTimeMillis();
    		long tempStart = start;
    		BufferedReader reader = new BufferedReader(new FileReader(new File(interpolated_trajectory_data)));
    		
    		String line;
    		Data point;
    		int count = 1;
    		while ((line = reader.readLine()) != null) {
    			if (line.trim().equals("")) continue; // skip the empty line
    			
    			String[] fields = line.split(",");
    			int pointID = Integer.valueOf(fields[1]);
    			float lon = Float.valueOf(fields[2]);
    			float lat = Float.valueOf(fields[3]);
    			float direction = Float.valueOf(fields[4]);
    			
    			point = new Data(DIMENSION, pointID);
    			point.data = new float[DIMENSION*2];
    			point.data[0] = lon; //LX
    			point.data[1] = lon; //UX
    			point.data[2] = lat; //LY
    			point.data[3] = lat; //UY
    			point.direction = direction;
    			point.trajectoryID = Integer.valueOf(fields[0]);
    			point.pointID = pointID;
    			
    			rtree.insert(point);
    			if (count++ == 1000000) { // commit rtree for every 10000 points
    				long tempEnd = System.currentTimeMillis();
    				rtree.delete();
    				rtree = new RTree(rtree_path, CACHESIZE);
    				count = 1;
    				System.out.println("Insert 1000000 points into Rtree:\t" + (tempEnd-tempStart)/1000 + "\tseconds");
    				tempStart = tempEnd;
    				System.gc();
    			}
    		}
    		
    		rtree.delete();
    		reader.close();
    		long end = System.currentTimeMillis();
    		System.out.println("RTree build time:\t" + (end-start)/1000 + "\tseconds");
    	} catch (Exception e) {
    		e.printStackTrace();
		}
    }
    
    public ArrayList<Data> rangeQuery(float lon, float lat, float radius) {
    	PPoint queryPoint = new PPoint(DIMENSION, lon, lat);
    	ArrayList<Data> result = new ArrayList<Data>();
    	SortedLinList res = new SortedLinList();
    	rtree.range(queryPoint, radius, res);
    	
    	for (int i=0; i<res.get_num(); i++) {
    		result.add((Data)res.get(i));
    	}
    	
    	return result;
    }
    
    public static void main(String[] args) {
    	new RTreeDB();
    }
}
