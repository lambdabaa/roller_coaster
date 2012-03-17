package roller_coaster;

public class ConstantSpeedProvider implements SpeedProvider {
	private int speed;
	
	@Override
	public SpeedProvider setSpeed(int speed) {
		this.speed = speed;
		return this;
	}

	@Override
	public int getSpeed(RollerCoaster rollerCoaster) {
		return speed;
	}

}
