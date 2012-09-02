package kr.hayarobee.httptest.robot.job.http;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Map;

import kr.hayarobee.httptest.robot.job.CodeBreaker;
import kr.hayarobee.httptest.robot.job.Scenario;
import kr.hayarobee.httptest.robot.job.ScenarioInventory;
import kr.hayarobee.httptest.robot.job.http.HttpClientEngineLoader;
import kr.hayarobee.httptest.robot.job.http.SimpleRequestCreator;
import kr.hayarobee.httptest.robot.model.Codebook;
import kr.hayarobee.httptest.robot.model.Item;
import kr.hayarobee.httptest.robot.model.ItemPair;
import kr.hayarobee.httptest.robot.model.ItemSingle;
import kr.hayarobee.httptest.robot.model.Robot;
import kr.hayarobee.httptest.robot.model.RobotBluePrint;
import kr.hayarobee.httptest.robot.model.Schedule;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.junit.Before;
import org.junit.Test;

public class HttpTestEngineLoaderTest implements CodeBreaker {
	
	private Codebook codebook = new Codebook();
	private HttpClientEngineLoader loader;

	@Before
	public void setUp() {
		this.loader = new HttpClientEngineLoader();
	}
	
	@Test
	public void testPaserAntTester() throws Exception {
		Map<String, Object> map = this.loader.load("test/readComplexJobInfo.xml");
		ScenarioInventory inven = (ScenarioInventory) map.get(HttpClientEngineLoader.Scenario);
		Scenario scenario = inven.get("job1");
	}
	
	@Test
	public void testMultiFileInfo() throws Exception {
		Map<String, Object> map = this.loader.load("test/readComplexJobInfo.xml");
		ScenarioInventory inven = (ScenarioInventory) map.get(HttpClientEngineLoader.Scenario);
		Scenario scenario = inven.get("job1");
		
		Item item = scenario.getMultipartParameters().get("attachFile");
		assertNotNull(item);
	}
	
	@Test
	public void testReadQueryJobInfo2() throws Exception {
		Map<String, Object> map = this.loader.load("test/readQueryJobInfo.xml");
		ScenarioInventory inven = (ScenarioInventory) map.get(HttpClientEngineLoader.Scenario);
		Scenario scenario = inven.get("job2");

		Item item1 = scenario.getQueryParameters().get("pair-key21-key31");
		assertNotNull(item1);
		assertTrue(item1 instanceof ItemPair);
		ItemPair ip1 = (ItemPair) item1;
		assertThat(ip1.size(), is(3));
		Map<String, String>value1 = ip1.getDefaultValue(this);
		assertThat(value1.get("key21"), is("value21"));
		assertThat(value1.get("key31"), is("value31"));
	}
	
	@Test
	public void testReadQueryJobInfo1() throws Exception {
		Map<String, Object> map = this.loader.load("test/readQueryJobInfo.xml");
		ScenarioInventory inven = (ScenarioInventory) map.get(HttpClientEngineLoader.Scenario);
		Scenario scenario = inven.get("job1");
		
		Item item1 = scenario.getQueryParameters().get("key1");
		assertTrue(item1 instanceof ItemSingle);
		ItemSingle is1 = (ItemSingle) item1;
		assertThat(is1.size(), is(1));
		assertThat(is1.getDefaultValue(this), is("value1"));
		
		Item item2 = scenario.getQueryParameters().get("key2");
		assertTrue(item2 instanceof ItemSingle);
		ItemSingle is2 = (ItemSingle) item2;
		assertThat(is2.size(), is(1));
		assertThat(is2.getDefaultValue(this), is("admin"));
		
		SimpleRequestCreator creator = new SimpleRequestCreator();
		HttpRequestBase method = creator.getHttpRequest(scenario, this);
		URI uri = method.getURI();
		assertTrue(uri.toString().contains("key1=value"));
		assertTrue(uri.toString().contains("key2=admin"));
	}

	@Test
	public void testReadRobotInfo() throws Exception {
		Map<String, Object> map = this.loader.load("test/readRobotInfo.xml");
		RobotBluePrint bean = (RobotBluePrint) map.get(HttpClientEngineLoader.Robot);
		assertThat(bean.getPrefix(), is("robot"));
		assertThat(bean.getStartNum(), is(1));
		assertThat(bean.getEndNum(), is(10));
		assertThat(bean.getSchedule(), is(Schedule.Loop));
	}

	@Test
	public void testSimpleJobInfo() throws Exception {
		Map<String, Object> map = this.loader.load("test/readSimpleJobInfo.xml");
		ScenarioInventory inven = (ScenarioInventory) map.get(HttpClientEngineLoader.Scenario);
		assertThat(inven.size(), is(1));
		assertNotNull(inven.get("job1"));
		
		Scenario scenario = inven.get("job1");
		SimpleRequestCreator creator = new SimpleRequestCreator();
		HttpRequestBase method = creator.getHttpRequest(scenario, this);
		URI uri = method.getURI();
		
		assertThat(uri.toString(), is("http://localhost:8080/app/job1-path"));
		assertThat(scenario.getMethod(), is(HttpGet.METHOD_NAME));
		assertThat(scenario.getMaxExecuteTimes(), is(23));
	}

	@Override
	public String readMacroCode(String code) {
		Robot robot = mock(Robot.class);
		when(robot.getName()).thenReturn("robot1");
		when(robot.getCurrentJobRepeatCounter()).thenReturn("1/100");
		return this.codebook.read(code, robot);
	}
}
