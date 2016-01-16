package net.wkbae.lifesimulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.wkbae.lifesimulator.Simulation.LifeSearchResult;

import org.jbox2d.callbacks.QueryCallback;
import org.jbox2d.collision.AABB;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;

public class Lifeform {
	private final static float DEFAULT_PREFERENCE = 1;
	private final static float DEFAULT_ALIKE_PREFERENCE = 3;
	private final static float[] ANGLE_PI = {
		0,
		MathUtils.QUARTER_PI,
		MathUtils.HALF_PI,
		MathUtils.HALF_PI + MathUtils.QUARTER_PI,
		MathUtils.PI,
		MathUtils.PI + MathUtils.QUARTER_PI,
		MathUtils.PI + MathUtils.HALF_PI,
		MathUtils.PI + MathUtils.HALF_PI + MathUtils.QUARTER_PI
	};
	private final static float ROOT2 = 1.4142136f;
	
	private final static boolean ENABLE_LIFESPAN = true;
	
	//private final static int BREEDING_COOLDOWN = 20;
	//public final int ancestorId;
	public final Gene gene;
	public Map<Integer, Float> preference;
	//private Body attachedBody;
	private Fixture fixture;
	private Body body;
	private World world;
	
	private final int color;
	private final int sight;
	private int lifespan;
	//private final int sightSq;
	private final int youngLifespan;
	private final int oldLifespan;
	
	private final int maxEnergy;
	private float energy;
	
	private final float size;
	//private int power;
	private final int speed;
	// 속도를 루트2로 나눈 값 - 대각선(45도) 방향의 속도를 구할 때 사용
	private final float speedDivRoot2;
	
	private boolean breeding = false;
	
	private int breedingCooldown;// = BREEDING_COOLDOWN;
	
	Lifeform(Gene gene, Map<Integer, Float> defaultPreference) {
		//this.ancestorId = ancestorId;
		this.gene = gene;
		
		color = gene.getColor();
		
		maxEnergy = gene.getMaxEnergy();
		energy = maxEnergy * 0.75f;
		//this.power = gene.getFactor(Factor.POWER);
		
		sight = gene.getSight();
		//sightSq = sight * sight;
		
		lifespan = gene.getLifespan();
		youngLifespan = lifespan - (int)(lifespan * 1 / 20.0); // 5/100
		oldLifespan = lifespan - (int)(lifespan * 9 / 10.0); // 90/100
		
		size = gene.getSize();
		
		speed = gene.getSpeed();
		speedDivRoot2 = speed / ROOT2;
		
		if(defaultPreference == null) {
			this.preference = new TreeMap<>();
		} else {
			this.preference = new TreeMap<>(defaultPreference);
		}
		
	}
	
	public boolean canBreed() {
		//System.out.println("canBreed: " + lifespan + " -> " + youngLifespan + " ~ " + oldLifespan + " = " + (lifespan < youngLifespan && lifespan > oldLifespan)); // TODO not breeding!!
		return !breeding && breedingCooldown == 0 && lifespan < youngLifespan && lifespan > oldLifespan;
	}
	
	private BreedingPair breedingPair;
	void startBreeding(BreedingPair pair) {
		breedingPair = pair;
		body.m_linearVelocity.set(0, 0);
		this.breeding = true;
		this.breedingCooldown = gene.getBreedingCooldown();
	}
	
	void endBreeding(boolean successful) {
		Lifeform other = breedingPair.one;
		if(other == this) {
			other = breedingPair.two;
		}
		
		if(successful) {
			Float pref = preference.get(other.getColor());
			if(pref == null) {
				pref = DEFAULT_ALIKE_PREFERENCE;
			}
			preference.put(other.getColor(), pref + 0.5f);
		}
		
		if(body != null && other.getBody() != null) {
			Vec2 diff = body.getPosition().sub(other.getBody().getPosition()); // to length 1 & * speed -> impulse
			diff.normalize();
			body.applyLinearImpulse(diff.mulLocal(speed), body.getWorldCenter());
		}
		
		breedingPair = null;
		this.breeding = false;
	}
	
	public boolean isBreeding() {
	 	return breeding;
	}
	
	public BreedingPair getBreedingPair() {
		return breedingPair;
	}
	
	public int getBreedingCooldown() {
		return breedingCooldown;
	}
	
	public int getSight() {
		return sight;
	}
	
