package opresearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Plus courts chemins et détection de cycle négatif dans le résiduel. */
public class BellmanFord {

    public static final long INF = Long.MAX_VALUE / 4;

    public static class Result {
        public long[] dist;
        public Edge[] parentEdge;
        public boolean hasNegativeCycleReachable;
    }

    public static Result shortestPaths(Graph g, int source) {
        Result r = new Result();
        r.dist = new long[g.n];
        r.parentEdge = new Edge[g.n];
        Arrays.fill(r.dist, INF);
        r.dist[source] = 0;

        for (int i = 0; i < g.n - 1; i++) {
            if (!relaxOnePass(g, r.dist, r.parentEdge)) break;
        }
        r.hasNegativeCycleReachable = relaxOnePass(g, r.dist, r.parentEdge);
        return r;
    }

    private static boolean relaxOnePass(Graph g, long[] dist, Edge[] parentEdge) {
        boolean changed = false;
        for (int u = 0; u < g.n; u++) {
            if (dist[u] == INF) continue;
            for (Edge e : g.outgoing(u)) {
                if (e.capacity <= 0) continue;
                long nd = dist[u] + e.cost;
                if (nd < dist[e.to]) {
                    dist[e.to] = nd;
                    parentEdge[e.to] = e;
                    changed = true;
                }
            }
        }
        return changed;
    }

    public static boolean hasNegativeCycle(Graph g) {
        return findNegativeCycle(g) != null;
    }

    /** Renvoie un cycle négatif (liste d'arcs) ou null s'il n'y en a pas.
     *  Initialise dist[v]=0 partout pour détecter un cycle où qu'il soit. */
    public static List<Edge> findNegativeCycle(Graph g) {
        long[] dist = new long[g.n];
        Edge[] parentEdge = new Edge[g.n];
        Arrays.fill(dist, 0);

        int last = -1;
        for (int i = 0; i < g.n; i++) {
            last = -1;
            for (int u = 0; u < g.n; u++) {
                for (Edge e : g.outgoing(u)) {
                    if (e.capacity <= 0) continue;
                    if (dist[u] + e.cost < dist[e.to]) {
                        dist[e.to] = dist[u] + e.cost;
                        parentEdge[e.to] = e;
                        last = e.to;
                    }
                }
            }
            if (last == -1) return null;
        }

        int v = last;
        for (int i = 0; i < g.n; i++) v = parentEdge[v].from;

        List<Edge> cycle = new ArrayList<>();
        int cur = v;
        do {
            Edge e = parentEdge[cur];
            cycle.add(e);
            cur = e.from;
        } while (cur != v);
        Collections.reverse(cycle);
        return cycle;
    }

    public static long cycleCost(List<Edge> cycle) {
        long c = 0;
        for (Edge e : cycle) c += e.cost;
        return c;
    }
}
