package kr.hayarobee.httptest.robot.job;

public enum JobType {
	Init("init"),
	Login("login"),
	ReqLicense("requestLicense"),
	Logout("logout"),
	Shutdown("shutdown"),
	Regular("regular");
	
	private String id;
	private JobType(String id) { 
		this.id = id; 
	}
	
	public boolean is(String name) {
		if (name == null) return false;
		return name.trim().toLowerCase().equals(this.id);
	}

	public String get() {
		return this.id;
	}

	public static JobType get(String name) {
		if (name == null) throw new IllegalArgumentException();
		for (JobType type : values()) {
			if (type.is(name)) return type;
		}
		return Regular;
	}
	
	public boolean isSpecialJob() {
		return Regular != this;
	}
}