	public int getMaxEnergy() {
		return maxEnergy;
	}
	
	public float getEnergy() {
		return energy;
	}
	
	public int getSpeed() {
		return speed;
	}
	
	synchronized void destroy() {
		if(body != null) {
			synchronized (world) {
				world.destroyBody(body);
				this.body = null;
				this.fixture = null;
				this.dead = true;
			}
		}
	}
	
	/**
	 * 
	 * @return <code>false</code>면 사망
	 */
	boolean liveTick() {
		if(ENABLE_LIFESPAN) {
			lifespan--;
		}
		
		energy -= gene.getMetabolism();
		
		if(lifespan <= 0 || energy <= 0) {
			dead = true;
			return false;
		}
		return true;
	}
	
	LifeSearchResult tick() {
		if(breedingCooldown > 0) {
			breedingCooldown--;
		}
		
		if(!liveTick()) {
			return new Simulation.LifeSearchResult(this, null, null, -1);
		}
		
		List<Lifeform> foundLives = new LinkedList<>();
		Vec2 baseLoc = body.getPosition();
		Vec2 sightVec = new Vec2(sight, sight).mulLocal(0.5f);
		synchronized (world) {
			world.queryAABB(new SightListener(foundLives, baseLoc), new AABB(baseLoc.sub(sightVec), baseLoc.add(sightVec)));
		}
		
		//List<Float> angles = new ArrayList<>(foundLives.size());
		//List<Float> rangedPrefs = new ArrayList<>(foundLives.size());
		
		float[] angle = new float[foundLives.size()];
		float[] prefer = new float[foundLives.size()];
		
		int i = 0;
		for(Lifeform life : foundLives) {
			Vec2 relLoc;
			//synchronized (life) {
				if(life.getBody() == null) continue;
				relLoc = life.getBody().getPosition().sub(baseLoc);
			//}
			float angl = MathUtils.atan2(relLoc.y, relLoc.x);
			
			float dist = relLoc.length();
			float pref = getPreference(life.getColor()) * ((energy / maxEnergy <= 0.1)? 5 : 1);
			float ranged = PROBABILITY_CONSTANT * pref / (dist * dist); // 선호도에 비례, 거리의 제곱에 반비례
			
			angle[i] = angl;
			prefer[i] = ranged;
			i++;
			//angles.add(angle);
			//rangedPrefs.add(ranged);
		}
		
		return new Simulation.LifeSearchResult(this, angle, prefer, i);
		//float[] angle = new float[angles.size()];
		//float[] pref = new float[rangedPrefs.size()];
		
		/*for (int i = 0; i < angles.size(); i++) {
			Float f = angles.get(i);
		    angle[i] = (f != null ? f : 0);
		    
		    f = rangedPrefs.get(i);
		    pref[i] = (f != null ? f : 0);
		}*/
		
		//return new float[][]{angle, prefer};
	}
	
