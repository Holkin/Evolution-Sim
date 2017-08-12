package evolv.io;

import evolv.io.temp.CreatureFactory;
import evolv.io.temp.ICreature;
import evolv.io.temp.ISoftBody;
import evolv.io.model.World;
import evolv.io.renderers.TileRenderer;
import evolv.io.util.MathUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.SortOrder;

import static evolv.io.Configuration.*;

public class Board {
	private static final String[] SORT_METRIC_NAMES = { "Biggest", "Smallest", "Youngest", "Oldest", "A to Z", "Z to A",
			"Highest Gen", "Lowest Gen" };
	private static final Comparator<ICreature>[] CREATURE_COMPARATORS = new Comparator[] {
			new CreatureComparators.SizeComparator(SortOrder.DESCENDING),
			new CreatureComparators.SizeComparator(SortOrder.ASCENDING),
			new CreatureComparators.AgeComparator(SortOrder.DESCENDING),
			new CreatureComparators.AgeComparator(SortOrder.ASCENDING),
			new CreatureComparators.NameComparator(SortOrder.DESCENDING),
			new CreatureComparators.NameComparator(SortOrder.ASCENDING),
			new CreatureComparators.GenComparator(SortOrder.DESCENDING),
			new CreatureComparators.GenComparator(SortOrder.ASCENDING), };

	private final EvolvioApplet evolvioApplet;
	private final int randomSeed;

	// CreatureOld
	private final List<ISoftBody>[][] softBodiesInPositions = new List[Configuration.BOARD_WIDTH][Configuration.BOARD_HEIGHT];
	private final List<ICreature> creatureOlds = new ArrayList<>();
	private final ICreature[] list = new ICreature[Configuration.LIST_SLOTS];
	private float spawnChance = Configuration.SPAWN_CHANCE;
	private ICreature selectedCreatureOld;
	private int creatureIDUpTo;
	private int sortMetric;
	private boolean isPressingKeyB;

	// Time or History
	private final double timeStep;
	private final int[] populationHistory = new int[Configuration.POPULATION_HISTORY_LENGTH];
	private double year;
	private int playSpeed = 1;

	// Temperature
	private float minTemperature = Configuration.MINIMUM_TEMPERATURE;
	private float maxTemperature = Configuration.MAXIMUM_TEMPERATURE;
	private double temperature;

	// Saving
	private final int[] fileSaveCounts;
	private final double[] fileSaveTimes;
	private double imageSaveInterval = 1;
	private double textSaveInterval = 1;

	// Misc or Unsorted
	private final int backgroundColor;
	private final int buttonColor;
	private boolean userControl;
	private boolean render = true;

	// TODO move this somewhere
	private World world;
	private final TileRenderer tileRenderer;

	public Board(EvolvioApplet evolvioApplet, int randomSeed) {
		this.backgroundColor = evolvioApplet.color(0, 0, 0.1f);
		this.buttonColor = evolvioApplet.color(0.82f, 0.8f, 0.7f);
		this.evolvioApplet = evolvioApplet;
		this.randomSeed = randomSeed;
		this.evolvioApplet.noiseSeed(randomSeed);
		this.evolvioApplet.randomSeed(randomSeed);

		// ==========================================
		this.world = new World(Configuration.BOARD_WIDTH, Configuration.BOARD_HEIGHT);
		this.world.recreate(
				(x,y) -> {
                    double climateType = this.evolvioApplet.noise(
                            x * Configuration.NOISE_STEP_SIZE * 0.2f + 10000,
                            y * Configuration.NOISE_STEP_SIZE * 0.2f + 10000
                    ) * 1.63f - 0.4f;
                    return Math.min(Math.max(climateType, 0), 0.8f);
                },
				(x,y) -> {
					double bigForce = Math.pow(1.0 * x / Configuration.BOARD_HEIGHT, 0.5);
					return this.evolvioApplet.noise(
							x * Configuration.NOISE_STEP_SIZE * 3,
							y * Configuration.NOISE_STEP_SIZE * 3
						) * (1 - bigForce) * 5.0f
							+ this.evolvioApplet.noise(
							x * Configuration.NOISE_STEP_SIZE * 0.5f,
							y * Configuration.NOISE_STEP_SIZE * 0.5f
						) * bigForce * 5.0f - 1.5f;
		});
		this.world.setFoodGen((fertility, foodLevel, growthRate) -> {
			if (fertility < Configuration.MAX_FERTILITY) {
				if (growthRate > 0) {
					// Food is growing. Exponentially approach maxGrowthLevel.
					if (foodLevel < MAX_GROWTH_LEVEL) {
						double newDistToMax = (MAX_GROWTH_LEVEL - foodLevel) *
								MathUtil.fastExp(-growthRate * fertility * FOOD_GROWTH_RATE);
						return  (MAX_GROWTH_LEVEL - newDistToMax) - foodLevel;
					}
				} else {
					// Food is dying off. Exponentially approach 0.
					return foodLevel * MathUtil.fastExp(growthRate * FOOD_GROWTH_RATE) - foodLevel;
				}
			}
			return 0;
		});
		this.tileRenderer = new TileRenderer(evolvioApplet);
		// ==========================================

		for (int x = 0; x < Configuration.BOARD_WIDTH; x++) {
			for (int y = 0; y < Configuration.BOARD_HEIGHT; y++) {
				softBodiesInPositions[x][y] = new ArrayList<>(0);
			}
		}

		this.fileSaveCounts = new int[4];
		this.fileSaveTimes = new double[4];
		for (int i = 0; i < 4; i++) {
			fileSaveTimes[i] = -999;
		}
		this.timeStep = Configuration.TIME_STEP;
	}

