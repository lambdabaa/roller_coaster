package roller_coaster;

public class Vector3 {
	private double x;
	private double y;
	private double z;
	
	Vector3(double x, double y, double z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	Vector3(){
		new Vector3(0, 0, 0);
	}
	
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
	
	public double dot(Vector3 rhs) {

	    return x * rhs.x + y * rhs.y + z * rhs.z;
	}
	
    public void normalize() {
	    double dist = Math.sqrt(x * x + y * y + z * z);
	    if (dist != 0) {
	      x /= dist;
	      y /= dist;
	      z /= dist;
	    }
	  }
  
  public void sub(Point3 p1, Point3 p2) {

	    this.x = p1.getX() - p2.getX();
	    this.y = p1.getY() - p2.getY();
	    this.z = p1.getZ() - p2.getZ();

	  }
}
