package net.wkbae.lifesimulator;

import static net.wkbae.lifesimulator.Gene.Factor.BOUNCY;
import static net.wkbae.lifesimulator.Gene.Factor.BREED_FREQUENCY;
import static net.wkbae.lifesimulator.Gene.Factor.BREED_SPEED;
import static net.wkbae.lifesimulator.Gene.Factor.COLOR;
import static net.wkbae.lifesimulator.Gene.Factor.ENERGY;
import static net.wkbae.lifesimulator.Gene.Factor.LIFESPAN;
import static net.wkbae.lifesimulator.Gene.Factor.METABOLISM;
import static net.wkbae.lifesimulator.Gene.Factor.MOVE_FREQUENCY;
import static net.wkbae.lifesimulator.Gene.Factor.POWER;
import static net.wkbae.lifesimulator.Gene.Factor.SIGHT;
import static net.wkbae.lifesimulator.Gene.Factor.SIZE;
import static net.wkbae.lifesimulator.Gene.Factor.SPEED;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Random;

import com.badlogic.gdx.math.MathUtils;

public class Gene implements Serializable {
	private static final long serialVersionUID = -114320637006733605L;

	// (크기, 탄성) (속도, 번식 속도, 대사량(비례)) (시야, 이동 빈도) (수명, 번식 빈도) (에너지, 힘) (색)
	public static enum Factor {
		SIZE(5), BOUNCY(5),
		SPEED(4), BREED_SPEED(4), METABOLISM(4),
		ENERGY(4), POWER(4),
		SIGHT(4), MOVE_FREQUENCY(4),
		LIFESPAN(3), BREED_FREQUENCY(3),
		COLOR(12);
		
		private final int factorLength;
		Factor(int factorLength) {
			this.factorLength = factorLength;
		}
		
		/*public Factor getLinkedFactor() {
			switch(this) {
			case SIZE:
				return BOUNCY;
			case BOUNCY:
				return SIZE;
			case SPEED:
				return METABOLISM;
			case METABOLISM:
				return SPEED;
			case ENERGY:
				return POWER;
			case POWER:
				return ENERGY;
			case SIGHT:
				return MOVE_FREQUENCY;
			case MOVE_FREQUENCY:
				return SIGHT;
			case LIFESPAN:
				return BREED_FREQUENCY;
			case BREED_FREQUENCY:
				return LIFESPAN;
			default:
				return this;
			}
		}*/
		
		public int getOppositeValue(int value) {
			/*
			1 << factorLength(5) = 0b100000
			0b100000 - 1 = 0b11111
			value(10110) ~ = 0b1...101001
			~value & 0b11111 = 0b01001
			*/
			return (~value) & ((1 << factorLength) - 1);
		}
	}
	
	private EnumMap<Factor, Integer> factors = new EnumMap<>(Factor.class);
	
	public Gene(int sizeFactor, int speedFactor, int energyFactor, int sightFactor, int lifespanFactor, int colorFactor) {
		factors.put(SIZE, sizeFactor);
		factors.put(BOUNCY, SIZE.getOppositeValue(sizeFactor));
		factors.put(SPEED, speedFactor);
		factors.put(BREED_SPEED, speedFactor);
		factors.put(METABOLISM, speedFactor);
		factors.put(ENERGY, energyFactor);
		factors.put(POWER, POWER.getOppositeValue(energyFactor));
		factors.put(SIGHT, sightFactor);
		factors.put(MOVE_FREQUENCY, SIGHT.getOppositeValue(sightFactor));
		factors.put(LIFESPAN, lifespanFactor);
		factors.put(BREED_FREQUENCY, LIFESPAN.getOppositeValue(lifespanFactor));
		factors.put(COLOR, colorFactor);
	}
	
	public int getFactor(Factor factor) {
		return factors.get(factor);
	}
	
	public int getColor() {
		return getFactor(COLOR) & 0xFFF;
	}
	
	public int getMaxEnergy() {
		return getFactor(ENERGY) * 50;
	}
	
	public int getSight() {
		return getFactor(SIGHT) * 5;
	}
	
	public int getLifespan() {
		return getFactor(LIFESPAN) * 30;
	}
	
	public float getSize() {
		return getFactor(SIZE) / 10.0f;
	}
	
