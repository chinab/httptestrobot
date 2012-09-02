package kr.hayarobee.httptest.robot;

import kr.hayarobee.httptest.robot.model.RobotManager;
import kr.hayarobee.httptest.robot.view.DynaRobotManagerMonitor;

public class DynaRobotUser {

	private RobotManager manager;
	private DynaRobotManagerMonitor monitor;
	
	private void start() {
		this.manager = new RobotManager();
		this.monitor = new DynaRobotManagerMonitor(this.manager);
		this.monitor.initFrame();
		this.monitor.setVisible(true);
	}

	public static void main(String[] args) throws Exception {
		DynaRobotUser user = new DynaRobotUser();
		user.start();
	}
}
