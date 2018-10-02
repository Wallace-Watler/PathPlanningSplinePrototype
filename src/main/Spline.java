package main;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Spline {

	private static final Color SPLINE_COLOR = Color.CYAN;
	private static final double T_STEP = 0.001;
	
	private final List<Point> entireCurve;
	private final List<Point> controlPoints;
	
	public Spline(Vector startPos, Vector endPos, Vector startDir, Vector endDir) {
		entireCurve = new ArrayList<Point>();
		controlPoints = new LinkedList<Point>();
		controlPoints.add(new Point(startPos.clone(), startDir.clone(), 0));
		controlPoints.add(new Point(endPos.clone(), endDir.clone(), 1));
		calculateEntireCurve();
	}
	
	public void render(Graphics g) {
		g.setColor(SPLINE_COLOR);
		Vector currentPosition, prevPosition = entireCurve.get(0).position.clone();
		for(Point p : entireCurve) {
			currentPosition = p.position.clone();
			g.drawLine((int) prevPosition.x, (int) prevPosition.y, (int) currentPosition.x, (int) currentPosition.y);
			prevPosition = currentPosition.clone();
		}
	}
	
	private void calculateEntireCurve() {
		entireCurve.clear();
		int curveIndex = 0;
		
		//Looking at every control point in pairs
		Point prevControlPoint = controlPoints.get(0);
		for(Point controlPoint : controlPoints) {
			entireCurve.add(prevControlPoint);
			
			Vector prevPosition = prevControlPoint.position.clone();
			Vector prevDirection = prevControlPoint.direction.clone();
			Vector nextPosition = controlPoint.position.clone();
			Vector nextDirection = controlPoint.direction.clone();
			double scaledT, scaledTSqr, scaledTCube, prevControlT, lastCurveT;
			//For every t between the previous control point and the next control point, going by T_STEP
			for(Point lastCurvePoint = entireCurve.get(curveIndex).clone(); controlPoint.t - lastCurvePoint.t > T_STEP; curveIndex++, lastCurvePoint = entireCurve.get(curveIndex).clone()) {
				lastCurveT = lastCurvePoint.t;
				prevControlT = prevControlPoint.t;
				scaledT = (lastCurveT - prevControlT) / (controlPoint.t - prevControlT);
				scaledTSqr = scaledT * scaledT;
				scaledTCube = scaledTSqr * scaledT;
				//Add new point based on Hermite spline
				Vector newPosition = prevPosition.multiply(2 * scaledTCube - 3 * scaledTSqr + 1)
										.add(prevDirection.multiply(scaledTCube - 2 * scaledTSqr + scaledT))
										.add(nextPosition.multiply(-2 * scaledTCube + 3 * scaledTSqr))
										.add(nextDirection.multiply(scaledTCube - scaledTSqr));
				Vector newDirection = prevPosition.multiply(6 * scaledTSqr - 6 * scaledT)
										.add(prevDirection.multiply(3 * scaledTSqr - 4 * scaledT + 1))
										.add(nextPosition.multiply(-6 * scaledTSqr + 6 * scaledT))
										.add(nextDirection.multiply(3 * scaledTSqr - 2 * scaledT));
				entireCurve.add(new Point(newPosition, newDirection, lastCurveT + T_STEP));
			}
			//If at the final control point, add it to the curve
			if(controlPoint.t == 1) entireCurve.add(controlPoint.clone());
			prevControlPoint = controlPoint.clone();
		}
	}
	
	private class Point implements Cloneable {
		private Vector position;
		private Vector direction;
		private double t;
		
		public Point(Vector pos, Vector dir, double t) {
			position = pos.clone();
			direction = dir.clone();
			this.t = t;
		}
		
		@Override
		public Point clone() {
			return new Point(position.clone(), direction.clone(), t);
		}
		
		@Override
		public String toString() {
			return "Pos: " + position + "\nDir: " + direction + "\nt: " + t;
		}
	}
}
