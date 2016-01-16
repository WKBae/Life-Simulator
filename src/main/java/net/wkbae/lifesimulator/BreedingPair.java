package net.wkbae.lifesimulator;

import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.DistanceJointDef;
import org.jbox2d.dynamics.joints.Joint;

class BreedingPair {
	//private final static int BREEDING_TICKS = 20;

	private final World world;
	public final Lifeform one, two;
	public final int loop;
	private int ticksLeft;
	//private BreedingPair next;
	private boolean cancel = false;
	
	private Joint joint;
	
	public BreedingPair(World world, int loop, Lifeform one, Lifeform two) {
		this.world = world;
		this.loop = loop;
		this.one = one;
		this.two = two;
		ticksLeft = (one.gene.getBreedingTicks() + two.gene.getBreedingTicks()) / 2;
	}
	
	public void startBreeding() {

		DistanceJointDef jdef = new DistanceJointDef();
		jdef.bodyA = one.getBody();
		jdef.bodyB = two.getBody();
		jdef.localAnchorA.set(0, 0);
		jdef.localAnchorB.set(0, 0);
		jdef.length = one.getSize() + two.getSize();
		jdef.collideConnected = false;
		this.joint = world.createJoint(jdef);
		
		one.startBreeding(this);
		two.startBreeding(this);
	}
	
	public void cancelBreeding() {
		this.cancel = true;
	}
	
	public boolean isCancelled() {
		return cancel;
	}
	
	public void endBreeding(boolean success) {
		world.destroyJoint(joint);
		joint = null;
		one.endBreeding(success);
		two.endBreeding(success);
	}

	public int getTicksLeft() {
		return ticksLeft;
	}
	
	/**
	 * 
	 * @return <code>true</code>면 분열 끝
	 */
	public boolean tick() {
		boolean oneAlive = one.liveTick();
		boolean twoAlive = two.liveTick();
		if(!oneAlive || !twoAlive) {
			cancelBreeding();
			return true;
		}
		return --ticksLeft == 0; // TODO
	}

	/*public void setNext(LifeformPair nextPair) {
		this.next = nextPair;
	}

	public LifeformPair getNext() {
		return next;
	}*/

	/*public void cancelBreeding() {
		one.endBreeding();
		two.endBreeding();
		cancelled = true;
		breedingCancelled = true;
	}
	public boolean isCancelled() {
		return cancelled;
	}*/
}
