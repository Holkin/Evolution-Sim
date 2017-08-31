package evolv.io.temp;


import evolv.io.model.World;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CollisionDetector {
    public static Set<Collision> getCollisions(World world, List<ISoftBody> bodies) {
        System.out.println("get collisions start");
        int[][] m = world.getPositions();
        clear(m);
        Set<Collision> set = new HashSet<>();
        for (ISoftBody body : bodies) {
            draw(body, m, set);
        }
        System.out.println("get collisions return "+set.size());
        return set;
    }

    private static void draw(ISoftBody body, int[][] m, Set<Collision> set) {
        int offx = (int) Math.round(body.getPx() * World.RESOLUTION);
        int offy = (int) Math.round(body.getPy() * World.RESOLUTION);
        int r = (int) Math.round(body.getRadius() * World.RESOLUTION);
        int id = body.getId();

        double r2 = (r + .5) * (r + .5);
        for (int i = 0; i <= r; i++) {
            int x = (int) (Math.sqrt(r2 - i * i));
            for (int j = offx - x; j <= offx + x; j++) {
                try {
                    if (m[offy + i][j] < 0) {
                        m[offy + i][j] = id;
                    } else {
                        set.add(new Collision(body, Singletons.FAUNA.getById(m[offy + i][j])));
                        System.out.printf("new collision %d %d\n", id, m[offy + i][j]);
                    }
                    if (i == 0) continue;
                    if (m[offy - i][j] < 0) {
                        m[offy - i][j] = id;
                    } else {
                        set.add(new Collision(body, Singletons.FAUNA.getById(m[offy - i][j])));
                        System.out.printf("new collision %d %d\n", id, m[offy - i][j]);
                    }
                } catch (ArrayIndexOutOfBoundsException ex) {
                    continue; // it's ok
                }
            }
        }
    }

    private static void clear(int[][] m) {
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[i].length; j++) {
                m[i][j] = -1;
            }
        }
    }

}
