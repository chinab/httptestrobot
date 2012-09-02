package kr.hayarobee.httptest.robot.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScenarioInventory {
	
	private List<String> keys;
	private Map<String, Scenario> pool;
	
	public ScenarioInventory() {
		this.keys = new ArrayList<String>();
		this.pool = new HashMap<String, Scenario>();
	}

	public void add(Scenario scenario) {
		String key = scenario.getName();
		this.keys.add(this.keys.size(), key);
		this.pool.put(key, scenario);
	}

	public int size() {
		return this.pool.size();
	}

	public Scenario get(String key) {
		return this.pool.get(key);
	}
	
	public List<String> getKeys() {
		return this.keys;
	}
}
