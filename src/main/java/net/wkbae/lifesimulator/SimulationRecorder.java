package net.wkbae.lifesimulator;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.jbox2d.common.MathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.Global;

public class SimulationRecorder extends Thread {
	
	//private final static int FRAMES_PER_SECOND = 60;
	
	//private final static long FRAME_RATE = Global.DEFAULT_TIME_UNIT.convert(Math.round(1000000.0 / FRAMES_PER_SECOND), TimeUnit.MICROSECONDS);

	private final static GraphicsConfiguration graphicConfig = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
	
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
	//private SequenceEncoder encoder;
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
			while(!stop) {
				if(paintQueue.isEmpty()) {
					synchronized (paintQueue) {
						paintQueue.wait();
					}
				}
				//Iterator<SimulationPainter> iter = paintQueue.iterator();
				SimulationPainter painter;
				//synchronized(this) {
					painter = paintQueue.pollFirst();
				//}
				//int i = 0;
				while(painter != null) {
					// = iter.next();
					BufferedImage frame = new BufferedImage(size, height, BufferedImage.TYPE_3BYTE_BGR);
					
					BufferedImage compat = graphicConfig.createCompatibleImage(size, size);
					Graphics2D g = compat.createGraphics();	
					
					painter.paint(g, size);
					
					g.dispose();
					
					g = frame.createGraphics();
					g.drawImage(compat, 0, 0, null);
					
					g.setColor(Color.BLACK);
					g.fillRect(0, size, size, height - size);
					
					g.setColor(Color.WHITE);
					String elapsed = (MathUtils.round((float) (painter.getTime() / 100.0)) / 10.0) + "s";
					Rectangle2D bound = g.getFontMetrics().getStringBounds(elapsed, g);
					g.drawString(elapsed, 5, size + 5 + MathUtils.round((float) (bound.getHeight() / 2)));
					
					g.dispose();
					
					long frameTime = Global.DEFAULT_TIME_UNIT.convert(painter.getTime(), TimeUnit.MILLISECONDS);
					writer.encodeVideo(0, frame, frameTime, Global.DEFAULT_TIME_UNIT);
					//encoder.encodeImage(frame);
					
					//iter.remove();
					//synchronized (this) {
						painter = paintQueue.pollFirst();
					//}
					
				//	i++;
				}
				//System.out.println("Recorded " + i + " Frames");
				synchronized(paintQueue) {
					paintQueue.notifyAll();
				}
				if(Thread.interrupted()) break;
			//	if(paintQueue.isEmpty()) Thread.yield();
			}
		} catch (InterruptedException e) {}
	}
	
	public synchronized void finish() {
		if(started) {
			stop = true;
			sim.removeSimulationListener(listener);
			recordingSimulations.remove(sim);
			
			synchronized(paintQueue) {
				paintQueue.notifyAll();
			}
			
			this.interrupt();
			try {
				this.join();
			} catch (InterruptedException e1) {
				Thread.currentThread().interrupt();
			}
			
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
	
	//private Queue<SimulationPainter> paintQueue = new LinkedList<>();
	
	private TreeSet<SimulationPainter> paintQueue = new TreeSet<>(new PainterComparator());
	
	//private long updateCount = 0;
	//private long lastTime = -1;
	
	private class RecordListener implements SimulationListener {
		@Override
		public void simulationPainterUpdated(Simulation simulation, SimulationPainter painter) {
			if(started && painter.getSimulation() == sim) {
				paintQueue.add(painter);
				//if(paintQueue.size() >= 60) {
					synchronized(paintQueue) {
						paintQueue.notifyAll();
					}
					synchronized(paintQueue) {
						if(paintQueue.size() > 600) {
							try {
								paintQueue.wait();
							} catch (InterruptedException e) {
								Thread.currentThread().interrupt();
								return;
							}
						}
					}
				//}
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
	
	
	private class PainterComparator implements Comparator<SimulationPainter> {
		@Override
		public int compare(SimulationPainter o1, SimulationPainter o2) {
			return (int) (o1.getTime() - o2.getTime());
		}
	}
}