	public void drawBoard(float scaleUp, float camZoom, int mouseX, int mouseY) {
		if (!render) {
			return;
		}
		drawTiles(scaleUp, camZoom);
		if (mouseX >= 0 && mouseX < Configuration.BOARD_WIDTH && mouseY >= 0 && mouseY < Configuration.BOARD_HEIGHT) {
			tileRenderer.drawTileInfo(getTile(mouseX, mouseY), scaleUp, camZoom);
		}
		for (ICreature creatureOld : creatureOlds) {
			creatureOld.drawSoftBody(scaleUp, camZoom, true);
		}
	}

	private void drawTiles(float scaleUp, float camZoom) {
		Arrays.stream(world.getTiles()).flatMap(Arrays::stream)
				.forEach(tile -> tileRenderer.drawTile(tile, scaleUp));
	}

	public void drawUI(float scaleUp, float camZoom, double timeStep, int x1, int y1, int x2, int y2) {
		this.evolvioApplet.fill(0, 0, 0);
		this.evolvioApplet.noStroke();
		this.evolvioApplet.rect(x1, y1, x2 - x1, y2 - y1);

		this.evolvioApplet.pushMatrix();
		this.evolvioApplet.translate(x1, y1);

		this.evolvioApplet.fill(0, 0, 1);
		this.evolvioApplet.textAlign(EvolvioApplet.RIGHT);
		this.evolvioApplet.text(EvolvioApplet.nfs(camZoom * 100, 0, 3) + " %", 0, y2 - y1 - 30);
		this.evolvioApplet.textAlign(EvolvioApplet.LEFT);
		this.evolvioApplet.textSize(48);
		String yearText = "Year " + EvolvioApplet.nf((float) year, 0, 2);
		this.evolvioApplet.text(yearText, 10, 48);
		float seasonTextXCoor = this.evolvioApplet.textWidth(yearText) + 50;
		this.evolvioApplet.textSize(20);
		this.evolvioApplet.text("Population: " + creatureOlds.size(), 10, 80);
		String[] seasons = { "Winter", "Spring", "Summer", "Autumn" };
		this.evolvioApplet.text(seasons[(int) (getSeason() * 4)] + "\nSeed: " + randomSeed, seasonTextXCoor, 30);

		if (selectedCreatureOld == null) {
			Collections.sort(creatureOlds, CREATURE_COMPARATORS[sortMetric]);
			Arrays.fill(list, null);
			for (int i = 0; i < Configuration.LIST_SLOTS && i < creatureOlds.size(); i++) {
				list[i] = creatureOlds.get(i);
			}
			double maxEnergy = 0;
			for (int i = 0; i < Configuration.LIST_SLOTS; i++) {
				if (list[i] != null && list[i].getEnergy() > maxEnergy) {
					maxEnergy = list[i].getEnergy();
				}
			}
			for (int i = 0; i < Configuration.LIST_SLOTS; i++) {
				if (list[i] != null) {
					list[i].setPreferredRank(list[i].getPreferredRank() + ((i - list[i].getPreferredRank()) * 0.4f));
					float y = y1 + 175 + 70 * list[i].getPreferredRank();
					drawCreature(list[i], 45, y + 5, 2.3f, scaleUp);
					this.evolvioApplet.textSize(24);
					this.evolvioApplet.textAlign(EvolvioApplet.LEFT);
					this.evolvioApplet.noStroke();
					this.evolvioApplet.fill(0.333f, 1, 0.4f);
					float multi = (x2 - x1 - 200);
					if (list[i].getEnergy() > 0) {
						this.evolvioApplet.rect(85, y + 5, (float) (multi * list[i].getEnergy() / maxEnergy), 25);
					}
					if (list[i].getEnergy() > 1) {
						this.evolvioApplet.fill(0.333f, 1, 0.8f);
						this.evolvioApplet.rect(85 + (float) (multi / maxEnergy), y + 5,
								(float) (multi * (list[i].getEnergy() - 1) / maxEnergy), 25);
					}
					this.evolvioApplet.fill(0, 0, 1);
					this.evolvioApplet.text(
							list[i].getName() + " [" + list[i].getId() + "] (" + toAge(list[i].getAge()) + ")", 90, y);
					this.evolvioApplet.text("Energy: " + EvolvioApplet.nf(100 * (float) (list[i].getEnergy()), 0, 2), 90,
							y + 25);
				}
			}
			this.evolvioApplet.noStroke();
			this.evolvioApplet.fill(buttonColor);
			this.evolvioApplet.rect(10, 95, 220, 40);
			this.evolvioApplet.rect(240, 95, 220, 40);
			this.evolvioApplet.fill(0, 0, 1);
			this.evolvioApplet.textAlign(EvolvioApplet.CENTER);
			this.evolvioApplet.text("Reset zoom", 120, 123);
			this.evolvioApplet.text("Sort by: " + SORT_METRIC_NAMES[sortMetric], 350, 123);

			this.evolvioApplet.textSize(15);
			/*
			 * TODO put these button texts in the same place as the board
			 * actions
			 */
			String[] buttonTexts = { "Brain Control",
					"Spawn Chance " + EvolvioApplet.nf(spawnChance, 0, 2) + "%", "Screenshot now",
					"-   Image every " + EvolvioApplet.nf((float) imageSaveInterval, 0, 2) + " years   +",
					"Text file now",
					"-    Text every " + EvolvioApplet.nf((float) textSaveInterval, 0, 2) + " years    +",
					"-    Play Speed (" + playSpeed + "x)    +", "Toggle Rendering" };
			if (userControl) {
				buttonTexts[0] = "Keyboard Control";
			}

			for (int i = 0; i < 8; i++) {
				float x = (i % 2) * 230 + 10;
				float y = EvolvioApplet.floor(i / 2) * 50 + 570;
				this.evolvioApplet.fill(buttonColor);
				this.evolvioApplet.rect(x, y, 220, 40);
				if (i >= 2 && i < 6) {
					// TODO can pow be replaced with something faster?
					double flashAlpha = 1.0f
							* Math.pow(0.5f, (year - fileSaveTimes[i - 2]) * Configuration.FLASH_SPEED);
					this.evolvioApplet.fill(0, 0, 1, (float) flashAlpha);
					this.evolvioApplet.rect(x, y, 220, 40);
				}
				this.evolvioApplet.fill(0, 0, 1, 1);
				this.evolvioApplet.text(buttonTexts[i], x + 110, y + 17);
				if (i == 0) {
				} else if (i == 1) {
					this.evolvioApplet.text("-" + EvolvioApplet.nf(Configuration.SPAWN_CHANCE_INCREMENT, 0, 2)
							+ "                    +" + EvolvioApplet.nf(Configuration.SPAWN_CHANCE_INCREMENT, 0, 2),
							x + 110, y + 37);
				} else if (i <= 5) {
					this.evolvioApplet.text(getNextFileName(i - 2), x + 110, y + 37);
				}
			}
		} else {
			float energyUsage = (float) selectedCreatureOld.getEnergyUsage(timeStep);
			this.evolvioApplet.noStroke();
			if (energyUsage <= 0) {
				this.evolvioApplet.fill(0, 1, 0.5f);
			} else {
				this.evolvioApplet.fill(0.33f, 1, 0.4f);
			}
			float EUbar = 20 * energyUsage;
			this.evolvioApplet.rect(110, 280, Math.min(Math.max(EUbar, -110), 110), 25);
			if (EUbar < -110) {
				this.evolvioApplet.rect(0, 280, 25, (-110 - EUbar) * 20 + 25);
			} else if (EUbar > 110) {
				float h = (EUbar - 110) * 20 + 25;
				this.evolvioApplet.rect(185, 280 - h, 25, h);
			}
			this.evolvioApplet.fill(0, 0, 1);
			this.evolvioApplet.text("Name: " + selectedCreatureOld.getName(), 10, 225);
			this.evolvioApplet.text(
					"Energy: " + EvolvioApplet.nf(100 * (float) selectedCreatureOld.getEnergy(), 0, 2) + " yums", 10, 250);
			this.evolvioApplet.text("" + EvolvioApplet.nf(100 * energyUsage, 0, 2) + " yums/year", 10, 275);

			this.evolvioApplet.text("ID: " + selectedCreatureOld.getId(), 10, 325);
			this.evolvioApplet.text("X: " + EvolvioApplet.nf((float) selectedCreatureOld.getPx(), 0, 2), 10, 350);
			this.evolvioApplet.text("Y: " + EvolvioApplet.nf((float) selectedCreatureOld.getPy(), 0, 2), 10, 375);
			this.evolvioApplet.text("Rotation: " + EvolvioApplet.nf((float) selectedCreatureOld.getRotation(), 0, 2), 10,
					400);
			this.evolvioApplet.text("Birthday: " + toDate(selectedCreatureOld.getBirthTime()), 10, 425);
			this.evolvioApplet.text("(" + toAge(selectedCreatureOld.getAge()) + ")", 10, 450);
			this.evolvioApplet.text("Generation: " + selectedCreatureOld.getGen(), 10, 475);
			this.evolvioApplet.text("Parents: " + selectedCreatureOld.getParents(), 10, 500, 210, 255);
			this.evolvioApplet.text("Hue: " + EvolvioApplet.nf((float) (selectedCreatureOld.getHue()), 0, 2), 10, 550, 210,
					255);
			this.evolvioApplet.text("Mouth Hue: " + EvolvioApplet.nf((float) (selectedCreatureOld.getMouthHue()), 0, 2), 10,
					575, 210, 255);

			if (userControl) {
				this.evolvioApplet.text(
						"Controls:\nUp/Down: Move\nLeft/Right: Rotate\nSpace: Eat\nF: Fight\nV: Vomit\nU, J: Change color"
								+ "\nI, K: Change mouth color\nB: Give birth (Not possible if under "
								+ Math.round((Configuration.MANUAL_BIRTH_SIZE + 1) * 100) + " yums)",
						10, 625, 250, 400);
			}
			this.evolvioApplet.pushMatrix();
			this.evolvioApplet.translate(400, 80);
			float apX = EvolvioApplet
					.round(((this.evolvioApplet.mouseX) - 400 - Brain.NEURON_OFFSET_X - x1) / 50.0f / 1.2f);
			float apY = EvolvioApplet.round((this.evolvioApplet.mouseY - 80 - Brain.NEURON_OFFSET_Y - y1) / 50.0f);
			selectedCreatureOld.drawBrain(50, (int) apX, (int) apY);
			this.evolvioApplet.popMatrix();
		}

		drawPopulationGraph(x1, x2, y1, y2);
		this.evolvioApplet.fill(0, 0, 0);
		this.evolvioApplet.textAlign(EvolvioApplet.RIGHT);
		this.evolvioApplet.textSize(24);
		this.evolvioApplet.text("Population: " + creatureOlds.size(), x2 - x1 - 10, y2 - y1 - 10);
		this.evolvioApplet.popMatrix();

		this.evolvioApplet.pushMatrix();
		this.evolvioApplet.translate(x2, y1);
		if (selectedCreatureOld == null) {
			this.evolvioApplet.textAlign(EvolvioApplet.RIGHT);
			this.evolvioApplet.textSize(24);
			this.evolvioApplet.text("Temperature", -10, 24);
			drawThermometer(-45, 30, 20, 660, temperature, Configuration.THERMOMETER_MINIMUM,
					Configuration.THERMOMETER_MAXIMUM, this.evolvioApplet.color(0, 1, 1));
		}
		this.evolvioApplet.popMatrix();

		if (selectedCreatureOld != null) {
			drawCreature(selectedCreatureOld, x1 + 65, y1 + 147, 2.3f, scaleUp);
		}
	}

