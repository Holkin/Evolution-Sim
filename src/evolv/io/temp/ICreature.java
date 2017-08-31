package evolv.io.temp;


import evolv.io.Board;
import evolv.io.Brain;
import evolv.io.Eye;

import java.awt.geom.Point2D;
import java.util.List;

/**
 * Temporary solution for refactoring. Consider removal.
 */
public interface ICreature extends ISoftBody {

    void eat(double v, double v1);
    void fight(double v, double v1);
    void reproduce(double manualBirthSize, double timeStep);
    void useBrain(double timeStep, boolean b);
    void see();
    void metabolize(double timeStep);
    void dropEnergy(double v);
    void addEnergy(double safeSize);

    void accelerate(double v, double v1);
    void rotate(double v, double v1);
    void collide(double timeStep);
    void applyMotions(double v);


    int getGen();
    String getName();
    double getAge();
    double getBirthTime();
    float getPreferredRank();
    double getEnergy();
    double getBabyEnergy();
    double getMouthHue();
    double getEnergyUsage(double timeStep);
    double getRotation();
    String getParents();
    Brain getBrain();
    List<Eye> getEyes();

    void setPreferredRank(float v);
    void setEnergy(double v);
    void setPreviousEnergy();
    void setHue(double v);
    void setMouthHue(double v);

    Point2D getPoint2D(); // TODO remove this
    void drawBrain(float i, int apX, int apY); // TODO remove this
    void drawSoftBody(float scaleUp, float camZoom, boolean b);
    Board getBoard(); // TODO OMG!!! do something about that!!!
}
