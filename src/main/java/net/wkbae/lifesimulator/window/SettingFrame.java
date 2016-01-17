package net.wkbae.lifesimulator.window;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Hashtable;
import java.util.Random;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.wkbae.lifesimulator.Gene;
import net.wkbae.lifesimulator.Simulation;
import net.wkbae.lifesimulator.SimulationListener;
import net.wkbae.lifesimulator.SimulationPainter;
import net.wkbae.lifesimulator.SimulationRecorder;
import net.wkbae.lifesimulator.SimulationSetting;

import org.apache.commons.codec.binary.Base64;
import org.jbox2d.common.Vec2;

public class SettingFrame extends JDialog implements ActionListener, ChangeListener, SimulationListener {
	private static final long serialVersionUID = 1432445203635177426L;
	
	private SimulatorFrame simulatorFrame;
	
	private JTextArea seed;
	
	private JLabel sizeLabel;
	private JLabel mutationLabel;
	
	private JSlider sizeSlider;
	private JSlider viewSlider;
	private JSlider speedSlider;
	private JSlider fpsSlider;
	private JSlider mutationSlider;
	private JSlider dampingSlider;
	
	private JCheckBox chkRecord;
	
	private JButton delSeed;
	
	private JButton startBtn;
	private JToggleButton statBtn;
	private JButton resetBtn;
	
	private JFormattedTextField cellCount;
	
