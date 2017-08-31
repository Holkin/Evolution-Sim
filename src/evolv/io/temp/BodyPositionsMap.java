package evolv.io.temp;


import java.util.*;

public class BodyPositionsMap {
    private static Map<String, List<ISoftBody>> map = new HashMap<>();
    public static List<ISoftBody> getBodiesAtPosition(int x, int y) {
        List<ISoftBody> list = map.get(x + "." + y);
        return list == null ? Collections.EMPTY_LIST : list;
    }
    public static void update() {
        map.clear();
        List<ISoftBody> list = Singletons.FAUNA.getAliveCreatures();
        for (ISoftBody body : list) {
            int left = (int) Math.floor(body.getPx() - body.getRadius());
            int right = (int) Math.ceil(body.getPx() + body.getRadius());
            int up = (int) Math.floor(body.getPy() - body.getRadius());
            int bottom = (int) Math.ceil(body.getPy() + body.getRadius());
            for (int i=left; i<=right; i++) {
                for (int j=up; j<=bottom; j++) {
                    String key = i + "." + j;
                    List<ISoftBody> bodies = map.get(key);
                    if (bodies == null) {
                        bodies = new ArrayList<>();
                        map.put(key, bodies);
                    }
                    bodies.add(body);
                }
            }
        }
    }

    public static Map<String, List<ISoftBody>> getMap() {
        return map;
    }
}
