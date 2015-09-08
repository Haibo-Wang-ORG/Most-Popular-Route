package graph_constructor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

import rstar.Data;

/**
 * Here we mainly interpolate the data along a straight line. 
 * 
 * If combined with a road map, we can also interpolate the data along the shortest path between two data points. 
 * In that case, we should first map every data point into road network, which involves map matching work.
 * Is there any good practical map matching algorithm? I don't know. Maybe when I finish this work, I can have a 
 * systematic investigation.
 * 
 * @author uqhwan15
 * @since 2011/04/27
 */
public class Interpolation {
	String processed_trajectory_data = "./DataSet/Processed_Trajectory_Data/random_selected_trajectories.txt";
	String interpolated_trajectory_data = "./DataSet/Interpolated_Trajectory_Data/interpolated_trajectory.txt";
	String trajectories_directory = "./DataSet/Trajectories_Directory/";

	private float interpolation_threshold = 100; //meters
	
	public void interpolate() {
		int trajectoryID = 0;
		int pointID = 0;
		ArrayList<Data> trajectory = new ArrayList<Data>();
		
		String line;
		Data point, prePoint = null;
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(processed_trajectory_data)));
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(interpolated_trajectory_data)));
			
			while ((line = reader.readLine()) != null) {
				if (line.trim().equals("")) { // the trajectory ends here
					// store the current trajectory
					int id = 0;
					for (Data p : trajectory) {
						writer.write(trajectoryID+","+(id++)+","+p.data[0]+","+p.data[2]+","+p.direction+"\n");
					}
					writer.write("\n");
					writer.flush();
					
					// store every trajectory in to a unique file
					id = 0;
					BufferedWriter tempWriter = new BufferedWriter(new FileWriter(new File(trajectories_directory + trajectoryID + ".txt")));
					for (Data p : trajectory) {
						tempWriter.write(trajectoryID+","+(id++)+","+p.data[0]+","+p.data[2]+","+p.direction+"\n");
					}
					tempWriter.flush();
					tempWriter.close();
					
					// start a new trajectory and reset related parameters
					trajectoryID++;
					pointID = 0;
					trajectory.clear();
					
					continue; // read the next line
				}
				
				String[] fields = line.split(" ");
				float lon = Float.valueOf(fields[0]);
				float lat = Float.valueOf(fields[1]);
				
				point = new Data(2, pointID);
				point.data = new float[2*2];
				point.data[0] = lon; // LX
				point.data[1] = lon; // UX
				point.data[2] = lat; // LY
				point.data[3] = lat; // UY
				point.trajectoryID = trajectoryID;
				point.pointID = pointID;
				
				if (pointID == 0) { // the first point
					prePoint = point;
					prePoint.direction = 0.f;
				} else {
					// If prePoint are the same with the current point
					if (prePoint.data[0] == point.data[0] && prePoint.data[2] == point.data[2]) { 
						point.direction = prePoint.direction;
						if (pointID == 1) trajectory.add(prePoint);
						trajectory.add(point);
					} else {
						float direction = this.getDirection(prePoint, point);
						point.direction = direction;
						
						if (pointID == 1) { // make sure the current trajectory add the first point
							prePoint.direction = direction;
							trajectory.add(prePoint);
						}
						
						if (Util.distance(prePoint, point) > interpolation_threshold) { // interpolation if needed
							ArrayList<Data> middlePoints = interpolation(prePoint, point);
							setDirection(middlePoints, direction);
							trajectory.addAll(middlePoints);
						}
						
						trajectory.add(point);
					}
				}
				
				prePoint = point;
				pointID ++;
			}
			
			System.out.println("Total trajecotries:\t" + trajectoryID);
			writer.flush();
			writer.close();
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public float getDirection(Data prePoint, Data point)
	{
		return this.getDirection(prePoint.data[0], prePoint.data[2], point.data[0], point.data[2]);
	}

	/**
	 * Direction valued in interval [-pi, pi]
	 */
    public float getDirection(float longitude1, float latitude1, float longitude2, float latitude2) {
    	double deltaX = longitude2 - longitude1;
		double deltaY = latitude2 - latitude1;
		double dis = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

		if (longitude2 >= longitude1) {
			if (latitude2 >= latitude1)// the first quadrant
			{
				return (float) Math.asin(deltaY / dis);
			} else// the forth quadrant
			{
				return (float) Math.asin(deltaY / dis);
			}
		} else {
			if (latitude2 >= latitude1)// the second quadrant
			{
				return (float) (Math.PI - Math.asin(deltaY / dis));
			} else// the third quadrant
			{
				return (float) (-Math.PI - Math.asin(deltaY / dis));
			}
		}
	}
	
    private ArrayList<Data> interpolation(Data prePoint, Data point)
    {
    	float x1 = prePoint.data[0];
    	float y1 = prePoint.data[2];
    	float x2 = point.data[0];
    	float y2 = point.data[2];
    	float deltaX = (x2-x1);
    	float deltaY = (y2-y1);
    	
    	int number = (int)(Util.distance(prePoint, point)/interpolation_threshold);
    	deltaX/=(float)(number+1);
    	deltaY/=(float)(number+1);
    	
    	ArrayList<Data> middlePoints = new ArrayList<Data>();
    	for(int i=1;i<=number; i++)
    	{
    		Data pt = new Data();
    		pt.data[0] = pt.data[1] = x1+ deltaX * (float)i;
    		pt.data[2] = pt.data[3] = y1+ deltaY * (float)i;
    		//pt.trajectoryID = prePoint.trajectoryID;
    		middlePoints.add(pt);
    	}
    	
    	return middlePoints;
    }
	
    private void setDirection(ArrayList<Data> points, float direction)
    {
    	for(int i=0;i<points.size();i++)
    	{
    		points.get(i).direction = direction;
    	}
    }
    
    public static void main(String[] args) {
    	Interpolation interpolation = new Interpolation();
    	interpolation.interpolate();
    }
}
