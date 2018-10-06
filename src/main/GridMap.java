package main;

import java.awt.Color;
import java.awt.Graphics;

/**
 * The map containing information about where each obstacle is.<br>
 * Information is stored as a pixelated array of boolean values, {@code true}
 * indicating where a collision with an obstacle would occur.
 */
public class GridMap {

	/**
	 * Level of pixelation of the map.
	 */
	private static final int COARSENESS = 0;
	private static final int CELL_WIDTH = (int) Math.pow(2, COARSENESS);
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
	
	/**
	 * Add an obstacle's information to the map.
	 * @param o - the obstacle to add
	 */
	public static void registerObstacle(Obstacle o) {
		Vector location = o.getLocation().clone();
		Vector gridLocation = positionToGrid(location);
		double safeDistance = o.getRadius() + Main.ROBOT_WIDTH + CELL_WIDTH / Math.sqrt(2);
		int gridRadiusToCheck = (int) Math.ceil(safeDistance) >> COARSENESS;
		
		for(int j = (int) gridLocation.y - gridRadiusToCheck; j <= (int) gridLocation.y + gridRadiusToCheck; j++)
			for(int i = (int) gridLocation.x - gridRadiusToCheck; i <= (int) gridLocation.x + gridRadiusToCheck; i++) {
				Vector cellCenter = new Vector((i << COARSENESS) + CELL_WIDTH / 2.0, (j << COARSENESS) + CELL_WIDTH / 2.0);
				try {
					if(cellCenter.subtract(location).length() <= safeDistance)
						obstacleMap[i][j] = true;
				} catch(ArrayIndexOutOfBoundsException e) {}
			}
	}
	
	/**
	 * Maps a continuous Cartesian position to a grid cell on the map.
	 * @param position - the position to convert
	 * @return A Vector containing the x-y coordinates of the grid map.
	 */
	public static Vector positionToGrid(Vector position) {
		return new Vector((int) position.x >> COARSENESS, (int) position.y >> COARSENESS);
	}
	
	/**
	 * @param position - the Cartesian position to check
	 * @return If the position lies within a collision zone.
	 * @throws ArrayIndexOutOfBoundsException if the position lies outside of the grid
	 */
	public static boolean positionResultsInCollision(Vector position) throws ArrayIndexOutOfBoundsException {
		Vector gridV = positionToGrid(position);
		return obstacleMap[(int) gridV.x][(int) gridV.y];
	}
}