	public SettingFrame(SimulatorFrame simulatorFrame) {
		super(simulatorFrame, "설정");
		//this.setIconImage(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB_PRE));
		
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		this.simulatorFrame = simulatorFrame;
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{10, 0, 0, 0, 0, 10};
		gridBagLayout.rowHeights = new int[]{10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 0.0};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
		getContentPane().setLayout(gridBagLayout);
		
		JLabel lblRandomSeed = new JLabel("시드");
		GridBagConstraints gbc_lblRandomSeed = new GridBagConstraints();
		gbc_lblRandomSeed.insets = new Insets(0, 0, 5, 5);
		gbc_lblRandomSeed.gridx = 1;
		gbc_lblRandomSeed.gridy = 1;
		getContentPane().add(lblRandomSeed, gbc_lblRandomSeed);
		
		seed = new JTextArea();
		seed.setLineWrap(true);
		seed.setWrapStyleWord(false);
		JScrollPane seedScroll = new JScrollPane(seed);
		GridBagConstraints gbc_seed = new GridBagConstraints();
		gbc_seed.insets = new Insets(0, 0, 5, 5);
		gbc_seed.fill = GridBagConstraints.HORIZONTAL;
		gbc_seed.gridwidth = 2;
		gbc_seed.gridx = 3;
		gbc_seed.gridy = 1;
		getContentPane().add(seedScroll, gbc_seed);
		
		JPanel seedPanel = new JPanel();
		seedPanel.setLayout(new BorderLayout(0, 5));
		GridBagConstraints gbc_seedPanel = new GridBagConstraints();
		gbc_seedPanel.insets = new Insets(0, 0, 5, 5);
		gbc_seedPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_seedPanel.gridx = 3;
		gbc_seedPanel.gridy = 2;
		gbc_seedPanel.gridwidth = 2;
		
		JButton copySeed = new JButton("복사");
		copySeed.setActionCommand("CopySeed");
		copySeed.addActionListener(this);
		/*GridBagConstraints gbc_saveSeed = new GridBagConstraints();
		gbc_saveSeed.insets = new Insets(0, 0, 5, 5);
		gbc_saveSeed.fill = GridBagConstraints.HORIZONTAL;
		gbc_saveSeed.gridx = 3;
		gbc_saveSeed.gridy = 2;
		getContentPane().add(btnSaveSeed, gbc_saveSeed);*/
		seedPanel.add(copySeed, BorderLayout.WEST);
		
		delSeed = new JButton("삭제");
		delSeed.setActionCommand("DeleteSeed");
		delSeed.addActionListener(this);
		/*GridBagConstraints gbc_loadSeed = new GridBagConstraints();
		gbc_loadSeed.insets = new Insets(0, 0, 5, 5);
		gbc_loadSeed.fill = GridBagConstraints.HORIZONTAL;
		gbc_loadSeed.gridx = 4;
		gbc_loadSeed.gridy = 2;
		getContentPane().add(btnLoadSeed, gbc_loadSeed);*/
		seedPanel.add(delSeed, BorderLayout.EAST);
		
		getContentPane().add(seedPanel, gbc_seedPanel);
		
		JSeparator separator = new JSeparator();
		//separator.setForeground(SystemColor.windowBorder);
		//separator.setBorder(new EmptyBorder(10, 10, 0, 0));
		GridBagConstraints gbc_separator = new GridBagConstraints();
		gbc_separator.fill = GridBagConstraints.HORIZONTAL;
		gbc_separator.insets = new Insets(0, 0, 5, 5);
		gbc_separator.gridx = 1;
		gbc_separator.gridy = 3;
		gbc_separator.gridwidth = 4;
		getContentPane().add(separator, gbc_separator);
		
		JLabel lblWorldSize = new JLabel("세계 크기");
		GridBagConstraints gbc_lblWorldSize = new GridBagConstraints();
		gbc_lblWorldSize.insets = new Insets(0, 0, 5, 5);
		gbc_lblWorldSize.gridx = 1;
		gbc_lblWorldSize.gridy = 4;
		getContentPane().add(lblWorldSize, gbc_lblWorldSize);
		
		sizeSlider = new JSlider();
		sizeSlider.setMinorTickSpacing(1);
		sizeSlider.setMajorTickSpacing(1);
		sizeSlider.setPaintTicks(true);
		sizeSlider.setSnapToTicks(true);
		sizeSlider.setValue(3);
		sizeSlider.setMinimum(1);
		sizeSlider.setMaximum(5);
		GridBagConstraints gbc_sizeSlider = new GridBagConstraints();
		gbc_sizeSlider.fill = GridBagConstraints.HORIZONTAL;
		gbc_sizeSlider.insets = new Insets(0, 0, 5, 5);
		gbc_sizeSlider.gridx = 3;
		gbc_sizeSlider.gridy = 4;
		getContentPane().add(sizeSlider, gbc_sizeSlider);
		
		sizeLabel = new JLabel("256");
		GridBagConstraints gbc_sizeLabel = new GridBagConstraints();
		gbc_sizeLabel.insets = new Insets(0, 0, 5, 5);
		gbc_sizeLabel.gridx = 4;
		gbc_sizeLabel.gridy = 4;
		sizeSlider.addChangeListener(this);
		getContentPane().add(sizeLabel, gbc_sizeLabel);
		
		JLabel lblDisplaySize = new JLabel("표시 크기");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 1;
		gbc_lblNewLabel.gridy = 5;
		getContentPane().add(lblDisplaySize, gbc_lblNewLabel);
		
		viewSlider = new JSlider();
		viewSlider.setValue(2);
		viewSlider.setSnapToTicks(true);
		viewSlider.setPaintTicks(true);
		viewSlider.setPaintLabels(true);
		viewSlider.setMinorTickSpacing(1);
		viewSlider.setMinimum(1);
		viewSlider.setMaximum(16);
		viewSlider.setMajorTickSpacing(2);
		viewSlider.addChangeListener(this);

		Hashtable<Integer, JComponent> viewLabels = new Hashtable<>();
		for(int i = 1; i <= 8; i+=2) {
			JLabel label = new JLabel(i + "x");
			viewLabels.put(i * 2, label);
		}
		viewSlider.setLabelTable(viewLabels);
		
		GridBagConstraints gbc_viewSlider = new GridBagConstraints();
		gbc_viewSlider.fill = GridBagConstraints.HORIZONTAL;
		gbc_viewSlider.gridwidth = 2;
		gbc_viewSlider.insets = new Insets(0, 0, 5, 5);
		gbc_viewSlider.gridx = 3;
		gbc_viewSlider.gridy = 5;
		getContentPane().add(viewSlider, gbc_viewSlider);
		
		JLabel lblSpeed = new JLabel("실험 속도");
		GridBagConstraints gbc_lblSpeed = new GridBagConstraints();
		gbc_lblSpeed.insets = new Insets(0, 0, 5, 5);
		gbc_lblSpeed.gridx = 1;
		gbc_lblSpeed.gridy = 6;
		getContentPane().add(lblSpeed, gbc_lblSpeed);
		
		Hashtable<Integer, JComponent> speedLabels = new Hashtable<>();
		for(int i = 1; i <= 10; i+=2) {
			JLabel label = new JLabel(i + "x");
			speedLabels.put(i * 2, label);
		}
		
		speedSlider = new JSlider();
		speedSlider.setPaintLabels(true);
		speedSlider.setValue(2);
		speedSlider.setPaintTicks(true);
		speedSlider.setSnapToTicks(true);
		speedSlider.setMinorTickSpacing(1);
		speedSlider.setMajorTickSpacing(2);
		speedSlider.setMaximum(20);
		speedSlider.setLabelTable(speedLabels);
		speedSlider.addChangeListener(this);
		
		GridBagConstraints gbc_speedSlider = new GridBagConstraints();
		gbc_speedSlider.insets = new Insets(0, 0, 5, 5);
		gbc_speedSlider.gridx = 3;
		gbc_speedSlider.gridy = 6;
		gbc_speedSlider.gridwidth = 2;
		gbc_speedSlider.fill = GridBagConstraints.HORIZONTAL;
		getContentPane().add(speedSlider, gbc_speedSlider);
		
		JLabel lblFps = new JLabel("FPS");
		GridBagConstraints gbc_lblFps = new GridBagConstraints();
		gbc_lblFps.insets = new Insets(0, 0, 5, 5);
		gbc_lblFps.gridx = 1;
		gbc_lblFps.gridy = 7;
		getContentPane().add(lblFps, gbc_lblFps);
		
		fpsSlider = new JSlider();
		fpsSlider.setValue(30);
		fpsSlider.setMinorTickSpacing(5);
		fpsSlider.setMajorTickSpacing(15);
		fpsSlider.setMinimum(0);
		fpsSlider.setMaximum(60);
		fpsSlider.setSnapToTicks(true);
		fpsSlider.setPaintTicks(true);
		fpsSlider.setPaintLabels(true);
		fpsSlider.addChangeListener(this);
		
		Hashtable<Integer, JComponent> fpsLabels = new Hashtable<>();
		fpsLabels.put(0, new JLabel("0"));
		fpsLabels.put(15, new JLabel("15"));
		fpsLabels.put(30, new JLabel("30"));
		fpsLabels.put(45, new JLabel("45"));
		fpsLabels.put(60, new JLabel("60"));
		fpsSlider.setLabelTable(fpsLabels);
		
		GridBagConstraints gbc_fpsSlider = new GridBagConstraints();
		gbc_fpsSlider.fill = GridBagConstraints.HORIZONTAL;
		gbc_fpsSlider.gridwidth = 2;
		gbc_fpsSlider.insets = new Insets(0, 0, 5, 5);
		gbc_fpsSlider.gridx = 3;
		gbc_fpsSlider.gridy = 7;
		getContentPane().add(fpsSlider, gbc_fpsSlider);
		
		
		
		JSeparator separator_1 = new JSeparator();
		GridBagConstraints gbc_separator_1 = new GridBagConstraints();
		gbc_separator_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_separator_1.gridwidth = 4;
		gbc_separator_1.insets = new Insets(0, 0, 5, 5);
		gbc_separator_1.gridx = 1;
		gbc_separator_1.gridy = 8;
		getContentPane().add(separator_1, gbc_separator_1);
		
		JLabel lblCellCount = new JLabel("생명체 갯수");
		GridBagConstraints gbc_lblCellCount = new GridBagConstraints();
		gbc_lblCellCount.insets = new Insets(0, 0, 5, 5);
		gbc_lblCellCount.gridx = 1;
		gbc_lblCellCount.gridy = 9;
		getContentPane().add(lblCellCount, gbc_lblCellCount);
		
		NumberFormat format = NumberFormat.getIntegerInstance();
		format.setMaximumIntegerDigits(4);
		cellCount = new JFormattedTextField(format);
		cellCount.setText("50");
		
		GridBagConstraints gbc_cellCount = new GridBagConstraints();
		gbc_cellCount.gridwidth = 2;
		gbc_cellCount.insets = new Insets(0, 0, 5, 5);
		gbc_cellCount.fill = GridBagConstraints.HORIZONTAL;
		gbc_cellCount.gridx = 3;
		gbc_cellCount.gridy = 9;
		getContentPane().add(cellCount, gbc_cellCount);
		
		JLabel lblMutation = new JLabel("돌연변이");
		GridBagConstraints gbc_lblMutation = new GridBagConstraints();
		gbc_lblMutation.insets = new Insets(0, 0, 5, 5);
		gbc_lblMutation.gridx = 1;
		gbc_lblMutation.gridy = 10;
		getContentPane().add(lblMutation, gbc_lblMutation);
		
		mutationSlider = new JSlider();
		mutationSlider.setValue(10);
		mutationSlider.setSnapToTicks(true);
		mutationSlider.setPaintTicks(true);
		mutationSlider.setMinorTickSpacing(1);
		mutationSlider.setMinimum(0);
		mutationSlider.setMaximum(100);
		mutationSlider.setMajorTickSpacing(10);
		mutationSlider.addChangeListener(this);
		GridBagConstraints gbc_mutationSlider = new GridBagConstraints();
		gbc_mutationSlider.fill = GridBagConstraints.HORIZONTAL;
		gbc_mutationSlider.insets = new Insets(0, 0, 5, 5);
		gbc_mutationSlider.gridx = 3;
		gbc_mutationSlider.gridy = 10;
		getContentPane().add(mutationSlider, gbc_mutationSlider);
		
		mutationLabel = new JLabel("10%");
		GridBagConstraints gbc_mutationLabel = new GridBagConstraints();
		gbc_mutationLabel.insets = new Insets(0, 0, 5, 5);
		gbc_mutationLabel.gridx = 4;
		gbc_mutationLabel.gridy = 10;
		getContentPane().add(mutationLabel, gbc_mutationLabel);
		
		JLabel lblDamping = new JLabel("감속률");
		GridBagConstraints gbc_lblDamping = new GridBagConstraints();
		gbc_lblDamping.insets = new Insets(0, 0, 5, 5);
		gbc_lblDamping.gridx = 1;
		gbc_lblDamping.gridy = 11;
		getContentPane().add(lblDamping, gbc_lblDamping);
		
		dampingSlider = new JSlider();
		dampingSlider.setValue(50);
		dampingSlider.setSnapToTicks(true);
		dampingSlider.setPaintTicks(true);
		//dampingSlider.setPaintLabels(true);
		dampingSlider.setMinorTickSpacing(5);
		dampingSlider.setMinimum(0);
		dampingSlider.setMaximum(100);
		dampingSlider.setMajorTickSpacing(20);
		dampingSlider.addChangeListener(this);
		GridBagConstraints gbc_dampingSlider = new GridBagConstraints();
		gbc_dampingSlider.fill = GridBagConstraints.HORIZONTAL;
		gbc_dampingSlider.gridwidth = 2;
		gbc_dampingSlider.insets = new Insets(0, 0, 5, 5);
		gbc_dampingSlider.gridx = 3;
		gbc_dampingSlider.gridy = 11;
		getContentPane().add(dampingSlider, gbc_dampingSlider);
		
		JSeparator separator_2 = new JSeparator();
		GridBagConstraints gbc_separator_2 = new GridBagConstraints();
		gbc_separator_2.fill = GridBagConstraints.HORIZONTAL;
		gbc_separator_2.gridwidth = 4;
		gbc_separator_2.insets = new Insets(0, 0, 5, 5);
		gbc_separator_2.gridx = 1;
		gbc_separator_2.gridy = 12;
		getContentPane().add(separator_2, gbc_separator_2);
		
		chkRecord = new JCheckBox("시뮬레이션 녹화");
		chkRecord.setActionCommand("Record");
		chkRecord.addActionListener(this);
		GridBagConstraints gbc_chkRecord = new GridBagConstraints();
		gbc_chkRecord.fill = GridBagConstraints.HORIZONTAL;
		gbc_chkRecord.gridwidth = 4;
		gbc_chkRecord.insets = new Insets(0, 0, 5, 5);
		gbc_chkRecord.gridx = 1;
		gbc_chkRecord.gridy = 13;
		getContentPane().add(chkRecord, gbc_chkRecord);
		
		
		JPanel buttonPanel = new JPanel();
		GridBagConstraints gbc_buttonPanel = new GridBagConstraints();
		//gbc_buttonPanel.gridheight = 2;
		gbc_buttonPanel.gridwidth = 4;
		gbc_buttonPanel.insets = new Insets(0, 0, 0, 5);
		gbc_buttonPanel.fill = GridBagConstraints.BOTH;
		gbc_buttonPanel.gridx = 1;
		gbc_buttonPanel.gridy = 14;
		getContentPane().add(buttonPanel, gbc_buttonPanel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0};
		gbl_panel.rowHeights = new int[]{0, 0};
		gbl_panel.columnWeights = new double[]{1.0, 1.0};
		gbl_panel.rowWeights = new double[]{1.0, 1.0};
		buttonPanel.setLayout(gbl_panel);
		
		startBtn = new JButton("Start");
		startBtn.setActionCommand("Start");
		startBtn.addActionListener(this);
		GridBagConstraints gbc_startBtn = new GridBagConstraints();
		gbc_startBtn.fill = GridBagConstraints.BOTH;
		gbc_startBtn.gridheight = 2;
		gbc_startBtn.gridx = 0;
		gbc_startBtn.gridy = 0;
		buttonPanel.add(startBtn, gbc_startBtn);
		
		statBtn = new JToggleButton("Statistics");
		statBtn.setActionCommand("StatisticsShow");
		statBtn.addActionListener(this);
		statBtn.setEnabled(false);
		GridBagConstraints gbc_statBtn = new GridBagConstraints();
		gbc_statBtn.fill = GridBagConstraints.BOTH;
		gbc_statBtn.gridx = 1;
		gbc_statBtn.gridy = 0;
		buttonPanel.add(statBtn, gbc_statBtn);
		
		resetBtn = new JButton("Reset");
		resetBtn.setActionCommand("Reset");
		resetBtn.addActionListener(this);
		GridBagConstraints gbc_resetBtn = new GridBagConstraints();
		gbc_resetBtn.fill = GridBagConstraints.BOTH;
		gbc_resetBtn.gridx = 1;
		gbc_resetBtn.gridy = 1;
		buttonPanel.add(resetBtn, gbc_resetBtn);
		
		//this.setResizable(false);
		//this.setPreferredSize(new Dimension(200, 480));
		this.pack();
		
		simulatorFrame.setSize(512, 512);
	}
	
