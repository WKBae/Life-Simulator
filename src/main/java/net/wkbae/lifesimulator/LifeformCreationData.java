package net.wkbae.lifesimulator;

import java.io.Serializable;
import java.util.Map;

import com.badlogic.gdx.math.Vector2;

public class LifeformCreationData implements Serializable {
	private static final long serialVersionUID = 7059517537757013680L;
	
	public final Gene gene;
	public final Vector2 location;
	public final Map<Integer, Float> preference;
	
	public LifeformCreationData(Gene gene, Vector2 location, Map<Integer, Float> preference) {
		this.gene = gene;
		this.location = location;
		this.preference = preference;
	}
	
}