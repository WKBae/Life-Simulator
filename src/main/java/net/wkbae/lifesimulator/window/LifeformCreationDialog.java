package net.wkbae.lifesimulator.window;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import net.wkbae.lifesimulator.Gene;
import net.wkbae.lifesimulator.Simulation;

public class LifeformCreationDialog extends JDialog implements ChangeListener, ActionListener {
	private static final long serialVersionUID = 8483313828133354397L;

	private static final int APPEARENCE_SIZE = 100;
	
	private JPanel lifeDisplayPanel;
	private JSlider sizeSlider;
	private JSlider speedSlider;
	private JSlider energySlider;
	private JSlider sightSlider;
	private JSlider lifespanSlider;
	
	private JSlider redSlider;
	private JSlider greenSlider;
	private JSlider blueSlider;
	
	private JSpinner cntSpinner;
	
	private Simulation sim;
	private float x, y;
	
	public LifeformCreationDialog(SettingFrame setting, Simulation simulation, float x, float y) {
		super(setting);
		
		this.sim = simulation;
		this.x = x;
		this.y = y;
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{10, 0, 0, 0, 10};
		gridBagLayout.rowHeights = new int[]{10, 0, 10, 0, 0, 0, 0, 0, 0, 0, 10};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, 0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0};
		getContentPane().setLayout(gridBagLayout);
		
		lifeDisplayPanel = new JPanel() {
			private static final long serialVersionUID = 4631559487216195773L;

			@Override
			public void paint(Graphics g) {
				super.paint(g);
				paintLife(g);
			}
		};
		lifeDisplayPanel.setPreferredSize(new Dimension(APPEARENCE_SIZE, APPEARENCE_SIZE));
		GridBagConstraints gbc_lifeDisplayPanel = new GridBagConstraints();
		gbc_lifeDisplayPanel.gridwidth = 3;
		gbc_lifeDisplayPanel.insets = new Insets(0, 0, 5, 5);
		gbc_lifeDisplayPanel.fill = GridBagConstraints.VERTICAL;
		gbc_lifeDisplayPanel.gridx = 1;
		gbc_lifeDisplayPanel.gridy = 1;
		getContentPane().add(lifeDisplayPanel, gbc_lifeDisplayPanel);
		
		JLabel lblSize = new JLabel("크기");
		GridBagConstraints gbc_lblSize = new GridBagConstraints();
		gbc_lblSize.insets = new Insets(0, 0, 5, 5);
		gbc_lblSize.gridx = 1;
		gbc_lblSize.gridy = 3;
		getContentPane().add(lblSize, gbc_lblSize);
		
		sizeSlider = new JSlider();
		sizeSlider.setMinimum(1);
		sizeSlider.setMaximum(Gene.Factor.SIZE.getOppositeValue(1));
		sizeSlider.setValue((sizeSlider.getMaximum() - sizeSlider.getMinimum()) / 2);
		sizeSlider.setInverted(true);
		sizeSlider.addChangeListener(this);
		sizeSlider.setPaintTicks(true);
		sizeSlider.setMinorTickSpacing(1);
		GridBagConstraints gbc_sizeSlider = new GridBagConstraints();
		gbc_sizeSlider.fill = GridBagConstraints.HORIZONTAL;
		gbc_sizeSlider.insets = new Insets(0, 0, 5, 5);
		gbc_sizeSlider.gridx = 2;
		gbc_sizeSlider.gridy = 3;
		getContentPane().add(sizeSlider, gbc_sizeSlider);
		
		JLabel lblBounciness = new JLabel("탄성");
		GridBagConstraints gbc_lblBounciness = new GridBagConstraints();
		gbc_lblBounciness.insets = new Insets(0, 0, 5, 5);
		gbc_lblBounciness.gridx = 3;
		gbc_lblBounciness.gridy = 3;
		getContentPane().add(lblBounciness, gbc_lblBounciness);
		
		JLabel lblSpeed = new JLabel("<html><div style=\"text-align:center\">이동 속도<br>번식 속도</div></html>");
		lblSpeed.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_lblSpeed = new GridBagConstraints();
		gbc_lblSpeed.insets = new Insets(0, 0, 5, 5);
		gbc_lblSpeed.gridx = 1;
		gbc_lblSpeed.gridy = 4;
		getContentPane().add(lblSpeed, gbc_lblSpeed);
		
