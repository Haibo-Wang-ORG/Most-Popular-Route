package graph_constructor;

import rstar.Data;

/**
 * Provide some utility functions.
 * 
 * @author uqhwan15
 * @since 2011/04/29
 */
public class Util {
    
	/**
	 * Compute the Euclidean distance between Data a and b according to their 
	 * longitudes and latitudes.
	 * @param a Data
	 * @param b Data
	 * @return Euclidean distance between Data a and b
	 */
	public static double distance(Data a, Data b){
    	double dx = a.data[0] - b.data[0];
    	double dy = a.data[2] - b.data[2];
        return Math.sqrt(dx*dx+dy*dy);
    }
	
	/**
	 * Return the global ID of data point. As for all trajectories, non of them have over
	 * 1000 consecutive points, so we just multiply 1000 to get a unique ID for each point.
	 * @param point
	 * @return the global ID of data point
	 */
	public static int getID(Data point) {
		return point.trajectoryID * 1000 + point.pointID;
	}
	
}
