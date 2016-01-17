package net.wkbae.lifesimulator.window;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Panel;
import java.awt.Point;
import java.awt.ScrollPane;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferStrategy;
import java.awt.image.VolatileImage;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.slf4j.LoggerFactory;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;

import net.wkbae.lifesimulator.Lifeform;
import net.wkbae.lifesimulator.Simulation;
import net.wkbae.lifesimulator.SimulationPainter;

public class SimulatorFrame extends Frame implements WindowListener {
	private static final long serialVersionUID = -682403930435583320L;
	
	//private DrawPanel panel;
	public final Canvas drawCanvas;
	private ScrollPane scroll;
	private SettingFrame setting;
	
	SimulatorFrame() {
		super("생명 시뮬레이터");
		
		addWindowListener(this);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
		setting = new SettingFrame(this);
		
		//getContentPane().setLayout(new BorderLayout());
		setLayout(new BorderLayout());
		
		scroll = new ScrollPane();
		/*scroll.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				super.componentResized(e);
				Component comp = e.getComponent();
				drawCanvas.setLocation((comp.getWidth() / 2) - (drawCanvas.getWidth() / 2), (comp.getHeight() / 2) - (drawCanvas.getHeight() / 2));
			}
		});*/
		//getContentPane().add(scroll, BorderLayout.CENTER);
		add(scroll, BorderLayout.CENTER);
		
		final Panel alignPanel = new Panel() {
			private static final long serialVersionUID = 5257372737237899399L;
			
			@Override
			public void paint(Graphics g) {
				//super.paint(g);
				g.setColor(Color.BLACK);
				g.fillRect(0, 0, getWidth(), getHeight());
			}
		};
		alignPanel.setLayout(new BorderLayout());
		//GLG2DCanvas g = new GLG2DCanvas();
		drawCanvas = new Canvas() {
			private static final long serialVersionUID = 1L;

			public void update(Graphics g) {
				paint(g);
			}
			
			public void paint(Graphics g) {
				Simulation sim = setting.getCurrentSimulation();
				if(sim != null) {
					sim.getPainter().paint((Graphics2D) g, this.getWidth());
				}
			}
		};
		
		drawCanvas.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				drawCanvas.setSize(SettingFrame.setting.getDisplaySize(), SettingFrame.setting.getDisplaySize());
				drawCanvas.setLocation((alignPanel.getWidth() / 2) - (drawCanvas.getWidth() / 2), (alignPanel.getHeight() / 2) - (drawCanvas.getHeight() / 2));
				//scroll.revalidate();
			}
		});
		
		
		/*drawCanvas = new Canvas() {
			private static final long serialVersionUID = -2068588611933791347L;
			
			@Override
			public void update(Graphics g) {
				paint(g);
			}
			@Override
			public void paint(Graphics g) {
				//super.paint(g);
				Simulation sim = Simulation.getCurrentSimulation();
				if(sim != null) {
					sim.getPainter().paint((Graphics2D)getBufferStrategy().getDrawGraphics(), this.getWidth());
				}
			}
		};*/
		//getContentPane().add(canvas, BorderLayout.NORTH);
		drawCanvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() >= 2) { // double click
					Simulation sim = setting.getCurrentSimulation();
					if(sim != null) {
						float x = e.getX() / sim.getSetting().getDisplayRatio();
						float y = e.getY() / sim.getSetting().getDisplayRatio();
						sim.world.QueryAABB(new QueryCallback() {
							@Override
							public boolean reportFixture(Fixture fix) {
								if(fix.getUserData() instanceof Lifeform) {
									final Lifeform life = (Lifeform) fix.getUserData();
									SwingUtilities.invokeLater(new Runnable() {
										@Override
										public void run() {
											new LifeformInfoDialog(setting, life);
										}
									});
									return false;
								} else {
									return true;
								}
							}
						}, x - 0.25f, y - 0.25f, x + 0.25f, y + 0.25f);
						
					}
				} else if(e.getButton() == MouseEvent.BUTTON3) { // right click
					final Simulation sim = setting.getCurrentSimulation();
					if(sim != null) {
						final float x = e.getX() / sim.getSetting().getDisplayRatio();
						final float y = e.getY() / sim.getSetting().getDisplayRatio();
						SwingUtilities.invokeLater(new Runnable() {
							
							@Override
							public void run() {
								new LifeformCreationDialog(setting, sim, x, y);
							}
						});
					}
				}
			}
		});
		
		alignPanel.add(drawCanvas, BorderLayout.CENTER);
		scroll.add(alignPanel);
		
		this.setPreferredSize(new Dimension(512, 512));
		this.pack();
		
		this.setVisible(true);
		
		Point loc = this.getLocationOnScreen();
		loc.x += this.getWidth();
		setting.setLocation(loc);
		
		drawCanvas.createBufferStrategy(2);
		this.paintThread = new PaintThread(drawCanvas);
		
		setting.setVisible(true);
		
		paintThread.start();
	}
	
	private PaintThread paintThread;
	
	/*@Override
    public void paint(Graphics g) {
        Dimension d = getSize();
        Dimension m = getMaximumSize();
        boolean resize = d.width > m.width || d.height > m.height;
        d.width = Math.min(m.width, d.width);
        d.height = Math.min(m.height, d.height);
        if (resize) {
            Point p = getLocation();
            setVisible(false);
            setSize(d);
            setLocation(p);
            setVisible(true);
        }
        super.paint(g);
    }*/

	@Override
	public void windowActivated(WindowEvent e) {}

	@Override
	public void windowClosed(WindowEvent e) {}

	@Override
	public void windowClosing(WindowEvent e) {
		/*if(this.paintThread != null) {
			paintThread.interrupt();
		}*/
		
		//long time1 = System.currentTimeMillis();
		setting.setVisible(false);
		//long time2 = System.currentTimeMillis();
		setting.dispose();
		//long time3 = System.currentTimeMillis();
		
		this.dispose();
		//long time4 = System.currentTimeMillis();
		//System.out.println((time2 - time1) + ", " + (time3 - time2) + ", " + (time4 - time3));
		System.exit(0);
	}

	@Override
	public void windowDeactivated(WindowEvent e) {}

	@Override
	public void windowDeiconified(WindowEvent e) {}

	@Override
	public void windowIconified(WindowEvent e) {}

	@Override
	public void windowOpened(WindowEvent e) {}
	
	private class PaintThread extends Thread {
		//private Canvas canvas;
		private PainterRunnable runnable;
		private long lastTime = -1;
		
		private PaintThread(Canvas canvas) {
			//this.canvas = drawCanvas;
			this.runnable = new PainterRunnable(canvas);
			this.setDaemon(true);
		}
		
		@Override
		public void run() {
			try {
				while(!Thread.interrupted()) {
					if(SettingFrame.setting.getFrameRate() == Float.POSITIVE_INFINITY) {
						sleep(100);
					} else {
						Simulation sim = setting.getCurrentSimulation();
						if(sim == null) {
							sleep(100);
							continue;
						}
						
						long loopStart = System.nanoTime();
						
						try {
							if(sim != null) {
								SimulationPainter painter = sim.getPainter();
								if(painter.getTime() != lastTime) {
									SwingUtilities.invokeAndWait(runnable.setPainter(painter));
									lastTime = painter.getTime();
								}
							}
						} catch(Exception e) {
							LoggerFactory.getLogger(SimulatorFrame.class).warn("Exception occured while painting screen: ", e);
						}
						
						long loopEnd = System.nanoTime();
						long sleepTime = MathUtils.round((float) (sim.getSetting().getFrameRate() * 1000 - (loopEnd - loopStart)*0.00_000_1));
						if(sleepTime > 0) {
							Thread.sleep(sleepTime);
						}
					}
					if(Thread.interrupted()) break;
				}
			} catch (InterruptedException e) {}
		}
	}
	
	private static class PainterRunnable implements Runnable {
		
		private Canvas canvas;
		
		public PainterRunnable(Canvas canvas) {
			this.canvas = canvas;
		}
		
		private SimulationPainter painter;
		
		public PainterRunnable setPainter(SimulationPainter painter) {
			this.painter = painter;
			return this;
		}
		
		@Override
		public void run() {
			BufferStrategy bs = canvas.getBufferStrategy();
			
			VolatileImage vi = canvas.createVolatileImage(canvas.getWidth(), canvas.getHeight());
			Graphics vig = vi.getGraphics();
			painter.paint((Graphics2D) vig, canvas.getWidth());
			vig.dispose();
			if(!vi.contentsLost()) {
				Graphics bsg = bs.getDrawGraphics();
				bsg.drawImage(vi, 0, 0, null);
				bsg.dispose();
				if(!bs.contentsLost()) {
					bs.show();
				}
			}
			
		}
	}
}
