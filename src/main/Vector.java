package main;

public class Vector implements Cloneable {

	public double x, y;
	
	public Vector(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public Vector add(Vector v) {
		return new Vector(x + v.x, y + v.y);
	}
	
	public Vector multiply(double d) {
		return new Vector(d * x, d * y);
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