	private final static float EXPONENTIAL_BASE = 1.2f; // 확률 지수의 밑
	private final static float EXPONENTIAL_CONSTANT = 2.0f; // 확률 지수의 상수(계수)
	private final static float PROBABILITY_CONSTANT = 2.0f; // [선호도 / (거리^2)]의 비례 상수
	void applyMove(LifeSearchResult result, float randomProb) {
		
		float[] direction = new float[]{0, 0, 0, 0, 0, 0, 0, 0};
		
		for(int i = 0; i < result.length; i++) {
			float angle = result.angle[i];
			float ranged = result.preference[i];
			
			if(angle < 0) {
				angle = MathUtils.PI + angle;
			}
			
			if(angle == ANGLE_PI[0]) {
				direction[0] += ranged;
				continue;
			} else if(angle == ANGLE_PI[1]) {
				direction[1] += ranged;
				continue;
			} else if(angle == ANGLE_PI[2]) {
				direction[2] += ranged;
				continue;
			} else if(angle == ANGLE_PI[3]) {
				direction[3] += ranged;
				continue;
			} else if(angle == ANGLE_PI[4]) {
				direction[4] += ranged;
				continue;
			} else if(angle == ANGLE_PI[5]) {
				direction[5] += ranged;
				continue;
			} else if(angle == ANGLE_PI[6]) {
				direction[6] += ranged;
				continue;
			} else if(angle == ANGLE_PI[7]) {
				direction[7] += ranged;
				continue;
			}
			
			byte angle1, angle2; // angle1, angle2 < 8
			float diff1, diff2; // diff2 > diff1
			if(angle < ANGLE_PI[4]) {
				if(angle < ANGLE_PI[2]) {
					if(angle < ANGLE_PI[1]) { // ANGLE_PI[0] < angle < ANGLE_PI[1]
						diff1 = angle - ANGLE_PI[0];
						diff2 = ANGLE_PI[1] - angle;
						angle1 = 0;
						angle2 = 1;
					} else { // ANGLE_PI[1] < angle < ANGLE_PI[2]
						diff1 = angle - ANGLE_PI[1];
						diff2 = ANGLE_PI[2] - angle;
						angle1 = 1;
						angle2 = 2;
					}
				} else {
					if(angle < ANGLE_PI[3]) { // ANGLE_PI[2] < angle < ANGLE_PI[3]
						diff1 = angle - ANGLE_PI[2];
						diff2 = ANGLE_PI[3] - angle;
						angle1 = 2;
						angle2 = 3;
					} else { // ANGLE_PI[3] < angle < ANGLE_PI[4]
						diff1 = angle - ANGLE_PI[3];
						diff2 = ANGLE_PI[4] - angle;
						angle1 = 3;
						angle2 = 4;
					}
				}
			} else {
				if(angle < ANGLE_PI[6]) {
					if(angle < ANGLE_PI[5]) { // ANGLE_PI[4] < angle < ANGLE_PI[5]
						diff1 = angle - ANGLE_PI[4];
						diff2 = ANGLE_PI[5] - angle;
						angle1 = 4;
						angle2 = 5;
					} else { // ANGLE_PI[5] < angle < ANGLE_PI[6]
						diff1 = angle - ANGLE_PI[5];
						diff2 = ANGLE_PI[6] - angle;
						angle1 = 5;
						angle2 = 6;
					}
				} else {
					if(angle < ANGLE_PI[7]) { // ANGLE_PI[6] < angle < ANGLE_PI[7]
						diff1 = angle - ANGLE_PI[6];
						diff2 = ANGLE_PI[7] - angle;
						angle1 = 6;
						angle2 = 7;
					} else { // ANGLE_PI[7] < angle < ANGLE_PI[8](=PI*2)
						diff1 = angle - ANGLE_PI[7];
						diff2 = MathUtils.TWOPI - angle;
						angle1 = 7;
						angle2 = 0;
					}
				}
			}
			float rev1 = 1 / diff1;
			float rev2 = 1 / diff2;
			float divSum = ranged / (rev1 + rev2);
			direction[angle1] += divSum * rev1;
			direction[angle2] += divSum * rev2;
		}
		
		// 각각 방향의 선호도 수치를 지수로  사용(음수 선호도)
		for(int i = 0; i <= 7; i++) {
			direction[i] = EXPONENTIAL_CONSTANT * MathUtils.fastPow(EXPONENTIAL_BASE, direction[i]);//MathUtils.fastPow(2, direction[i]);
		}
		
		// 룰렛 방식 - 전체를 1로 보고 4가지 경우의 확률을 각각 0.2, 0.4, 0.3, 0.1 로 정한 뒤 0~1 사이의 수를 무작위로 선택, 0.2보다 작으면 1, 0.6(누적)보다 작으면 2, 0.9보다 작으면 3, ..
		float prob = randomProb * (direction[0] + direction[1] + direction[2] + direction[3] + direction[4] + direction[5] + direction[6] + direction[7]);
		
		Vec2 impulse = new Vec2();
		probLoop:
		for(int i = 0; i <= 7; i++) {
			if(prob <= direction[i]) {
				switch(i) {
				case 0:
					impulse.addLocal(speed, 0); // 동
					break probLoop;
				case 1:
					impulse.addLocal(speedDivRoot2, speedDivRoot2); // 남동
					break probLoop;
				case 2:
					impulse.addLocal(0, speed); // 남
					break probLoop;
				case 3:
					impulse.addLocal(-speedDivRoot2, speedDivRoot2); // 남서
					break probLoop;
				case 4:
					impulse.addLocal(-speed, 0); // 서
					break probLoop;
				case 5:
					impulse.addLocal(-speedDivRoot2, -speedDivRoot2); // 북서
					break probLoop;
				case 6:
					impulse.addLocal(0, -speed); // 북
					break probLoop;
				case 7:
					impulse.addLocal(speedDivRoot2, -speedDivRoot2); // 북동
					break probLoop;
				}
			} else {
				prob -= direction[i];
			}
		}
		
		body.applyLinearImpulse(impulse, body.getWorldCenter());
	}
	
