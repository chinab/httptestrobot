package kr.hayarobee.httptest.robot.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import kr.hayarobee.httptest.robot.model.RobotManager;

public class DynaRobotManagerMonitor extends JFrame implements MouseListener {
	private static final long serialVersionUID = 1L;
	
	private JTable table;

	private RobotManager robotManager;

	public DynaRobotManagerMonitor(RobotManager manager) {
		this.robotManager = manager;
	}
	
	public void initFrame() {
		setPreferredSize(new Dimension(1024, 480));
		
		this.table = new JTable(this.robotManager);
		this.table.setDefaultRenderer(Object.class, this.robotManager.getRenderer());
		this.table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.table.addMouseListener(this);
		
		add(new JScrollPane(this.table), BorderLayout.CENTER);
		
		JButton btLoad = new JButton("작업 정보 읽기");
		JButton btStart = new JButton("작업 시작");
		
		btLoad.addActionListener(new LoadActionListener(this, btStart));
		btStart.addActionListener(new StartActionListener());

		JPanel panel = new JPanel();
		panel.add(btLoad);
		panel.add(btStart);
		
		add(panel, BorderLayout.SOUTH);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
	}
	
	private class StartActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			Thread t = new Thread(robotManager);
			t.start();
		}
		
	}
	
	private class LoadActionListener implements ActionListener {
		
		private JFrame parent;
		private JButton btStart;
		private JFileChooser chooser;

		public LoadActionListener(JFrame parent, JButton btStart) {
			this.parent = parent;
			this.btStart = btStart;
			this.chooser = new JFileChooser("conf");
			btStart.setEnabled(false);
			chooser.setMultiSelectionEnabled(false);
			chooser.setCurrentDirectory(new File("conf"));
		}

		@Override
		public void actionPerformed(ActionEvent ae) {
			if (JFileChooser.APPROVE_OPTION == this.chooser.showDialog(this.parent, "Choose")) {
				File selFile = this.chooser.getSelectedFile();
				if (selFile == null) return;
				if (selFile.isFile()) {
					try {
						robotManager.loadConfigFile(selFile);
						setTitle(selFile.getCanonicalPath());
						btStart.setEnabled(true);
					} catch (IOException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		int colSize = this.table.getColumnCount();
		int columnIndex = this.table.getSelectedColumn();
		if (columnIndex == (colSize - 1)) {
			int rowIndex = this.table.getSelectedRow();
			String msg = (String) this.robotManager.getValueAt(rowIndex, columnIndex);
			if (msg == null || msg.trim() == "") return;
			System.out.println(msg);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
}
