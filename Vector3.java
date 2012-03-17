package roller_coaster;

public class Vector3 {
	private double x;
	private double y;
	private double z;
	
	public Vector3 setX(double x) {
		this.x = x;
		return this;
	}
	
	public double getX() {
		return x;
	}

	public Vector3 setY(double y) {
		this.y = y;
		return this;
	}

	public double getY() {
		return y;
	}

	public Vector3 setZ(double z) {
		this.z = z;
		return this;
	}

	public double getZ() {
		return z;
	}
}
