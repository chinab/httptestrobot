package kr.hayarobee.httptest.robot.model;

import kr.hayarobee.httptest.robot.JobExecutionFailException;
import kr.hayarobee.httptest.robot.job.CodeBreaker;
import kr.hayarobee.httptest.robot.job.JobType;
import kr.hayarobee.httptest.robot.job.RandomWaitTimer;
import kr.hayarobee.httptest.robot.job.RobotWaitTimer;
import kr.hayarobee.httptest.robot.job.ScenarioInventory;
import kr.hayarobee.httptest.robot.job.http.HttpClientEngine;
import kr.hayarobee.httptest.robot.view.CellRefreshEventListener;

import org.apache.http.client.HttpClient;

public class Robot implements Runnable, CodeBreaker {

	private HttpClient client;
	private EngineRoom engineRoom;
	private Codebook codebook;
	
	private String name;
	private String errorLog = "";

	private HttpClientEngine curTestEngine;
	private CellRefreshEventListener listener;
	private RobotWaitTimer waitTimer = new RandomWaitTimer();

	private boolean login;
	private long startTime;
	private long endTime;
	private float maxExecuteTime = 0l;
	private String maxExecuteTimeJob;
	private Status status = Status.Stop;
	private Schedule schedule = Schedule.Loop;

	public Robot(String prefix, String serialNo) {
		this.name = prefix + serialNo;
		this.engineRoom = new EngineRoom();
		this.codebook = new Codebook();
	}
	
	public void setHttpClient(HttpClient client) {
		this.client = client;
	}
	
	public String getName() {
		return this.name;
	}

	public String getAppName() {
		if (this.curTestEngine == null)
			throw new IllegalStateException("App 이름을 알 수 없습니다.");
		return this.curTestEngine.getAppName();
	}

	public void clearJobs() {
		this.engineRoom.reset();
		this.curTestEngine = null;
	}

	public void setup(ScenarioInventory inven) {
		this.engineRoom.setup(this.name, inven);
	}

	private HttpClientEngine getJob(JobType type) {
		if (type == null) {
			throw new IllegalArgumentException();
		}
		return this.engineRoom.get(type, this.schedule);
	}

	@Override
	public void run() {
		this.errorLog = "";
		try {
			if (this.engineRoom.isEmpty()) {
				while (this.engineRoom.isSpecialJobComplete() == false) {
					specialJobExecute(JobType.Init);
					specialJobExecute(JobType.Login);
					specialJobExecute(JobType.Logout);
					specialJobExecute(JobType.Shutdown);
				}
			} else {
				login();
				while (this.engineRoom.isComplete() == false) {
					HttpClientEngine engine = this.engineRoom.get(JobType.Regular, this.schedule);
					if (engine == null) break;
					execute(true, engine);
				}
				logout();
				complete();
			}
		} catch (JobExecutionFailException e) {
			this.errorLog = e.getMessage();
			setAlarm(Status.Error);
		} catch (Exception e) {
			this.errorLog = e.getMessage();
			setAlarm(Status.Error);
		}
	}

	private void logout() throws JobExecutionFailException, Exception {
		if (this.login == false) return;
		specialJobExecute(JobType.Logout);
		specialJobExecute(JobType.Shutdown);
		this.login = false;
	}

	private void login() throws JobExecutionFailException, Exception {
		if (this.login == true) return;
		specialJobExecute(JobType.Init);
		specialJobExecute(JobType.Login);
		this.login = true;
	}

	private void complete() {
		this.curTestEngine = null;
		this.status = Status.Complete;
		setAlarm(Status.Complete);
	}

	private void specialJobExecute(JobType type) throws JobExecutionFailException, Exception {
		if (type == JobType.Regular) return;
		HttpClientEngine specialJob = getJob(type);
		if (specialJob == null) return;
		execute(false, specialJob);
	}
	
	private void execute(boolean hasBreakTime, HttpClientEngine engine) throws JobExecutionFailException, Exception {
		if (engine.isComplete()) return;
		setupStart(engine);
		engine.execute(this.client, this);
		setupEnd(hasBreakTime);
	}
	
	private void setupStart(HttpClientEngine job) {
		this.curTestEngine = job;
		this.startTime = System.currentTimeMillis();
		setAlarm(Status.Doing);
	}

	private void setupEnd(boolean hasBreakTime) {
		this.endTime = System.currentTimeMillis();
		float executeTime = ((float)(this.endTime - this.startTime) / 1000);
		if (executeTime >= this.maxExecuteTime) {
			this.maxExecuteTime  = executeTime;
			if (this.curTestEngine != null)
				this.maxExecuteTimeJob = this.curTestEngine.getName() + this.curTestEngine.getExecuteCounter();
		}
		setAlarm(Status.Done);
		if (hasBreakTime)
			this.waitTimer.execute();
	}

	private void setAlarm(Status status) {
		this.status = status;
		if (this.listener != null)
			this.listener.changed();
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

	public void setRobotWaitTimer(RobotWaitTimer waitTimer) {
		this.waitTimer = waitTimer;
	}

	public String getErrorLog() {
		return this.errorLog;
	}

	public String getCurrentJobName() {
		if (this.curTestEngine == null)
			return "-";
		return this.curTestEngine.getName();
	}

	public String getCurrentJobRepeatCounter() {
		if (this.curTestEngine == null)
			return "0";
		return this.curTestEngine.getExecuteCounter();
	}
	
	public String getMaxExecuteTime() {
		return String.valueOf(this.maxExecuteTime) + " 초 [" + this.maxExecuteTimeJob + "]";
	}

	public Status getCurrentJobStatus() {
		return this.status;
	}

	public void setCellRefreshEventListener(CellRefreshEventListener listener) {
		this.listener = listener;
	}

	public void initExecute() {
		this.login = false;
	}

	@Override
	public String readMacroCode(String code) {
		return this.codebook.read(code, this);
	}
}