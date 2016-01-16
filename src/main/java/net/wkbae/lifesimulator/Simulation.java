package net.wkbae.lifesimulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;
import org.slf4j.LoggerFactory;

import net.wkbae.lifesimulator.Gene.Factor;

/**
 * This class represents the simulation itself and runs throughout the overall simulation progress.<br/>
 * The construction of the simulation requires the {@link SimulationSetting}.
 * @author William
 *
 */
public class Simulation implements Runnable, ContactListener {
	
	private boolean running = false;
	private volatile boolean stop = false;
	
	private Random random;
	
	private long loopCounter;
	
	private Thread runningThread;
	
	public final World world;
	final float worldSizeReverse;
	
	private final ArrayList<SimulationListener> listeners = new ArrayList<>();
	
	private SimulationSetting setting; // TODO init & use setting
	
	public Simulation(SimulationSetting setting) {
		this.setting = setting;
		
		//this.drawCanvas = drawCanvas;
		this.random = new Random(setting.getSeed());
		
		worldSizeReverse = 1.0f / setting.getWorldSize();
		
		world = new World(new Vec2(0.0f, 0.0f));
		world.setAllowSleep(true);
		world.setContactListener(this);
		
		createWall();
		
		initLives();
		
		updatePainter0();
	}
	
	/**
	 * Starts the simulation.<br/>
	 * Simulations are run in other threads named "Simulation Thread"(Main, Physics thread) and "Simulator Thread <i>n</i>"(Lifeform calculation, <i>n</i> max. to the process count of the host computer)
	 */
	public synchronized void start() {
		if(!running) {
			if(lifeformTicker == null) {
				lifeformTicker = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new SimulationThreadFactory());
			}
			
			setting.setSimulationStarted(true);
			
			runningThread = new Thread(this, "Simulation Thread");
			runningThread.setPriority(Thread.NORM_PRIORITY + 1);
			runningThread.start();
		}
	}
	
	/**
	 * Pauses the simulation<br/>
	 * The simulation can be resumed by calling {@link #start()} again.<br/>
	 * Notice that the pausing progress can take quite amount of time, because the progress waits for the simulation to
	 */
	public synchronized void stop() {
		if(running) {
			stop = true;
			if(runningThread != null) {
				runningThread.interrupt();
				try {
					runningThread.join();
				} catch (InterruptedException e) {}
				runningThread = null;
			}
		}
	}
	
	/**
	 * Finishes the simulation, so that the simulation cannot be started again.
	 */
	public synchronized void finish() {
		stop();
		if(lifeformTicker != null) {
			try {
				lifeformTicker.shutdownNow();
				lifeformTicker = null;
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			setting.setSimulationStarted(false);
		}
	}
	
	private long startTime;
	private long elapsedTime;
	
	private ExecutorService lifeformTicker;
	
	@Override
	public void run() {
		running = true;
		stop = false;
		synchronized (this) {
			this.notifyAll();
		}
		
		Object[] lists;
		synchronized (listeners) {
			lists = listeners.toArray();
		}
		for(Object listener : lists) {
			((SimulationListener)listener).simulationStarted(this);
		}
		lists = null;
		
		try {
			long loopStart, loopEnd;
			while(!stop) {
				try {
					loopStart = System.nanoTime();
					
					synchronized (world) {
						calculatePhysics();
					}
					if(stop && Thread.interrupted()) {
						break;
					}
					
					calculateLives();
					
					if(stop && Thread.interrupted()) {
						break;
					}
					
					if(++loopCounter == Long.MAX_VALUE) {
						loopCounter = loopCounter % SimulationSetting.calculatesPerSecond;
					}
					
					try {
						if(updatePainter.compareAndSet(true, false) || SimulationRecorder.isRecording(this)) {
							updatePainter0();
							synchronized (updatePainter) {
								updatePainter.notifyAll();
							}
						}
					} catch (Exception e) {
						LoggerFactory.getLogger(Simulation.class).warn("Cannot update Simulation Painter: ", e);
					}
					
					if(!hasLifeform()) {
						stop();
					}
					
					if(stop && Thread.interrupted()) {
						break;
					}
					
					loopEnd = System.nanoTime();
					
					// (1.0 / 60.0 - (loopEnd - loopStart)*0.00_000_000_1) * 1000
					long sleepTime = MathUtils.round((float) ((SimulationSetting.timeStep*1000 - (loopEnd - loopStart)*0.00_000_1) * setting.getSimulationSpeed()));
					elapsedTime += MathUtils.round(SimulationSetting.timeStep * 1000);
					if(sleepTime > 0) {
						Thread.sleep(sleepTime);
					}
					if(Runtime.getRuntime().freeMemory() < 3_000_000) {
						Runtime runtime = Runtime.getRuntime();
						runtime.gc();
						System.out.println("Low Memory");
						while(runtime.maxMemory() - runtime.totalMemory() + runtime.freeMemory() < 3_000_000) { // 3MB
							Thread.sleep(50);
						}
						System.out.println("Low Memory Out");
					}
				} catch (InterruptedException e){
					continue; // stop signal
				}
			}
		} catch(Throwable e) {
			LoggerFactory.getLogger(Simulation.class).error("Error occured while performing simulation.", e);
			this.stop();
		}
		
		running = false;
		
		synchronized (listeners) {
			lists = listeners.toArray();
		}
		for(Object listener : lists) {
			((SimulationListener)listener).simulationStopped(this);
		}
	}
	
	private boolean hasLifeform() {
		return world.getBodyCount() > 4; // 4 walls
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public long getStartTime() {
		return startTime;
	}
	
	public long getElapsedTime() {
		return elapsedTime;
	}
	
	private void createWall() {
		BodyDef bdef = new BodyDef();
		bdef.type = BodyType.STATIC;
		
		final float halfWorld = setting.getWorldSize() / 2;
		
		PolygonShape verticalWall = new PolygonShape();
		verticalWall.setAsBox(5, halfWorld + 2.5f);
		
		PolygonShape horizontalWall = new PolygonShape();
		horizontalWall.setAsBox(halfWorld + 2.5f, 5);
		
		FixtureDef vdef = new FixtureDef();
		vdef.shape = verticalWall;
		//vdef.density = 0;
		vdef.restitution = 0.3f;
		
		FixtureDef hdef = new FixtureDef();
		hdef.shape = horizontalWall;
		//hdef.density = 0;
		hdef.restitution = 0.3f;
		
		bdef.position.set(halfWorld-2.5f, -5);//(-5, -5);
		Body wallT = world.createBody(bdef);
		wallT.createFixture(hdef);
		
		bdef.position.set(setting.getWorldSize() + 5, halfWorld-2.5f);
		Body wallR = world.createBody(bdef);
		wallR.createFixture(vdef);
		
		bdef.position.set(halfWorld+2.5f, setting.getWorldSize() + 5);
		Body wallB = world.createBody(bdef);
		wallB.createFixture(hdef);
		
		bdef.position.set(-5, halfWorld+2.5f);
		Body wallL = world.createBody(bdef);
		wallL.createFixture(vdef);
	}
	
	private void initLives() {
		lifeforms[0] = new ArrayList<>();
		for(int i = 0; i < setting.getLifeformAmount(); i++) {
			Vec2 loc = new Vec2(random.nextInt(setting.getWorldSize()), random.nextInt(setting.getWorldSize()));
			lifeforms[0].add(createLifeform(Gene.random(random), loc, new HashMap<Integer, Float>()));
		}
	}
	
	private void calculatePhysics() {
		world.step(SimulationSetting.timeStep, 8, 3);
	}
	
	@SuppressWarnings("unchecked")
	private ArrayList<Lifeform>[] lifeforms = new ArrayList[SimulationSetting.calculatesPerSecond];//HashMap<Integer, ArrayList<Lifeform>> lifeforms = new HashMap<>();
	@SuppressWarnings("unchecked")
	private ArrayList<BreedingPair>[] breedingPairs = new ArrayList[SimulationSetting.calculatesPerSecond];
	@SuppressWarnings("unchecked")
	private void calculateLives() throws ExecutionException, InterruptedException {
		int loop = (int)(loopCounter % SimulationSetting.calculatesPerSecond);
		
		processDeadLives();
		processLifeAddition(loop);
		processBreedingQue(loop);
		
		if(breedingPairs[loop] != null && !breedingPairs[loop].isEmpty()) {
			for(BreedingPair pair : (ArrayList<BreedingPair>)breedingPairs[loop].clone()) {
				if(pair.isCancelled()) {
					if(lifeforms[loop] == null) {
						lifeforms[loop] = new ArrayList<>();
					}
					pair.endBreeding(false);
					lifeforms[loop].add(pair.one);
					lifeforms[loop].add(pair.two);
					
					breedingPairs[loop].remove(pair);
					continue;
				}
				if(pair.tick()) {
					if(lifeforms[loop] == null) {
						lifeforms[loop] = new ArrayList<>();
					}
					if(pair.isCancelled()) {
						pair.endBreeding(false);
						lifeforms[loop].add(pair.one);
						lifeforms[loop].add(pair.two);
						
						breedingPairs[loop].remove(pair);
						continue;
					}
					Body oneBody = pair.one.getBody();
					Body twoBody = pair.two.getBody();
					lifeforms[loop].add(createLifeform(
											Gene.crossover(random, pair.one.gene, pair.two.gene, setting.getMutationRatio()),
											oneBody.getPosition().add(twoBody.getPosition()).mulLocal(0.5f),// mean value
											pair.one.preference, pair.two.preference)
										);
					pair.endBreeding(true);
					lifeforms[loop].add(pair.one);
					lifeforms[loop].add(pair.two);
					
					breedingPairs[loop].remove(pair);
				}
			}
		}
		
		if(Thread.interrupted()) {
			throw new InterruptedException();
		}
		
		boolean interrupt = false;
	 	
		if(lifeforms[loop] != null && !lifeforms[loop].isEmpty()) {
			int size = lifeforms[loop].size();
			
			CountDownLatch latch = new CountDownLatch(size);
			Future<LifeSearchResult>[] result = new Future[size];
			
			int i = 0;
			Iterator<Lifeform> iter = lifeforms[loop].iterator();
			while(iter.hasNext()) {
				Lifeform life = iter.next();
				
				if(life.isBreeding()) {
					iter.remove();
					size--;
					latch.countDown();
					continue;
				}
				if(life.isDead()) {
					life.destroy();
					iter.remove();
					size--;
					latch.countDown();
					continue;
				}
				
				result[i] = lifeformTicker.submit(new LifeformTick(life, latch));
				//float[][] res = life.tick();
				
				
				i++;
			}
			
			latch.await();
			
			Future<Lifeform>[] calcRes = new Future[size];
			for(int j = 0; j < size; j++) {
				//try {
					LifeSearchResult res;
					try {
						res = result[j].get();
					} catch (InterruptedException e) {
						interrupt = true;
						try {
							res = result[j].get();
						} catch (InterruptedException e1) {
							try {
								res = result[j].get();
							} catch (InterruptedException e2) {
								e2.printStackTrace();
								throw new Error(e2);
							}
						}
					}
					if(res.length == -1 || res.life.isDead()) {
						res.life.destroy();
						lifeforms[loop].remove(res.life);
						size--;
						j--;
						i--;
						continue;
					}
					
					calcRes[j] = lifeformTicker.submit(new LifeformCalculate(res, random.nextFloat()));
			}
			
			for(int j = 0; j < size; j++) {
				Lifeform life;
				try {
					life = calcRes[j].get();
				} catch(InterruptedException e) {
					interrupt = true;
					try {
						life = calcRes[j].get();
					} catch(InterruptedException e1) {
						try {
							life = calcRes[j].get();
						} catch (InterruptedException e2) {
							e1.printStackTrace();
							throw new Error(e2);
						}
					}
				}
				if(life.isDead()) {
					life.destroy();
					lifeforms[loop].remove(life);
					continue;
				}
				
				int cooldown = life.gene.getMovingCooldown();
				int nextLoop = (loop + cooldown) % SimulationSetting.calculatesPerSecond;
				if(nextLoop != loop) {
					lifeforms[loop].remove(life);
					if(lifeforms[nextLoop] == null) {
						lifeforms[nextLoop] = new ArrayList<>();
					}
					lifeforms[nextLoop].add(life);
				}
			}
		}
		if(interrupt) runningThread.interrupt();
	}
	
	private void processDeadLives() {
		if(!deadLives.isEmpty()) {
			for(Lifeform life : deadLives) {
				life.destroy();
			}
			deadLives.clear();
		}
	}
	
	private void processLifeAddition(int loop) {
		LifeformCreationData[] dataArr = setting.getCreatedLifeform(loopCounter);
		if(dataArr != null) {
			if(lifeforms[loop] == null) {
				lifeforms[loop] = new ArrayList<>();
			}
			for(LifeformCreationData data : dataArr) {
				lifeforms[loop].add(createLifeform(data.gene, data.location, data.preference));
			}
		}
		
		boolean hasCreation = false;
		synchronized (lifeformAddQueue) {
			Iterator<LifeformCreationData> iter = lifeformAddQueue.iterator();
			if(lifeforms[loop] == null && iter.hasNext()) {
				lifeforms[loop] = new ArrayList<>();
			}
			while(iter.hasNext()) {
				LifeformCreationData data = iter.next();
				lifeforms[loop].add(createLifeform(data.gene, data.location, data.preference));
				
				setting.newLifeform(loopCounter, data);
				
				iter.remove();
				
				hasCreation = true;
			}
		}
		
		if(hasCreation) {
			Object[] lists;
			synchronized (listeners) {
				lists = listeners.toArray();
			}
			for(Object listener : lists) {
				((SimulationListener)listener).simulationSettingChanged(this, setting);
			}
		}
	}
	
	private void processBreedingQue(int loop) { // TODO make log to analyze
	 	if(!breedingQue.isEmpty()) {
	 	 	if(breedingPairs[loop] == null) {
				/*
	 	 	 	LifeformPair first = breedingQue.remove(0);
				first.one.startBreeding();
				first.two.startBreeding();
				breedingLives[loop] = first;
				if(breedingQue.isEmpty()) {
					return;
				}
				*/
				breedingPairs[loop] = new ArrayList<>();
	 	 	}
			for(Lifeform[] lives : breedingQue) {
				assert(lives.length == 2);
				if(lives[0] == null || lives[1] == null || lives[0].isBreeding() || lives[1].isBreeding() || lives[0].isDead() || lives[1].isDead()) {
					continue;
				}
				
				BreedingPair pair = new BreedingPair(world, loop, lives[0], lives[1]);
				pair.startBreeding();
				breedingPairs[loop].add(pair);
			}
			breedingQue.clear();
			/*
	 	 	LifeformPair lastPair;
	 	 	for(lastPair = breedingLives[loop]; lastPair.getNext() != null; lastPair = lastPair.getNext()); // loop to last pair

	 	 	for(LifeformPair pair : breedingQue) {
				if(pair.one.isBreeding() || pair.two.isBreeding()) {
					continue;
				}
				pair.one.startBreeding();
				pair.two.startBreeding();
	 	 	 	lastPair.setNext(pair);
	 	 	 	lastPair = pair;
	 	 	}*/
			
	 	}
	}
	
	private ArrayList<Lifeform[]> breedingQue = new ArrayList<>();
	private void addBreedingQue(Lifeform one, Lifeform two) {
	 	breedingQue.add(new Lifeform[]{one, two});//new BreedingPair(one, another));
	}
	
	//private float fps;
	
	private SimulationPainter currentPainter;
	
	//private long frames;
	//private long paintStart;
	private void updatePainter0() {
		try {
			List<SimulationPainter.LifePaintInfo> paintInfo = new ArrayList<>();
			for(Body body = world.getBodyList(); body != null; body = body.getNext()) {
				Fixture fixt = body.getFixtureList();
				if(fixt.getUserData() instanceof Lifeform) {
					paintInfo.add(new SimulationPainter.LifePaintInfo((Lifeform)fixt.getUserData()));
				}
			}
			
			currentPainter = new SimulationPainter(this, elapsedTime, paintInfo);
			
			Object[] lists;
			synchronized (listeners) {
				lists = listeners.toArray();
			}
			for(Object listener : lists) {
				((SimulationListener)listener).simulationPainterUpdated(this, currentPainter);
			}
		} catch (Exception e) {
			LoggerFactory.getLogger(Simulation.class).warn("Exception occured while updating painter.", e);
		}
	}
	
	private AtomicBoolean updatePainter = new AtomicBoolean(false);
	public void updatePainter() {
		if(!running) return;
		try {
			synchronized(updatePainter) {
				updatePainter.set(true);
				updatePainter.wait();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	public SimulationPainter getPainter() {
		if(currentPainter.getTime() < elapsedTime) {
			updatePainter();
		}
		return currentPainter;
	}
	
	public SimulationSetting getSetting() {
		return setting;
	}
	
	//private long lastPaint;
	private Lifeform createLifeform(Gene gene, Vec2 loc, Map<Integer, Float> parentPref1, Map<Integer, Float> parentPref2) {
		HashMap<Integer, Float> preference = new HashMap<>((parentPref1.size() + parentPref2.size()) / 2);
		for(Entry<Integer, Float> entry : parentPref1.entrySet()) {
			preference.put(entry.getKey(), (entry.getValue() - 1) / 2 + 1);
		}
		for(Entry<Integer, Float> entry : parentPref2.entrySet()) {
			Integer key = entry.getKey();
			if(!preference.containsKey(key)) {
				preference.put(key, (entry.getValue() - 1) / 2 + 1);
			} else {
				preference.put(key, (preference.get(key) - 1 + (entry.getValue() - 1)/2) / 2 + 1); // 두 값의 평균
			}
		}
		return createLifeform(gene, loc, preference);
	}	
	
	private Lifeform createLifeform(Gene gene, Vec2 loc, Map<Integer, Float> preference) {
		
		Lifeform life = new Lifeform(gene, preference);
		
		BodyDef bdef = new BodyDef();
		bdef.type = BodyType.DYNAMIC;
		bdef.bullet = true;
		bdef.position.set(loc);
		bdef.fixedRotation = true;
		bdef.linearDamping = setting.getDamping();
		
		CircleShape shape = new CircleShape();
		shape.m_radius = life.getSize(); //gene.getFactor(Factor.SIZE) / 10.0f;
		FixtureDef fdef = new FixtureDef();
		fdef.shape = shape;
		//fdef.density = gene.getFactor(Factor.MASS) / (shape.m_radius * shape.m_radius * MathUtils.PI);
		fdef.restitution = gene.getFactor(Factor.BOUNCY) / 50.0f;
		fdef.userData = life;
		
		synchronized (world) {
			Body body = world.createBody(bdef);
			
			life.setFixture(body.createFixture(fdef));
		}
		return life;
	}
	
	private List<LifeformCreationData> lifeformAddQueue = new ArrayList<>();
	
	public void addLifeform(Gene gene, Vec2 location, Map<Integer, Float> preferences) {
		synchronized (lifeformAddQueue) {
			lifeformAddQueue.add(new LifeformCreationData(gene, location, preferences));
		}
	}
	
	
	
	//private boolean record = false;
	//public void startRecording() {
	//	record = true;
	//}
	
	@Override
	public void beginContact(Contact contact) {}
	@Override
	public void endContact(Contact contact) {}
	
	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
		Fixture a = contact.getFixtureA();
		Fixture b = contact.getFixtureB();
		if(a.getUserData() instanceof Lifeform && b.getUserData() instanceof Lifeform) {
			Lifeform lifeA = (Lifeform) a.getUserData();
			Lifeform lifeB = (Lifeform) b.getUserData();
			if(lifeA.isCloseTo(lifeB) && lifeA.canBreed() && lifeB.canBreed()) {
				addBreedingQue(lifeA, lifeB);
				contact.getManifold().pointCount = 0;
			}
		}
	}
	
	//private final static float ATTACKING_THRESHOLD = 10.0f;
	private ArrayList<Lifeform> deadLives = new ArrayList<>();
	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		Fixture a = contact.getFixtureA();
		Fixture b = contact.getFixtureB();
		if(a.getUserData() instanceof Lifeform && b.getUserData() instanceof Lifeform) {// && impulse.normalImpulses[0] > ATTACKING_THRESHOLD) {
			Lifeform lifeA = (Lifeform) a.getUserData();
			Lifeform lifeB = (Lifeform) b.getUserData();
			if(!lifeA.isCloseTo(lifeB)) {
				int forceA = lifeA.gene.getFactor(Factor.POWER);
				int forceB = lifeB.gene.getFactor(Factor.POWER);
				float impul = impulse.normalImpulses[0] / 10;
				
				float powerA = impul * forceA / 3.0f * a.getBody().getLinearVelocity().length();
				float powerB = impul * forceB / 3.0f * b.getBody().getLinearVelocity().length();
				
				
				if(powerA > powerB) {
					float damage = powerA - powerB;
					lifeA.heal(lifeB, damage);
					if(lifeB.damage(lifeA, damage)) {
						deadLives.add(lifeB);
					}
					if(lifeB.isBreeding()) {
						lifeB.getBreedingPair().cancelBreeding();
					}
				} else if(powerB > powerA) {
					float damage = powerB - powerA;
					if(lifeA.damage(lifeB, damage)) {
						deadLives.add(lifeA);
					}
					lifeB.heal(lifeA, damage);
					if(lifeA.isBreeding()) {
						lifeA.getBreedingPair().cancelBreeding();
					}
				}
			}
		}
	}
	
	public void addSimulationListener(SimulationListener listener) {
		if(listener != null) {
			synchronized(listeners) {
				if(!listeners.contains(listener)) {
					listeners.add(listener);
				}
			}
		} else {
			throw new NullPointerException();
		}
	}
	
	public void removeSimulationListener(SimulationListener listener) {
		synchronized(listeners) {
			listeners.remove(listener);
		}
	}
	
	static class LifeSearchResult {
		public final Lifeform life;
		public final float[] angle;
		public final float[] preference;
		public final int length;
		
		public LifeSearchResult(Lifeform life, float[] angle, float[] preference, int length) {
			this.life = life;
			this.angle = angle;
			this.preference = preference;
			this.length = length;
		}
	}
	
	private class LifeformTick implements Callable<LifeSearchResult> {
		
		private final Lifeform life;
		private CountDownLatch latch;
		
		public LifeformTick(Lifeform life, CountDownLatch latch) {
			this.life = life;
			this.latch = latch;
		}
		
		@Override
		public LifeSearchResult call() throws Exception {
			LifeSearchResult result = life.tick();
			latch.countDown();
			return result;
		}
	}
	
	private class LifeformCalculate implements Callable<Lifeform> {
		
		private final LifeSearchResult lastResult;
		private final float randomProb;
		
		public LifeformCalculate(LifeSearchResult lastResult, float randomProb) {
			this.lastResult = lastResult;
			this.randomProb = randomProb;
		}
		
		@Override
		public Lifeform call() throws Exception {
			lastResult.life.applyMove(lastResult, randomProb);
			return lastResult.life;
		}
	}
	
	private static class SimulationThreadFactory implements ThreadFactory {
		
		private static AtomicInteger ID = new AtomicInteger(1);
		
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r, "Simulator Thread " + ID.getAndIncrement());
			t.setPriority(Thread.NORM_PRIORITY + 2);
			return t;
		}
	}
}
