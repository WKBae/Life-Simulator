package net.wkbae.lifesimulator;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Collections;
import java.util.List;

import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;

import com.jogamp.opengl.GL2;

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
			int x = MathUtils.round(locx - rad) + startX;
			int y = MathUtils.round(locy - rad) + startY;
			int s = MathUtils.round(rad*2);
			
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
	
	private final static int GL_CIRCLE_DIVISION = 12;
	public void paint(GL2 gl, float startX, float startY, float endX, float endY) {
		float width = Math.abs(endX - startX), height = Math.abs(endY - startY);
		float size = Math.min(width, height);
		float squareStartX = (width - size) / 2;
		float squareStartY = (height - size) / 2;
		float directionX = endX > startX? 1 : -1;
		float directionY = endY > startY? 1 : -1;
		
		float multiplyer = size * this.simulation.worldSizeReverse;
		
		gl.glColor3f(1, 1, 1);
		gl.glRectf(squareStartX, squareStartY, squareStartX + (size * directionX), squareStartY + (size * directionY));
		
		for(LifePaintInfo info : infos) {
			float locx = info.x * multiplyer;
			float locy = info.y * multiplyer;

			float rad = info.size * multiplyer;
			float x = squareStartX + locx * directionX;
			float y = squareStartY + locy * directionY;
			//float s = rad * 2;

			byte red = (byte)((info.color >> 8) & 0xF);
			byte green = (byte)((info.color >> 4) & 0xF);
			byte blue = (byte)(info.color & 0xF);
			red |= red << 4;
			green |= green << 4;
			blue |= blue << 4;
			
			gl.glPushMatrix();
			
			gl.glTranslatef(x, y, 0);
			gl.glColor3ub(red, green, blue);
			
			gl.glBegin(GL2.GL_POLYGON);
			for(double i = 0; i <= 2 * Math.PI; i += Math.PI / GL_CIRCLE_DIVISION) {
				gl.glVertex2d(Math.cos(i) * rad, Math.sin(i) * rad);
			}
			gl.glEnd();
			
			gl.glColor3f(0, 0, 0);
			
			gl.glLineWidth(0.8f);
			gl.glBegin(GL2.GL_LINE_LOOP);
			for(double i = 0; i <= 2 * Math.PI; i += Math.PI / GL_CIRCLE_DIVISION) {
				gl.glVertex2d(Math.cos(i) * rad, Math.sin(i) * rad);
			}
			gl.glEnd();
			
			gl.glPopMatrix();
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
			Vec2 loc = life.getBody().getPosition();
			this.x = loc.x;
			this.y = loc.y;
			
			this.size = life.getSize();
			this.sight = size + life.getSight();
			this.color = life.getColor();
		}
	}
}