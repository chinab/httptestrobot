package kr.hayarobee.httptest.robot.job.http;

import org.apache.http.HttpResponse;

public interface ResultTester {

	boolean check(HttpClientEngine engine, HttpResponse res);

}