	public int getSpeed() {
		return getFactor(SPEED) * 2 / 3 + 5;
	}
	
	private int cooldown = -1;
	
	public int getMovingCooldown() {
		if(cooldown == -1) {
			cooldown = MathUtils.round(getFactor(MOVE_FREQUENCY) * SimulationSetting.calculatesPerSecond * 1.0f / MOVE_FREQUENCY.getOppositeValue(1));
		}
		return cooldown;
		//return getFactor(MOVE_FREQUENCY);
	}
	
	public int getBreedingCooldown() {
		return getFactor(BREED_FREQUENCY) * 4;
	}
	
	public int getBreedingTicks() {
		return getFactor(BREED_SPEED) + 5;
	}
	
	public float getMetabolism() {
		return getFactor(METABOLISM) / 2.0f;
	}
	
	public static Gene random(Random rand) {
		return new Gene(rand.nextInt((1 << 5) - 3) + 1, // (100,000 - 1) => 11,111
						rand.nextInt((1 << 4) - 3) + 1, // 11,111 - 2 = 11,101
						rand.nextInt((1 << 4) - 3) + 1, // + minimum 00,001 = maximum 11,110
						rand.nextInt((1 << 3) - 3) + 1,
						rand.nextInt((1 << 3) - 3) + 1,
						rand.nextInt((1 << 12) - 3) + 1);
	}
	
