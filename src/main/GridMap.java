package main;

import java.awt.Color;
import java.awt.Graphics;

public class GridMap {

	private static final int COARSENESS = 2;
	private static final int CELL_WIDTH = (int) Math.pow(2, COARSENESS);
	private static final int OBSTACLE_DEFINITION = 1000;
	private static final Color OBSTACLE_COLOR = new Color(127, 127, 255);
	
	private static boolean obstacleMap[][];
	
	public static void initializeMap() {
		obstacleMap = new boolean[Main.WIDTH >> COARSENESS][Main.HEIGHT >> COARSENESS];
		for(int j = 0; j < obstacleMap.length; j++)
			for(int i = 0; i < obstacleMap[j].length; i++)
				obstacleMap[i][j] = false;
	}
	
	public static void render(Graphics g) {
		g.setColor(OBSTACLE_COLOR);
		for(int j = 0; j < obstacleMap.length; j++)
			for(int i = 0; i < obstacleMap[j].length; i++)
				if(obstacleMap[i][j])
					g.fillRect(i << COARSENESS, j << COARSENESS, CELL_WIDTH, CELL_WIDTH);
	}
	
	public static void registerObstacle(Obstacle o) {
		Vector location = o.getLocation().clone();
		double safeDistance = o.getRadius() + Robot.WIDTH;
		
		for(int r = 0; r <= OBSTACLE_DEFINITION; r++)
			for(int theta = 0; theta < OBSTACLE_DEFINITION; theta++) {
				double rScaled = safeDistance * r / OBSTACLE_DEFINITION;
				double thetaScaled = 2 * Math.PI * theta / OBSTACLE_DEFINITION;
				Vector v = new Vector(rScaled * Math.cos(thetaScaled), rScaled * Math.sin(thetaScaled)).add(location);
				Vector gridV = positionToGrid(v);
				obstacleMap[(int) gridV.x][(int) gridV.y] = true;
			}
	}
	
	public static Vector positionToGrid(Vector position) {
		return new Vector((int) position.x >> COARSENESS, (int) position.y >> COARSENESS);
	}
}
