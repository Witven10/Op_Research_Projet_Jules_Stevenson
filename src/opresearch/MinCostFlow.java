package opresearch;

import java.util.Arrays;
import java.util.PriorityQueue;

/** Min-cost (max-)flow par chemins augmentants successifs (SSP).
 *  Deux variantes : Bellman-Ford ou Dijkstra avec potentiels. */
public class MinCostFlow {

    public static class Result {
        public long flow;
        public long cost;
        public int iterations;

        @Override
        public String toString() {
            return String.format("flow=%d, cost=%d, iterations=%d", flow, cost, iterations);
        }
    }

    public static Result solveWithBellmanFord(Graph g, long maxFlow, boolean checkInvariant) {
        Result r = new Result();
        while (r.flow < maxFlow) {
            BellmanFord.Result bf = BellmanFord.shortestPaths(g, g.source);
            if (bf.dist[g.sink] >= BellmanFord.INF) break;

            long bottleneck = maxFlow - r.flow;
            for (int v = g.sink; v != g.source; v = bf.parentEdge[v].from) {
                bottleneck = Math.min(bottleneck, bf.parentEdge[v].capacity);
            }
            for (int v = g.sink; v != g.source; v = bf.parentEdge[v].from) {
                bf.parentEdge[v].pushFlow(bottleneck);
            }
            r.flow += bottleneck;
            r.cost += bottleneck * bf.dist[g.sink];
            r.iterations++;

            if (checkInvariant && BellmanFord.hasNegativeCycle(g)) {
                throw new AssertionError("Cycle négatif détecté dans le résiduel après l'itération "
                        + r.iterations);
            }
        }
        return r;
    }

    public static Result solveWithDijkstra(Graph g, long maxFlow, boolean checkInvariant) {
        Result r = new Result();

        // Init des potentiels par un Bellman-Ford depuis la source
        long[] potential = new long[g.n];
        BellmanFord.Result init = BellmanFord.shortestPaths(g, g.source);
        for (int v = 0; v < g.n; v++) {
            potential[v] = (init.dist[v] >= BellmanFord.INF) ? 0 : init.dist[v];
        }

        long[] dist = new long[g.n];
        Edge[] parentEdge = new Edge[g.n];

        while (r.flow < maxFlow) {
            // Dijkstra avec coûts réduits c'(u,v) = c(u,v) + φ[u] - φ[v]
            Arrays.fill(dist, BellmanFord.INF);
            Arrays.fill(parentEdge, null);
            dist[g.source] = 0;
            PriorityQueue<long[]> pq = new PriorityQueue<>((a, b) -> Long.compare(a[0], b[0]));
            pq.add(new long[]{0L, g.source});

            while (!pq.isEmpty()) {
                long[] top = pq.poll();
                long d = top[0];
                int u = (int) top[1];
                if (d > dist[u]) continue;

                for (Edge e : g.outgoing(u)) {
                    if (e.capacity <= 0) continue;
                    long reduced = e.cost + potential[u] - potential[e.to];
                    if (reduced < 0) {
                        throw new IllegalStateException(
                            "Coût réduit négatif (" + reduced + ") sur arc " + e);
                    }
                    long nd = d + reduced;
                    if (nd < dist[e.to]) {
                        dist[e.to] = nd;
                        parentEdge[e.to] = e;
                        pq.add(new long[]{nd, e.to});
                    }
                }
            }
            if (dist[g.sink] >= BellmanFord.INF) break;

            // Mise à jour des potentiels
            for (int v = 0; v < g.n; v++) {
                if (dist[v] < BellmanFord.INF) potential[v] += dist[v];
            }

            long bottleneck = maxFlow - r.flow;
            for (int v = g.sink; v != g.source; v = parentEdge[v].from) {
                bottleneck = Math.min(bottleneck, parentEdge[v].capacity);
            }
            long pathRealCost = 0;
            for (int v = g.sink; v != g.source; v = parentEdge[v].from) {
                pathRealCost += parentEdge[v].cost;
                parentEdge[v].pushFlow(bottleneck);
            }
            r.flow += bottleneck;
            r.cost += bottleneck * pathRealCost;
            r.iterations++;

            if (checkInvariant && BellmanFord.hasNegativeCycle(g)) {
                throw new AssertionError("Cycle négatif détecté dans le résiduel après l'itération "
                        + r.iterations);
            }
        }
        return r;
    }

    public static Result solveWithBellmanFord(Graph g, long maxFlow) {
        return solveWithBellmanFord(g, maxFlow, true);
    }

    public static Result solveWithDijkstra(Graph g, long maxFlow) {
        return solveWithDijkstra(g, maxFlow, true);
    }

    public static Result minCostMaxFlowBellmanFord(Graph g) {
        return solveWithBellmanFord(g, Long.MAX_VALUE / 4, true);
    }

    public static Result minCostMaxFlowDijkstra(Graph g) {
        return solveWithDijkstra(g, Long.MAX_VALUE / 4, true);
    }
}
