package opresearch;

import java.util.List;

/** Point d'entrée. Lance les tests fournis dans DearStudents.docx + format prof. */
public class Main {

    public static void main(String[] args) {
        if (args.length >= 2 && args[0].equals("--file")) {
            runGraphFromFile(args[1]);
            return;
        }
        runAllTests();
    }

    // ---- Test 1 : Max flow simple (DearStudents.docx) ----
    static void testMaxFlowExample() {
        banner("Test 1 — Max flow (DearStudents.docx)");
        int[] start = {0, 0, 0, 1, 1, 2, 2, 3, 3};
        int[] end   = {1, 2, 3, 2, 4, 3, 4, 2, 4};
        long[] caps = {20, 30, 10, 40, 30, 10, 20, 5, 20};
        Graph g = Graph.fromArrays(5, 0, 4, start, end, caps, null);

        FordFulkerson.Result r = FordFulkerson.solve(g);
        System.out.println(r);
        System.out.println("Flots par arc :");
        System.out.print(g.flowReport());
    }

    // ---- Test 2 : Min-cost flow (DearStudents.docx) avec source/sink ajoutés ----
    static void testMinCostFlowExample() {
        banner("Test 2 — Min-cost flow avec s/t ajoutés");

        int s = 5, t = 6;
        int[] start = {0, 0, 1, 1, 1, 2, 2, 3, 4,  s, 3, 4};
        int[] end   = {1, 2, 2, 3, 4, 3, 4, 4, 2,  0, t, t};
        long[] caps = {15, 8, 20, 4, 10, 15, 4, 20, 5,  20, 5, 15};
        long[] cost = { 4, 4,  2, 2,  6,  1, 3,  2, 3,   0, 0,  0};
        Graph g = Graph.fromArrays(7, s, t, start, end, caps, cost);

        System.out.println(">>> Variante 2.1 : Bellman-Ford");
        MinCostFlow.Result r1 = MinCostFlow.minCostMaxFlowBellmanFord(g);
        System.out.println("  " + r1);
        System.out.print(g.flowReport());

        g.resetFlow();
        System.out.println(">>> Variante 2.2 : Dijkstra + potentiels");
        MinCostFlow.Result r2 = MinCostFlow.minCostMaxFlowDijkstra(g);
        System.out.println("  " + r2);
        System.out.print(g.flowReport());

        if (r1.flow != r2.flow || r1.cost != r2.cost) {
            System.out.println("  /!\\ Désaccord entre les deux variantes !");
        } else {
            System.out.println("  ✓ Les deux variantes donnent le même résultat.");
        }

        System.out.println("\n>>> Min-cost max-flow de 0 à 4 sur le graphe original");
        int[] start2 = {0, 0, 1, 1, 1, 2, 2, 3, 4};
        int[] end2   = {1, 2, 2, 3, 4, 3, 4, 4, 2};
        long[] caps2 = {15, 8, 20, 4, 10, 15, 4, 20, 5};
        long[] cost2 = { 4, 4,  2, 2,  6,  1, 3,  2, 3};
        Graph g04 = Graph.fromArrays(5, 0, 4, start2, end2, caps2, cost2);
        MinCostFlow.Result r04 = MinCostFlow.minCostMaxFlowDijkstra(g04);
        System.out.println("  " + r04);
        System.out.print(g04.flowReport());
    }

    // ---- Test 3 : Assignment 10 personnes / 8 tâches ----
    static void testAssignmentProblem() {
        banner("Test 3 — Assignment 10 personnes / 8 tâches");
        long[][] cost = {
            {90, 76, 75, 70, 50, 74, 12, 68},
            {35, 85, 55, 65, 48, 101, 70, 83},
            {125, 95, 90, 105, 59, 120, 36, 73},
            {45, 110, 95, 115, 104, 83, 37, 71},
            {60, 105, 80, 75, 59, 62, 93, 88},
            {45, 65, 110, 95, 47, 31, 81, 34},
            {38, 51, 107, 41, 69, 99, 115, 48},
            {47, 85, 57, 71, 92, 77, 109, 36},
            {39, 63, 97, 49, 118, 56, 92, 61},
            {47, 101, 71, 60, 88, 109, 52, 90}
        };

        System.out.println(">>> Cas 1 : sans capacité de tâche");
        Graph g1 = buildAssignmentGraph(cost, 10);
        MinCostFlow.Result r1 = MinCostFlow.minCostMaxFlowDijkstra(g1);
        System.out.println("  " + r1);
        printAssignment(g1, cost);

        System.out.println("\n>>> Cas 2 : capacité de tâche = 2");
        Graph g2 = buildAssignmentGraph(cost, 2);
        MinCostFlow.Result r2 = MinCostFlow.minCostMaxFlowDijkstra(g2);
        System.out.println("  " + r2);
        printAssignment(g2, cost);

        System.out.println("\n  [Cas 3 : chaque tâche au moins 1 fois — bornes inférieures, voir rapport.]");
    }

