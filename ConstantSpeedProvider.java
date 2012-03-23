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

  @Override
  public SpeedProvider speedUp() {
    speed += 1;
    return this;
  }

  @Override
  public SpeedProvider slowDown() {
    if (speed > 0) {
      speed -= 1;
    }
    
    return this;
  }

}
