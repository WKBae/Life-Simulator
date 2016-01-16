package net.wkbae.lifesimulator;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Collections;
import java.util.List;

import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;

public class SimulationPainter {
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
			int x = MathUtils.round(locx - rad) + startX;
			int y = MathUtils.round(locy - rad) + startY;
			int s = MathUtils.round(rad*2);
			g.setColor(info.color);
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
		public final Color color;
		public final float size;
		public final float sight;
		public final float x, y;
		
		LifePaintInfo(Lifeform life) {
			Vec2 loc = life.getBody().getPosition();
			x = loc.x;
			y = loc.y;
			
			size = life.getSize();
			sight = size + life.getSight();
			int color = life.getColor();
			int re = (color >> 8) & 0xF;
			int gr = (color >> 4) & 0xF;
			int bl = color & 0xF;
			this.color = new Color(re << 4 | re, gr << 4 | gr, bl << 4 | bl);
		}
	}
}