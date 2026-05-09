package opresearch;

/** Arc du graphe résiduel. Chaque arc original (u,v) est représenté par
 *  deux Edge liés via {@code reverse} : un forward et un backward. */
public class Edge {

    public final int from;
    public final int to;
    public final long cost;
    public final long initialCapacity;

    public long capacity;
    public Edge reverse;

    public Edge(int from, int to, long capacity, long cost) {
        this.from = from;
        this.to = to;
        this.capacity = capacity;
        this.initialCapacity = capacity;
        this.cost = cost;
    }

    public boolean isForward() {
        return initialCapacity > 0;
    }

    /** Flot actuel sur l'arc original = capacité de l'arc inverse. */
    public long flow() {
        return reverse.capacity;
    }

    public void pushFlow(long amount) {
        this.capacity -= amount;
        this.reverse.capacity += amount;
    }

    @Override
    public String toString() {
        return String.format("(%d->%d, cap=%d, cost=%d)", from, to, capacity, cost);
    }
}