	private void drawPopulationGraph(float x1, float x2, float y1, float y2) {
		float barWidth = (x2 - x1) / ((Configuration.POPULATION_HISTORY_LENGTH));
		this.evolvioApplet.noStroke();
		this.evolvioApplet.fill(0.33333f, 1, 0.6f);
		int maxPopulation = 0;
		for (int population : populationHistory) {
			if (population > maxPopulation) {
				maxPopulation = population;
			}
		}
		for (int i = 0; i < Configuration.POPULATION_HISTORY_LENGTH; i++) {
			float h = (((float) populationHistory[i]) / maxPopulation) * (y2 - 770);
			this.evolvioApplet.rect((Configuration.POPULATION_HISTORY_LENGTH - 1 - i) * barWidth, y2 - h, barWidth, h);
		}
	}

	private String getNextFileName(int type) {
		String[] modes = { "manualImgs", "autoImgs", "manualTexts", "autoTexts" };
		String ending = ".png";
		if (type >= 2) {
			ending = ".txt";
		}
		return Configuration.INITIAL_FILE_NAME + "/" + modes[type] + "/" + EvolvioApplet.nf(fileSaveCounts[type], 5)
				+ ending;
	}

	public void iterate(double timeStep) {
		double prevYear = year;
		year += timeStep;
		if (Math.floor(year / Configuration.RECORD_POPULATION_EVERY) != Math
				.floor(prevYear / Configuration.RECORD_POPULATION_EVERY)) {
			for (int i = Configuration.POPULATION_HISTORY_LENGTH - 1; i >= 1; i--) {
				populationHistory[i] = populationHistory[i - 1];
			}
			populationHistory[0] = creatureOlds.size();
		}
		temperature = getGrowthRate(getSeason());
		// ======================
		world.update(getGrowthRate(prevYear)*timeStep); // TODO refactor growthRate scaling
		// ======================
		for (int i = 0; i < creatureOlds.size(); i++) {
			creatureOlds.get(i).setPreviousEnergy();
		}
		/*
		 * for(int i = 0; i < rocks.size(); i++) {
		 * rocks.get(i).collide(timeStep*OBJECT_TIMESTEPS_PER_YEAR); }
		 */
		randomSpawnCreature(false);
		for (int i = 0; i < creatureOlds.size(); i++) {
			ICreature me = creatureOlds.get(i);
			me.collide(timeStep);
			me.metabolize(timeStep);
			me.useBrain(timeStep, !userControl);
			if (userControl) {
				if (me == selectedCreatureOld) {
					if (this.evolvioApplet.keyPressed) {
						if (this.evolvioApplet.key == EvolvioApplet.CODED) {
							if (this.evolvioApplet.keyCode == EvolvioApplet.UP)
								me.accelerate(0.04f, timeStep * Configuration.TIMESTEPS_PER_YEAR);
							if (this.evolvioApplet.keyCode == EvolvioApplet.DOWN)
								me.accelerate(-0.04f, timeStep * Configuration.TIMESTEPS_PER_YEAR);
							if (this.evolvioApplet.keyCode == EvolvioApplet.LEFT)
								me.rotate(-0.1f, timeStep * Configuration.TIMESTEPS_PER_YEAR);
							if (this.evolvioApplet.keyCode == EvolvioApplet.RIGHT)
								me.rotate(0.1f, timeStep * Configuration.TIMESTEPS_PER_YEAR);
						} else {
							if (this.evolvioApplet.key == ' ')
								me.eat(0.1f, timeStep * Configuration.TIMESTEPS_PER_YEAR);
							if (this.evolvioApplet.key == 'v' || this.evolvioApplet.key == 'V')
								me.eat(-0.1f, timeStep * Configuration.TIMESTEPS_PER_YEAR);
							if (this.evolvioApplet.key == 'f' || this.evolvioApplet.key == 'F')
								me.fight(0.5f, timeStep * Configuration.TIMESTEPS_PER_YEAR);
							if (this.evolvioApplet.key == 'u' || this.evolvioApplet.key == 'U')
								me.setHue(me.getHue() + 0.02f);
							if (this.evolvioApplet.key == 'j' || this.evolvioApplet.key == 'J')
								me.setHue(me.getHue() - 0.02f);

							if (this.evolvioApplet.key == 'i' || this.evolvioApplet.key == 'I')
								me.setMouthHue(me.getMouthHue() + 0.02f);
							if (this.evolvioApplet.key == 'k' || this.evolvioApplet.key == 'K')
								me.setMouthHue(me.getMouthHue() - 0.02f);
							if (this.evolvioApplet.key == 'b' || this.evolvioApplet.key == 'B') {
								if (!isPressingKeyB) {
									me.reproduce(Configuration.MANUAL_BIRTH_SIZE, timeStep);
								}
								isPressingKeyB = true;
							} else {
								isPressingKeyB = false;
							}
						}
					}
				}
			}
		}
		finishIterate(timeStep);
	}

