package evolv.io.temp;


import evolv.io.*;

public class CreatureFactory {
    public static ICreature makeCreature(
            EvolvioApplet evolvioApplet,
            Board board,
            double tpx, double tpy,
            double tvx, double tvy,
            double tenergy,
            double thue, double tsaturation, double tbrightness,
            double rot,
            double tvr,
            String tname,
            String tparents,
            boolean mutateName,
            Brain brain,
            int tgen,
            double tmouthHue,
            double[] teyeDistances,
            double[] teyeAngles
    ) {
        return new CreatureOld(evolvioApplet, board, tpx, tpy, tvx, tvy, tenergy, thue, tsaturation, tbrightness,
                rot, tvr, tname, tparents, mutateName, brain, tgen, tmouthHue, teyeDistances, teyeAngles);
    }

    public static ICreature makeCreature(EvolvioApplet evolvioApplet, Board board) {
        return makeCreature(evolvioApplet, board, evolvioApplet.random(0, Configuration.BOARD_WIDTH),
                evolvioApplet.random(0, board.getBoardHeight()), 0, 0,
                evolvioApplet.random(Configuration.MINIMUM_CREATURE_ENERGY, Configuration.MAXIMUM_CREATURE_ENERGY),
                evolvioApplet.random(0, 1), 1, 1, evolvioApplet.random(0, 2 * EvolvioApplet.PI), 0, "", "[PRIMORDIAL]",
                true, null, 1, evolvioApplet.random(0, 1), new double[Configuration.NUM_EYES], new double[Configuration.NUM_EYES]);
    }
}
