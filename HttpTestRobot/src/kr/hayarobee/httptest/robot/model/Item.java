package kr.hayarobee.httptest.robot.model;

import java.util.List;

import kr.hayarobee.httptest.robot.job.CodeBreaker;

import org.apache.http.NameValuePair;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.params.HttpParams;

public interface Item {
	
	int size();
	
	String getKey();
	
	boolean isFile();
	
	void addPart(MultipartEntity entity, CodeBreaker breaker) throws Exception;

	void getQueryString(StringBuffer buffer, CodeBreaker breaker);

	void addParameter(HttpParams params, CodeBreaker breaker);

	void addNameValuePair(List<NameValuePair> pairs, CodeBreaker breaker);

}
