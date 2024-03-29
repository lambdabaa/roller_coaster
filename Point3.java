package roller_coaster;

public class Point3 {
	private double x;
	private double y;
	private double z;
	
	public Point3(double _x, double _y, double _z) {
		x = _x;
		y = _y;
		z = _z;
	}
	
	public Point3 setX(double x) {
		this.x = x;
		return this;
	}
	
	public double getX() {
		return x;
	}

	public Point3 setY(double y) {
		this.y = y;
		return this;
	}

	public double getY() {
		return y;
	}

	public Point3 setZ(double z) {
		this.z = z;
		return this;
	}

	public double getZ() {
		return z;
	}
	
	@Override
	public String toString() {
		return String.format("x = %f, y = %f, z = %f", x, y, z);
	}
}
