package app;

import com.google.gson.Gson;
import graph.common.Graph;
import graph.common.Metrics;
import graph.dagsp.DagShortestPaths;
import graph.scc.CondensationGraphBuilder;
import graph.scc.TarjanSCC;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {

    static class JsonEdge {
        int u;
        int v;
        int w;
    }

    static class JsonGraph {
        boolean directed;
        int n;
        JsonEdge[] edges;
        Integer source;
        String weight_model;
    }

    public static Graph loadGraph(String path, int[] sourceOut) throws IOException {
        String json = Files.readString(Path.of(path));
        Gson gson = new Gson();
        JsonGraph jg = gson.fromJson(json, JsonGraph.class);

        Graph g = new Graph(jg.n, jg.directed);
        for (JsonEdge e : jg.edges) {
            g.addEdge(e.u, e.v, e.w);
        }

        if (jg.source != null) {
            sourceOut[0] = jg.source;
        } else {
            sourceOut[0] = 0;
        }

        System.out.println("Loaded graph: n=" + jg.n +
                ", edges=" + jg.edges.length +
                ", weight_model=" + jg.weight_model);
        return g;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: java app.Main <path-to-tasks.json>");
            return;
        }

        int[] sourceHolder = new int[1];
        Graph g = loadGraph(args[0], sourceHolder);
        int source = sourceHolder[0];

        Metrics sccMetrics = new Metrics();
        TarjanSCC tarjan = new TarjanSCC(g, sccMetrics);
        TarjanSCC.Result sccRes = tarjan.run();

        System.out.println("=== Strongly Connected Components (SCC) ===");
        int cid = 0;
        for (List<Integer> comp : sccRes.components) {
            System.out.println("Component " + cid + " (size=" + comp.size() + "): " + comp);
            cid++;
        }
        System.out.println("Total SCCs = " + sccRes.components.size());
        System.out.println("Tarjan: dfsVisits=" + sccMetrics.dfsVisits +
                ", dfsEdges=" + sccMetrics.dfsEdges +
                ", time=" + sccMetrics.elapsedMillis() + " ms");

        Metrics condMetrics = new Metrics();
        Graph dag = CondensationGraphBuilder.buildCondensation(
                g, sccRes.compId, sccRes.components.size(), condMetrics
        );

        System.out.println("\n=== Condensation DAG ===");
        for (int u = 0; u < dag.n(); u++) {
            System.out.print("C" + u + " -> ");
            for (Graph.Edge e : dag.neighbors(u)) {
                System.out.print("C" + e.to + "(w=" + e.weight + ") ");
            }
            System.out.println();
        }

        Metrics topoMetrics = new Metrics();
        List<Integer> topoOrder = graph.topo.TopologicalSort.kahn(dag, topoMetrics);

        System.out.println("\n=== Topological order of components ===");
        System.out.println(topoOrder);
        System.out.println("Derived order of original tasks:");
        for (int c : topoOrder) {
            System.out.println("Component " + c + " -> " + sccRes.components.get(c));
        }
        System.out.println("Kahn: pushes=" + topoMetrics.topoPushes +
                ", pops=" + topoMetrics.topoPops);

        Metrics spMetrics = new Metrics();
        int sourceComp = sccRes.compId[source];
        System.out.println("\n=== Shortest paths on condensation DAG ===");
        System.out.println("Source task = " + source + ", component = " + sourceComp);

        DagShortestPaths.Result shortest =
                DagShortestPaths.shortestPaths(dag, sourceComp, spMetrics);

        long INF = Long.MAX_VALUE / 8;
        for (int v = 0; v < dag.n(); v++) {
            long d = shortest.dist[v];
            String val = (d >= INF) ? "INF" : Long.toString(d);
            System.out.println("dist[" + v + "] = " + val);
        }
        System.out.println("Relaxations (shortest) = " + spMetrics.relaxations +
                ", time=" + spMetrics.elapsedMillis() + " ms");

        for (int v = 0; v < dag.n(); v++) {
            if (shortest.dist[v] < INF) {
                List<Integer> path = DagShortestPaths.reconstructPath(v, shortest);
                System.out.println("Shortest path to component " + v + ": " + path);
            }
        }

        Metrics longMetrics = new Metrics();
        System.out.println("\n=== Longest paths (critical path) on condensation DAG ===");
        DagShortestPaths.Result longest =
                DagShortestPaths.longestPaths(dag, sourceComp, longMetrics);

        int criticalTarget = DagShortestPaths.findCriticalTarget(longest);
        List<Integer> criticalPath =
                DagShortestPaths.reconstructPath(criticalTarget, longest);
        System.out.println("Critical path (components): " + criticalPath);
        System.out.println("Critical path length = " + longest.dist[criticalTarget]);
        System.out.println("Relaxations (longest) = " + longMetrics.relaxations +
                ", time=" + longMetrics.elapsedMillis() + " ms");
    }
}
