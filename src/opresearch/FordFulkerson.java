package opresearch;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Max flow + min cut via Edmonds-Karp (Ford-Fulkerson avec BFS). */
public class FordFulkerson {

    public static class Result {
        public long maxFlow;
        public Set<Integer> minCutSourceSide;
        public List<Edge> minCutEdges;
        public long minCutCapacity;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Max flow value : ").append(maxFlow).append('\n');
            sb.append("Min cut (côté source) : ").append(minCutSourceSide).append('\n');
            sb.append("Min cut capacité : ").append(minCutCapacity).append('\n');
            sb.append("Arcs de la coupe :\n");
            for (Edge e : minCutEdges) {
                sb.append(String.format("  (%d -> %d) capa=%d%n", e.from, e.to, e.initialCapacity));
            }
            return sb.toString();
        }
    }

    public static Result solve(Graph g) {
        long totalFlow = 0;
        Edge[] parentEdge = new Edge[g.n];

        while (bfs(g, parentEdge)) {
            long bottleneck = Long.MAX_VALUE;
            for (int v = g.sink; v != g.source; v = parentEdge[v].from) {
                bottleneck = Math.min(bottleneck, parentEdge[v].capacity);
            }
            for (int v = g.sink; v != g.source; v = parentEdge[v].from) {
                parentEdge[v].pushFlow(bottleneck);
            }
            totalFlow += bottleneck;
        }

        Result r = new Result();
        r.maxFlow = totalFlow;
        r.minCutSourceSide = reachableFromSource(g);
        r.minCutEdges = new ArrayList<>();
        long cutCapa = 0;
        for (Edge e : g.originalEdges) {
            if (r.minCutSourceSide.contains(e.from) && !r.minCutSourceSide.contains(e.to)) {
                r.minCutEdges.add(e);
                cutCapa += e.initialCapacity;
            }
        }
        r.minCutCapacity = cutCapa;
        return r;
    }

    private static boolean bfs(Graph g, Edge[] parentEdge) {
        Arrays.fill(parentEdge, null);
        boolean[] visited = new boolean[g.n];
        Deque<Integer> queue = new ArrayDeque<>();
        queue.add(g.source);
        visited[g.source] = true;
        while (!queue.isEmpty()) {
            int u = queue.poll();
            if (u == g.sink) return true;
            for (Edge e : g.outgoing(u)) {
                if (!visited[e.to] && e.capacity > 0) {
                    visited[e.to] = true;
                    parentEdge[e.to] = e;
                    queue.add(e.to);
                }
            }
        }
        return visited[g.sink];
    }

    public static Set<Integer> reachableFromSource(Graph g) {
        Set<Integer> set = new HashSet<>();
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(g.source);
        set.add(g.source);
        while (!stack.isEmpty()) {
            int u = stack.pop();
            for (Edge e : g.outgoing(u)) {
                if (e.capacity > 0 && !set.contains(e.to)) {
                    set.add(e.to);
                    stack.push(e.to);
                }
            }
        }
        return set;
    }
}
