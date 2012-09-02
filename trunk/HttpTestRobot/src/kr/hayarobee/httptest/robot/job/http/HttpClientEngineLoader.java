package kr.hayarobee.httptest.robot.job.http;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.hayarobee.httptest.robot.job.Scenario;
import kr.hayarobee.httptest.robot.job.ScenarioInventory;
import kr.hayarobee.httptest.robot.model.RobotBluePrint;
import kr.hayarobee.httptest.robot.model.Schedule;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

public class HttpClientEngineLoader {
	
	public static final String Robot = "robot";
	public static final String Scenario = "scenario";

	public Map<String, Object> load(String filename) throws Exception {
		File file = new File(filename);
		Map<String, Object> map = new HashMap<String, Object>();
		loadJobExecutorInfo(file, map);
		return map;
	}
	
	private Element getRootElement(File file, Map<String, Object> map) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(file);
		return doc.getRootElement();
	}

	private void loadJobExecutorInfo(File file, Map<String, Object> map) throws Exception {
		Element root = getRootElement(file, map);
		
		RobotBluePrint robot = new RobotBluePrint();
		Element robotElement = root.getChild("robot");
		robot.setPrefix(robotElement.getAttributeValue("prefix"));
		robot.setStartNum(Integer.parseInt(robotElement.getAttributeValue("start")));
		robot.setEndNum(Integer.parseInt(robotElement.getAttributeValue("end")));
		robot.setSchedule(Schedule.get(robotElement.getAttributeValue("schedule")));

		ScenarioInventory pool = new ScenarioInventory();
		String host = root.getAttributeValue("host");
		String app = root.getAttributeValue("app");
		int port = Integer.parseInt(root.getAttributeValue("port"));
		List<Element> elementList = root.getChildren("job");
		for (Element element : elementList) {
			pool.add(createJobExecutor(host, port, app, element));
		}
		
		map.put(Scenario, pool);
		map.put(Robot, robot);
	}
	
	private Scenario createJobExecutor(String host, int port, String appName, Element jobInfoElement) throws URISyntaxException {
		Scenario scenario = new Scenario();
		scenario.setConnectionInfo(host, port, appName, jobInfoElement);
		scenario.setResponseParser("parser", jobInfoElement);
		scenario.setResultTester("tester", jobInfoElement);
		scenario.setParameters("request-param", jobInfoElement);
		scenario.setParameters("form-param", jobInfoElement);
		scenario.setParameters("session-param", jobInfoElement);
		scenario.setParameters("query", jobInfoElement);
		return scenario;
	}

}
