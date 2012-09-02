package kr.hayarobee.httptest.robot.job;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.hayarobee.httptest.robot.job.http.ResponseParser;
import kr.hayarobee.httptest.robot.job.http.ResultTester;
import kr.hayarobee.httptest.robot.model.Item;
import kr.hayarobee.httptest.robot.model.ItemPair;
import kr.hayarobee.httptest.robot.model.ItemSingle;
import kr.hayarobee.httptest.robot.tester.SimpleResultTester;

import org.jdom2.Element;

public class Scenario {
	
	private ResponseParser responseParser;
	private ResultTester resultTester;
	
	private String name;
	private int maxExecuteTimes;

	private String host;
	private int port;
	private String appName;
	private String path;
	private String fragment;
	private String method;

	private Map<String, Item> reqParams;
	private Map<String, Item> formParams;
	private Map<String, Item> multiPartInfo;
	private Map<String, Item> queryParams;

	
	public Scenario() {
		this.reqParams = new HashMap<String, Item>();
		this.formParams = new HashMap<String, Item>();
		this.multiPartInfo = new HashMap<String, Item>();
		this.queryParams = new HashMap<String, Item>();
	}

	public String getName() {
		return this.name;
	}
	
	public String getHost() {
		return this.host;
	}

	public String getAppName() {
		return this.appName;
	}

	public int getPort() {
		return this.port;
	}

	public String getPath() {
		return this.path;
	}

	public String getFragment() {
		return this.fragment;
	}
	
	public Item[] getQuery() {
		return createQueryItemList(this.queryParams);
	}

	private Item[] createQueryItemList(Map<String, Item> query) {
		if (this.queryParams.isEmpty()) return null;
		
		String[] keys = query.keySet().toArray(new String[0]);
		Item[] items = new ItemSingle[keys.length];
		for (int idx = 0; idx < keys.length; idx++) {
			String key = keys[idx];
			items[idx] = query.get(key);
		}
		return items;
	}

	public void setConnectionInfo(String host, int port, String appName, Element element) {
		final String name = element.getAttributeValue("name");
		final String path = element.getAttributeValue("path");
		final String method = element.getAttributeValue("method");
		final String times = element.getAttributeValue("times");
		
		this.host = host;
		this.port = port;
		this.appName = appName;
		this.name = name;
		this.path = path;
		this.method = (method == null) ? "GET" : method;
		this.maxExecuteTimes = (times == null) ? 1 : Integer.parseInt(times);
	}

	public void setResponseParser(String name, Element parent) {
		Element element = parent.getChild(name);
		if (element == null) {
			setDefaultResponseParser();
			return;
		}
		String className = element.getText();
		if (className == null) {
			setDefaultResponseParser();
			return;
		}
		
		try {
			createResponseParser(className);
		} catch (Exception e) {
			e.printStackTrace();
			setDefaultResponseParser();
		}
	}

	@SuppressWarnings("unchecked")
	private void createResponseParser(String className) throws Exception {
		Class<ResponseParser> clazz = (Class<ResponseParser>) ClassLoader.getSystemClassLoader().loadClass(className);
		Constructor<ResponseParser> constructor = clazz.getConstructor();
		this.responseParser = constructor.newInstance();
	}

	private void setDefaultResponseParser() {
		this.responseParser = new ResponseParser();
	}

	public void setResultTester(String name, Element parent) {
		Element element = parent.getChild(name);
		if (element == null) {
			setDefaultResultTester();
			return;
		}
		String className = element.getText();
		if (className == null) {
			setDefaultResultTester();
			return;
		}
		
		try {
			createResultTester(className);
		} catch (Exception e) {
			e.printStackTrace();
			setDefaultResultTester();
		}
	}

	@SuppressWarnings("unchecked")
	private void createResultTester(String className) throws Exception {
		Class<ResultTester> clazz = (Class<ResultTester>) ClassLoader.getSystemClassLoader().loadClass(className);
		Constructor<ResultTester> constructor = clazz.getConstructor();
		this.resultTester = constructor.newInstance();
	}
	
	private void setDefaultResultTester() {
		this.resultTester = new SimpleResultTester();
	}

	public void setParameters(String method, Element parent) {
		Element element = parent.getChild(method);
		if (element == null) return;
		
		List<Element> pairs = element.getChildren("pair");
		for (Element pair : pairs) {
			Item item = parsingItemPair(pair);
			if (item == null) continue;
			addParameter(method, item);
		}
		
		List<Element> params = element.getChildren("item");
		for (Element param : params) {
			Item item = parsingItemSingle(param);
			if (item == null) continue;
			addParameter(method, item);
		}
	}

	private void addParameter(String method, Item item) {
		Map<String, Item> map = getParameterMap(item.isFile(), method);
		map.put(item.getKey(), item);
	}
	
	private Item parsingItemPair(Element element) {
		List<Element> params = element.getChildren("item");
		if (params == null || params.isEmpty()) return null;
		
		List<ItemSingle> list = new ArrayList<ItemSingle>();
		for (Element param : params) {
			ItemSingle item = parsingItemSingle(param);
			if (item == null) continue;
			list.add(item);
		}
		
		ItemPair pair = new ItemPair();
		pair.link(list.toArray(new ItemSingle[0]));
		return pair;
	}

	private ItemSingle parsingItemSingle(Element itemElement) {
		String key = itemElement.getAttributeValue("key");
		String value = itemElement.getAttributeValue("value");
		ItemSingle item = new ItemSingle(key, value);

		String file = itemElement.getAttributeValue("file");
		boolean isFile = (file == null) ? false : Boolean.valueOf(file);
		item.setFile(isFile);
		
		List<Element> list = itemElement.getChildren("others");
		for (Element e : list) {
			String v = e.getAttributeValue("value");
			item.add(v);
		}
		return item;
	}

	private Map<String, Item> getParameterMap(boolean isFile, String method) {
		if (method.equals("request-param")) {
			return this.reqParams;
		} else if (method.equals("form-param")) {
			if (isFile) return this.multiPartInfo;
			else return this.formParams;
		} else if (method.equals("query")) {
			return this.queryParams;
		} else {
			throw new IllegalArgumentException();
		}
	}

	public String getMethod() {
		return this.method;
	}

	public int getMaxExecuteTimes() {
		return this.maxExecuteTimes;
	}
	
	public Map<String, Item> getQueryParameters() {
		return this.queryParams;
	}

	public Map<String, Item> getRequestParameters() {
		return this.reqParams;
	}

	public Map<String, Item> getFormParameters() {
		return this.formParams;
	}

	public Map<String, Item> getMultipartParameters() {
		return this.multiPartInfo;
	}

	public ResponseParser getResponseParser() {
		return this.responseParser;
	}
	
	public ResultTester getResultTester() {
		return this.resultTester;
	}
}
