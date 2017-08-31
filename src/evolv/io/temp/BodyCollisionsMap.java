package evolv.io.temp;


import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

// TODO rework this shit
public class BodyCollisionsMap {
    private static Map<ISoftBody, Collection<ISoftBody>> map = Collections.synchronizedMap(new HashMap<>());
    private static ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    public static Collection<ISoftBody> getCollidedBodies(ISoftBody creature) {
        Collection<ISoftBody> list = map.get(creature);
        return list == null ? Collections.EMPTY_LIST : list;
    }

    public static void update() {
        map.clear();
        List<ISoftBody> list = getPotentiallyCollidedBodies();
        for (ISoftBody body : list) {
            executor.execute(() -> {
                Collection<ISoftBody> colliders = new HashSet<>();
                int left = (int) Math.floor(body.getPx() - body.getRadius());
                int right = (int) Math.ceil(body.getPx() + body.getRadius());
                int up = (int) Math.floor(body.getPy() - body.getRadius());
                int bottom = (int) Math.ceil(body.getPy() + body.getRadius());
                for (int i = left; i <= right; i++) {
                    for (int j = up; j <= bottom; j++) {
                        String key = i + "." + j;
                        List<ISoftBody> potentialColliders = BodyPositionsMap.getMap().get(key);
                        for (ISoftBody collider : potentialColliders) {
                            if (collide(collider, body)) {
                                colliders.add(collider);
                            }
                        }
                    }
                }
                map.put(body, colliders);
            });
        }
    }

    private static List<ISoftBody> getPotentiallyCollidedBodies() {
        List<ISoftBody> result = Singletons.FAUNA.getAliveCreatures();
        for (Map.Entry<String, List<ISoftBody>> entry : BodyPositionsMap.getMap().entrySet()) {
            List<ISoftBody> list = entry.getValue();
            if (list.size() < 2) {
                result.removeAll(list);
            }
        }
        return result;
    }

    private static boolean collide(ISoftBody body1, ISoftBody body2) {
        double d = Math.hypot(body1.getPx() - body2.getPx(), body1.getPy() - body2.getPy());
        d -= body2.getRadius() + body1.getRadius();
        return d <= 0 && body2 != body1;
    }
}