	private void finishIterate(double timeStep) {
		for (int i = 0; i < creatureOlds.size(); i++) {
			creatureOlds.get(i).applyMotions(timeStep * Configuration.TIMESTEPS_PER_YEAR);
			creatureOlds.get(i).see();
		}
		if (Math.floor(fileSaveTimes[1] / imageSaveInterval) != Math.floor(year / imageSaveInterval)) {
			prepareForFileSave(1);
		}
		if (Math.floor(fileSaveTimes[3] / textSaveInterval) != Math.floor(year / textSaveInterval)) {
			prepareForFileSave(3);
		}
	}

	private double getGrowthRate(double theTime) {
		double temperatureRange = maxTemperature - minTemperature;
		return minTemperature + temperatureRange * 0.5f - temperatureRange * 0.5f * Math.cos(theTime * 2 * Math.PI);
	}

	public double getGrowthOverTimeRange(double startTime, double endTime) {
		double temperatureRange = maxTemperature - minTemperature;
		double m = minTemperature + temperatureRange * 0.5f;
		return (endTime - startTime) * m + (temperatureRange / Math.PI / 4.0f)
				* (Math.sin(2 * Math.PI * startTime) - Math.sin(2 * Math.PI * endTime));
	}

	private double getSeason() {
		return (year % 1.0f);
	}