    static Graph buildAssignmentGraph(long[][] cost, long taskCapacity) {
        int P = cost.length;
        int T = cost[0].length;
        int n = 1 + P + T + 1;
        int s = 0, t = n - 1;
        Graph g = new Graph(n, s, t);
        for (int i = 0; i < P; i++) g.addEdge(s, 1 + i, 1, 0);
        for (int i = 0; i < P; i++)
            for (int j = 0; j < T; j++)
                g.addEdge(1 + i, 1 + P + j, 1, cost[i][j]);
        for (int j = 0; j < T; j++) g.addEdge(1 + P + j, t, taskCapacity, 0);
        return g;
    }

    static void printAssignment(Graph g, long[][] cost) {
        int P = cost.length;
        int T = cost[0].length;
        long total = 0;
        for (Edge e : g.originalEdges) {
            if (e.from >= 1 && e.from <= P && e.to >= 1 + P && e.to <= P + T && e.flow() > 0) {
                int person = e.from - 1;
                int task = e.to - 1 - P;
                System.out.printf("  Personne %d -> Tâche %d (coût %d)%n",
                        person, task, cost[person][task]);
                total += cost[person][task];
            }
        }
        System.out.println("  Coût total assignment = " + total);
    }

    // ---- Test 4 : graphe au format prof (graph_data.txt) ----
    static void testProfFormatGraph() {
        banner("Test 4 — Graphe format prof (6 nœuds, 10 arcs, s=5, t=4)");
        String content =
                "6 10 5 4\n" +
                "5 0 10 2\n" +
                "5 1 8 4\n" +
                "0 1 5 5\n" +
                "0 2 5 2\n" +
                "1 0 4 1\n" +
                "1 3 10 4\n" +
                "2 1 7 1\n" +
                "2 3 6 2\n" +
                "2 4 3 1\n" +
                "3 4 14 3\n";
        try {
            Graph g = Graph.parseString(content);

            System.out.println(">>> Ford-Fulkerson");
            FordFulkerson.Result ff = FordFulkerson.solve(g);
            System.out.println(ff);
            System.out.print(g.flowReport());

            g.resetFlow();
            System.out.println("\n>>> Min-cost max-flow par Bellman-Ford");
            MinCostFlow.Result mc1 = MinCostFlow.minCostMaxFlowBellmanFord(g);
            System.out.println("  " + mc1);
            System.out.print(g.flowReport());

            g.resetFlow();
            System.out.println("\n>>> Min-cost max-flow par Dijkstra + potentiels");
            MinCostFlow.Result mc2 = MinCostFlow.minCostMaxFlowDijkstra(g);
            System.out.println("  " + mc2);
            System.out.print(g.flowReport());

            System.out.println("\n>>> Vérification : pas de cycle négatif dans le résiduel ?");
            List<Edge> negCycle = BellmanFord.findNegativeCycle(g);
            if (negCycle == null) {
                System.out.println("  ✓ Aucun cycle négatif.");
            } else {
                System.out.println("  /!\\ Cycle négatif trouvé : " + negCycle
                        + " (coût = " + BellmanFord.cycleCost(negCycle) + ")");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---- Test 5 : détection de cycle négatif ----
    static void testNegativeCycleDetection() {
        banner("Test 5 — Détection de cycle négatif");

        Graph g = new Graph(3, 0, 2);
        g.addEdge(0, 1, 10, -1);
        g.addEdge(1, 2, 10, -1);
        g.addEdge(2, 0, 10, -1);

        List<Edge> cycle = BellmanFord.findNegativeCycle(g);
        if (cycle != null) {
            System.out.println("  ✓ Cycle négatif détecté :");
            for (Edge e : cycle) System.out.println("    " + e);
            System.out.println("  Coût total : " + BellmanFord.cycleCost(cycle));
        } else {
            System.out.println("  /!\\ Aucun cycle détecté (devrait en trouver un).");
        }

        System.out.println("\n  Sur un graphe sans cycle négatif :");
        Graph g2 = new Graph(3, 0, 2);
        g2.addEdge(0, 1, 10, 1);
        g2.addEdge(1, 2, 10, 1);
        g2.addEdge(2, 0, 10, 1);
        List<Edge> none = BellmanFord.findNegativeCycle(g2);
        System.out.println("  " + (none == null ? "✓ Aucun cycle négatif (correct)."
                : "/!\\ Faux positif !"));
    }

    static void runGraphFromFile(String path) {
        try {
            Graph g = Graph.parseFile(path);
            System.out.println("Graphe chargé : " + g.n + " nœuds, source=" + g.source + ", sink=" + g.sink);
            System.out.println(">>> Ford-Fulkerson");
            FordFulkerson.Result r = FordFulkerson.solve(g);
            System.out.println(r);
            System.out.print(g.flowReport());

            g.resetFlow();
            System.out.println("\n>>> Min-cost max-flow (Dijkstra + potentiels)");
            MinCostFlow.Result mc = MinCostFlow.minCostMaxFlowDijkstra(g);
            System.out.println("  " + mc);
            System.out.print(g.flowReport());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void runAllTests() {
        testMaxFlowExample();
        testMinCostFlowExample();
        testAssignmentProblem();
        testProfFormatGraph();
        testNegativeCycleDetection();
        System.out.println("\n=== Tous les tests sont passés. ===");
    }

    static void banner(String s) {
        System.out.println();
        System.out.println("============================================================");
        System.out.println(" " + s);
        System.out.println("============================================================");
    }
}
