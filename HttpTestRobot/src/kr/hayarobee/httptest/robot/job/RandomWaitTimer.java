package kr.hayarobee.httptest.robot.job;

public class RandomWaitTimer implements RobotWaitTimer {

	@Override
	public void execute() {
		try {
			long wait = (long) (Math.random() * 500);
			Thread.sleep(wait);
		} catch (InterruptedException e) {}
	}

}
