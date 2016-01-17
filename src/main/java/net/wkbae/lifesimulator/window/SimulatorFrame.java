package net.wkbae.lifesimulator.window;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.Point;
import java.awt.ScrollPane;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jbox2d.callbacks.QueryCallback;
import org.jbox2d.collision.AABB;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Fixture;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

import net.wkbae.lifesimulator.Lifeform;
import net.wkbae.lifesimulator.Simulation;

public class SimulatorFrame extends Frame implements WindowListener {
	private static final long serialVersionUID = -682403930435583320L;
	
	//private DrawPanel panel;
	public final GLCanvas drawCanvas;
	private FPSAnimator animator;
	private int previousFps;
	
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
		
		GLProfile profile = GLProfile.getDefault();
		GLCapabilities cap = new GLCapabilities(profile);
		cap.setSampleBuffers(true);
		cap.setNumSamples(8);
		drawCanvas = new GLCanvas(cap);
		
		previousFps = SettingFrame.setting.getFrameRate();
		animator = new FPSAnimator(drawCanvas, previousFps);
		
		drawCanvas.addGLEventListener(new GLEventListener() {
			private int size;
			@Override
			public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
				this.size = width;
				
				GL2 gl = drawable.getGL().getGL2();
				
				gl.glEnable(GL2.GL_MULTISAMPLE);
				
				gl.glMatrixMode(GL2.GL_PROJECTION);
				gl.glLoadIdentity();
				gl.glOrtho(0, width, height, 0, -1, 1);
				
				gl.glMatrixMode(GL2.GL_MODELVIEW);
				gl.glLoadIdentity();
				gl.glViewport(0, 0, width, height);
			}
			
			@Override
			public void init(GLAutoDrawable drawable) {
				animator.start();
			}
			
			@Override
			public void dispose(GLAutoDrawable drawable) {
				animator.stop();
			}
			
			@Override
			public void display(GLAutoDrawable drawable) {
				Simulation sim = setting.getCurrentSimulation();
				if(sim != null) {
					GL2 gl = drawable.getGL().getGL2();
					gl.glClear(GL.GL_COLOR_BUFFER_BIT);
					gl.glLoadIdentity();
					sim.getPainter().paint(gl, 0, 0, size, size);
				} else {
					GL2 gl = drawable.getGL().getGL2();
					gl.glClear(GL.GL_COLOR_BUFFER_BIT);
					gl.glLoadIdentity();
					gl.glColor3f(1, 1, 1);
					gl.glRectf(0, 0, size, size);
				}
				
				if(SettingFrame.setting.getFrameRate() != previousFps) {
					animator.stop();
					previousFps = SettingFrame.setting.getFrameRate();
					animator.setFPS(previousFps);
					animator.start();
				}
			}
		});
		
		drawCanvas.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				drawCanvas.setSize(SettingFrame.setting.getDisplaySize(), SettingFrame.setting.getDisplaySize());
				drawCanvas.setLocation((alignPanel.getWidth() / 2) - (drawCanvas.getWidth() / 2), (alignPanel.getHeight() / 2) - (drawCanvas.getHeight() / 2));
				//scroll.revalidate();
			}
		});
		
		drawCanvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() >= 2) { // double click
					Simulation sim = setting.getCurrentSimulation();
					if(sim != null) {
						float x = e.getX() / sim.getSetting().getDisplayRatio();
						float y = e.getY() / sim.getSetting().getDisplayRatio();
						sim.world.queryAABB(new QueryCallback() {
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
						}, new AABB(new Vec2(x - 0.25f, y - 0.25f), new Vec2(x + 0.25f, y + 0.25f)));
						
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
		setting.pack();
		
		setting.setVisible(true);
	}
	
	@Override
	public void windowActivated(WindowEvent e) {}

	@Override
	public void windowClosed(WindowEvent e) {}

	@Override
	public void windowClosing(WindowEvent e) {
		setting.setVisible(false);
		
		setting.dispose();
		
		this.dispose();
		
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
	
}
