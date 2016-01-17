package net.wkbae.lifesimulator.window;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import net.wkbae.lifesimulator.Gene;
import net.wkbae.lifesimulator.Simulation;
import net.wkbae.lifesimulator.SimulationListener;
import net.wkbae.lifesimulator.SimulationPainter;
import net.wkbae.lifesimulator.SimulationPainter.LifePaintInfo;
import net.wkbae.lifesimulator.SimulationSetting;

import org.jbox2d.common.MathUtils;

public class LifeformStatisticsDialog extends JDialog implements ActionListener, SimulationListener {
	private static final long serialVersionUID = 4382549644263744170L;

	private final static String[] COLUMN_NAMES = {
		"",
		"색",
		"개체수",
		"점유 면적",
		"절대 피도",
		"절대 밀도",
		"절대 빈도",
		"상대 피도",
		"상대 밀도",
		"상대 빈도",
		"우점도"
	};
	
	private final static int COL_SELECT = 0;
	private final static int COL_COLOR = 1;
	private final static int COL_COUNT = 2;
	private final static int COL_EXTENT = 3;
	
	private final static int COL_COVER_ABSOLUTE = 4;
	private final static int COL_DENSITY_ABSOLUTE = 5;
	private final static int COL_FREQUENCY_ABSOLUTE = 6;
	
	private final static int COL_COVER_RELATIVE = 7;
	private final static int COL_DENSITY_RELATIVE = 8;
	private final static int COL_FREQUENCY_RELATIVE = 9;
	
	private final static int COL_DOMINANCE = 10;
	
	private JTable lifeListTable;
	
	private JLabel lblShannon;
	private JLabel lblSimpson;
	
	public LifeformStatisticsDialog(Dialog parent, Simulation sim) {
		super(parent);
		
		JPanel contentPanel = new JPanel();
		
		contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		contentPanel.setLayout(new BorderLayout(5, 5));
		
		lifeListTable = new JTable(new DefaultTableModel(COLUMN_NAMES, 0) {
			private static final long serialVersionUID = 2088958804906695908L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return column == COL_SELECT;
			}
		}) {
			private static final long serialVersionUID = -4335279532349899891L;

			@Override
			public Class<?> getColumnClass(int column) {
				switch(column) {
				case COL_SELECT:
					return Boolean.class;
					
				case COL_COLOR:
					return String.class;
					
				case COL_COUNT:
					return Integer.class;
					
				case COL_EXTENT:
					return Float.class;
					
				case COL_COVER_ABSOLUTE:
				case COL_DENSITY_ABSOLUTE:
				case COL_FREQUENCY_ABSOLUTE:
				case COL_COVER_RELATIVE:
				case COL_DENSITY_RELATIVE:
				case COL_FREQUENCY_RELATIVE:
				case COL_DOMINANCE:
					return Double.class;
					
				default:
					return String.class;
				}
			}
		};
		lifeListTable.setDefaultRenderer(String.class, new ColorCellRenderer());
		contentPanel.add(new JScrollPane(lifeListTable), BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel();
		contentPanel.add(buttonPanel, BorderLayout.NORTH);
		buttonPanel.setLayout(new BorderLayout(0, 0));
		
		JButton btnAddSpecies = new JButton("종 추가(A)");
		btnAddSpecies.setMnemonic('A');
		btnAddSpecies.setActionCommand("Add");
		btnAddSpecies.addActionListener(this);
		buttonPanel.add(btnAddSpecies, BorderLayout.WEST);
		
		JButton btnNewButton = new JButton("종 삭제(R)");
		btnNewButton.setMnemonic('R');
		btnNewButton.setActionCommand("Remove");
		btnNewButton.addActionListener(this);
		buttonPanel.add(btnNewButton, BorderLayout.EAST);
		
		JPanel resultPanel = new JPanel();
		contentPanel.add(resultPanel, BorderLayout.SOUTH);
		GridBagLayout gbl_resultPanel = new GridBagLayout();
		gbl_resultPanel.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_resultPanel.rowHeights = new int[]{0, 0, 0};
		gbl_resultPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_resultPanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		resultPanel.setLayout(gbl_resultPanel);
		
		JLabel shan = new JLabel("Shannon 지수:");
		GridBagConstraints gbc_shan = new GridBagConstraints();
		gbc_shan.insets = new Insets(0, 0, 5, 5);
		gbc_shan.gridx = 0;
		gbc_shan.gridy = 0;
		resultPanel.add(shan, gbc_shan);
		
		lblShannon = new JLabel("");
		GridBagConstraints gbc_lblShannon = new GridBagConstraints();
		gbc_lblShannon.insets = new Insets(0, 0, 5, 5);
		gbc_lblShannon.gridx = 2;
		gbc_lblShannon.gridy = 0;
		resultPanel.add(lblShannon, gbc_lblShannon);
		
		JLabel simp = new JLabel("Simpson 지수:");
		GridBagConstraints gbc_simp = new GridBagConstraints();
		gbc_simp.insets = new Insets(0, 0, 0, 5);
		gbc_simp.gridx = 0;
		gbc_simp.gridy = 1;
		resultPanel.add(simp, gbc_simp);
		
		lblSimpson = new JLabel("");
		GridBagConstraints gbc_lblSimpson = new GridBagConstraints();
		gbc_lblSimpson.gridx = 2;
		gbc_lblSimpson.gridy = 1;
		resultPanel.add(lblSimpson, gbc_lblSimpson);
		
		getContentPane().add(contentPanel);
		
		sim.addSimulationListener(this);
		this.setPreferredSize(new Dimension(1000, 300));
		this.pack();
		//this.setVisible(true);
	}
	
