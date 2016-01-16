package net.wkbae.lifesimulator.window;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JScrollPane;

import net.wkbae.lifesimulator.Gene.Factor;
import net.wkbae.lifesimulator.Lifeform.LifeformEnergyListener;
import net.wkbae.lifesimulator.Lifeform;

import java.awt.GridBagLayout;
import javax.swing.JPanel;

import org.jbox2d.common.MathUtils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.swing.JLabel;

public class LifeformInfoDialog extends JDialog implements LifeformEnergyListener {
	private static final long serialVersionUID = -3231470103894470945L;
	
	private final static int APPEARENCE_SIZE = 100;
	
	private InformationPanel currentValues;
	
	public LifeformInfoDialog(SettingFrame setting, final Lifeform life) {
		super(setting);
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{10, 0, 0, 10};
		gridBagLayout.rowHeights = new int[]{10, 0, 10, 0, 10, 0, 10, 0, 10};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		getContentPane().setLayout(gridBagLayout);
		
		JPanel appearencePanel = new JPanel() {
			private static final long serialVersionUID = -2191286485313357457L;

			@Override
			public void paint(Graphics g) {
				super.paint(g);
				g.setColor(Color.WHITE);
				g.fillRect(0, 0, APPEARENCE_SIZE, APPEARENCE_SIZE);
				((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				float rad = life.getSize() * 10;
				int color = life.getColor();
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
		appearencePanel.setPreferredSize(new Dimension(APPEARENCE_SIZE, APPEARENCE_SIZE));
		GridBagConstraints gbc_appearencePanel = new GridBagConstraints();
		gbc_appearencePanel.insets = new Insets(0, 0, 5, 0);
		gbc_appearencePanel.fill = GridBagConstraints.BOTH;
		gbc_appearencePanel.gridx = 1;
		gbc_appearencePanel.gridy = 1;
		getContentPane().add(appearencePanel, gbc_appearencePanel);
		
		LinkedHashMap<String, String> geneValues = new LinkedHashMap<>();
		/*SIZE(5), BOUNCY(5),
		SPEED(4), METABOLISM(4),
		ENERGY(4), POWER(4),
		SIGHT(3), MOVE_FREQUENCY(3),
		LIFESPAN(3), BREED_SPEED(3),
		COLOR(12);
		*/
		geneValues.put("크기", life.getSize() + "");
		geneValues.put("탄성", life.gene.getFactor(Factor.BOUNCY)/50.0 + "");
		geneValues.put("속도", life.getSpeed() + "");
		geneValues.put("대사량", life.gene.getFactor(Factor.METABOLISM) + "");
		geneValues.put("최대 에너지", life.gene.getFactor(Factor.ENERGY)*50 + "");
		geneValues.put("힘", life.gene.getFactor(Factor.POWER) + "");
		geneValues.put("시야", life.getSight() + "");
		geneValues.put("이동 빈도", life.gene.getFactor(Factor.MOVE_FREQUENCY) + "");
		geneValues.put("수명", life.gene.getFactor(Factor.LIFESPAN) + "");
		geneValues.put("번식 속도", life.gene.getFactor(Factor.BREED_SPEED) + "");
		geneValues.put("색깔", Integer.toHexString(life.gene.getFactor(Factor.COLOR)).toUpperCase(Locale.ENGLISH));
		
		JPanel genePanel = new InformationPanel("유전자정보", geneValues);
		GridBagConstraints gbc_genePanel = new GridBagConstraints();
		gbc_genePanel.fill = GridBagConstraints.BOTH;
		gbc_genePanel.gridx = 1;
		gbc_genePanel.gridy = 3;
		getContentPane().add(genePanel, gbc_genePanel);
		
		LinkedHashMap<String, String> current = new LinkedHashMap<>();
		current.put("에너지", MathUtils.round(life.getEnergy() * 100) / 100.0 +"");
		
		currentValues = new InformationPanel("현재 정보", current);
		GridBagConstraints gbc_currentPanel = new GridBagConstraints();
		gbc_currentPanel.fill = GridBagConstraints.BOTH;
		gbc_currentPanel.gridx = 1;
		gbc_currentPanel.gridy = 5;
		getContentPane().add(currentValues, gbc_currentPanel);
		
		
		TreeMap<Integer, Float> prefValues = new TreeMap<>(life.getAllPreferences());
		
		JComponent prefPanel = newPreferencePanel("선호도", prefValues);
		GridBagConstraints gbc_prefPanel = new GridBagConstraints();
		gbc_prefPanel.fill = GridBagConstraints.BOTH;
		gbc_prefPanel.gridx = 1;
		gbc_prefPanel.gridy = 7;
		getContentPane().add(prefPanel, gbc_prefPanel);
		
		this.pack();
		
		life.addEnergyListener(this);
		
		this.setVisible(true);
		//SIZE(5), BOUNCY(5), SPEED(4), MASS(4), ENERGY(4), POWER(4), SIGHT(3), COLOR(12)
	}
	
	private JComponent newPreferencePanel(String title, Map<Integer, Float> values) {
		JPanel innerPanel = new JPanel();
		//innerPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		innerPanel.setMinimumSize(new Dimension(20, 100));
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0};
		gbl_panel.rowHeights = new int[values.size() * 2 + 1];
		gbl_panel.columnWeights = new double[]{0.0, 0.0};
		gbl_panel.rowWeights = new double[values.size() * 2 + 1];
		
		for(int i = 1; i < gbl_panel.rowHeights.length; i += 2) {
			gbl_panel.rowHeights[i] = 5;
		}
		
		innerPanel.setLayout(gbl_panel);
		
		int index = 0;
		for(Entry<Integer, Float> entry : values.entrySet()) {

			//JLabel name = new JLabel(entry.getKey());
			ColorLabel label = new ColorLabel(entry.getKey());
			GridBagConstraints gbc_label = new GridBagConstraints();
			gbc_label.gridx = 0;
			gbc_label.gridy = index;
			innerPanel.add(label, gbc_label);

			JLabel value = new JLabel(MathUtils.round(entry.getValue() * 100) / 100.0 + "");
			GridBagConstraints gbc_value = new GridBagConstraints();
			gbc_value.gridx = 1;
			gbc_value.gridy = index;
			innerPanel.add(value, gbc_value);
			
			index += 2;
		}
		
		JScrollPane scroll = new JScrollPane(innerPanel);
		scroll.setBorder(null);
		scroll.setPreferredSize(new Dimension(100, 150));
		scroll.getVerticalScrollBar().setUnitIncrement(10);
		
		//JPanel outerPanel = new JPanel();
		//outerPanel.setLayout(new BorderLayout());
		//outerPanel.add(scroll, BorderLayout.CENTER);
		scroll.setBorder(BorderFactory.createTitledBorder(title));
		//outerPanel.setMinimumSize(new Dimension(20, 100));
		//outerPanel.setMaximumSize(new Dimension(100, 300));
		return scroll;
		//return outerPanel;
	}
	
	@Override
	public void onEnergyChanged(Lifeform life, float energy) {
		currentValues.getLabel("에너지").setText(MathUtils.round(energy * 100) / 100.0 +"");
	}
	
	private static class InformationPanel extends JPanel {
		private static final long serialVersionUID = -2703210545005924639L;
		
		private HashMap<String, JLabel> labels;
		
		public InformationPanel(String title, Map<String, String> data) {
			labels = new HashMap<>(data.size());
			
			setBorder(BorderFactory.createTitledBorder(title));
			GridBagLayout gbl_panel = new GridBagLayout();
			gbl_panel.columnWidths = new int[]{0, 0};
			gbl_panel.rowHeights = new int[data.size() * 2 + 1];
			gbl_panel.columnWeights = new double[]{0.0, 0.0};
			gbl_panel.rowWeights = new double[data.size() * 2 + 1];
			
			for(int i = 1; i < gbl_panel.rowHeights.length; i += 2) {
				gbl_panel.rowHeights[i] = 5;
			}
			
			setLayout(gbl_panel);
			
			int index = 0;
			for(Entry<String, String> entry : data.entrySet()) {
				JLabel name = new JLabel(entry.getKey());
				GridBagConstraints gbc_name = new GridBagConstraints();
				gbc_name.gridx = 0;
				gbc_name.gridy = index;
				add(name, gbc_name);

				JLabel value = new JLabel(entry.getValue());
				GridBagConstraints gbc_value = new GridBagConstraints();
				gbc_value.gridx = 1;
				gbc_value.gridy = index;
				add(value, gbc_value);
				
				labels.put(entry.getKey(), value);
				
				index += 2;
			}
		}
		
		public JLabel getLabel(String name) {
			return labels.get(name);
		}
	}

	private static class ColorLabel extends JPanel {
		private static final long serialVersionUID = 5565438103078920967L;
		
		private final Color color;
		public ColorLabel(int color) {
			int re = (color >> 8) & 0xF;
			int gr = (color >> 4) & 0xF;
			int bl = color & 0xF;
			this.color = new Color(re << 4 | re, gr << 4 | gr, bl << 4 | bl);
			
			setPreferredSize(new Dimension(10, 10));
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(color);
			g.fillOval(0, 0, 9, 9);
			g.setColor(Color.BLACK);
			g.drawOval(0, 0, 9, 9);
		}
	}
}
