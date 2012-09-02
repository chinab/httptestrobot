package kr.hayarobee.httptest.robot.model;

public enum Schedule {
	Loop("loop"),
	Step("step"),
	Random("random");
	
	private String id;
	private Schedule(String id) {
		this.id = id;
	}
	
	public static Schedule get(String name) {
		if (name == null) return Loop;
		for (Schedule s : values()) {
			if (name.trim().toLowerCase().equals(s.id))
				return s;
		}
		return Loop;
	}
}
