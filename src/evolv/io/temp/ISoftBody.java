package evolv.io.temp;

/**
 * Temporary solution for refactoring. Consider removal.
 */
public interface ISoftBody {
    int getId(); // for collision detection

    double getPx();
    double getPy();

    double getHue();
    double getSaturation();
    double getBrightness();

    double getRadius();
}
