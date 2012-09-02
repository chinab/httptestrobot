package kr.hayarobee.httptest.robot.model;

import kr.hayarobee.httptest.robot.job.RobotWaitTimer;

public class LongWaitTimer implements RobotWaitTimer {

	@Override
	public void execute() {
		try {
			long wait = (long) (Math.random() * 1000 * 10);
			Thread.sleep(wait);
		} catch (InterruptedException e) {}
	}

}
