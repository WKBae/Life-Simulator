package net.wkbae.lifesimulator;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jogamp.opengl.DefaultGLCapabilitiesChooser;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.awt.AWTGLReadBufferUtil;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.Global;

public class SimulationRecorder extends Thread {
	
	//private final static int FRAMES_PER_SECOND = 60;
	
	//private final static long FRAME_RATE = Global.DEFAULT_TIME_UNIT.convert(Math.round(1000000.0 / FRAMES_PER_SECOND), TimeUnit.MICROSECONDS);

	private final static int QUEUE_LIMIT = 1200;
	private final static int RENDERER_COUNT = 5;
	
	
	private static Set<Simulation> recordingSimulations = new HashSet<>();
	public static boolean isRecording(Simulation sim) {
		return recordingSimulations.contains(sim);
	}
	
	private Simulation sim;
	
	/*public SimulationRecorder() {
		this(Simulation.getCurrentSimulation());
	}*/
	
	public SimulationRecorder(Simulation sim) {
		this(sim, "simulation" + File.separator + new SimpleDateFormat("yy.MM.dd;HH.mm.ss''SSS").format(new Date()) + ".mkv");
	}
	
	private String output;
	public SimulationRecorder(Simulation sim, String output) {
		super("Simulation Recorder");
		if(sim == null) {
			throw new NullPointerException();
		}
		
		this.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				LoggerFactory.getLogger(SimulationRecorder.class).error("Exception thrown while recording simulation.", e);
				try {
					finish();
				} catch(Exception ex) {}
			}
		});
		
		if(output == null) {
			output = "null.mkv";
		}
		this.sim = sim;
		this.output = output;
	}
	
	private int size = 1024;
	private int height;
	private boolean started = false;
	
	private IMediaWriter writer;
	
	private BlockingQueue<GLAutoDrawable> drawablePool;
	
	private ExecutorService executor = new ThreadPoolExecutor(0, RENDERER_COUNT, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(QUEUE_LIMIT) {
		private static final long serialVersionUID = -211514487914423576L;

		@Override
		public boolean offer(Runnable e) {
			try {
				put(e);
				return true;
			} catch(InterruptedException ex) {
				Thread.currentThread().interrupt();
				return false;
			}
		};
	});
	
	private RecordListener listener = new RecordListener();
	
	@Override
	public synchronized void start() {
		try {
			//size = Simulation.Setting.worldSize * 2;
			
			new File("simulation").mkdirs();
			
			writer = ToolFactory.makeWriter(output);
			//encoder = new SequenceEncoder(new File(output));
			
			int bottomHeight = getDefaultFontMetrics().getHeight() + 10;
			height = size + bottomHeight;
			
			writer.addVideoStream(0, 0, size, height);
			
			started = true;
			
			recordingSimulations.add(sim);
			sim.addSimulationListener(listener);
			
			try(Writer out = new FileWriter(new File(output + ".seed"))) {
				out.write(sim.getSetting().encode());
			} catch (IOException e) {
				LoggerFactory.getLogger(SimulationRecorder.class).warn("Cannot write seed file!", e);
			}
			
			super.start();
		} catch(Exception e) {
			Logger recordLogger = LoggerFactory.getLogger(SimulationRecorder.class);
			recordLogger.warn("Cannot start Simulation Recorder");
			this.started = false;
			sim.removeSimulationListener(listener);
			recordingSimulations.remove(sim);
			//throw e;
		}
	}
	
	private boolean stop = false;
	
	@Override
	public void run() {
		try {
			initDrawablePool();
			
			while(!stop) {
				ConcurrentSkipListMap.Entry<Long, Future<Frame>> entry;
				while((entry = renderedFrames.pollFirstEntry()) == null) {
					Thread.sleep(30);
				}
				
				try {
					Frame frame = entry.getValue().get();
					
					long frameTime = Global.DEFAULT_TIME_UNIT.convert(frame.frameTime, TimeUnit.MILLISECONDS);
					writer.encodeVideo(0, frame.image, frameTime, Global.DEFAULT_TIME_UNIT);
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(Thread.interrupted()) break;
			}
		} catch (InterruptedException e) {}
	}
	
	
	private void initDrawablePool() {
		GLProfile glp = GLProfile.getDefault();
		GLCapabilities caps = new GLCapabilities(glp);
		caps.setHardwareAccelerated(true);
		caps.setDoubleBuffered(false);
		caps.setOnscreen(false);
		caps.setSampleBuffers(true);
		caps.setNumSamples(8);
		
		GLDrawableFactory factory = GLDrawableFactory.getFactory(glp);
		
		drawablePool = new LinkedBlockingQueue<>();
		for(int i = 0; i < RENDERER_COUNT; i++) {
			GLAutoDrawable drawable = factory.createOffscreenAutoDrawable(factory.getDefaultDevice(), caps, new DefaultGLCapabilitiesChooser(), size, size);
			drawable.display();
			drawable.getContext().makeCurrent();
			GL2 gl = drawable.getGL().getGL2();
			gl.glEnable(GL2.GL_MULTISAMPLE);
			
			gl.glMatrixMode(GL2.GL_PROJECTION);
			gl.glLoadIdentity();
			gl.glOrtho(0, size, size, 0, -1, 1);
			
			gl.glMatrixMode(GL2.GL_MODELVIEW);
			gl.glLoadIdentity();
			gl.glViewport(0, 0, size, size);
			drawable.getContext().release();
			
			drawablePool.add(drawable);
		}
	}
	
	private static class Frame {
		public final long frameTime;
		public final BufferedImage image;
		Frame(long frameTime, BufferedImage image) {
			this.frameTime = frameTime;
			this.image = image;
		}
	}
	private class FrameRenderer implements Callable<Frame> {
		
		private SimulationPainter painter;
		
		public FrameRenderer(SimulationPainter painter) {
			this.painter = painter;
		}
		
		@Override
		public Frame call() throws Exception {
			GLAutoDrawable drawable = drawablePool.take();
			
			AWTGLReadBufferUtil bufferReader = new AWTGLReadBufferUtil(drawable.getGLProfile(), true);
			
			BufferedImage frame = new BufferedImage(size, height, BufferedImage.TYPE_3BYTE_BGR);
			
			drawable.getContext().makeCurrent();
			GL2 gl = drawable.getGL().getGL2();
			gl.glColor3f(1, 1, 1);
			gl.glRectf(0, 0, size, size);
			
			gl.glClear(GL.GL_COLOR_BUFFER_BIT);
			gl.glLoadIdentity();
			painter.paint(gl, 0, 0, size, size);
			
			Graphics2D g = frame.createGraphics();
			g.drawImage(bufferReader.readPixelsToBufferedImage(gl, true), 0, 0, null);
			drawable.getContext().release();
			
			g.setColor(Color.BLACK);
			g.fillRect(0, size, size, height - size);
			
			g.setColor(Color.WHITE);
			String elapsed = String.format("%.1fs", painter.getTime() / 1000.0f + 0.05f);//(MathUtils.round((float) (painter.getTime() / 100.0)) / 10.0) + "s";
			Rectangle2D bound = g.getFontMetrics().getStringBounds(elapsed, g);
			g.drawString(elapsed, 5, size + 5 + (int)(bound.getHeight() / 2.0f + 0.5f));//MathUtils.round((float) (bound.getHeight() / 2)));
			
			g.dispose();
			
			drawablePool.put(drawable);
			
			return new Frame(painter.getTime(), frame);
		}
	}
	
	public synchronized void finish() {
		if(started) {
			stop = true;
			sim.removeSimulationListener(listener);
			recordingSimulations.remove(sim);
			
			executor.shutdown();
			this.interrupt();
			try {
				executor.awaitTermination(10, TimeUnit.SECONDS);
				this.join();
				executor.shutdownNow();
			} catch (InterruptedException e1) {
				Thread.currentThread().interrupt();
			}
			
			drawablePool = null;
			renderedFrames.clear();
			
			try {
				writer.flush();
				writer.close();
			} catch(Exception e) {
				Logger recordLogger = LoggerFactory.getLogger(SimulationRecorder.class);
				recordLogger.warn("Cannot close media writer: ", e);
			}
			
			this.started = false;
		}
	}
	
	//private long updateCount = 0;
	//private long lastTime = -1;
	
	private ConcurrentSkipListMap<Long, Future<Frame>> renderedFrames = new ConcurrentSkipListMap<>();
	
	private class RecordListener implements SimulationListener {
		@Override
		public void simulationPainterUpdated(Simulation simulation, SimulationPainter painter) {
			if(started && painter.getSimulation() == sim) {
				renderedFrames.put(painter.getTime(), executor.submit(new FrameRenderer(painter)));
			}
		}
		
		@Override
		public void simulationStarted(Simulation simulation) {}
		@Override
		public void simulationStopped(Simulation simulation) {}
		@Override
		public void simulationFinishing(Simulation simulation) {}
		@Override
		public void simulationSettingChanged(Simulation simulation, SimulationSetting setting) {
			File f = new File(output + ".seed");
			f.delete();
			try(Writer out = new FileWriter(f)) {
				out.write(setting.encode());
			} catch (IOException e) {
				LoggerFactory.getLogger(SimulationRecorder.class).warn("Cannot write seed file!", e);
			}
		}
	}
	
	private static FontMetrics getDefaultFontMetrics() {
		BufferedImage bi = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB_PRE);
		Graphics g = bi.getGraphics();
		FontMetrics fm = g.getFontMetrics();
		g.dispose();
		bi = null;
		return fm;
	}
	
}
