package graph.scc;

import graph.common.Graph;
import graph.common.Metrics;

/**
 * Builds the condensation graph of a directed graph.
 * <p>
 * Each strongly connected component is contracted to a single node.
 * The resulting graph is always a DAG (directed acyclic graph).
 */

public class CondensationGraphBuilder {

    /**
     * Builds the condensation DAG.
     *
     * @param g         original directed graph
     * @param compId    component id for each vertex (as returned by TarjanSCC)
     * @param compCount number of components
     * @param metrics   metrics used to count edges if needed
     * @return condensation graph with compCount vertices
     */
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
