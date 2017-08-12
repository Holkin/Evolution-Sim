package evolv.io.model;

import evolv.io.Board;
import evolv.io.Brain;
import evolv.io.Eye;
import evolv.io.temp.ICreature;

import java.awt.geom.Point2D;
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

    private int faunaLimit = 1000;

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
        freeSlots.addAll(IntStream.range(max - 1, min - 1).boxed().collect(Collectors.toList())); // put in reverse order
    }

    private void increaseFaunaLimit() {
        int newLimit = faunaLimit * INC_LIMIT_FACTOR;
        Creature[] oldCreatures = creatures;
        creatures = initCreatureArr(newLimit);
        System.arraycopy(oldCreatures, 0, creatures, 0, faunaLimit);
        double[][] oldParams = cParams;
        cParams = new double[LENGTH][newLimit];
        for (int i=0; i<LENGTH; i++) {
            System.arraycopy(oldParams, 0, cParams, 0, faunaLimit);
        }
        initFreeSlots(faunaLimit, newLimit);
        faunaLimit = newLimit;
    }

    private Creature[] initCreatureArr(int limit) {
        Creature[] arr = new Creature[faunaLimit];
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

    public Creature produceCreature(double birthDate, double px, double py) {
        if (freeSlots.isEmpty()) {
            increaseFaunaLimit();
        }
        int index = freeSlots.pop();
        Creature creature = lookupCreature(index);
        creature.init(birthDate);
        cParams[PX][index] = px;
        cParams[PY][index] = py;
        return creature;
    }

    public void killCreature(Creature dead) {
        int index = dead.index;
        lookupCreature(index).die();
        freeSlots.push(index);
    }

    public class Creature implements ICreature {
        private final int index;
        private double birthDate;
        private boolean alive;

        public Creature(int index) {
            this.index = index;
        }

        private void init(double birthDate) {
            this.alive = true;
            this.birthDate = birthDate;
        }
        private void die() {
            alive = false;
        }

        @Override
        public void eat(double v, double v1) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void fight(double v, double v1) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void reproduce(double manualBirthSize, double timeStep) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void useBrain(double timeStep, boolean b) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void see() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void metabolize(double timeStep) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void dropEnergy(double v) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addEnergy(double safeSize) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void accelerate(double v, double v1) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void rotate(double v, double v1) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void collide(double timeStep) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void applyMotions(double v) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getId() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getGen() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public double getAge() {
            throw new UnsupportedOperationException();
        }

        @Override
        public double getBirthTime() {
            return birthDate;
        }

        @Override
        public float getPreferredRank() {
            throw new UnsupportedOperationException();
        }

        @Override
        public double getEnergy() {
            throw new UnsupportedOperationException();
        }

        @Override
        public double getBabyEnergy() {
            throw new UnsupportedOperationException();
        }

        @Override
        public double getMouthHue() {
            throw new UnsupportedOperationException();
        }

        @Override
        public double getEnergyUsage(double timeStep) {
            throw new UnsupportedOperationException();
        }

        @Override
        public double getRotation() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getParents() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Brain getBrain() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Eye> getEyes() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setPreferredRank(float v) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setEnergy(double v) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setPreviousEnergy() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setHue(double v) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setMouthHue(double v) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Point2D getPoint2D() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void drawBrain(float i, int apX, int apY) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void drawSoftBody(float scaleUp, float camZoom, boolean b) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Board getBoard() {
            throw new UnsupportedOperationException();
        }

        @Override
        public double getPx() {
            throw new UnsupportedOperationException();
        }

        @Override
        public double getPy() {
            throw new UnsupportedOperationException();
        }

        @Override
        public double getHue() {
            throw new UnsupportedOperationException();
        }

        @Override
        public double getSaturation() {
            throw new UnsupportedOperationException();
        }

        @Override
        public double getBrightness() {
            throw new UnsupportedOperationException();
        }

        @Override
        public double getRadius() {
            throw new UnsupportedOperationException();
        }
    }
}
