package graph.scc;

import graph.common.Graph;
import graph.common.Metrics;

public class CondensationGraphBuilder {

    public static Graph buildCondensation(Graph g, int[] compId, int compCount, Metrics metrics) {
        Graph dag = new Graph(compCount, true);

        boolean[][] hasEdge = new boolean[compCount][compCount];

        for (int u = 0; u < g.n(); u++) {
            int cu = compId[u];
            for (Graph.Edge e : g.neighbors(u)) {
                int v = e.to;
                int cv = compId[v];
                if (cu != cv && !hasEdge[cu][cv]) {
                    dag.addEdge(cu, cv, e.weight);
                    hasEdge[cu][cv] = true;
                }
            }
        }

        return dag;
    }
}
