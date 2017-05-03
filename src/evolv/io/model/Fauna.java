package evolv.io.model;

import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represents all living objects.
 */
public class Fauna {

    private static boolean INIT_EVERYTHING = true; // set false for faster launch

    private int faunaLimit = 1000;

    private Creature[] creatures;
    private Stack<Integer> freeSlots;

    public Fauna() {
        freeSlots = new Stack<>();
        freeSlots.addAll(IntStream.range(0, faunaLimit).boxed().collect(Collectors.toList()));
        creatures = new Creature[faunaLimit];
        if (INIT_EVERYTHING) {
            for (int i=0; i<faunaLimit; i++) {
                creatures[i] = new Creature(i);
            }
        }
    }

    public Creature produceCreature(double birthDate) {
        int index = freeSlots.pop();
        Creature creature = lookupCreature(index);
        creature.init(birthDate);
        return creature;
    }

    public void killCreature(Creature dead) {
        int index = dead.index;
        lookupCreature(index).die();
        freeSlots.add(index);
    }

    private Creature lookupCreature(int index) {
        Creature creature = creatures[index];
        if (creature == null) {
            creature = new Creature(index);
            creatures[index] = creature;
        }
        return creature;
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
