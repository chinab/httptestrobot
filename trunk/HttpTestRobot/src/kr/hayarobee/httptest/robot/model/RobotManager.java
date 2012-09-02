package kr.hayarobee.httptest.robot.model;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import kr.hayarobee.httptest.robot.job.ScenarioInventory;
import kr.hayarobee.httptest.robot.job.http.HttpClientEngineLoader;
import kr.hayarobee.httptest.robot.view.CellRefreshEventListener;

import org.apache.http.impl.client.DefaultHttpClient;

public class RobotManager extends AbstractTableModel implements Runnable, CellRefreshEventListener {
	private static final long serialVersionUID = 1L;
	
	private List<Robot> robotList;
	private Map<String, Object> confMap;
	
	public RobotManager() {
		this.robotList = new ArrayList<Robot>();
	}
	
	public int size() {
		return this.robotList.size();
	}
	
	@Override
	public void run() {
		if (setupRobots()) {
			startRobots();
		}
	}


	public void loadConfigFile(File configFile) throws IOException, Exception {
		initControlBox();
		HttpClientEngineLoader reader = new HttpClientEngineLoader();
		this.confMap = reader.load(configFile.getCanonicalPath());
		createRobot();
	}
	
	private void initControlBox() {
		this.robotList.clear();
		changed();
	}
	
	private boolean setupRobots() {
		if (this.confMap == null) return false;
		RobotBluePrint bluePrint = (RobotBluePrint) this.confMap.get(HttpClientEngineLoader.Robot);
		ScenarioInventory inven = (ScenarioInventory) this.confMap.get(HttpClientEngineLoader.Scenario);
		
		Schedule schedule = bluePrint.getSchedule();
		for (Robot robot : this.robotList) {
			robot.setHttpClient(new DefaultHttpClient());
			robot.setRobotWaitTimer(new LongWaitTimer());
			robot.setCellRefreshEventListener(this);
			robot.setSchedule(schedule);
			robot.setup(inven);
		}
		return true;
	}
	
	private void createRobot() {
		if (this.confMap == null) return;
		RobotBluePrint bluePrint = (RobotBluePrint) this.confMap.get(HttpClientEngineLoader.Robot);
		String prefix = bluePrint.getPrefix();
		int start = bluePrint.getStartNum();
		int end = bluePrint.getEndNum();
		for (int idx = start; idx <= end; idx++) {
			Robot robot = new Robot(prefix, String.valueOf(idx));
			this.robotList.add(robot);
			changed();
		}
	}

	private void startRobots() {
		for (Robot robot : this.robotList) {
			Thread tRobot = new Thread(robot, robot.getName());
			robot.initExecute();
			tRobot.start();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}
	}

	@Override
	public int getRowCount() {
		return this.robotList.size();
	}

	@Override
	public int getColumnCount() {
		return 6;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0: return "로봇명";
		case 1: return "현재 작업명";
		case 2: return "작업 횟수";
		case 3: return "작업 상태";
		case 4: return "최대 작업 시간";
		case 5: return "상세";
		default:
			return "";
		}
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Robot robot = this.robotList.get(rowIndex);
		switch (columnIndex) {
		case 0: return robot.getName();
		case 1: return robot.getCurrentJobName();
		case 2: return robot.getCurrentJobRepeatCounter();
		case 3: return robot.getCurrentJobStatus();
		case 4: return robot.getMaxExecuteTime();
		case 5: return robot.getErrorLog();
		default:
			return "Invalid";
		}
	}

	@Override
	public void changed() {
		fireTableDataChanged();
	}

	private final TableRenderer renderer = new TableRenderer();
	public TableCellRenderer getRenderer() {
		return this.renderer;
	}
	
	private class TableRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;
		
		public TableRenderer() {
			setOpaque(true);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Status status = (Status) getValueAt(row, 3);
			Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column); 
			if (status == null) return comp;
			switch (status) {
			case Error: comp.setBackground(Color.RED); break;
			case Done: comp.setBackground(new Color(240, 240, 240)); break;
			case Doing: comp.setBackground(Color.GREEN); break;
			case Stop: comp.setBackground(Color.WHITE); break;
			case Complete: comp.setBackground(Color.WHITE); break;
			}
			return comp;
		}
	}

}
