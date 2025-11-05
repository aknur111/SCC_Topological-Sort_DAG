package graph.topo;

import graph.common.Graph;
import graph.common.Metrics;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class TopologicalSort {

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
