package templab;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class MaxMinLonLat {
	String processed_trajectory_data = "./DataSet/Processed_Trajectory_Data/random_selected_trajectories.txt";
	
	public void maxmin() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(processed_trajectory_data)));
			
			int minLon, minLat, maxLon, maxLat;
			minLon = minLat = Integer.MAX_VALUE;
			maxLon = maxLat = Integer.MIN_VALUE;
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.trim().equals("")) continue; // skip empty line
				String[] fields = line.split(" ");
				int lon = Integer.valueOf(fields[0]);
				int lat = Integer.valueOf(fields[1]);
				
				if (lon > maxLon) {
					maxLon = lon;
				} else if (lon < minLon) {
					minLon = lon;
				}
				
				if (lat > maxLat) {
					maxLat = lat;
				} else if (lat < minLat) {
					minLat = lat;
				}
			}
			
			System.out.println(minLon);
			System.out.println(minLat);
			System.out.println(maxLon);
			System.out.println(maxLat);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		MaxMinLonLat maxmin = new MaxMinLonLat();
		maxmin.maxmin();
	}
}