	public double getYear() {
		return year;
	}

	private void drawThermometer(float x1, float y1, float w, float h, double prog, double min, double max,
			int fillColor) {
		this.evolvioApplet.noStroke();
		this.evolvioApplet.fill(0, 0, 0.2f);
		this.evolvioApplet.rect(x1, y1, w, h);
		this.evolvioApplet.fill(fillColor);
		double proportionFilled = (prog - min) / (max - min);
		this.evolvioApplet.rect(x1, (float) (y1 + h * (1 - proportionFilled)), w, (float) (proportionFilled * h));

		double zeroHeight = (0 - min) / (max - min);
		double zeroLineY = y1 + h * (1 - zeroHeight);
		this.evolvioApplet.textAlign(EvolvioApplet.RIGHT);
		this.evolvioApplet.stroke(0, 0, 1);
		this.evolvioApplet.strokeWeight(3);
		this.evolvioApplet.line(x1, (float) (zeroLineY), x1 + w, (float) (zeroLineY));
		double minY = y1 + h * (1 - (minTemperature - min) / (max - min));
		double maxY = y1 + h * (1 - (maxTemperature - min) / (max - min));
		this.evolvioApplet.fill(0, 0, 0.8f);
		this.evolvioApplet.line(x1, (float) (minY), x1 + w * 1.8f, (float) (minY));
		this.evolvioApplet.line(x1, (float) (maxY), x1 + w * 1.8f, (float) (maxY));
		this.evolvioApplet.line(x1 + w * 1.8f, (float) (minY), x1 + w * 1.8f, (float) (maxY));

		this.evolvioApplet.fill(0, 0, 1);
		this.evolvioApplet.text("Zero", x1 - 5, (float) (zeroLineY + 8));
		this.evolvioApplet.text(EvolvioApplet.nf(minTemperature, 0, 2), x1 - 5, (float) (minY + 8));
		this.evolvioApplet.text(EvolvioApplet.nf(maxTemperature, 0, 2), x1 - 5, (float) (maxY + 8));
	}

