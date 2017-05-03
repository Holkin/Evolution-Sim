package evolv.io.util;


import processing.core.PApplet;

public class ColorUtil {
    // TODO javadoc; what a, b, x mean?
    public static int interColorFixedHue(int a, int b, double x, double hue, PApplet applet) {
        double satB = applet.saturation(b);
        if (applet.brightness(b) == 0) {
            // I want black to be calculated as 100% saturation
            satB = 1;
        }
        double sat = inter(applet.saturation(a), satB, x);
        double bri = inter(applet.brightness(a), applet.brightness(b), x);
        // I know it's dumb to do interpolation with HSL but oh well
        return applet.color((float) (hue), (float) (sat), (float) (bri));
    }

    public static int interColor(int a, int b, double x, PApplet applet) {
        double hue = inter(applet.hue(a), applet.hue(b), x);
        double sat = inter(applet.saturation(a), applet.saturation(b), x);
        double bri = inter(applet.brightness(a), applet.brightness(b), x);
        // I know it's dumb to do interpolation with HSL but oh well
        return applet.color((float) (hue), (float) (sat), (float) (bri));
    }

    public static double inter(double a, double b, double x) {
        return a + (b - a) * x;
    }
}