		speedSlider = new JSlider();
		speedSlider.setMinimum(1);
		speedSlider.setMaximum(Gene.Factor.SPEED.getOppositeValue(1));
		speedSlider.setValue((speedSlider.getMaximum() - speedSlider.getMinimum()) / 2);
		speedSlider.setInverted(true);
		speedSlider.addChangeListener(this);
		speedSlider.setPaintTicks(true);
		speedSlider.setMinorTickSpacing(1);
		GridBagConstraints gbc_speedSlider = new GridBagConstraints();
		gbc_speedSlider.fill = GridBagConstraints.HORIZONTAL;
		gbc_speedSlider.insets = new Insets(0, 0, 5, 5);
		gbc_speedSlider.gridx = 2;
		gbc_speedSlider.gridy = 4;
		getContentPane().add(speedSlider, gbc_speedSlider);
		
		JLabel lblMetabolism = new JLabel("기초대사량");
		GridBagConstraints gbc_lblMetabolism = new GridBagConstraints();
		gbc_lblMetabolism.insets = new Insets(0, 0, 5, 5);
		gbc_lblMetabolism.gridx = 3;
		gbc_lblMetabolism.gridy = 4;
		getContentPane().add(lblMetabolism, gbc_lblMetabolism);
		
		JLabel lblEnergy = new JLabel("총 에너지");
		GridBagConstraints gbc_lblEnergy = new GridBagConstraints();
		gbc_lblEnergy.insets = new Insets(0, 0, 5, 5);
		gbc_lblEnergy.gridx = 1;
		gbc_lblEnergy.gridy = 5;
		getContentPane().add(lblEnergy, gbc_lblEnergy);
		
		energySlider = new JSlider();
		energySlider.setMinimum(1);
		energySlider.setMaximum(Gene.Factor.ENERGY.getOppositeValue(1));
		energySlider.setValue((energySlider.getMaximum() - energySlider.getMinimum()) / 2);
		energySlider.setInverted(true);
		energySlider.addChangeListener(this);
		energySlider.setPaintTicks(true);
		energySlider.setMinorTickSpacing(1);
		GridBagConstraints gbc_energySlider = new GridBagConstraints();
		gbc_energySlider.fill = GridBagConstraints.HORIZONTAL;
		gbc_energySlider.insets = new Insets(0, 0, 5, 5);
		gbc_energySlider.gridx = 2;
		gbc_energySlider.gridy = 5;
		getContentPane().add(energySlider, gbc_energySlider);
		
		JLabel lblPower = new JLabel("세기");
		GridBagConstraints gbc_lblPower = new GridBagConstraints();
		gbc_lblPower.insets = new Insets(0, 0, 5, 5);
		gbc_lblPower.gridx = 3;
		gbc_lblPower.gridy = 5;
		getContentPane().add(lblPower, gbc_lblPower);
		
		JLabel lblSight = new JLabel("시야");
		GridBagConstraints gbc_lblSight = new GridBagConstraints();
		gbc_lblSight.insets = new Insets(0, 0, 5, 5);
		gbc_lblSight.gridx = 1;
		gbc_lblSight.gridy = 6;
		getContentPane().add(lblSight, gbc_lblSight);
		
		sightSlider = new JSlider();
		sightSlider.setMinimum(1);
		sightSlider.setMaximum(Gene.Factor.SIGHT.getOppositeValue(1));
		sightSlider.setValue((sightSlider.getMaximum() - sightSlider.getMinimum()) / 2);
		sightSlider.setInverted(true);
		sightSlider.addChangeListener(this);
		sightSlider.setPaintTicks(true);
		sightSlider.setMinorTickSpacing(1);
		GridBagConstraints gbc_sightSlider = new GridBagConstraints();
		gbc_sightSlider.fill = GridBagConstraints.HORIZONTAL;
		gbc_sightSlider.insets = new Insets(0, 0, 5, 5);
		gbc_sightSlider.gridx = 2;
		gbc_sightSlider.gridy = 6;
		getContentPane().add(sightSlider, gbc_sightSlider);
		
		JLabel lblMoveFrequency = new JLabel("이동 빈도");
		GridBagConstraints gbc_lblMoveFrequency = new GridBagConstraints();
		gbc_lblMoveFrequency.insets = new Insets(0, 0, 5, 5);
		gbc_lblMoveFrequency.gridx = 3;
		gbc_lblMoveFrequency.gridy = 6;
		getContentPane().add(lblMoveFrequency, gbc_lblMoveFrequency);
		
		JLabel lblLifespan = new JLabel("수명");
		GridBagConstraints gbc_lblLifespan = new GridBagConstraints();
		gbc_lblLifespan.insets = new Insets(0, 0, 5, 5);
		gbc_lblLifespan.gridx = 1;
		gbc_lblLifespan.gridy = 7;
		getContentPane().add(lblLifespan, gbc_lblLifespan);
		