	private void drawVerticalSlider(float x1, float y1, float w, float h, double prog, int fillColor, int antiColor) {
		this.evolvioApplet.noStroke();
		this.evolvioApplet.fill(0, 0, 0.2f);
		this.evolvioApplet.rect(x1, y1, w, h);
		if (prog >= 0) {
			this.evolvioApplet.fill(fillColor);
		} else {
			this.evolvioApplet.fill(antiColor);
		}
		this.evolvioApplet.rect(x1, (float) (y1 + h * (1 - prog)), w, (float) (prog * h));
	}

	public boolean setMinTemperature(float temp) {
		minTemperature = tempBounds(Configuration.THERMOMETER_MINIMUM
				+ temp * (Configuration.THERMOMETER_MAXIMUM - Configuration.THERMOMETER_MINIMUM));
		if (minTemperature > maxTemperature) {
			float placeHolder = maxTemperature;
			maxTemperature = minTemperature;
			minTemperature = placeHolder;
			return true;
		}
		return false;
	}

	public boolean setMaxTemperature(float temp) {
		maxTemperature = tempBounds(Configuration.THERMOMETER_MINIMUM
				+ temp * (Configuration.THERMOMETER_MAXIMUM - Configuration.THERMOMETER_MINIMUM));
		if (minTemperature > maxTemperature) {
			float placeHolder = maxTemperature;
			maxTemperature = minTemperature;
			minTemperature = placeHolder;
			return true;
		}
		return false;
	}

