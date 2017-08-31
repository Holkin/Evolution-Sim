package evolv.io.temp;


public class Collision {
    private final ISoftBody a, b;

    public Collision(ISoftBody a, ISoftBody b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Collision c = (Collision) o;

        if (a == null || c.a == null || b == null || c.b == null) {
            return false;
        }
        return (a.equals(c.a) && b.equals(c.b) || (a.equals(c.b) && b.equals(c.a)));

    }

    @Override
    public int hashCode() {
        int hashA = a != null ? a.hashCode() : 0;
        int hashB = b != null ? b.hashCode() : 0;
        return hashA^hashB;
    }
}
