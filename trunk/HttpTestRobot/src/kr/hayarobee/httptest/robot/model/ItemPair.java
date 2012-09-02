package kr.hayarobee.httptest.robot.model;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import kr.hayarobee.httptest.robot.job.CodeBreaker;

import org.apache.http.NameValuePair;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;

public class ItemPair implements Item {

	private int size;
	private List<ItemSingle> itemPool;
	private Random rand = new Random();
	
	public ItemPair() {
		this.itemPool = new ArrayList<ItemSingle>();
	}

	@Override
	public int size() {
		return this.size;
	}

	public void link(ItemSingle ...items) {
		if (items == null || items.length <= 0) 	return;
		
		this.size = items[0].size();
		for (ItemSingle item : items) {
			if (this.size != item.size()) {
				throw new IllegalArgumentException("연결하려는 Item의 크기가 다릅니다.");
			}
			this.itemPool.add(item);
			this.size = item.size();
		}
	}

	public Map<String, String> getDefaultValue(CodeBreaker breaker) {
		Map<String, String>map = new HashMap<String, String>();
		for (ItemSingle item : this.itemPool) {
			map.put(item.getKey(), item.getDefaultValue(breaker));
		}
		return map;
	}

	public Map<String, String> getValue(CodeBreaker breaker) {
		rand.setSeed(System.currentTimeMillis());
		int idx = rand.nextInt(size());
		Map<String, String>map = new HashMap<String, String>();
		for (ItemSingle item : this.itemPool) {
			map.put(item.getKey(), item.getValue(idx, breaker));
		}
		return map;
	}

	@Override
	public String getKey() {
		StringBuffer buf = new StringBuffer("pair");
		for (ItemSingle item : this.itemPool) {
			buf.append('-').append(item.getKey());
		}
		return buf.toString();
	}

	@Override
	public boolean isFile() {
		return false;
	}

	@Override
	public void getQueryString(StringBuffer buffer, CodeBreaker breaker) {
		Map<String, String> map = getValue(breaker);
		for (String key : map.keySet()) {
			String value = map.get(key);
			buffer.append(key).append("=").append(value).append("&");
		}
	}

	@Override
	public void addParameter(HttpParams params, CodeBreaker breaker) {
		Map<String, String> map = getValue(breaker);
		for (String key : map.keySet()) {
			String value = map.get(key);
			params.setParameter(key, value);
		}
	}

	@Override
	public void addNameValuePair(List<NameValuePair> pairs, CodeBreaker breaker) {
		Map<String, String> map = getValue(breaker);
		for (String key : map.keySet()) {
			String value = map.get(key);
			NameValuePair pair = new BasicNameValuePair(key, value);
			pairs.add(pair);
		}
	}

	@Override
	public void addPart(MultipartEntity entity, CodeBreaker breaker) throws UnsupportedEncodingException {
		Map<String, String> map = getValue(breaker);
		for (String key : map.keySet()) {
			String value = breaker.readMacroCode(map.get(key));
			entity.addPart(key, new StringBody(value, "text/plain", Charset.forName("UTF-8")));
		}
	}
}
