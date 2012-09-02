package kr.hayarobee.httptest.robot.model;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import kr.hayarobee.httptest.robot.job.CodeBreaker;
import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicMatch;

import org.apache.http.NameValuePair;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;

public class ItemSingle implements Item {

	private String key;
	private List<String> values;
	private Random rand = new Random();
	private boolean isFile;
	
	private ItemSingle() {
		this.values = new ArrayList<String>();
	}
	
	public ItemSingle(String key, String defaultValue) {
		this();
		this.key = key;
		this.values.add(0, defaultValue);
	}

	@Override
	public String getKey() {
		return key;
	}
	
	@Override
	public int size() {
		return this.values.size();
	}

	public void setFile(boolean isFile) {
		this.isFile = isFile;
	}
	
	@Override
	public boolean isFile() {
		return this.isFile;
	}

	public void add(String ...values) {
		for (String value : values) {
			this.values.add(this.values.size(), value);
		}
	}

	public List<String> getValues() {
		return this.values;
	}

	public String getDefaultValue(CodeBreaker breaker) {
		return getValue(0, breaker);
	}

	public String getValue(int idx, CodeBreaker breaker) {
		return breaker.readMacroCode(this.values.get(idx));
	}

	public String getValue(CodeBreaker breaker) {
		this.rand.setSeed(System.currentTimeMillis());
		return getValue(this.rand.nextInt(this.values.size()), breaker);
	}

	@Override
	public void getQueryString(StringBuffer buffer, CodeBreaker breaker) {
		String value = getValue(breaker);
		buffer.append(getKey()).append("=").append(value).append("&");
	}

	@Override
	public void addParameter(HttpParams params, CodeBreaker breaker) {
		String value = getValue(breaker);
		params.setParameter(getKey(), value);
	}

	@Override
	public void addNameValuePair(List<NameValuePair> pairs, CodeBreaker breaker) {
		String value = getValue(breaker);
		pairs.add(new BasicNameValuePair(getKey(), value));
	}

	@Override
	public void addPart(MultipartEntity entity, CodeBreaker breaker) {
		if (isFile()) {
			String path = getValue(breaker);
			File file = new File(path);
			if (file.isDirectory()) return;
			MagicMatch match = null;
			try {
				match = Magic.getMagicMatch(file, false);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				String mimeType;
				if (match == null) {
					mimeType = "application/octet-stream";
				} else {
					mimeType = match.getMimeType();
				}
				ContentBody fileBody = new FileBody(file, mimeType);
				entity.addPart(getKey(), fileBody);
			}
		} else {
			String value = getValue(breaker);
			try {
				entity.addPart(getKey(), new StringBody(value, "text/plain", Charset.forName("UTF-8")));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}
}
