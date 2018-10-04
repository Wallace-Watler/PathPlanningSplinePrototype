package main;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Spline {

	private static final Color BASE_SPLINE_COLOR = Color.RED;
	private static final Color ACTUAL_SPLINE_COLOR = Color.CYAN;
	private static final double T_STEP = 0.001;
	
	private final List<Point> entireCurve;
	private final List<Point> controlPoints;
	private final Color color;
	
	public Spline(Vector startPos, Vector endPos, Vector startDir, Vector endDir, boolean runAlgorithm) {
		entireCurve = new ArrayList<Point>();
		controlPoints = new LinkedList<Point>();
		controlPoints.add(new Point(startPos.clone(), startDir.clone(), 0));
		controlPoints.add(new Point(endPos.clone(), endDir.clone(), 1));
		calculateEntireCurve();
		if(runAlgorithm) {
			color = ACTUAL_SPLINE_COLOR;
			runAlgorithm();
		}else color = BASE_SPLINE_COLOR;
	}
	
	public void render(Graphics g) {
		g.setColor(color);
		Vector currentPosition, prevPosition = entireCurve.get(0).position.clone();
		for(Point p : entireCurve) {
			currentPosition = p.position.clone();
			g.drawLine((int) prevPosition.x, (int) prevPosition.y, (int) currentPosition.x, (int) currentPosition.y);
			prevPosition = currentPosition.clone();
		}
	}
	
	private boolean runAlgorithm() {
		calculateEntireCurve();
		Tuple<Point, Point> collision = findFirstCollision();
		if(collision != null) {
			Point newControlPoint = createControlPointMidway(collision);
			return moveControlPointPerpendicular(newControlPoint) ? runAlgorithm() : false;
		}
		return true;
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
	
	private Tuple<Point, Point> findFirstCollision(){
		int curveSize = entireCurve.size();
		for(int firstIndex = 0; firstIndex < curveSize; firstIndex++) {
			Point p0 = entireCurve.get(firstIndex);
			if(GridMap.positionResultsInCollision(p0.position))
				for(int lastIndex = firstIndex; lastIndex < curveSize; lastIndex++)
					if(!GridMap.positionResultsInCollision(entireCurve.get(lastIndex).position))
						return new Tuple<Point, Point>(p0, entireCurve.get(lastIndex - 1));
		}
		return null;
	}
	
	private Point createControlPointMidway(Tuple<Point, Point> collision) {
		Vector newPos = collision.x.position.add(collision.y.position).divide(2);
		Vector newVel = collision.x.velocity.add(collision.y.velocity).divide(2);
		Point newControlPoint = new Point(newPos, newVel, (collision.x.t + collision.y.t) / 2);
		for(int i = 0; i < controlPoints.size(); i++)
			if(newControlPoint.t < controlPoints.get(i).t) {
				controlPoints.add(i, newControlPoint);
				return controlPoints.get(i);
			}
		return null;
	}
	
	private boolean moveControlPointPerpendicular(Point controlPoint) {
		Vector moveDirection = controlPoint.velocity.normal();
		for(int i = 1; true; i++) {
			Vector p = controlPoint.position.add(moveDirection.multiply(i));
			try {
				if(!GridMap.positionResultsInCollision(p)) {
					controlPoint.position = p;
					return true;
				}
			} catch(ArrayIndexOutOfBoundsException e0) {
				p = controlPoint.position.add(moveDirection.multiply(-i));
				try {
					if(!GridMap.positionResultsInCollision(p)) {
						controlPoint.position = p;
						return true;
					}
				} catch(ArrayIndexOutOfBoundsException e1) {
					return false;
				}
			}
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