	/*@Override
	@Transient
	public Dimension getMinimumSize() {
		return new Dimension(300, super.getMinimumSize().height);
	}
	
	@Override
	@Transient
	public Dimension getMaximumSize() {
		return new Dimension(300, super.getMaximumSize().height);
	}
	
	@Override
	@Transient
	public Dimension getPreferredSize() {
		return new Dimension(300, super.getPreferredSize().height);
	}*/
	
	static SimulationSetting setting = new SimulationSetting();
	private Simulation currentSimulation = null;
	
	private SimulationRecorder currentRecorder = null;
	
	private boolean simulationStarted = false;
	
	
	private LifeformStatisticsDialog statDialog = null;
	
	public Simulation getCurrentSimulation() {
		return currentSimulation;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() instanceof AbstractButton) {
			//JButton button = (JButton) e.getSource();
			switch(e.getActionCommand()) {
			case "Start":
				if(!simulationStarted) {
					if(!"".equals(seed.getText()) && Base64.isBase64(seed.getText())) {
						try {
							applySetting(SimulationSetting.decode(seed.getText()), false);
						} catch (IOException e1) {
							e1.printStackTrace();
							
							try {
								cellCount.commitEdit();
							} catch (ParseException e2) {
								e2.printStackTrace();
							}
							setting.setLifeformAmount(((Long)cellCount.getValue()).intValue());
							setting.setSeed(seedToLong());
						}
					} else {
						try {
							cellCount.commitEdit();
						} catch (ParseException e2) {
							e2.printStackTrace();
						}
						setting.setLifeformAmount(((Long)cellCount.getValue()).intValue());
						setting.setSeed(seedToLong());
					}
					seed.setText(setting.encode());
					
					if(currentSimulation == null) {
						currentSimulation = new Simulation(setting);
						
						if(chkRecord.isSelected()) {
							currentRecorder = new SimulationRecorder(currentSimulation);
							currentRecorder.start();
						}
					}
					
					if(statDialog != null) {
						statDialog.setVisible(false);
						statDialog.dispose();
						statBtn.setSelected(false);
						statDialog = null;
					}
					
					currentSimulation.addSimulationListener(this);
					
					simulationStarted = true;
				}
				
				currentSimulation.start();
				
				setStopButton(true);
				statBtn.setEnabled(true);
				//button.setText("Stop");
				//button.setActionCommand("Stop");
				break;
			case "Stop":
				if(currentSimulation != null) {
					//currentSimulation.stop();
					new SimulationStopWorker().execute();
				}
				
				//button.setText("Start");
				//button.setActionCommand("Start");
				break;
			case "Reset":
				if(currentSimulation != null) {
					simulationStarted = false;
					new SimulationFinishWorker().execute();
					/*if(currentRecorder != null) {
						currentRecorder.finish();
						currentRecorder = null;
					}
					currentSimulation.finish();
					
					if(simPaused) {
						finishSimulation();
					}*/
				}
				
				break;
			case "StatisticsShow":
				if(currentSimulation != null) {
					if(statDialog == null) {
						statDialog = new LifeformStatisticsDialog(this, currentSimulation);
					}
					statDialog.setVisible(true);
					statDialog.addWindowListener(new StatWindowListener((JToggleButton)e.getSource()));
					((JToggleButton)e.getSource()).setActionCommand("StatisticsHide");
				}
				break;
			case "StatisticsHide":
				if(currentSimulation != null) {
					if(statDialog != null) {
						statDialog.setVisible(false);
					}
					((JToggleButton)e.getSource()).setActionCommand("StatisticsShow");
				}
				break;
			case "Record":
				setting.setRecord(chkRecord.isSelected());
				break;
			//case "SaveSeed":
			//	saveSeed();
			//	break;
			//case "LoadSeed":
			//	
			//	break;
			case "CopySeed":
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(seed.getText()), null);
				break;
			case "DeleteSeed":
				seed.setText("");
				break;
			}
		}
	}
	
	private static class StatWindowListener implements WindowListener {
		
		private JToggleButton toggle;
		public StatWindowListener(JToggleButton toggle) {
			this.toggle = toggle;
		}
		
		@Override
		public void windowActivated(WindowEvent e) {}

		@Override
		public void windowClosed(WindowEvent e) {}

		@Override
		public void windowClosing(WindowEvent e) {
			toggle.setActionCommand("StatisticsShow");
			toggle.setSelected(false);
		}

		@Override
		public void windowDeactivated(WindowEvent e) {}

		@Override
		public void windowDeiconified(WindowEvent e) {}

		@Override
		public void windowIconified(WindowEvent e) {}

		@Override
		public void windowOpened(WindowEvent e) {}
		
	}
	
	private class SimulationStopWorker extends SwingWorker<Void, Void> {
		
		@Override
		protected Void doInBackground() throws Exception {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					setButtonsEnabled(false);
				}
			});
			
			currentSimulation.stop();
			return null;
		}
		
		@Override
		protected void done() {
			setStopButton(false);
			setButtonsEnabled(true);
		}
	}
	
	private class SimulationFinishWorker extends SwingWorker<Void, Void> {
		
		@Override
		protected Void doInBackground() throws Exception {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					setButtonsEnabled(false);
					setInputsEnabled(false);
				}
			});
			
			if(currentRecorder != null) {
				currentRecorder.finish();
				currentRecorder = null;
			}
			currentSimulation.finish();
			currentSimulation = null;
			return null;
		}
		
		@Override
		protected void done() {
			setStopButton(false);
			setButtonsEnabled(true);
			setInputsEnabled(true);
			statBtn.setEnabled(false);
			simulationStarted = false;
		}
	}
	
	private void applySetting(SimulationSetting setting, boolean setSeed) {
		SettingFrame.setting = setting;
		
		if(setSeed) {
			seed.setText(setting.encode());
		}
		
		int size;
		for(size = 1; setting.getWorldSize() >> (size+6) != 0; size++);
		sizeSlider.setValue(size);
		
		mutationSlider.setValue((int)(setting.getMutationRatio() * 100));
		dampingSlider.setValue((int)(setting.getDamping() * 100));
		
		setting.setDisplaySize(setting.getWorldSize() * viewSlider.getValue() / 2);
		Dimension viewSize = new Dimension(setting.getDisplaySize(), setting.getDisplaySize());
		simulatorFrame.drawCanvas.setSize(viewSize);
		simulatorFrame.revalidate();
		
		setting.setSimulationSpeed(2.0f / speedSlider.getValue());
		setting.setFrameRate(fpsSlider.getValue());
		
		cellCount.setValue(setting.getLifeformAmount());
	}
	
	/*private void saveSeed() {
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new FileExtensionFilter("seed", "시드 저장 형식(.seed)"));
		
		int ret = fc.showSaveDialog(this);
		if(ret == JFileChooser.APPROVE_OPTION) {
			File saveFile;
			if(fc.getFileFilter() instanceof FileExtensionFilter) {
				File file = fc.getSelectedFile();
				if(file.exists()) {
					int res = JOptionPane.showConfirmDialog(this, "이미 파일이 존재합니다. 덮어쓰시겠습니까?", "Life Simulator", JOptionPane.YES_NO_OPTION);
					if(res == JOptionPane.NO_OPTION) {
						saveSeed();
						return;
					} else {
						saveFile = file;
					}
				} else {
					saveFile = new File(fc.getSelectedFile() + ((FileExtensionFilter)fc.getFileFilter()).getExtension());
				}
			} else {
				saveFile = fc.getSelectedFile();
			}
			FileOutputStream fos = null;
			FileChannel ch = null;
			try {
				fos = new FileOutputStream(saveFile);
				byte[] data = setting.asBinary();
				ByteBuffer buf = ByteBuffer.allocateDirect(data.length);
				buf.put(data);
				buf.flip();
				ch = fos.getChannel();
				ch.write(buf);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if(ch != null) {
					try {
						ch.close();
					} catch (IOException e) {}
				}
				if(fos != null) {
					try {
						fos.close();
					} catch (IOException e) {}
				}
			}
			
		}
		
	}*/
	
	private void setStopButton(boolean isStop) {
		String txt = isStop? "Stop" : "Start";
		startBtn.setText(txt);
		startBtn.setActionCommand(txt);
	}
	
	private void setButtonsEnabled(boolean enabled) {
		startBtn.setEnabled(enabled);
		resetBtn.setEnabled(enabled);
	}
	
	private void setInputsEnabled(boolean enabled) {
		//seed.setEnabled(enabled);
		seed.setEditable(enabled);
		sizeSlider.setEnabled(enabled);
		cellCount.setEnabled(enabled);
		mutationSlider.setEnabled(enabled);
		dampingSlider.setEnabled(enabled);
		chkRecord.setEnabled(enabled);
		delSeed.setEnabled(enabled);
	}
	
	private long seedToLong() {
		String txt = seed.getText();
		if(txt.equals("")) {
			return new Random(System.currentTimeMillis()).nextLong();
		} else {
			try {
				return Long.parseLong(txt);
			} catch (NumberFormatException e) {
				// txt == full string
				return txt.hashCode();
			}
		}
	}
	
	public void addLifeform(Gene gene, Vec2 loc) {
		//// TODO Current Simulation?
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		if(e.getSource() instanceof JSlider) {
			JSlider slider = (JSlider)e.getSource();
			if(slider == sizeSlider) {
				setting.setWorldSize(1 << (slider.getValue() + 5));
				sizeLabel.setText(setting.getWorldSize() + "");
				
				setting.setDisplaySize(setting.getWorldSize() * viewSlider.getValue() / 2);
				Dimension size = new Dimension(setting.getDisplaySize(), setting.getDisplaySize());
				simulatorFrame.drawCanvas.setSize(size);
				simulatorFrame.revalidate();
			} else if(slider == viewSlider) {
				setting.setDisplaySize(setting.getWorldSize() * slider.getValue() / 2);
				Dimension size = new Dimension(setting.getDisplaySize(), setting.getDisplaySize());
				simulatorFrame.drawCanvas.setSize(size);
				simulatorFrame.revalidate();
			} else if(slider == speedSlider) {
				if(slider.getValue() == 0) {
					slider.setValue(1);
				}
				setting.setSimulationSpeed(2.0f / slider.getValue()); // 1 / (value / 2) = 2 / value
			} else if(slider == fpsSlider) {
				setting.setFrameRate(slider.getValue());
			} else if(slider == mutationSlider) {
				setting.setMutationRatio(slider.getValue() / 100.0f);
				mutationLabel.setText(slider.getValue() + "%");
			} else if(slider == dampingSlider) {
				setting.setDamping(slider.getValue() / 100.0f);
			}
		}
	}
	
	@Override
	public void dispose() {
		if(currentRecorder != null) {
			currentRecorder.finish();
			currentRecorder = null;
		}
		if(currentSimulation != null) {
			currentSimulation.finish();
			currentSimulation = null;
		}
		
		super.dispose();
	}
	
	@Override
	public void simulationStarted(Simulation sim) {
		setInputsEnabled(false);
		startBtn.setText("Stop");
		startBtn.setActionCommand("Stop");
	}

	@Override
	public void simulationPainterUpdated(Simulation sim, SimulationPainter painter) {}
	
	@Override
	public void simulationStopped(Simulation sim) {
		setStopButton(false);
	}
	
	@Override
	public void simulationFinishing(Simulation simulation) {}
	
	@Override
	public void simulationSettingChanged(Simulation simulation, SimulationSetting setting) {
		seed.setText(setting.encode());
	}
}
