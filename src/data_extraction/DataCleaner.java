package data_extraction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

/**
 * 
 * @author Wang Haibo
 * @since 2011/04/19
 */
public class DataCleaner {
	public static final double MAX_TIME_SPAN = 900;	// 900s
	public static final double MAX_SPEED = 30;		// 30m/s
	public static final double MAX_DISTANCE = 5000;	// 5000m
	public static final int MIN_NUMBER = 10;
	
	String[] dataFiles = {"./Trajectory_Data/14.txt", "./Trajectory_Data/15.txt", "./Trajectory_Data/16.txt",};
	String processed_trajectory_data = "./Processed_Trajectory_Data/trajectories.txt";
//	String[] dataFiles = {"./1"};	
	
	ArrayList<Trajectory> trajectories;
	
	public DataCleaner() {
		trajectories = new ArrayList<Trajectory>();
	}
	
	public void clean() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(processed_trajectory_data)));
			for (String file : dataFiles) {
				BufferedReader reader = new BufferedReader(new FileReader(new File(file)));
				
				// load data
				String line;
				RawTrajectory rawTrajectory = new RawTrajectory();
				while ((line = reader.readLine()) != null) {
					String[] items = line.split(",");
					if (!items[0].equals(rawTrajectory.id)) { // here we start a new raw trajectory, so we need clean old 
															  // trajectory and added the valid ones into trajectories
						ArrayList<Trajectory> temp_trajs = rawTrajectory.doClean();
						if (temp_trajs != null)
							trajectories.addAll(temp_trajs);
						
						rawTrajectory = new RawTrajectory(); // create a new raw trajectory
						rawTrajectory.id = items[0];
					}
					
					DataPoint point = new DataPoint();
					point.time = getTime(items[1]);
					point.lon = Integer.valueOf(items[2]);
					point.lat = Integer.valueOf(items[3]);
					rawTrajectory.rawDataPoints.add(point);
				}
				
				reader.close();
				
				System.out.println(trajectories.size());
				// store trajectories into a processed trajectory data file and reset trajectories ArrayList
				for (Trajectory traj : trajectories) {
					for (DataPoint point : traj.points) {
						writer.write(point.lon + " " + point.lat + "\n");
					}
					writer.write("\n");
				}
				writer.flush();
				trajectories.clear(); // reset trajectories ArrayList
			}
			
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// specified for BeiJing trajectory data time format
	private int getTime(String time) {
		int t = Integer.valueOf(time.substring(8, 10))*3600 + Integer.valueOf(time.substring(10, 12))*60 + Integer.valueOf(time.substring(12, 14));
		return t;
	}
	
	public static void main(String[] args) {
		DataCleaner cleaner = new DataCleaner();
		cleaner.clean();
	}
}
