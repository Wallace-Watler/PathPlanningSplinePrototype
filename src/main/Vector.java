package main;

/**
 * Mathematical representation of a 2D vector.
 */
public class Vector implements Cloneable {

	public double x, y;
	
	public Vector(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public double length() {
		return Math.sqrt(x * x + y * y);
	}
	
	public Vector normalize() {
		double r = length();
		return new Vector(x / r, y / r);
	}
	
	public Vector negate() {
		return new Vector(-x, -y);
	}
	
	public Vector add(Vector v) {
		return new Vector(x + v.x, y + v.y);
	}
	
	public Vector subtract(Vector v) {
		return this.add(v.negate());
	}
	
	public Vector multiply(double d) {
		return new Vector(d * x, d * y);
	}
	
	public Vector divide(double d) {
		return this.multiply(1 / d);
	}
	
	public Vector normal() {
		return new Vector(y, -x).normalize();
	}
	
	public static Vector average(Vector v1, Vector v2) {
		return v1.add(v2).divide(2);
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof Vector)) return false;
		Vector v = (Vector) o;
		return v == null ? this == null : (v.x == x && v.y == y);
	}
	
	@Override
	public Vector clone() {
		return new Vector(x, y);
	}
	
	@Override
	public String toString() {
		return "[" + x + ", " + y + "]";
	}
}
