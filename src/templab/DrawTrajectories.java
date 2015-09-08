package templab;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.swing.JFrame;

public class DrawTrajectories extends JFrame{
	private static final long serialVersionUID = -6720368520110137897L;

	String trajectories_directory = "./DataSet/Trajectories_Directory/";
	String raw_trajectories_directory = "./DataSet/RAW_Trajectories_Directory/";
	String raw_trajectories = "./DataSet/Processed_Trajectory_Data/trajectories.txt";
	
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
	
	public DrawTrajectories(int height) {
		this.height = height;
		this.width = (int) ((float)this.height * ((float)deltaWidth / (float)deltaHeight));
		img = new BufferedImage(this.height, this.width, BufferedImage.TYPE_INT_ARGB);
		Graphics g = img.createGraphics();
//		drawTrajectories(g);
//		drawRawTrajectories(g);
		drawAllTrajectories(g);
		g.dispose();
	}
	
	public void paint(Graphics eg) {
		eg.drawImage(img, 0, 0, null);
	}
	
	private void drawTrajectories(Graphics g) {
		try {
			File file = new File(trajectories_directory);
			String[] trajs = file.list();
			
			g.setColor(Color.blue);
			for (String traj : trajs) {
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
//					g.drawLine(getScreenLon(preLon), getScreenLat(preLat), getScreenLon((int)lon), getScreenLat((int)lat));
					g.drawOval(getScreenLon((int)lon), getScreenLat((int)lat), 1, 1);
					preLon = (int)lon;
					preLat = (int)lat;
				}
				reader.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void drawRawTrajectories(Graphics g) {
		try {
			File file = new File(raw_trajectories_directory);
			String[] trajs = file.list();
			
			g.setColor(Color.blue);
			for (String traj : trajs) {
				BufferedReader reader = new BufferedReader(new FileReader(new File(raw_trajectories_directory + traj)));
				String line;
				int preLon = -1, preLat = -1;
				while ((line = reader.readLine()) != null) {
					String[] fields = line.split(" ");
					float lon = Float.valueOf(fields[0]);
					float lat = Float.valueOf(fields[1]);
					if (preLon == -1 && preLat == -1) {
						preLon = (int) lon;
						preLat = (int) lat;
						continue;
					}
					g.drawOval(getScreenLon((int)lon), getScreenLat((int)lat), 1, 1);
					preLon = (int)lon;
					preLat = (int)lat;
				}
				reader.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void drawAllTrajectories(Graphics g) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(raw_trajectories)));
			
			String line;
			g.setColor(Color.blue);
			while ((line = reader.readLine()) != null) {
				if (line.trim().equals("")) continue; // skip empty lines
				String[] fields = line.split(" ");
				float lon = Float.valueOf(fields[0]);
				float lat = Float.valueOf(fields[1]);
				g.drawOval(getScreenLon((int)lon), getScreenLat((int)lat), 1, 1);
			}
			
			System.out.println("Draw all trajectories complete!");
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private int getScreenLon(int lon) {
		return width*(lon-minLon)/deltaWidth;
	}
	
	private int getScreenLat(int lat) {
		return height - height*(lat-minLat)/deltaHeight;
	}
	
	public static void main(String[] args) {
		DrawTrajectories show = new DrawTrajectories(800);
		
		show.setBounds(50,50,800,800);
		show.setBackground(Color.WHITE);
		show.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		show.setVisible(true);
	}
}
