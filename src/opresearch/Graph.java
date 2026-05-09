package opresearch;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Graphe orienté capacité-coût en liste d'adjacence sur le résiduel. */
public class Graph {

    public final int n;
    public int source;
    public int sink;
    public final List<List<Edge>> adj;
    public final List<Edge> originalEdges;

    public Graph(int n, int source, int sink) {
        this.n = n;
        this.source = source;
        this.sink = sink;
        this.adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        this.originalEdges = new ArrayList<>();
    }

    public Edge addEdge(int from, int to, long capacity, long cost) {
        Edge forward  = new Edge(from, to, capacity, cost);
        Edge backward = new Edge(to, from, 0, -cost);
        forward.reverse = backward;
        backward.reverse = forward;
        adj.get(from).add(forward);
        adj.get(to).add(backward);
        originalEdges.add(forward);
        return forward;
    }

    public List<Edge> outgoing(int u) {
        return adj.get(u);
    }

    public void resetFlow() {
        for (Edge e : originalEdges) {
            e.capacity = e.initialCapacity;
            e.reverse.capacity = 0;
        }
    }

    // ---- Parsing format prof : "N M s t" puis M lignes "u v capa cost" ----

    public static Graph parseFile(String path) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            return parseReader(br);
        }
    }

    public static Graph parseString(String content) throws IOException {
        try (BufferedReader br = new BufferedReader(new StringReader(content))) {
            return parseReader(br);
        }
    }

    private static Graph parseReader(BufferedReader br) throws IOException {
        StreamTokenizer st = new StreamTokenizer(br);
        st.nextToken(); int n = (int) st.nval;
        st.nextToken(); int m = (int) st.nval;
        st.nextToken(); int s = (int) st.nval;
        st.nextToken(); int t = (int) st.nval;
        Graph g = new Graph(n, s, t);
        for (int i = 0; i < m; i++) {
            st.nextToken(); int u = (int) st.nval;
            st.nextToken(); int v = (int) st.nval;
            st.nextToken(); long c = (long) st.nval;
            st.nextToken(); long k = (long) st.nval;
            g.addEdge(u, v, c, k);
        }
        return g;
    }

    public static Graph fromArrays(int n, int source, int sink,
                                   int[] startNodes, int[] endNodes,
                                   long[] capacities, long[] unitCosts) {
        Graph g = new Graph(n, source, sink);
        for (int i = 0; i < startNodes.length; i++) {
            long cost = (unitCosts == null) ? 0 : unitCosts[i];
            g.addEdge(startNodes[i], endNodes[i], capacities[i], cost);
        }
        return g;
    }

    // ---- Sortie ----

    public String flowReport() {
        StringBuilder sb = new StringBuilder();
        long totalCost = 0;
        for (Edge e : originalEdges) {
            sb.append(String.format(Locale.ROOT,
                    "  arc (%d -> %d) : flow = %d / %d, cost unit = %d, contribution = %d%n",
                    e.from, e.to, e.flow(), e.initialCapacity, e.cost, e.flow() * e.cost));
            totalCost += e.flow() * e.cost;
        }
        sb.append(String.format(Locale.ROOT, "  total cost = %d%n", totalCost));
        return sb.toString();
    }

    public long totalFlowValue() {
        long sum = 0;
        for (Edge e : adj.get(source)) {
            if (e.isForward()) sum += e.flow();
        }
        return sum;
    }

    public long totalCost() {
        long c = 0;
        for (Edge e : originalEdges) c += e.flow() * e.cost;
        return c;
    }
}
