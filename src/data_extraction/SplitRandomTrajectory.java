package data_extraction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class SplitRandomTrajectory {
	String selected_trajecotry_data = "./DataSet/Processed_Trajectory_Data/random_selected_trajectories.txt";
	String raw_trajectories_directory = "./DataSet/RAW_Trajectories_Directory/";
	
	public void doSplit() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(selected_trajecotry_data)));
			
			String line;
			int id = 0;
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(raw_trajectories_directory + id + ".txt")));
			while ((line = reader.readLine()) != null) {
				if (line.trim().equals("")) {
					writer.close();
					id ++;
					writer = new BufferedWriter(new FileWriter(new File(raw_trajectories_directory + id + ".txt")));
					continue;
				}
				
				writer.write(line + "\n");
			}
			
			writer.close();
			reader.close();
			
			// delete the last empty file
			File file = new File(raw_trajectories_directory + id + ".txt");
			file.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		SplitRandomTrajectory splitFiles = new SplitRandomTrajectory();
		splitFiles.doSplit();
	}
}
