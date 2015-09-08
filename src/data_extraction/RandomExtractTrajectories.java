package data_extraction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

public class RandomExtractTrajectories {	
	public static void main(String[] args) {
		String processed_trajecotry_data = "./DataSet/Processed_Trajectory_Data/trajectories.txt";
		String selected_trajecotry_data = "./DataSet/Processed_Trajectory_Data/random_selected_trajectories1.txt";
		String selected_trajecotry_data_aid = "./DataSet/Processed_Trajectory_Data/random_selected_trajectories_aid1.txt";
		
		SortedSet<Integer> rNums = new TreeSet<Integer>();
		Random rand = new Random();
		while (rNums.size() < 1000) {
			int n = rand.nextInt(100310);
			if (!rNums.contains(n)) {
				rNums.add(n);
			}
		}
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(processed_trajecotry_data)));
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(selected_trajecotry_data)));
			BufferedWriter writer_aid = new BufferedWriter(new FileWriter(new File(selected_trajecotry_data_aid)));
			
			String line;
			int num = 1, trajectoryID = 1, pointID = 1;
			while ((line = reader.readLine()) != null) {
				if (rNums.contains(num)) { // store the selected trajectory data into file selected_trajectories.txt
					if (line.trim().equals("")) {
						num++;
						continue;
					}
					do {
						writer_aid.write(trajectoryID + "," + pointID + "," + line.split(" ")[0] + "," + line.split(" ")[1] + "\n");
						writer.write(line + "\n");
						pointID++;
					} while ((line = reader.readLine()) != null && !line.trim().equals(""));
					trajectoryID++;
					writer.write("\n");
				}
				if (line.trim().equals("")) {
					num++;
					pointID = 1;
				}
			}
			
			System.out.println(num);
			writer.flush();
			writer.close();
			writer_aid.flush();
			writer_aid.close();
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		for (int n: rNums) {
			System.out.println(n);
		}
	}
}
