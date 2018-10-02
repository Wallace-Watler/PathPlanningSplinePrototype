package main;

import java.awt.Color;
import java.awt.Graphics;

public class Robot {

	public static final double WIDTH = 60;
	public static final Color ROBOT_COLOR = Color.WHITE;
	
	private Vector position;
	private Vector direction;
	private Vector goalPosition;
	private Vector goalDirection;
	private Spline spline;
	
	public Robot(Vector pos, Vector dir) {
		position = pos.clone();
		direction = dir.clone();
		goalPosition = pos.clone();
		goalDirection = dir.clone();
		updateSpline();
	}
	
	public void tick() {
		
	}
	
	public void render(Graphics g) {
		spline.render(g);
		g.setColor(ROBOT_COLOR);
		double halfWidth = WIDTH / 2;
		g.fillOval((int) (position.x - halfWidth), (int) (position.y - halfWidth), (int) WIDTH, (int) WIDTH);
	}
	
	public void setGoal(Vector goalPos, Vector goalDir) {
		goalPosition = goalPos.clone();
		goalDirection = goalDir.clone();
		updateSpline();
	}
	
	private void updateSpline() {
		spline = new Spline(position, goalPosition, direction, goalDirection);
	}
}
