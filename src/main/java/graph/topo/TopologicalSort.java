package graph.topo;

import graph.common.Graph;
import graph.common.Metrics;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;


/**
 * Topological sorting algorithms for DAGs.
 * <p>
 * In this assignment we use Kahn's algorithm with a queue to compute
 * a valid topological ordering of the condensation graph.
 */
public class TopologicalSort {


    /**
     * Computes a topological order of the given DAG using Kahn's algorithm.
     *
     * @param dag     directed acyclic graph
     * @param metrics metrics object used to count pushes/pops and time
     * @return list of vertices in topological order
     * @throws IllegalArgumentException if the graph contains a cycle
     */
    public static List<Integer> kahn(Graph dag, Metrics metrics) {
        int n = dag.n();
        int[] indeg = new int[n];

        for (int u = 0; u < n; u++) {
            for (Graph.Edge e : dag.neighbors(u)) {
                indeg[e.to]++;
            }
        }

        Deque<Integer> q = new ArrayDeque<>();
        for (int v = 0; v < n; v++) {
            if (indeg[v] == 0) {
                q.add(v);
                metrics.topoPushes++;
            }
        }

        List<Integer> order = new ArrayList<>();
        while (!q.isEmpty()) {
            int v = q.remove();
            metrics.topoPops++;
            order.add(v);
            for (Graph.Edge e : dag.neighbors(v)) {
                int to = e.to;
                indeg[to]--;
                if (indeg[to] == 0) {
                    q.add(to);
                    metrics.topoPushes++;
                }
            }
        }

        if (order.size() != n) {
            throw new IllegalStateException("Graph is not a DAG");
        }

        return order;
    }
}
