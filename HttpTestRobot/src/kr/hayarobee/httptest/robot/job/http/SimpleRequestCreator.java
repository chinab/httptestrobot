package kr.hayarobee.httptest.robot.job.http;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kr.hayarobee.httptest.robot.job.CodeBreaker;
import kr.hayarobee.httptest.robot.job.Scenario;
import kr.hayarobee.httptest.robot.model.Item;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;

public class SimpleRequestCreator implements RequestCreator {

	@Override
	public HttpRequestBase getHttpRequest(Scenario scenario, CodeBreaker breaker) throws Exception {
		String method = scenario.getMethod();
		HttpRequestBase req = getHttpRequestBase(method);
		req.setURI(getURI(scenario, breaker));
		addFormParameters(req, scenario, breaker);
		return req;
	}

	private URI getURI(Scenario scenario, CodeBreaker breaker) throws URISyntaxException {
		String host = scenario.getHost();
		int port = scenario.getPort();
		String query = getQueryString(scenario, breaker);
		String fragment = scenario.getFragment();
		String fullPath = scenario.getAppName() + scenario.getPath();
		return URIUtils.createURI("http", host, port, fullPath, query, fragment);
	}

	private String getQueryString(Scenario scenario, CodeBreaker breaker) {
		Item[] items = scenario.getQuery();
		if (items == null || items.length == 0) return null;
		StringBuffer buffer = new StringBuffer();
		for (Item item : items) {
			item.getQueryString(buffer, breaker);
		}
		buffer.deleteCharAt(buffer.length()-1);
		return buffer.toString();
	}

	private HttpRequestBase getHttpRequestBase(String method) {
		HttpRequestBase req;
		String reqMethod = method.trim().toUpperCase();
		if (reqMethod.equals("GET")) {
			req = new HttpGet();
		} else if (reqMethod.equals("POST")) {
			req = new HttpPost();
		} else {
			req = null;
			throw new UnsupportedOperationException("지원하지 않는 메소드입니다.");
		}
		return req;
	}

	private void addFormParameters(HttpRequestBase req, Scenario scenario, CodeBreaker breaker) throws Exception {
		if (req.getMethod().equals(HttpPost.METHOD_NAME) == false) return;
		
		Map<String, Item> fMap = scenario.getFormParameters();
		Map<String, Item> mMap = scenario.getMultipartParameters();
		
		if (mMap.isEmpty()) {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			Set<String> keys = fMap.keySet();
			for (String key : keys) {
				Item item = fMap.get(key);
				item.addNameValuePair(pairs, breaker);
			}
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(pairs, "UTF-8");
			HttpPost post = (HttpPost) req;
			post.setEntity(entity);
		} else {
			MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
			Set<String> keys = fMap.keySet();
			for (String key : keys) {
				Item item = fMap.get(key);
				item.addPart(entity, breaker);
			}
			Set<String> files = mMap.keySet();
			for (String key : files) {
				Item item = mMap.get(key);
				item.addPart(entity, breaker);
			}
			HttpPost post = (HttpPost) req;
			post.setEntity(entity);
		}
	}
}
