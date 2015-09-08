package data_extraction;

import java.util.ArrayList;

/**
 * 
 * @author Wang Haibo
 * @since 2011/04/19
 */
public class RawTrajectory {
	String id;
	ArrayList<DataPoint> rawDataPoints;
	
	public RawTrajectory() {
		id = null;
		rawDataPoints = new ArrayList<DataPoint>();
	}
	
	/**
	 * Clean a raw trajectory data according the following four rules:
	 * 
	 * For two consecutive points,
	 * 1. the time span should less than MAX_TIME_SPAN s, here is 900s
	 * 2. the distance should less than MAX_DISTANCE m, here is 5000m
	 * 3. the average speed should less than MAX_SPEED m/s, here is 30m/s
	 * 4. a valid trajectory should contain more than 10 consecutive data points
	 * 
	 * When the whole raw trajectory was cleaned (or pruned), this function may return a null.
	 * @return trajectories or null when trajectories contains no data
	 */
	public ArrayList<Trajectory> doClean() {
		ArrayList<Trajectory> trajectories = new ArrayList<Trajectory>();
		
		Trajectory temp_traj = new Trajectory();
		for (int i=0; i<rawDataPoints.size()-1; i++) {
			DataPoint p1 = rawDataPoints.get(i), p2 = rawDataPoints.get(i+1);
			
//			// for some consecutive points, they are actually the same point, here we just prune them to one point
			if ((p1.lon == p2.lon) && (p1.lat == p2.lat)) continue; 

			int t = p2.time - p1.time;
			double distance = getDistance(p1.lon, p1.lat, p2.lon, p2.lat);
			double speed = distance/t;
			
			if (t<DataCleaner.MAX_TIME_SPAN && distance<DataCleaner.MAX_DISTANCE && speed<DataCleaner.MAX_SPEED) { 
				temp_traj.points.add(p1);
			} else {
				if (temp_traj.points.size() >= DataCleaner.MIN_NUMBER) {
					trajectories.add(temp_traj);
				}
				temp_traj = new Trajectory();
			}
		}
		
		if (trajectories.size() > 0) 
			return trajectories;
		return null;
	}
	
	private double getDistance(int lon1, int lat1, int lon2, int lat2) {
		return Math.sqrt((lon1-lon2)*(lon1-lon2)+(lat1-lat2)*(lat1-lat2));
	}
}
