package kr.hayarobee.httptest.robot.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import kr.hayarobee.httptest.robot.EmptyJobException;
import kr.hayarobee.httptest.robot.job.JobType;
import kr.hayarobee.httptest.robot.job.Scenario;
import kr.hayarobee.httptest.robot.job.ScenarioInventory;
import kr.hayarobee.httptest.robot.job.http.HttpClientEngine;
import kr.hayarobee.httptest.robot.job.http.RequestCreator;
import kr.hayarobee.httptest.robot.job.http.SimpleRequestCreator;

public class EngineRoom {
	
	private HttpClientEngine booting;
	private HttpClientEngine login;
	private HttpClientEngine reqLicense;
	private HttpClientEngine logout;
	private HttpClientEngine shutdown;
	private List<HttpClientEngine> list;
	
	private RequestCreator requestCreator;

	public EngineRoom() {
		this.requestCreator = new SimpleRequestCreator();
		this.list = new ArrayList<HttpClientEngine>();
		reset();
	}

	public void reset() {
		this.booting = null;
		this.login = null;
		this.reqLicense = null;
		this.logout = null;
		this.shutdown = null;
		this.list.clear();
	}
	
	public void setup(String name, ScenarioInventory inven) {
		List<String> keys = inven.getKeys();
		for (String key : keys) {
			Scenario scenario = inven.get(key);
			programming(key, scenario);
		}
	}
	

	private void programming(String key, Scenario scenario) {
		HttpClientEngine engine = new HttpClientEngine();
		engine.setRequestCreator(this.requestCreator);
		engine.setResponseParser(scenario.getResponseParser());
		engine.setup(scenario);
		register(key, engine);
	}

	private void register(String key, HttpClientEngine engine) {
		JobType type = JobType.get(key);
		engine.setJobType(type);
		switch (type) {
		case Init: this.booting = engine; break;
		case Login: this.login = engine; break;
		case ReqLicense: this.reqLicense = engine; break;
		case Logout: this.logout = engine; break;
		case Shutdown: this.shutdown = engine; break;
		default:
			this.list.add(this.list.size(), engine);
		}
	}

	public HttpClientEngine get(JobType type, Schedule schedule) {
		switch (type) {
		case Init: return booting;
		case Login: return login;
		case ReqLicense: return reqLicense;
		case Logout: return logout;
		case Shutdown: return shutdown;
		default:
			return get(schedule);
		}
	}

	private HttpClientEngine get(Schedule schedule) {
		switch (schedule) {
		case Step: return getNextStepSelect(this.list);
		case Random: return randomSelect(this.list);
		case Loop:
		default:
			return loopSelect(this.list);
		}
	}

	private HttpClientEngine getNextStepSelect(List<HttpClientEngine> list) {
		for (int idx = 0; idx < list.size(); idx++) {
			HttpClientEngine engine = list.get(idx);
			if (engine.isComplete()) continue;
			return engine;
		}
		return null;
	}

	private static Random rand = new Random();
	private HttpClientEngine randomSelect(List<HttpClientEngine> list) {
		if (list == null || list.isEmpty())
			throw new EmptyJobException();
		rand.setSeed(System.currentTimeMillis());
		int idx = rand.nextInt(list.size());
		return list.get(idx);
	}

	private int curSelIdx = 0;
	private HttpClientEngine loopSelect(List<HttpClientEngine> list) {
		if (list == null || list.isEmpty())
			throw new EmptyJobException();
		this.curSelIdx = this.curSelIdx % list.size();
		HttpClientEngine job = list.get(this.curSelIdx);
		this.curSelIdx += 1;
		return job;
	}
	
	public boolean isEmpty() {
		return this.list.isEmpty();
	}

	public boolean isComplete() {
		if (this.list.isEmpty()) return true;
		for (HttpClientEngine engine : this.list) {
			if (engine.isComplete() == false)
				return false;
		}
		return true;
	}
	
	public boolean isSpecialJobComplete() {
		if (this.booting.isComplete() == false) return false;
		if (this.login.isComplete() == false) return false;
		if (this.logout.isComplete() == false) return false;
		if (this.shutdown.isComplete() == false) return false;
		return true;
	}
}