	public void calculate(SimulationPainter painter) {
		TableModel model = lifeListTable.getModel();
		int count = model.getRowCount();
		
		int[] rowColor = new int[count];
		Map<Integer, Integer> countMap = new TreeMap<>();
		Map<Integer, Float> extentMap = new TreeMap<>();
		for(int i = 0; i < count; i++) {
			String colorStr = (String) model.getValueAt(i, COL_COLOR);
			int color = Integer.parseInt(colorStr, 16);
			rowColor[i] = color;
			countMap.put(color, 0);
			extentMap.put(color, 0.0f);
		}
		
		
		Set<Integer> colorSet = new TreeSet<>(countMap.keySet());
		for(LifePaintInfo info : painter.getLifeInfos()) {
			//int color = ((info.color.getRed()&0xF) << 8) | ((info.color.getGreen()&0xF) << 4) | (info.color.getBlue()&0xF);
			int color = info.color;
			
			for(Integer color2 : colorSet) {
				if(Gene.isCloseTo(color, color2)) {
					countMap.put(color2, countMap.get(color2) + 1);
					extentMap.put(color2, extentMap.get(color2) + (info.size * info.size / 4) * MathUtils.PI);
				}
			}
		}
		
		int worldSize = painter.getSimulation().getSetting().getWorldSize();
		int worldExtent = worldSize * worldSize;
		
		int totalCount = 0;
		float totalCover = 0;
		float totalDensity = 0;
		float totalFrequency = 0;
		
		int[] counts = new int[count];
		double[] covers = new double[count];
		double[] densities = new double[count];
		double[] frequencies = new double[count];
		
		for(int i = 0; i < count; i++) {
			int lCount = countMap.get(rowColor[i]);
			float extent = extentMap.get(rowColor[i]);
			double cover = extent*100.0 / worldExtent;
			double density = lCount*1.0 / worldExtent;
			double frequency = lCount > 0? 1 : 0;
			totalCount += lCount;
			totalCover += cover;
			totalDensity += density;
			totalFrequency += frequency;
			
			counts[i] = lCount;
			covers[i] = cover;
			densities[i] = density;
			frequencies[i] = frequency;
			
			model.setValueAt(lCount, i, COL_COUNT);
			model.setValueAt(extent, i, COL_EXTENT);
			model.setValueAt(cover, i, COL_COVER_ABSOLUTE);
			model.setValueAt(density, i, COL_DENSITY_ABSOLUTE);
			model.setValueAt(frequency, i, COL_FREQUENCY_ABSOLUTE);
		}
		
		double shannon = 0;
		double simpson = 0;
		
		for(int i = 0; i < count; i++) {
			double relCover = covers[i] / totalCover;
			double relDensity = densities[i] / totalDensity;
			double relFrequency = frequencies[i] / totalFrequency;
			
			model.setValueAt(relCover * 100, i, COL_COVER_RELATIVE);
			model.setValueAt(relDensity * 100, i, COL_DENSITY_RELATIVE);
			model.setValueAt(relFrequency * 100, i, COL_FREQUENCY_RELATIVE);
			model.setValueAt(relCover + relDensity + relFrequency, i, COL_DOMINANCE);
			
			if(counts[i] != 0) {
				double div = counts[i]*1.0 / totalCount;
				shannon += div * Math.log(div);
				simpson += div * div;
			}
		}
		shannon = -shannon;
		
		lblShannon.setText(shannon + "");
		lblSimpson.setText(simpson + "");
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		switch(e.getActionCommand()) {
		case "Add":
			ColorPickerDialog d = new ColorPickerDialog();
			d.setVisible(true);
			int color = d.getColor();
			if(color == -1) break;
			int r = (color >> 8) & 0xF;
			int g = (color >> 4) & 0xF;
			int b = color & 0xF;
			((DefaultTableModel)lifeListTable.getModel()).addRow(new Object[] {
					new Boolean(false), Integer.toHexString(r) + Integer.toHexString(g) + Integer.toHexString(b), 0,0,0,0,0,0,0,0,0
			});
			break;
		case "Remove":
			DefaultTableModel model = (DefaultTableModel) lifeListTable.getModel();
			for(int i = 0; i < model.getRowCount(); i++) {
				if(Boolean.TRUE.equals(model.getValueAt(i, COL_SELECT))) {
					model.removeRow(i);
					i--;
				}
			}
			break;
		}
	}
	
