package graph.topo;

import graph.common.Graph;
import graph.common.Metrics;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Utilities for computing topological orderings of directed acyclic graphs.
 * <p>
 * In this assignment we use Kahn's algorithm with a queue to compute
 * a valid topological ordering of the condensation graph (which is always a DAG
 * if SCCs were computed correctly).
 */
public final class TopologicalSort {

    private TopologicalSort() {
        // utility class
    }

    /**
     * Computes a topological order of the given DAG using Kahn's algorithm.
     *
     * @param g       directed acyclic graph
     * @param metrics metrics object used to count pushes/pops and time;
     *                may be {@code null} if instrumentation is not needed
     * @return list of vertices in topological order
     * @throws IllegalArgumentException if the graph contains a cycle
     */
    public static List<Integer> kahn(Graph g, Metrics metrics) {
        int n = g.n();
        int[] indeg = new int[n];

        for (int v = 0; v < n; v++) {
            for (Graph.Edge e : g.neighbors(v)) {
                indeg[e.to]++;
            }
        }

        Queue<Integer> q = new ArrayDeque<>();
        List<Integer> order = new ArrayList<>(n);

        if (metrics != null) {
            metrics.startTimer();
        }

        for (int v = 0; v < n; v++) {
            if (indeg[v] == 0) {
                q.add(v);
                if (metrics != null) {
                    metrics.topoPushes++;
                }
            }
        }

        while (!q.isEmpty()) {
            int v = q.remove();
            order.add(v);
            if (metrics != null) {
                metrics.topoPops++;
            }

            for (Graph.Edge e : g.neighbors(v)) {
                int to = e.to;
                if (--indeg[to] == 0) {
                    q.add(to);
                    if (metrics != null) {
                        metrics.topoPushes++;
                    }
                }
            }
        }

        if (metrics != null) {
            metrics.stopTimer();
        }

        if (order.size() != n) {
            throw new IllegalArgumentException("Graph is not a DAG: cycle detected in Kahn's algorithm");
        }

        return order;
    }
}
