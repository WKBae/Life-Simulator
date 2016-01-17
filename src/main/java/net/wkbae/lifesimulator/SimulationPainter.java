package net.wkbae.lifesimulator;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.math.Vector2;

public class SimulationPainter { //implements Comparable<SimulationPainter> { // no, SimulationPainter.simulation can be different.
	private final Simulation simulation;
	
	private final long time;
	
	private final List<LifePaintInfo> infos;
	
	SimulationPainter(Simulation simulation, long time, List<LifePaintInfo> info) {
		assert(simulation != null && info != null);
		
		this.simulation = simulation;
		this.time = time;
		this.infos = Collections.unmodifiableList(info);
	}
	
	public void paint(Graphics2D g, int size) {
		paint(g, 0, 0, size);
	}
	
	public void paint(Graphics2D g, int startX, int startY, int size) {
		float multiplyer = size * this.simulation.worldSizeReverse;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.WHITE);
		g.fillRect(startX, startY, startX+size, startY+size);
		
		for(LifePaintInfo info : infos) {
			//Vec2 loc = info.loc.mul(multiplyer);
			float locx = info.x * multiplyer;
			float locy = info.y * multiplyer;
		/* Paint Visible Area
			float visRad = info.sight * multiplyer;
			int visX = MathUtils.round(loc.x - visRad) + startX;
			int visY = MathUtils.round(loc.y - visRad) + startY;
			int visS = MathUtils.round(visRad * 2);
			g.setColor(new Color(0, 0, 0, 0x44));
			g.fillOval(visX, visY, visS, visS);
		}
		for(LifePaintInfo info : infos) {
			Vec2 loc = info.loc.mul(multiplyer);*/
			
			float rad = info.size * multiplyer;
			int x = (int)((locx - rad) + 0.5f) + startX;
			int y = (int)((locy - rad) + 0.5f) + startY;
			int s = (int)(rad*2 + 0.5f);
			
			int re = (info.color >> 8) & 0xF;
			int gr = (info.color >> 4) & 0xF;
			int bl = info.color & 0xF;
			Color color = new Color(re << 4 | re, gr << 4 | gr, bl << 4 | bl);
			g.setColor(color);
			
			g.fillOval(x, y, s, s);
			g.setColor(Color.BLACK);
			g.drawOval(x, y, s, s);
		}
	}
	
	public List<LifePaintInfo> getLifeInfos() {
		return infos;
	}
	
	public Simulation getSimulation() {
		return simulation;
	}
	
	public long getTime() {
		return time;
	}
	
	public static class LifePaintInfo {
		public final int color;
		public final float size;
		public final float sight;
		public final float x, y;
		
		LifePaintInfo(Lifeform life) {
			Vector2 loc = life.getBody().getPosition();
			this.x = loc.x;
			this.y = loc.y;
			
			this.size = life.getSize();
			this.sight = size + life.getSight();
			this.color = life.getColor();
		}
	}
}