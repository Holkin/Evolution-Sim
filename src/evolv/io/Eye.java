package evolv.io;

import evolv.io.temp.BodyPositionsMap;
import evolv.io.temp.ICreature;
import evolv.io.temp.ISoftBody;
import evolv.io.temp.Singletons;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class Eye {

	private static final float CROSS_SIZE = 0.022f;

	private final ICreature creatureOld;
	private final EvolvioApplet evolvioApplet;
	final double angle;
	final double distance;

	private final List<ISoftBody> potentialVisionOccluders = new ArrayList<>();
	private double visionOccludedX;
	private double visionOccludedY;

	private final EyeResult eyeResult;

	public class EyeResult {
		public double hue;
		public double saturation;
		public double brightness;
	}

	public Eye(EvolvioApplet evolvioApplet, ICreature creatureOld, double angle, double distance) {
		this.creatureOld = creatureOld;
		this.evolvioApplet = evolvioApplet;
		this.angle = angle;
		this.distance = distance;

		eyeResult = new EyeResult();
	}

	public void see() {
		
		Point2D visionStart = creatureOld.getPoint2D();
		double visionTotalAngle = creatureOld.getRotation() + angle;

		double endX = getVisionEndX();
		double endY = getVisionEndY();

		visionOccludedX = endX;
		visionOccludedY = endY;
		int c = creatureOld.getBoard().getColorAt(endX, endY);
		eyeResult.hue = evolvioApplet.hue(c);
		eyeResult.saturation = evolvioApplet.saturation(c);
		eyeResult.brightness = evolvioApplet.brightness(c);

		getPVOs(visionStart, visionTotalAngle);

		double[][] rotationMatrix = new double[2][2];
		rotationMatrix[1][1] = rotationMatrix[0][0] = Math.cos(-visionTotalAngle);
		rotationMatrix[0][1] = Math.sin(-visionTotalAngle);
		rotationMatrix[1][0] = -rotationMatrix[0][1];
		double visionLineLength = distance;
		for (ISoftBody body : potentialVisionOccluders) {
			double x = body.getPx() - creatureOld.getPx();
			double y = body.getPy() - creatureOld.getPy();
			double r = body.getRadius();
			double translatedX = rotationMatrix[0][0] * x + rotationMatrix[1][0] * y;
			double translatedY = rotationMatrix[0][1] * x + rotationMatrix[1][1] * y;
			if (Math.abs(translatedY) <= r) {
				if ((translatedX >= 0 && translatedX < visionLineLength && translatedY < visionLineLength)
						|| distance(0, 0, translatedX, translatedY) < r
						|| distance(visionLineLength, 0, translatedX, translatedY) < r) {
					// YES! There is an occlussion.
					visionLineLength = translatedX - Math.sqrt(r * r - translatedY * translatedY);
					visionOccludedX = visionStart.getX() + visionLineLength * Math.cos(visionTotalAngle);
					visionOccludedY = visionStart.getY() + visionLineLength * Math.sin(visionTotalAngle);
					eyeResult.hue = body.getHue();
					eyeResult.saturation = body.getSaturation();
					eyeResult.brightness = body.getBrightness();
				}
			}
		}
	}

	public void drawVisionAngle(float scaleUp) {
		int visionUIcolor = this.evolvioApplet.color(0, 0, 1);
		if (getEyeResult().brightness > Configuration.BRIGHTNESS_THRESHOLD) {
			visionUIcolor = this.evolvioApplet.color(0, 0, 0);
		}
		this.evolvioApplet.stroke(visionUIcolor);
		this.evolvioApplet.strokeWeight(Configuration.CREATURE_STROKE_WEIGHT);
		float endX = (float) getVisionEndX();
		float endY = (float) getVisionEndY();
		this.evolvioApplet.line((float) (creatureOld.getPx() * scaleUp), (float) (creatureOld.getPy() * scaleUp),
				endX * scaleUp, endY * scaleUp);
		this.evolvioApplet.noStroke();
		this.evolvioApplet.fill(visionUIcolor);
		this.evolvioApplet.ellipse((float) (visionOccludedX * scaleUp), (float) (visionOccludedY * scaleUp),
				2 * CROSS_SIZE * scaleUp, 2 * CROSS_SIZE * scaleUp);
		this.evolvioApplet.stroke((float) (getEyeResult().hue), (float) (getEyeResult().saturation),
				(float) (getEyeResult().brightness));
		this.evolvioApplet.strokeWeight(Configuration.CREATURE_STROKE_WEIGHT);
		this.evolvioApplet.line((float) ((visionOccludedX - CROSS_SIZE) * scaleUp),
				(float) ((visionOccludedY - CROSS_SIZE) * scaleUp), (float) ((visionOccludedX + CROSS_SIZE) * scaleUp),
				(float) ((visionOccludedY + CROSS_SIZE) * scaleUp));
		this.evolvioApplet.line((float) ((visionOccludedX - CROSS_SIZE) * scaleUp),
				(float) ((visionOccludedY + CROSS_SIZE) * scaleUp), (float) ((visionOccludedX + CROSS_SIZE) * scaleUp),
				(float) ((visionOccludedY - CROSS_SIZE) * scaleUp));
	}

	private double distance(double x1, double y1, double x2, double y2) {
		return (Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)));
	}
	
	private void getPVOs(Point2D visionStart, double visionTotalAngle) {
		potentialVisionOccluders.clear();
		
		int tileX = 0;
		int tileY = 0;
		int prevTileX = -1;
		int prevTileY = -1;
		
		for (int DAvision = 0; DAvision < distance + 1; DAvision++) {
			tileX = (int) (visionStart.getX() + Math.cos(visionTotalAngle) * DAvision);
			tileY = (int) (visionStart.getY() + Math.sin(visionTotalAngle) * DAvision);
			if (tileX != prevTileX || tileY != prevTileY) {
				addPVOs(tileX, tileY, potentialVisionOccluders);
				if (prevTileX >= 0 && tileX != prevTileX && tileY != prevTileY) {
					addPVOs(prevTileX, tileY, potentialVisionOccluders);
					addPVOs(tileX, prevTileY, potentialVisionOccluders);
				}
			}
			prevTileX = tileX;
			prevTileY = tileY;
		}
	}

	private void addPVOs(int x, int y, List<ISoftBody> PVOs) {
		if (x >= 0 && x < Configuration.BOARD_WIDTH && y >= 0 && y < creatureOld.getBoard().getBoardHeight()) {
			PVOs.addAll(BodyPositionsMap.getBodiesAtPosition(x, y));
			PVOs.remove(creatureOld);
		}
	}

	private double getVisionEndX() {
		double visionTotalAngle = creatureOld.getRotation() + angle;
		return creatureOld.getPx() + distance * Math.cos(visionTotalAngle);
	}

	private double getVisionEndY() {
		double visionTotalAngle = creatureOld.getRotation() + angle;
		return creatureOld.getPy() + distance * Math.sin(visionTotalAngle);
	}

	public EyeResult getEyeResult() {
		return eyeResult;
	}
}