	private float tempBounds(float temp) {
		return Math.min(Math.max(temp, Configuration.THERMOMETER_MINIMUM),
				Configuration.THERMOMETER_MAXIMUM);
	}

	public float getHighTempProportion() {
		return (maxTemperature - Configuration.THERMOMETER_MINIMUM)
				/ (Configuration.THERMOMETER_MAXIMUM - Configuration.THERMOMETER_MINIMUM);
	}

	public float getLowTempProportion() {
		return (minTemperature - Configuration.THERMOMETER_MINIMUM)
				/ (Configuration.THERMOMETER_MAXIMUM - Configuration.THERMOMETER_MINIMUM);
	}

	private String toDate(double d) {
		return "Year " + EvolvioApplet.nf((float) (d), 0, 2);
	}

	private String toAge(double d) {
		return EvolvioApplet.nf((float) d, 0, 2) + " yrs old";
	}

	public void increaseSpawnChance() {
		this.spawnChance = Math.min(1, this.spawnChance + Configuration.SPAWN_CHANCE_INCREMENT);
	}

	public void decreaseSpawnChance() {
		this.spawnChance = Math.max(0, this.spawnChance - Configuration.SPAWN_CHANCE_INCREMENT);

	}

	private void randomSpawnCreature(boolean choosePreexisting) {
		if (this.evolvioApplet.random(0, 1) < spawnChance) {
			if (choosePreexisting) {
				ICreature c = getRandomCreature();
				c.addEnergy(Configuration.SAFE_SIZE);
				c.reproduce(Configuration.SAFE_SIZE, timeStep);
			} else {
				creatureOlds.add(CreatureFactory.makeCreature(this.evolvioApplet, this));
			}
		}
	}

	public List<ISoftBody> getSoftBodiesInPosition(int x, int y) {
		return softBodiesInPositions[x][y];
	}

	public int getCreatureIdUpTo() {
		return creatureIDUpTo;
	}

	public void incrementCreatureIdUpTo() {
		creatureIDUpTo++;
	}

	private ICreature getRandomCreature() {
		int index = (int) (this.evolvioApplet.random(0, creatureOlds.size()));
		return creatureOlds.get(index);
	}

	public int getBackgroundColor() {
		return backgroundColor;
	}

	public World.Tile getTile(int x, int y) {
		return world.getTile(y,x);
	}

	public int getBoardHeight() {
		return Configuration.BOARD_HEIGHT;
	}

