package kr.hayarobee.httptest.robot.job.http;

import kr.hayarobee.httptest.robot.JobExecutionFailException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpRequestBase;

public class ResponseParser {

	public final HttpRequestBase parsing(HttpClientEngine engine, HttpResponse res) {
		ResultTester tester = engine.getResultTester();
		StatusLine statusLine = res.getStatusLine();
		int status = statusLine.getStatusCode();
		if (status == HttpStatus.SC_OK) {
			if (tester.check(engine, res) == false) {
				throw new JobExecutionFailException(engine.getName() + " 업무의 결과가 예상과 다릅니다. [" + statusLine.toString() + "]");
			}
		} else {
			return parsing(status, engine, res, tester);
		}
		return null;
	}
	
	protected HttpRequestBase parsing(int status, HttpClientEngine engine, HttpResponse res, ResultTester tester) {
		return null;
	}
	
}