	private final static float DISKILE_CONSTANT = 2.0f;
	
	private boolean dead = false;
	public boolean damage(Lifeform from, float amount) {
		energy -= amount;
		
		for(LifeformEnergyListener listener : listeners) {
			listener.onEnergyChanged(this, energy);
		}
		
		if(energy <= 0) {
			dead = true;
			return true;
		} else {
			int color = from.getColor();
			Float pref = preference.get(color);
			if(pref == null) {
				pref = DEFAULT_PREFERENCE;
			}
			preference.put(color, pref - amount*DISKILE_CONSTANT);
			
			return false;
		}
	}
	
	public void heal(Lifeform from, float amount) {
		double added = energy + amount;
		
		if(added > maxEnergy) {
			energy = maxEnergy;
		} else {
			energy = (float) added;
		}

		for(LifeformEnergyListener listener : listeners) {
			listener.onEnergyChanged(this, energy);
		}
		
		int color = from.getColor();
		Float pref = preference.get(color);
		if(pref == null) {
			pref = DEFAULT_PREFERENCE;
		}
		preference.put(color, pref + amount);
	}
	
	public boolean isDead() {
		return dead;
	}
	
	public int getColor() {
		return color;
	}
	
	public float getSize() {
		return size;
	}
	
	public Fixture getFixture() {
		return fixture;
	}
	
	public Body getBody() {
		return body;
	}
	
	public void setFixture(Fixture fixture) {
		this.fixture = fixture;
		this.body = fixture.getBody();
		this.world = body.getWorld();
	}
	
	public float getPreference(int color) {
		if(Gene.isCloseTo(this.color, color) && breedingCooldown > 0) {
			return 0;
		}
		
		Float res = preference.get(color);
		if(res == null) {
			double total = 0;
			int count = 0;
			for(Entry<Integer, Float> entry : preference.entrySet()) {
				if(Gene.isCloseTo(color, entry.getKey())) {
					total += entry.getValue();
					count++;
				}
			}
			if(count > 0) {
				preference.put(color, (float)(total / count));
			} else {
				if(Gene.isCloseTo(this.color, color)) {
					preference.put(color, DEFAULT_ALIKE_PREFERENCE);
				} else {
					preference.put(color, DEFAULT_PREFERENCE);
				}
			}
			res = preference.get(color);
		}
		//System.out.println("pref: " + res);
		return res;
	}
	
	public Map<Integer, Float> getAllPreferences() {
		return Collections.unmodifiableMap(preference);
	}
	
	public boolean isCloseTo(Lifeform other) {
		return Gene.isCloseTo(this.color, other.getColor());
	}
	
	private ArrayList<LifeformEnergyListener> listeners = new ArrayList<>();
	public void addEnergyListener(LifeformEnergyListener listener) {
		listeners.add(listener);
	}
	public void removeEnergyListener(LifeformEnergyListener listener) {
		listeners.remove(listener);
	}
	
	/*private static int abs(int num) { use MathUtils.abs (bit operators)
		if(num < 0) {
			return -num;
		} else {
			return num;
		}
	}*/
	
	private class SightListener implements QueryCallback {
		private List<Lifeform> found;
		private Vec2 baseLoc;
		
		SightListener(List<Lifeform> found, Vec2 baseLoc) {
			this.found = found;
			this.baseLoc = baseLoc;
		}
		
		@Override
		public boolean reportFixture(Fixture fixture) {
			if(!(fixture.getUserData() instanceof Lifeform)) {
				return true;
			}
			Lifeform other = (Lifeform)fixture.getUserData();
			if(other == Lifeform.this) {
				return true;
			}
			//System.out.println("found");
			float dist = MathUtils.distance(fixture.getBody().getPosition(), baseLoc);
			dist -= other.getSize();
			if(dist <= sight) {
				found.add(other);
			}
			return true;
		}
	}
	
	public static interface LifeformEnergyListener {
		public void onEnergyChanged(Lifeform life, float energy);
	}
}