	private double getRandomSize() {
		return EvolvioApplet.pow(this.evolvioApplet.random(Configuration.MINIMUM_ROCK_ENERGY_BASE,
				Configuration.MAXIMUM_ROCK_ENERGY_BASE), 4);
	}

	private void drawCreature(ICreature c, float x, float y, float scale, float scaleUp) {
		this.evolvioApplet.pushMatrix();
		float scaleIconUp = scaleUp * scale;
		this.evolvioApplet.translate((float) (-c.getPx() * scaleIconUp), (float) (-c.getPy() * scaleIconUp));
		this.evolvioApplet.translate(x, y);
		c.drawSoftBody(scaleIconUp, 40.0f / scale, false);
		this.evolvioApplet.popMatrix();
	}

	public void prepareForFileSave(int type) {
		fileSaveTimes[type] = -999999;
	}

	public void fileSave() {
		for (int i = 0; i < 4; i++) {
			if (fileSaveTimes[i] < -99999) {
				fileSaveTimes[i] = year;
				if (i < 2) {
					this.evolvioApplet.saveFrame(getNextFileName(i));
				} else {
					String[] data = this.toBigString();
					this.evolvioApplet.saveStrings(getNextFileName(i), data);
				}
				fileSaveCounts[i]++;
			}
		}
	}

	public void incrementSortMetric() {
		this.sortMetric = (this.sortMetric + 1) % SORT_METRIC_NAMES.length;
	}

	public void decrementSortMetric() {
		this.sortMetric = (this.sortMetric + SORT_METRIC_NAMES.length - 1) % SORT_METRIC_NAMES.length;
	}

	private String[] toBigString() { // Convert current evolvio board into
										// string. Does not work
		String[] placeholder = { "Goo goo", "Ga ga" };
		return placeholder;
	}

	public void addCreature(ICreature creatureOld) {
		creatureOlds.add(creatureOld);
	}

	public void removeCreature(ICreature creatureOld) {
		creatureOlds.remove(creatureOld);
	}

	public ICreature getSelectedCreatureOld() {
		return selectedCreatureOld;
	}

	public void setSelectedCreatureOld(ICreature selectedCreatureOld) {
		this.selectedCreatureOld = selectedCreatureOld;
	}

	public void unselect() {
		selectedCreatureOld = null;
	}

	public ICreature getCreatureInList(int slotIndex) {
		if (slotIndex < 0 || slotIndex >= list.length) {
			return null;
		}
		return list[slotIndex];
	}
	
	public int getColorAt(double x, double y) {
		if (x >= 0 && x < Configuration.BOARD_WIDTH && y >= 0 && y < getBoardHeight()) {
            World.Tile tile = getTile((int) x, (int) y);
            // TODO creature should not know about presentation layer
            // simulation logic should not interfere with rendering thread
            return tileRenderer.getFoodColor(tile);
		} else {
			return getBackgroundColor();
		}
	}

	public void increaseTextSaveInterval() {
		this.textSaveInterval *= 2;
		if (textSaveInterval >= 0.7f) {
			textSaveInterval = Math.round(textSaveInterval);
		}
	}

	public void decreaseTextSaveInterval() {
		this.textSaveInterval /= 2;
	}

	public void increaseImageSaveInterval() {
		this.imageSaveInterval *= 2;
		if (imageSaveInterval >= 0.7f) {
			imageSaveInterval = Math.round(imageSaveInterval);
		}
	}

	public void decreaseImageSaveInterval() {
		this.imageSaveInterval /= 2;
	}

	public void increasePlaySpeed() {
		if (playSpeed == 0) {
			playSpeed = 1;
		} else {
			playSpeed *= 2;
		}
	}

	public void decreasePlaySpeed() {
		playSpeed /= 2;
	}

	public int getPlaySpeed() {
		return playSpeed;
	}

	public void setPlaySpeed(int playSpeed) {
		this.playSpeed = playSpeed;
	}

	public boolean isUserControl() {
		return userControl;
	}

	public void setUserControl(boolean isUserControl) {
		this.userControl = isUserControl;
	}

	public boolean isRender() {
		return render;
	}

	public void setRender(boolean isRender) {
		this.render = isRender;
	}

	// TODO remove all below


	public float getMinTemperature() {
		return minTemperature;
	}

	public float getMaxTemperature() {
		return maxTemperature;
	}
}