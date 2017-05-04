package evolv.io.model;

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

    public class Creature {
        private final int index;
        private double birthDate;
        private boolean alive;

        public Creature(int index) {
            this.index = index;
        }

        public double getBirthDate() {
            return birthDate;
        }
        private void init(double birthDate) {
            this.alive = true;
            this.birthDate = birthDate;
        }
        private void die() {
            alive = false;
        }
    }
}