		lifespanSlider = new JSlider();
		lifespanSlider.setMinimum(1);
		lifespanSlider.setMaximum(Gene.Factor.LIFESPAN.getOppositeValue(1));
		lifespanSlider.setValue((lifespanSlider.getMaximum() - lifespanSlider.getMinimum()) / 2);
		lifespanSlider.setInverted(true);
		lifespanSlider.addChangeListener(this);
		lifespanSlider.setPaintTicks(true);
		lifespanSlider.setMinorTickSpacing(1);
		GridBagConstraints gbc_lifespanSlider = new GridBagConstraints();
		gbc_lifespanSlider.fill = GridBagConstraints.HORIZONTAL;
		gbc_lifespanSlider.insets = new Insets(0, 0, 5, 5);
		gbc_lifespanSlider.gridx = 2;
		gbc_lifespanSlider.gridy = 7;
		getContentPane().add(lifespanSlider, gbc_lifespanSlider);
		
		JLabel lblBreedingFrequency = new JLabel("번식 빈도");
		GridBagConstraints gbc_lblBreedingFrequency = new GridBagConstraints();
		gbc_lblBreedingFrequency.insets = new Insets(0, 0, 5, 5);
		gbc_lblBreedingFrequency.gridx = 3;
		gbc_lblBreedingFrequency.gridy = 7;
		getContentPane().add(lblBreedingFrequency, gbc_lblBreedingFrequency);
		
		JPanel colorPanel = new JPanel();
		colorPanel.setBorder(new TitledBorder(null, "색깔", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_colorPanel = new GridBagConstraints();
		gbc_colorPanel.gridwidth = 3;
		gbc_colorPanel.insets = new Insets(5, 0, 10, 0);
		gbc_colorPanel.fill = GridBagConstraints.BOTH;
		gbc_colorPanel.gridx = 1;
		gbc_colorPanel.gridy = 8;
		getContentPane().add(colorPanel, gbc_colorPanel);
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
		blueSlider.setValue((blueSlider.getMaximum() - blueSlider.getMinimum()) / 2);
		blueSlider.addChangeListener(this);
		blueSlider.setPaintTicks(true);
		blueSlider.setMinorTickSpacing(1);
		GridBagConstraints gbc_blueSlider = new GridBagConstraints();
		gbc_blueSlider.fill = GridBagConstraints.HORIZONTAL;
		gbc_blueSlider.gridx = 2;
		gbc_blueSlider.gridy = 3;
		colorPanel.add(blueSlider, gbc_blueSlider);
		
		JLabel lblCount = new JLabel("개체수");
		GridBagConstraints gbc_lblCount = new GridBagConstraints();
		gbc_lblCount.gridx = 1;
		gbc_lblCount.gridy = 9;
		getContentPane().add(lblCount, gbc_lblCount);
		
		SpinnerNumberModel model = new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1);
		cntSpinner = new JSpinner(model);
		GridBagConstraints gbc_cntSpinner = new GridBagConstraints();
		gbc_cntSpinner.gridx = 2;
		gbc_cntSpinner.gridy = 9;
		getContentPane().add(cntSpinner, gbc_cntSpinner);
		
		
		JButton createBtn = new JButton("생성");
		createBtn.setActionCommand("Create");
		createBtn.addActionListener(this);
		GridBagConstraints gbc_createBtn = new GridBagConstraints();
		gbc_createBtn.gridwidth = 3;
		gbc_createBtn.insets = new Insets(10, 50, 10, 50);
		gbc_createBtn.fill = GridBagConstraints.BOTH;
		gbc_createBtn.gridx = 1;
		gbc_createBtn.gridy = 10;
		getContentPane().add(createBtn, gbc_createBtn);
		
		
		//this.setPreferredSize(new Dimension(355, 570));
		//this.setResizable(false);
		this.pack();
		
		this.setVisible(true);
		
		stateChanged(null);
	}
	
	private Gene gene = null;
	@Override
	public void stateChanged(ChangeEvent e) {
		int r = redSlider.getValue() & 0xF;
		int g = greenSlider.getValue() & 0xF;
		int b = blueSlider.getValue() & 0xF;
		gene = new Gene(sizeSlider.getValue(), speedSlider.getValue(), energySlider.getValue(), sightSlider.getValue(), lifespanSlider.getValue(), (r << 8) | (g << 4) | (b));
		lifeDisplayPanel.repaint();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if("Create".equalsIgnoreCase(e.getActionCommand())) {
			for(int i = 0; i < (Integer) cntSpinner.getValue(); i++) {
				sim.addLifeform(gene, new Vector2(x, y), null); // TODO to setting.addLifeform
			}
			this.setVisible(false);
			this.dispose();
		}
	}
	
	private void paintLife(Graphics g) {
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, APPEARENCE_SIZE, APPEARENCE_SIZE);
		
		if(gene != null) {
			((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			float rad = gene.getSize() * 10;
			int color = gene.getColor();
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
	}
}
