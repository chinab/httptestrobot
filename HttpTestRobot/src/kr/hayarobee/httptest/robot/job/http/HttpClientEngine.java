package kr.hayarobee.httptest.robot.job.http;

import java.io.IOException;

import kr.hayarobee.httptest.robot.JobExecutionFailException;
import kr.hayarobee.httptest.robot.job.CodeBreaker;
import kr.hayarobee.httptest.robot.job.JobType;
import kr.hayarobee.httptest.robot.job.Scenario;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

public class HttpClientEngine implements RequestCreator {
	
	private String name;
	private JobType type;
	private Scenario scenario;
	private int curRepeatCount;
	private int maxRepeatCount;
	
	private ResultTester resultTester;
	private ResponseParser responseParser;
	private RequestCreator requestCreator;

	public HttpClientEngine() {
		this.requestCreator = this;
	}

	public String getName() {
		return this.name;
	}

	public String getAppName() {
		if (this.scenario == null)
			throw new IllegalStateException("App 이름을 알 수 없습니다.");
		return this.scenario.getAppName();
	}

	public JobType getJobType() {
		return this.type;
	}
	
	public void setJobType(JobType type) {
		this.type = type;
	}

	public void setup(Scenario scenario) {
		this.scenario = scenario;
		this.name = scenario.getName();
		this.curRepeatCount = 0;
		this.maxRepeatCount = scenario.getMaxExecuteTimes();
	}

	public void setResponseParser(ResponseParser responseParser) {
		this.responseParser = responseParser;
	}
	
	public void setRequestCreator(RequestCreator creator) {
		this.requestCreator = creator;
	}

	public ResultTester getResultTester() {
		return this.resultTester;
	}

	public String getExecuteCounter() {
		StringBuffer buf = new StringBuffer();
		buf.append(this.curRepeatCount).append('/').append(this.maxRepeatCount);
		return buf.toString();
	}

	public boolean isComplete() {
		return this.curRepeatCount >= this.maxRepeatCount;
	}

	public void execute(HttpClient client, CodeBreaker book) throws JobExecutionFailException, Exception {
		if (client == null) {
			throw new JobExecutionFailException("HttpClient 객체가 준비되지 않았습니다.");
		}
		this.curRepeatCount += 1;
		HttpRequestBase req = this.requestCreator.getHttpRequest(this.scenario, book);
		requestHttpServer(req, client);
	}
	
	private void requestHttpServer(HttpRequestBase request, HttpClient client) throws JobExecutionFailException {
		HttpResponse res = null;
		HttpContext httpContext = new BasicHttpContext();
		
		try {
			res = client.execute(request, httpContext);
		} catch (ClientProtocolException e) {
			throw new JobExecutionFailException(e.getCause());
		} catch (IOException e) {
			throw new JobExecutionFailException(e.getCause());
		} finally {
			HttpRequestBase req = null;
			if (this.responseParser != null) {
				req = this.responseParser.parsing(this, res);
			}
			
			try {
				if (res != null) {
					HttpEntity entity = res.getEntity();
					EntityUtils.consume(entity);
				}
			} catch (IOException e) {
				throw new JobExecutionFailException(e.getCause());
			}
			
			if (req != null) 
				requestHttpServer(req, client);
		}
	}

	@Override
	public HttpRequestBase getHttpRequest(Scenario scenario, CodeBreaker book) {
		throw new UnsupportedOperationException();
	}
}

