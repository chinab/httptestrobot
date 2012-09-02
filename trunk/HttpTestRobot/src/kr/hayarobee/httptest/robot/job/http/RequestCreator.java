package kr.hayarobee.httptest.robot.job.http;

import kr.hayarobee.httptest.robot.job.CodeBreaker;
import kr.hayarobee.httptest.robot.job.Scenario;

import org.apache.http.client.methods.HttpRequestBase;

public interface RequestCreator {

	HttpRequestBase getHttpRequest(Scenario scenario, CodeBreaker breaker) throws Exception;

}
