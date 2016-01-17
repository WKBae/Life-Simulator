package net.wkbae.lifesimulator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.TreeMap;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.LoggerFactory;

public class SimulationSetting implements Serializable {
	private static final long serialVersionUID = 1044723435288144383L;

	private long seed;
	
	private int worldSize = 256;
	private transient int displaySize = 256;
	
	private transient float displayRatio = displaySize / worldSize;
	
	private transient volatile int frameRate = 30;
	
	private int lifeformAmount = 50;
	
	private transient volatile float simulationSpeed = 1.0f; // 속도의 역수 -> 0.5배 = 1/(1/2) = 2
	
	private float mutationRatio = 0.10f;
	
	public final static int calculatesPerSecond = 60; // must be >1 & even number
	public final static float timeStep = 1.0f / calculatesPerSecond;
	
	private float damping = 0.5f;
	
	private transient boolean record = false;
	
	private TreeMap<Long, LifeformCreationData[]> lifeformCreations = new TreeMap<>();
	
	private transient boolean simulationStarted = false;
	
	void setSimulationStarted(boolean started) {
		this.simulationStarted = started;
	}
	
	public boolean isSimulationStarted() {
		return simulationStarted;
	}
	
	public void setSeed(long seed) {
		if(simulationStarted) throw new IllegalStateException("Simulation settings cannot be changed while the simulation is running.");
		this.seed = seed;
	}

	public void setWorldSize(int worldSize) {
		if(simulationStarted) throw new IllegalStateException("Simulation settings cannot be changed while the simulation is running.");
		this.worldSize = worldSize;
	}

	public void setDisplaySize(int displaySize) {
		this.displaySize = displaySize;
		this.displayRatio = 1.0f * displaySize / worldSize;
	}

	public void setFrameRate(int frameRate) {
		this.frameRate = frameRate;
	}

	public void setLifeformAmount(int amount) {
		if(simulationStarted) throw new IllegalStateException("Simulation settings cannot be changed while the simulation is running.");
		this.lifeformAmount = amount;
	}

	public void setSimulationSpeed(float simulationSpeed) {
		this.simulationSpeed = simulationSpeed;
	}
	
	public void setMutationRatio(float mutationRatio) {
		if(simulationStarted) throw new IllegalStateException("Simulation settings cannot be changed while the simulation is running.");
		this.mutationRatio = mutationRatio;
	}
	
	public void setDamping(float damping) {
		if(simulationStarted) throw new IllegalStateException("Simulation settings cannot be changed while the simulation is running.");
		this.damping = damping;
	}

	public void setRecord(boolean record) {
		if(simulationStarted) throw new IllegalStateException("Simulation settings cannot be changed while the simulation is running.");
		this.record = record;
	}
	
	public void newLifeform(long loopCount, LifeformCreationData data) {
		LifeformCreationData[] origData = lifeformCreations.get(loopCount);
		
		LifeformCreationData[] newData;
		if(origData == null) {
			newData = new LifeformCreationData[1];
			newData[0] = data;
		} else {
			newData = Arrays.copyOf(origData, origData.length + 1);
			newData[origData.length] = data;
		}
		
		lifeformCreations.put(loopCount, newData);
	}

	public long getSeed() {
		return seed;
	}

	public int getWorldSize() {
		return worldSize;
	}

	public int getDisplaySize() {
		return displaySize;
	}

	public float getDisplayRatio() {
		return displayRatio;
	}

	public int getFrameRate() {
		return frameRate;
	}
	
	public int getLifeformAmount() {
		return lifeformAmount;
	}
	
	public float getSimulationSpeed() {
		return simulationSpeed;
	}
	
	public float getMutationRatio() {
		return mutationRatio;
	}
	
	public float getDamping() {
		return damping;
	}
	
	public boolean isRecording() {
		return record;
	}
	
	public LifeformCreationData[] getCreatedLifeform(long loopCount) {
		return lifeformCreations.get(loopCount);
	}
	
	public byte[] asBinary() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(this);
			oos.flush();
			oos.close();
		} catch(IOException e) {
			LoggerFactory.getLogger(SimulationSetting.class).warn("Cannot encode SimulationSetting", e);
			System.out.println("err" + e);
			return null;
		} finally {
			if(oos != null) {
				try {
					oos.close();
				} catch (IOException e) {}
			}
		}
		
		return baos.toByteArray();
	}
	
	public static SimulationSetting decode(byte[] data) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		
		ObjectInputStream ois = new ObjectInputStream(bais);
		try {
			Object obj = ois.readObject();
			if(obj instanceof SimulationSetting) {
				return (SimulationSetting) obj;
			} else {
				throw new IOException("알 수 없는 객체입니다.");
			}
		} catch(ClassNotFoundException e) {
			throw new IOException("알 수 없는 객체입니다.", e);
		} finally {
			ois.close();
		}
	}
	
	public String encode() {
		byte[] data = asBinary();
		String base = Base64.encodeBase64String(data);
		//System.out.println("SEED: \"" + base + "\"");
		return base;
	}
	
	public static SimulationSetting decode(String encoded) throws IOException {
		if(!Base64.isBase64(encoded)) {
			throw new IllegalArgumentException("올바르지 않은 구문입니다.");
		} else {
			byte[] data = Base64.decodeBase64(encoded);
			return decode(data);
		}
	}
}