	public static Gene crossover(Random rand, Gene one, Gene two, float mutationRatio) {
		int random = rand.nextInt(2); // 0 / 1
		Gene first = (random == 1)? one : two;
		Gene second = (random == 1)? two : one;
		
		float mutation;
		
		random = rand.nextInt(6);
		int crossFactor = (1 << random) - 1; // 0b00000, 0b00001, 0b00011, 0b00111, 0b01111, 0b11111
		// one_size(0b01010) & crossFactor(0b00111) = 0b00|010|
		// two_size(0b11001) & ~crossFactor(0b11000) = 0b|11|000
		// 0b00010 | 0b11000 = 0b11010
		int size = (first.getFactor(SIZE) & crossFactor) | (second.getFactor(SIZE) & (~crossFactor));
		if(size == 0) {
			size = 1;
		} else if(size == 0b11111) {
			size = 0b11110;
		}
		mutation = rand.nextFloat();
		if(mutation <= mutationRatio) {
			size = ~size & ((1<<5) - 1);
		}
		
		random = rand.nextInt(5);
		crossFactor = (1 << random) - 1;
		int speed = (first.getFactor(SPEED) & crossFactor) | (second.getFactor(SPEED) & (~crossFactor));
		if(speed == 0) {
			speed = 1;
		} else if(speed == 0b1111) {
			speed = 0b1110;
		}
		mutation = rand.nextFloat();
		if(mutation <= mutationRatio) {
			speed = ~speed & ((1<<4) - 1);
		}
		
		random = rand.nextInt(5);
		crossFactor = (1 << random) - 1;
		int energy = (first.getFactor(ENERGY) & crossFactor) | (second.getFactor(ENERGY) & (~crossFactor));
		if(energy <= 0) {
			energy = 1;
		} else if(energy >= 0b1110) {
			energy = 0b1110;
		}
		mutation = rand.nextFloat();
		if(mutation <= mutationRatio) {
			energy = ~energy & ((1<<4) - 1);
		}

		random = rand.nextInt(4);
		crossFactor = (1 << random) - 1;
		int sight = (first.getFactor(SIGHT) & crossFactor) | (second.getFactor(SIGHT) & (~crossFactor));
		if(sight <= 0) {
			sight = 1;
		} else if(sight >= 0b110) {
			sight = 0b110;
		}
		mutation = rand.nextFloat();
		if(mutation <= mutationRatio) {
			sight = ~sight & ((1<<3) - 1);
		}
		
		random = rand.nextInt(4);
		crossFactor = (1 << random) - 1;
		int lifespan = (first.getFactor(LIFESPAN) & crossFactor) | (second.getFactor(LIFESPAN) & (~crossFactor));
		if(lifespan <= 0) {
			lifespan = 1;
		} else if(lifespan >= 0b110) {
			lifespan = 0b110;
		}
		mutation = rand.nextFloat();
		if(mutation <= mutationRatio) {
			lifespan = ~lifespan & ((1<<3) - 1);
		}
		
		int firstColor = first.getFactor(COLOR);
		int fR = (firstColor >> 8) & 0xF;
		int fG = (firstColor >> 4) & 0xF;
		int fB = firstColor & 0xF;
		
		int secondColor = second.getFactor(COLOR);
		int sR = (secondColor >> 8) & 0xF;
		int sG = (secondColor >> 4) & 0xF;
		int sB = secondColor & 0xF;
		
		int nR = (fR + sR) / 2;
		int nG = (fG + sG) / 2;
		int nB = (fB + sB) / 2;
		mutation = rand.nextFloat();
		if(mutation <= mutationRatio / 10) {
			// (r - nR)^2 + (g - nG)^2 + (b - nB)^2 < CLOSE_THRESHOLD
			float newR = (rand.nextFloat() * CLOSE_SINGLE_RADIUS * 2 - CLOSE_SINGLE_RADIUS) + nR;
			
			float newRadius = (float) Math.sqrt(CLOSE_THRESHOLD - newR * newR);
			float newG = (rand.nextFloat() * newRadius * 2 - newRadius) + nG;
			
			newRadius = (float) Math.sqrt(newRadius - newG * newG);
			float newB = (rand.nextFloat() * newRadius * 2 - newRadius) + nB;
			
			nR = (int) newR;
			nG = (int) newG;
			nB = (int) newB;
		}
		int color = (nR << 8) | (nG << 4) | nB;
		return new Gene(size, speed, energy, sight, lifespan, color);
	}
	
	
	private final static int CLOSE_THRESHOLD = 8000;
	private final static float CLOSE_SINGLE_RADIUS = (float) Math.sqrt(CLOSE_THRESHOLD);
	public static boolean isCloseTo(int colorOne, int colorTwo) {
		int thisR = (colorOne >> 8) & 0xF;
		thisR = thisR | (thisR << 4);
		int thisG = (colorOne >> 4) & 0xF;
		thisG = thisG | (thisG << 4);
		int thisB = colorOne & 0xF;
		thisB = thisB | (thisB << 4);
		
		int thatR = (colorTwo >> 8) & 0xF;
		thatR = thatR | (thatR << 4);
		int thatG = (colorTwo >> 4) & 0xF;
		thatG = thatG | (thatG << 4);
		int thatB = colorTwo & 0xF;
		thatB = thatB | (thatB << 4);
		
		// if( (thisR & thatR) > 0b1100 ) {}
		/*if((MathUtils.abs(thisR - thatR) < ALLOWED_COLOR_DIFFER) && (MathUtils.abs(thisG - thatG) < ALLOWED_COLOR_DIFFER) && (MathUtils.abs(thisB - thatB) < ALLOWED_COLOR_DIFFER)) {
			return true;
		} else {
			return false;
		}*/
		//double diff = Colors.colorDistanceSquared(thisR, thisG, thisB, thatR, thatG, thatB);
		int r = thisR - thatR;
		int g = thisG - thatG;
		int b = thisB - thatB;
		long diff = r*r + g*g + b*b;
		if(diff < CLOSE_THRESHOLD) {
			return true;
		} else {
			return false;
		}
	}
	
	/*public static Gene mutate(Random rand, Gene gene) {
		float ratio = Simulation.Setting.mutationRatio;
		
		int size = gene.getFactor(SIZE);
		if(rand.nextFloat() < ratio) {
			size = ~size & 0b11111;
		}
		
		int speed = gene.getFactor(SPEED);
		if(rand.nextFloat() < ratio) {
			speed = ~speed & 0b1111;
		}
		
		int energy = gene.getFactor(ENERGY);
		if(rand.nextFloat() < ratio) {
			energy = ~energy & 0b1111;
		}
		
		int sight = gene.getFactor(SIZE);
		if(rand.nextFloat() < ratio) {
			sight = ~sight & 0b111;
		}
		
		int lifespan = gene.getFactor(SIZE);
		if(rand.nextFloat() < ratio) {
			lifespan = ~lifespan & 0b111;
		}
		
		int color = gene.getFactor(COLOR);
		if(rand.nextFloat() < ratio) {
			color = ~color & ((1 << 12) - 1);
		}
		
		return new Gene(size, speed, energy, sight, lifespan, color);
	}*/
}
