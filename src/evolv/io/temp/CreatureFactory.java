package evolv.io.temp;


import evolv.io.*;
import evolv.io.model.Fauna;

public class CreatureFactory {

    private static final Fauna fauna = Singletons.FAUNA;

    public static ICreature makeCreature(
            EvolvioApplet evolvioApplet,
            Board board,
            double px, double py,
            double tvx, double tvy,
            double tenergy,
            double thue, double tsaturation, double tbrightness,
            double rot,
            double tvr,
            String tname,
            String tparents,
            boolean mutateName,
            Brain brain,
            int gen,
            double tmouthHue,
            double[] teyeDistances,
            double[] teyeAngles
    ) {
        Fauna.Creature creature = fauna.produceCreature(board.getYear(), px, py, gen);
        creature.old = new CreatureOld(evolvioApplet, board, px, py, tvx, tvy, tenergy, thue, tsaturation, tbrightness,
                rot, tvr, tname, tparents, mutateName, brain, gen, tmouthHue, teyeDistances, teyeAngles);
        return creature;
    }

    public static ICreature makeCreature(EvolvioApplet evolvioApplet, Board board) {
        return makeCreature(evolvioApplet, board, evolvioApplet.random(0, Configuration.BOARD_WIDTH),
                evolvioApplet.random(0, board.getBoardHeight()), 0, 0,
                evolvioApplet.random(Configuration.MINIMUM_CREATURE_ENERGY, Configuration.MAXIMUM_CREATURE_ENERGY),
                evolvioApplet.random(0, 1), 1, 1, evolvioApplet.random(0, 2 * EvolvioApplet.PI), 0, "", "[PRIMORDIAL]",
                true, null, 1, evolvioApplet.random(0, 1), new double[Configuration.NUM_EYES], new double[Configuration.NUM_EYES]);
    }

    public static ICreature removeCreature(ICreature creature) {
        try {
            return fauna.killCreature((Fauna.Creature) creature);
        } catch (ClassCastException ex) {
            return fauna.killOldCreature(creature);
        }
    }
}
