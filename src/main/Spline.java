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
	
	private void runAlgorithm() {
		Tuple<Point, Point> collision = findFirstCollision();
		if(collision != null) {
			//createControlPointMidway();
			//while(!controlPointIsClear())
				//moveControlPointPerpendicular();
			runAlgorithm();
		}
	}
	
	private Tuple<Point, Point> findFirstCollision(){
		int curveSize = entireCurve.size();
		for(int firstIndex = 0; firstIndex < curveSize; firstIndex++) {
			Point p0 = entireCurve.get(firstIndex);
			if(GridMap.positionResultsInCollision(p0.position)) {
				for(int lastIndex = firstIndex; lastIndex < curveSize; lastIndex++) {
					if(!GridMap.positionResultsInCollision(entireCurve.get(lastIndex).position)) {
						return new Tuple<Point, Point>(p0, entireCurve.get(lastIndex - 1));
					}
				}
			}
		}
		return null;
	}
	
	private void calculateEntireCurve() {
		entireCurve.clear();
		int curveIndex = 0;
		
		//Looking at every control point in pairs
		Point prevControlPoint = null;
		for(Point controlPoint : controlPoints) {
			if(prevControlPoint != null) {
				entireCurve.add(prevControlPoint);
				
				Vector prevPosition = prevControlPoint.position.clone();
				Vector prevVelocity = prevControlPoint.velocity.clone();
				Vector nextPosition = controlPoint.position.clone();
				Vector nextVelocity = controlPoint.velocity.clone();
				double scaledT, scaledTSqr, scaledTCube, prevControlT, thisCurveT;
				//For every t between the previous control point and the next control point, going by T_STEP
				for(Point lastCurvePoint = entireCurve.get(curveIndex).clone(); controlPoint.t - lastCurvePoint.t > T_STEP; curveIndex++, lastCurvePoint = entireCurve.get(curveIndex).clone()) {
					thisCurveT = lastCurvePoint.t + T_STEP;
					prevControlT = prevControlPoint.t;
					scaledT = (thisCurveT - prevControlT) / (controlPoint.t - prevControlT);
					scaledTSqr = scaledT * scaledT;
					scaledTCube = scaledTSqr * scaledT;
					//Add new point based on Hermite spline
					Vector newPosition = prevPosition.multiply(2 * scaledTCube - 3 * scaledTSqr + 1)
											.add(prevVelocity.multiply(scaledTCube - 2 * scaledTSqr + scaledT))
											.add(nextPosition.multiply(-2 * scaledTCube + 3 * scaledTSqr))
											.add(nextVelocity.multiply(scaledTCube - scaledTSqr));
					Vector newVelocity = prevPosition.multiply(6 * scaledTSqr - 6 * scaledT)
											.add(prevVelocity.multiply(3 * scaledTSqr - 4 * scaledT + 1))
											.add(nextPosition.multiply(-6 * scaledTSqr + 6 * scaledT))
											.add(nextVelocity.multiply(3 * scaledTSqr - 2 * scaledT));
					Point newPoint = new Point(newPosition, newVelocity, thisCurveT);
					entireCurve.add(newPoint);
					//If at the final point, add last control point to the curve
					if(newPoint.t >= 1 - T_STEP) entireCurve.add(controlPoint.clone());
				}
			}
			prevControlPoint = controlPoint.clone();
		}
	}
	
	private class Point implements Cloneable {
		private Vector position;
		private Vector velocity;
		private double t;
		
		public Point(Vector pos, Vector vel, double t) {
			position = pos.clone();
			velocity = vel.clone();
			this.t = t;
		}
		
		@Override
		public Point clone() {
			return new Point(position.clone(), velocity.clone(), t);
		}
		
		@Override
		public String toString() {
			return "Pos: " + position + "\nDir: " + velocity + "\nt: " + t;
		}
	}
}
