package main;

import java.awt.Color;
import java.awt.Graphics;

public class Obstacle {

	public static final Color OBSTACLE_COLOR = Color.LIGHT_GRAY;
	
	private Vector location;
	private double radius;
	
	public Obstacle(Vector location, double radius) {
		this.location = location;
		this.radius = radius;
		GridMap.registerObstacle(this);
	}
	
	public void render(Graphics g) {
		g.setColor(OBSTACLE_COLOR);
		g.fillOval((int) (location.x - radius), (int) (location.y - radius), (int) (2 * radius), (int) (2 * radius)); 
	}
	
	public Vector getLocation() {
		return location;
	}
	
	public double getRadius() {
		return radius;
	}
}
