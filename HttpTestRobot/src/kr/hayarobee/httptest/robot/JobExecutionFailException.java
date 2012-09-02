package kr.hayarobee.httptest.robot;


public class JobExecutionFailException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	public JobExecutionFailException(String msg) {
		super(msg);
	}

	public JobExecutionFailException(Throwable t) {
		super(t);
	}
}
