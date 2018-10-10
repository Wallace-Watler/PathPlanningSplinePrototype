package main;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

/**
 * Mathematical representation of a piecewise cubic Hermite spline curve.
 */
public class Spline {

	private static final Color BASE_SPLINE_COLOR = Color.RED;
	private static final Color ACTUAL_SPLINE_COLOR = Color.CYAN;
	private static final double CONTROL_POINT_MIN_RADIUS_SQR = Math.pow(150, 2);
	private static final double T_STEP = 0.001;
	
	private final List<Point> entireCurve;	//This represents the smooth path
	private final List<Point> controlPoints;	//These are the control points that define the curve
	private final Color color;
	
	/**
	 * Calculates an obstruction-free spline with the given starting and ending constraints.
	 * @param startPos - the start position
	 * @param endPos - the end position
	 * @param startVel - the initial velocity
	 * @param endVel - the final velocity
	 * @param runAlgorithm - whether or not to run the path-finding algorithm; for testing purposes only
	 */
	public Spline(Vector startPos, Vector endPos, Vector startVel, Vector endVel, boolean runAlgorithm) {
		entireCurve = new ArrayList<Point>();
		controlPoints = new ArrayList<Point>();
		controlPoints.add(new Point(startPos.clone(), startVel.clone(), 0));
		controlPoints.add(new Point(endPos.clone(), endVel.clone(), 1));
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
	
	/**
	 * Runs the path-finding algorithm on the spline curve.<br>
	 * <br>
	 * After calculating the entire path, it finds the first collision and places
	 * a new control point on the midpoint of the collision. It then moves that point
	 * perpendicular to the path until it lies outside of the collision zone. The algorithm is
	 * then recursively called on the resulting spline until there are no collisions.
	 * @return If the algorithm successfully found a clear path.
	 */
	private boolean runAlgorithm() {
		calculateEntireCurve();
		Tuple<Point, Point> collision = findFirstCollision();
		if(collision != null) {
			Point newControlPoint = createControlPointMidway(collision);
			return moveControlPointPerpendicular(newControlPoint) ? runAlgorithm() : false;
		}
		return true;
	}
	
	/**
	 * Calculates the entire curve based on the list control points using a cubic Hermite spline.
	 */
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
	
	/**
	 * Finds the first collision along the path.
	 * @return The points marking the beginning and end of the collision as a Tuple.
	 */
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
	
	/**
	 * Creates a control point halfway between the start and end of a collision.
	 * @param collision - a Tuple containing the start and end points of the collision
	 * @return The newly created control point.
	 */
	private Point createControlPointMidway(Tuple<Point, Point> collision) {
		Vector newPos = Vector.average(collision.x.position, collision.y.position);
		Vector newVel = Vector.average(collision.x.velocity, collision.y.velocity);
		Point newControlPoint = new Point(newPos, newVel, (collision.x.t + collision.y.t) / 2);
		for(int i = 0; i < controlPoints.size(); i++)
			if(newControlPoint.t < controlPoints.get(i).t) {
				controlPoints.add(i, newControlPoint);
				removeCloseControlPoints(i);
				return newControlPoint;
			}
		return null;	//Should never reach here
	}
	
	/**
	 * Removes control points that are too close to the given control point
	 * (specified by {@code CONTROL_POINT_MIN_RADIUS_SQR}).
	 * @param index - the index of {@code controlPoints} containing the given control point
	 */
	private void removeCloseControlPoints(int index) {
		Vector controlPointPos = controlPoints.get(index).position;
		Point prevPoint = controlPoints.get(index - 1);
		Point nextPoint = controlPoints.get(index + 1);
		if(Math.pow(prevPoint.position.x - controlPointPos.x, 2) + Math.pow(prevPoint.position.y - controlPointPos.y, 2) < CONTROL_POINT_MIN_RADIUS_SQR) {
			controlPoints.remove(prevPoint);
		}
		if(Math.pow(nextPoint.position.x - controlPointPos.x, 2) + Math.pow(nextPoint.position.y - controlPointPos.y, 2) < CONTROL_POINT_MIN_RADIUS_SQR) {
			controlPoints.remove(nextPoint);
		}
	}
	
	/**
	 * Moves a given control point perpendicular to the curve until
	 * it does not collide with an obstacle.
	 * @param controlPoint - the control point to move
	 * @return If the control point was successfully moved to a collision-free position.
	 */
	private boolean moveControlPointPerpendicular(Point controlPoint) {
		Vector moveDirection = controlPoint.velocity.normal();
		for(int i = 1; true; i++) {
			int exceptionCounter = 0;
			try {
				Vector p0 = controlPoint.position.add(moveDirection.multiply(i));
				if(!GridMap.positionResultsInCollision(p0)) {
					controlPoint.position = p0;
					return true;
				}
			} catch(ArrayIndexOutOfBoundsException e) { exceptionCounter++; }
			try {
				Vector p1 = controlPoint.position.add(moveDirection.multiply(-i));
				if(!GridMap.positionResultsInCollision(p1)) {
					controlPoint.position = p1;
					return true;
				}
			} catch(ArrayIndexOutOfBoundsException e) { exceptionCounter++; }
			if(exceptionCounter == 2) return false;
		}
	}
	
	/**
	 * Mathematical representation of a point along a spline curve.<br>
	 * Stores its position, velocity, and arbitrary parameter t.
	 */
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
