package evolv.io.model;

import evolv.io.Board;
import evolv.io.Brain;
import evolv.io.CreatureOld;
import evolv.io.Eye;
import evolv.io.temp.ICreature;
import evolv.io.temp.ISoftBody;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static evolv.io.model.BasicCreatureParams.*;

/**
 * Represents all living objects.
 */
public class Fauna {

    private static boolean INIT_EVERYTHING = true; // set false for faster launch
    private static int INC_LIMIT_FACTOR = 2;

    private int faunaLimit = 10;

    private Creature[] creatures;
    private Stack<Integer> freeSlots;
    private double[][] cParams;

    public Fauna() {
        freeSlots = new Stack<>();
        initFreeSlots(0, faunaLimit);
        creatures = initCreatureArr(faunaLimit);
        cParams = new double[LENGTH][faunaLimit];

    }

    private void initFreeSlots(int min, int max) {
        freeSlots.addAll(IntStream.range(min, max).boxed().collect(Collectors.toList()));
    }

    private void increaseFaunaLimit() {
        int newLimit = faunaLimit * INC_LIMIT_FACTOR;
        Creature[] oldCreatures = creatures;
        creatures = initCreatureArr(newLimit);
        System.arraycopy(oldCreatures, 0, creatures, 0, faunaLimit);
        double[][] oldParams = cParams;
        cParams = new double[LENGTH][newLimit];
        for (int i=0; i<LENGTH; i++) {
            System.arraycopy(oldParams[i], 0, cParams[i], 0, faunaLimit);
        }
        initFreeSlots(faunaLimit, newLimit);
        faunaLimit = newLimit;
    }

    private Creature[] initCreatureArr(int limit) {
        Creature[] arr = new Creature[limit];
        if (INIT_EVERYTHING) {
            for (int i=0; i<limit; i++) {
                arr[i] = new Creature(i);
            }
        }
        return arr;
    }

    private Creature lookupCreature(int index) {
        Creature creature = creatures[index];
        if (creature == null) {
            creature = new Creature(index);
            creatures[index] = creature;
        }
        return creature;
    }

    public Creature produceCreature(double birthDate, double px, double py, int generation) {
        if (freeSlots.isEmpty()) {
            increaseFaunaLimit();
        }
        int index = freeSlots.pop();
        Creature creature = lookupCreature(index);
        creature.init(birthDate, generation);
        cParams[PX][index] = px;
        cParams[PY][index] = py;
        return creature;
    }

    // TODO change signature to void
    public ICreature killCreature(Creature dead) {
        int index = dead.index;
        lookupCreature(index).die();
        freeSlots.push(index);
        return dead;
    }

    @Deprecated
    public ICreature killOldCreature(ICreature creature) {
        for (Creature c : creatures) {
            if (c.old == creature) {
                return killCreature(c);
            }
        }
        throw new IllegalArgumentException();
    }

    public Creature getNearCreature(double x, double y, double maxDistance) {
        double bestOptionDistance = Double.POSITIVE_INFINITY;
        Creature bestOption = null;

        for (Creature c : creatures) {
            if (!c.alive) {
                continue;
            }
            double distance = Math.max(0, Math.hypot(x - c.getPx(), y - c.getPy()) - c.getRadius());
            if (distance <= maxDistance && distance < bestOptionDistance) {
                bestOption = c;
                bestOptionDistance = distance;
            }
        }
        return bestOption;
    }

    public List<ISoftBody> getAliveCreatures() {
        List<ISoftBody> live = new ArrayList<>();
        for (Creature c : creatures) {
            if (c.alive) {
                live.add(c);
            }
        }
        return live;
    }

    public class Creature implements ICreature {

        //TODO remove
        public CreatureOld old;

        private final int index;
        private double birthDate;
        private boolean alive;
        private int generation;

        public Creature(int index) {
            this.index = index;
        }

        private void init(double birthDate, int generation) {
            this.alive = true;
            this.birthDate = birthDate;
            this.generation = generation;
        }
        private void die() {
            alive = false;
        }

        @Override
        public void eat(double v, double v1) {
            old.eat(v, v1);
        }

        @Override
        public void fight(double v, double v1) {
            old.fight(v, v1);
        }

        @Override
        public void reproduce(double manualBirthSize, double timeStep) {
            old.reproduce(manualBirthSize, timeStep);
        }

        @Override
        public void useBrain(double timeStep, boolean b) {
            old.useBrain(timeStep, b);
        }

        @Override
        public void see() {
            old.see();
        }

        @Override
        public void metabolize(double timeStep) {
            old.metabolize(timeStep);
        }

        @Override
        public void dropEnergy(double v) {
            old.dropEnergy(v);
        }

        @Override
        public void addEnergy(double safeSize) {
            old.addEnergy(safeSize);
        }

        @Override
        public void accelerate(double v, double v1) {
            old.accelerate(v, v1);
        }

        @Override
        public void rotate(double v, double v1) {
            old.rotate(v, v1);
        }

        @Override
        public void collide(double timeStep) {
            old.collide(timeStep);
        }

        @Override
        public void applyMotions(double v) {
            old.applyMotions(v);
        }

        @Override
        public int getId() {
            return index;
        }

        @Override
        public int getGen() {
            return old.getGen();
        }

        @Override
        public String getName() {
            return old.getName();
        }

        @Override
        public double getAge() {
            return old.getAge();
        }

        @Override
        public double getBirthTime() {
            return birthDate;
        }

        @Override
        public float getPreferredRank() {
            return old.getPreferredRank(); // TODO remake rendering
        }

        @Override
        public double getEnergy() {
            return old.getEnergy();
        }

        @Override
        public double getBabyEnergy() {
            return old.getBabyEnergy();
        }

        @Override
        public double getMouthHue() {
            return old.getMouthHue();
        }

        @Override
        public double getEnergyUsage(double timeStep) {
            return old.getEnergyUsage(timeStep);
        }

        @Override
        public double getRotation() {
            return old.getRotation();
        }

        @Override
        public String getParents() {
            return old.getParents();
        }

        @Override
        public Brain getBrain() {
            return old.getBrain();
        }

        @Override
        public List<Eye> getEyes() {
            return old.getEyes();
        }

        @Override
        public void setPreferredRank(float v) {
            old.setPreferredRank(v);
        }

        @Override
        public void setEnergy(double v) {
            old.setEnergy(v);
        }

        @Override
        public void setPreviousEnergy() {
            old.setPreviousEnergy();
        }

        @Override
        public void setHue(double v) {
            old.setHue(v);
        }

        @Override
        public void setMouthHue(double v) {
            old.setMouthHue(v);
        }

        @Override
        public Point2D getPoint2D() {
            return old.getPoint2D();
        }

        @Override
        public void drawBrain(float i, int apX, int apY) {
            if (alive) old.drawBrain(i, apX, apY);
        }

        @Override
        public void drawSoftBody(float scaleUp, float camZoom, boolean b) {
            if (alive) old.drawSoftBody(scaleUp, camZoom, b);
        }

        @Override
        public Board getBoard() {
            return old.getBoard();
        }

        @Override
        public double getPx() {
            return old.getPx();
        }

        @Override
        public double getPy() {
            return old.getPy();
        }

        @Override
        public double getHue() {
            return old.getHue();
        }

        @Override
        public double getSaturation() {
            return old.getSaturation();
        }

        @Override
        public double getBrightness() {
            return old.getBrightness();
        }

        @Override
        public double getRadius() {
            return old.getRadius();
        }
    }
}
