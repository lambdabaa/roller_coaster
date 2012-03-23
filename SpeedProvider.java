package roller_coaster;

public interface SpeedProvider {
	public SpeedProvider setSpeed(int speed);
	public int getSpeed(RollerCoaster rollerCoaster);
	public SpeedProvider speedUp();
	public SpeedProvider slowDown();
}
