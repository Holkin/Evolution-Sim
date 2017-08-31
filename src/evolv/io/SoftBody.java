package evolv.io;

import evolv.io.temp.BodyCollisionsMap;
import evolv.io.temp.ISoftBody;
import evolv.io.temp.Singletons;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class SoftBody implements ISoftBody {
	private static final float ENERGY_DENSITY = 1.0f
			/ (Configuration.MINIMUM_SURVIVABLE_SIZE * Configuration.MINIMUM_SURVIVABLE_SIZE * EvolvioApplet.PI);

	private final EvolvioApplet evolvioApplet;
	private final Board board;
	private final double birthTime;
	/*
	 * Set so when a creature is of minimum size, it equals one.
	 */
	private final List<ISoftBody> colliders = new ArrayList<>(0);

	private double px;
	private double py;
	private double vx;
	private double vy;
	private double energy;
	private double hue;
	private double saturation;
	private double brightness;
	private double fightLevel;

	private int SBIPMinX;
	private int SBIPMinY;
	private int SBIPMaxX;
	private int SBIPMaxY;

	public SoftBody(EvolvioApplet evolvioApplet, Board tb, double tpx, double tpy, double tvx, double tvy, double tenergy, double thue, double tsaturation, double tbrightness) {
		this.evolvioApplet = evolvioApplet;
		px = tpx;
		py = tpy;
		vx = tvx;
		vy = tvy;
		energy = tenergy;
		hue = thue;
		saturation = tsaturation;
		brightness = tbrightness;
		board = tb;
		setSBIP(false);
		setSBIP(false); // Just to set previous SBIPs as well.
		birthTime = tb.getYear();
	}

	/**
	 * Looks like a collision box.
     */
	public void setSBIP(boolean shouldRemove) {
		double radius = getRadius() * Configuration.FIGHT_RANGE;
		SBIPMinX = xBound((int) (Math.floor(px - radius)));
		SBIPMinY = yBound((int) (Math.floor(py - radius)));
		SBIPMaxX = xBound((int) (Math.floor(px + radius)));
		SBIPMaxY = yBound((int) (Math.floor(py + radius)));
	}

	public int xBound(int x) {
		return Math.min(Math.max(x, 0), Configuration.BOARD_WIDTH - 1);
	}

	public int yBound(int y) {
		return Math.min(Math.max(y, 0), board.getBoardHeight() - 1);
	}

	public double xBodyBound(double x) {
		double radius = getRadius();
		return Math.min(Math.max(x, radius), Configuration.BOARD_WIDTH - radius);
	}

	public double yBodyBound(double y) {
		double radius = getRadius();
		return Math.min(Math.max(y, radius), board.getBoardHeight() - radius);
	}

	public void collide(double timeStep) {
		colliders.clear();
		colliders.addAll(BodyCollisionsMap.getCollidedBodies(this));

		for (int i = 0; i < colliders.size(); i++) {
			ISoftBody collider = colliders.get(i);
			float distance = EvolvioApplet.dist((float) px, (float) py, (float) collider.getPx(), (float) collider.getPy());
			double combinedRadius = getRadius() + collider.getRadius();
			if (distance < combinedRadius) {
				double force = combinedRadius * Configuration.COLLISION_FORCE;
				vx += ((px - collider.getPx()) / distance) * force / getMass();
				vy += ((py - collider.getPy()) / distance) * force / getMass();
			}
		}
		fightLevel = 0;
	}

	public void applyMotions(double timeStep) {
		px = xBodyBound(px + vx * timeStep);
		py = yBodyBound(py + vy * timeStep);
		vx *= Math.max(0, 1 - Configuration.FRICTION / getMass());
		vy *= Math.max(0, 1 - Configuration.FRICTION / getMass());
		setSBIP(true);
	}

	public void drawSoftBody(float scaleUp) {
		double radius = getRadius();
		this.evolvioApplet.stroke(0);
		this.evolvioApplet.strokeWeight(Configuration.CREATURE_STROKE_WEIGHT);
		this.evolvioApplet.fill((float) hue, (float) saturation, (float) brightness);
		this.evolvioApplet.ellipseMode(EvolvioApplet.RADIUS);
		this.evolvioApplet.ellipse((float) (px * scaleUp), (float) (py * scaleUp), (float) (radius * scaleUp),
				(float) (radius * scaleUp));
	}

	public Board getBoard() {
		return board;
	}

	// TODO remove calculations from getter
	@Override
	public double getRadius() {
		if (energy <= 0) {
			return 0;
		} else {
			return Math.sqrt(energy / ENERGY_DENSITY / Math.PI) * 2.5;
		}
	}

	public double getMass() {
		return energy / ENERGY_DENSITY;
	}

	public List<ISoftBody> getColliders() {
		return colliders;
	}

	public double getPx() {
		return px;
	}

	public double getPy() {
		return py;
	}
	
	public Point2D getPoint2D() {
		return new Point2D.Double(px, py);
	}

	public double getVx() {
		return vx;
	}

	public double getVy() {
		return vy;
	}

	public void setVx(double vx) {
		this.vx = vx;
	}

	public void setVy(double vy) {
		this.vy = vy;
	}

	public double getEnergy() {
		return energy;
	}

	public void setEnergy(double energy) {
		this.energy = energy;
	}

	public double getBirthTime() {
		return birthTime;
	}

	public double getHue() {
		return hue;
	}

	public double getSaturation() {
		return saturation;
	}

	public double getBrightness() {
		return brightness;
	}

	public void setHue(double hue) {
		this.hue = Math.min(Math.max(hue, 0), 1);
	}

	public void setSaturation(double saturation) {
		this.saturation = Math.min(Math.max(saturation, 0), 1);
	}

	public void setBrightness(double brightness) {
		this.brightness = Math.min(Math.max(brightness, 0), 1);
	}

	public double getFightLevel() {
		return fightLevel;
	}

	public void setFightLevel(double fightLevel) {
		this.fightLevel = fightLevel;
	}

	public int getSBIPMinX() {
		return SBIPMinX;
	}

	public int getSBIPMaxX() {
		return SBIPMaxX;
	}

	public int getSBIPMinY() {
		return SBIPMinY;
	}

	public int getSBIPMaxY() {
		return SBIPMaxY;
	}
}
