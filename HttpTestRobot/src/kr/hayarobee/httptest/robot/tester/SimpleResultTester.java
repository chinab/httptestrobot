package kr.hayarobee.httptest.robot.tester;

import org.apache.http.HttpResponse;

import kr.hayarobee.httptest.robot.job.http.HttpClientEngine;
import kr.hayarobee.httptest.robot.job.http.ResultTester;

public class SimpleResultTester implements ResultTester {

	@Override
	public boolean check(HttpClientEngine engine, HttpResponse res) {
		return true;
	}

}