	@Override
	public void simulationStarted(Simulation simulation) {}
	 
	@Override
	public void simulationPainterUpdated(Simulation simulation, SimulationPainter painter) {
		calculate(painter);
	}
	
	@Override
	public void simulationStopped(Simulation simulation) {}
	
	@Override
	public void simulationFinishing(Simulation simulation) {
		this.setVisible(false);
		this.dispose();
	}
	
	@Override
	public void simulationSettingChanged(Simulation simulation, SimulationSetting setting) {}
	
	private static class ColorCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = -2049321694299400485L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if(column == COL_COLOR) {
				int color = Integer.parseInt((String) value, 16);
				int r = (color >> 8) & 0xF;
				int g = (color >> 4) & 0xF;
				int b = color & 0xF;
				c.setBackground(new Color(r << 4 | r, g << 4 | g, b << 4 | b));
			}
			return c;
			
		}
	}
	
	private static class ColorPickerDialog extends JDialog implements ActionListener, ChangeListener {
		private static final long serialVersionUID = -2473912004899736051L;

		private static final int APPEARENCE_SIZE = 100;
		
		private JPanel lifeDisplayPanel;
		
		private JSlider redSlider;
		private JSlider greenSlider;
		private JSlider blueSlider;
		
		public ColorPickerDialog() {
			this.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
			
			this.setLayout(new BorderLayout(10, 10));
			
			lifeDisplayPanel = new JPanel() {
				private static final long serialVersionUID = 4631559487216195773L;
				
				@Override
				public void paint(Graphics g) {
					super.paint(g);
					g.setColor(Color.WHITE);
					g.fillRect(0, 0, APPEARENCE_SIZE, APPEARENCE_SIZE);
					
					((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					float rad = APPEARENCE_SIZE / 2 - 10;
					int re = (color >> 8) & 0xF;
					int gr = (color >> 4) & 0xF;
					int bl = color & 0xF;
					g.setColor(new Color(re << 4 | re, gr << 4 | gr, bl << 4 | bl));//Color.decode(Integer.toHexString(life.getColor())));
					int s = MathUtils.round(rad*2);
					int pos = MathUtils.round(APPEARENCE_SIZE / 2 - rad);
					g.fillOval(pos, pos, s, s);
					g.setColor(Color.BLACK);
					g.drawOval(pos, pos, s, s);
					
				}
			};
			lifeDisplayPanel.setPreferredSize(new Dimension(APPEARENCE_SIZE, APPEARENCE_SIZE));
			getContentPane().add(lifeDisplayPanel, BorderLayout.NORTH);
			
			
			JPanel colorPanel = new JPanel();
			colorPanel.setBorder(new TitledBorder(null, "색깔", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			getContentPane().add(colorPanel, BorderLayout.CENTER);
			
			GridBagLayout gbl_colorPanel = new GridBagLayout();
			gbl_colorPanel.columnWidths = new int[]{5, 0, 0, 5};
			gbl_colorPanel.rowHeights = new int[]{5, 0, 0, 0, 5};
			gbl_colorPanel.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0};
			gbl_colorPanel.rowWeights = new double[]{0.0, 1.0, 1.0, 1.0, 0.0};
			colorPanel.setLayout(gbl_colorPanel);
			
			JLabel lblRed = new JLabel("빨강");
			GridBagConstraints gbc_lblRed = new GridBagConstraints();
			gbc_lblRed.insets = new Insets(0, 0, 5, 5);
			gbc_lblRed.gridx = 1;
			gbc_lblRed.gridy = 1;
			colorPanel.add(lblRed, gbc_lblRed);
			
			redSlider = new JSlider();
			redSlider.setMinimum(0);
			redSlider.setMaximum(0xF);
			redSlider.setValue(0);
			redSlider.setValue((redSlider.getMaximum() - redSlider.getMinimum()) / 2);
			redSlider.addChangeListener(this);
			redSlider.setPaintTicks(true);
			redSlider.setMinorTickSpacing(1);
			GridBagConstraints gbc_redSlider = new GridBagConstraints();
			gbc_redSlider.fill = GridBagConstraints.HORIZONTAL;
			gbc_redSlider.insets = new Insets(0, 0, 5, 0);
			gbc_redSlider.gridx = 2;
			gbc_redSlider.gridy = 1;
			colorPanel.add(redSlider, gbc_redSlider);
			
			JLabel lblGreen = new JLabel("초록");
			GridBagConstraints gbc_lblGreen = new GridBagConstraints();
			gbc_lblGreen.insets = new Insets(0, 0, 5, 5);
			gbc_lblGreen.gridx = 1;
			gbc_lblGreen.gridy = 2;
			colorPanel.add(lblGreen, gbc_lblGreen);
			
			greenSlider = new JSlider();
			greenSlider.setMinimum(0);
			greenSlider.setMaximum(0xF);
			greenSlider.setValue(0);
			greenSlider.setValue((greenSlider.getMaximum() - greenSlider.getMinimum()) / 2);
			greenSlider.addChangeListener(this);
			greenSlider.setPaintTicks(true);
			greenSlider.setMinorTickSpacing(1);
			GridBagConstraints gbc_greenSlider = new GridBagConstraints();
			gbc_greenSlider.fill = GridBagConstraints.HORIZONTAL;
			gbc_greenSlider.insets = new Insets(0, 0, 5, 0);
			gbc_greenSlider.gridx = 2;
			gbc_greenSlider.gridy = 2;
			colorPanel.add(greenSlider, gbc_greenSlider);
			
			JLabel lblBlue = new JLabel("파랑");
			GridBagConstraints gbc_lblBlue = new GridBagConstraints();
			gbc_lblBlue.insets = new Insets(0, 0, 0, 5);
			gbc_lblBlue.gridx = 1;
			gbc_lblBlue.gridy = 3;
			colorPanel.add(lblBlue, gbc_lblBlue);
			
			blueSlider = new JSlider();
			blueSlider.setMinimum(0);
			blueSlider.setMaximum(0xF);
			blueSlider.setValue(0);
			blueSlider.setValue((blueSlider.getMaximum() - blueSlider.getMinimum()) / 2);
			blueSlider.addChangeListener(this);
			blueSlider.setPaintTicks(true);
			blueSlider.setMinorTickSpacing(1);
			GridBagConstraints gbc_blueSlider = new GridBagConstraints();
			gbc_blueSlider.fill = GridBagConstraints.HORIZONTAL;
			gbc_blueSlider.gridx = 2;
			gbc_blueSlider.gridy = 3;
			colorPanel.add(blueSlider, gbc_blueSlider);
			
			JPanel buttonPanel = new JPanel();

			JButton confirmBtn = new JButton("확인");
			confirmBtn.setActionCommand("Confirm");
			confirmBtn.addActionListener(this);
			buttonPanel.add(confirmBtn, BorderLayout.WEST);

			JButton cancelBtn = new JButton("취소");
			cancelBtn.setActionCommand("Cancel");
			cancelBtn.addActionListener(this);
			buttonPanel.add(cancelBtn, BorderLayout.EAST);
			
			getContentPane().add(buttonPanel, BorderLayout.SOUTH);
			
			this.pack();
		}
		
		private int resColor = -1;
		public int getColor() {
			return resColor;
		}
		
		private int color = 0;
		@Override
		public void stateChanged(ChangeEvent e) {
			int r = redSlider.getValue() & 0xF;
			int g = greenSlider.getValue() & 0xF;
			int b = blueSlider.getValue() & 0xF;
			color = (r << 8) | (g << 4) | (b);
			lifeDisplayPanel.repaint();
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			switch(e.getActionCommand()) {
			case "Confirm":
				resColor = color;
				break;
			case "Cancel":
				resColor = -1;
				break;
			}
			this.setVisible(false);
			this.dispose();
		}
	}
}
