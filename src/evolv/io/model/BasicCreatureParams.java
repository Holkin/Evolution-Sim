package evolv.io.model;

/**
 * Values are used as index in array, i.e. arr[SAT] --> arr[5]
 */
public interface BasicCreatureParams {
    int PX = 0;
    int PY = 1;
    int VX = 2;
    int VY = 3;
    int HUE = 4;
    int SAT = 5;
    int BRI = 6;
    int EN = 7; // energy
    int FL = 8; // fight level

    int LENGTH = 9; // inc this value when add new constant
}
