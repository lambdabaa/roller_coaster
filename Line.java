package roller_coaster;


public class Line {
	private Point3 p1;
	private Point3 p2;
	
	public Line setP1(Point3 p1) {
		this.p1 = p1;
		return this;
	}
	
	public Point3 getP1() {
		return p1;
	}

	public Line setP2(Point3 p2) {
		this.p2 = p2;
		return this;
	}

	public Point3 getP2() {
		return p2;
	}
	
	public Vector3 getTangent() {
		return new Vector3()
			.setX(p2.getX() - p1.getX())
			.setY(p2.getY() - p1.getY())
			.setZ(p2.getZ() - p1.getZ());
	}
}
