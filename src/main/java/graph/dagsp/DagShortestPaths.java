package graph.dagsp;

import graph.common.Graph;
import graph.common.Metrics;
import graph.topo.TopologicalSort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * Single-source shortest and longest path algorithms for DAGs.
 * <p>
 * The implementation assumes non-negative edge weights and uses dynamic
 * programming over a topological order. Longest paths (critical paths)
 * are computed with a similar DP that maximizes distances.
 */
public class DagShortestPaths {

    /**
     * Result of shortest/longest path computation on a DAG.
     * Stores distances and parent pointers for path reconstruction.
     */
    public static class Result {
        /** dist[v] is the distance from the source to v. */
        public final long[] dist;

        /** parent[v] is the previous vertex on some optimal path to v. */
        public final int[] parent;

        public Result(long[] dist, int[] parent) {
            this.dist = dist;
            this.parent = parent;
        }
    }

    public static Result shortestPaths(Graph dag, int source, Metrics metrics) {
        int n = dag.n();
        long INF = Long.MAX_VALUE / 4;
        long[] dist = new long[n];
        int[] parent = new int[n];
        Arrays.fill(dist, INF);
        Arrays.fill(parent, -1);
        dist[source] = 0;

        List<Integer> topo = TopologicalSort.kahn(dag, metrics);

        metrics.startTimer();
        for (int v : topo) {
            if (dist[v] == INF) continue;
            for (Graph.Edge e : dag.neighbors(v)) {
                int to = e.to;
                long nd = dist[v] + e.weight;
                if (nd < dist[to]) {
                    dist[to] = nd;
                    parent[to] = v;
                    metrics.relaxations++;
                }
            }
        }
        metrics.stopTimer();
        return new Result(dist, parent);
    }

    public static Result longestPaths(Graph dag, int source, Metrics metrics) {
        int n = dag.n();
        long NEG_INF = Long.MIN_VALUE / 4;
        long[] dist = new long[n];
        int[] parent = new int[n];
        Arrays.fill(dist, NEG_INF);
        Arrays.fill(parent, -1);
        dist[source] = 0;

        List<Integer> topo = TopologicalSort.kahn(dag, metrics);

        metrics.startTimer();
        for (int v : topo) {
            if (dist[v] == NEG_INF) continue;
            for (Graph.Edge e : dag.neighbors(v)) {
                int to = e.to;
                long nd = dist[v] + e.weight;
                if (nd > dist[to]) {
                    dist[to] = nd;
                    parent[to] = v;
                    metrics.relaxations++;
                }
            }
        }
        metrics.stopTimer();
        return new Result(dist, parent);
    }

    public static List<Integer> reconstructPath(int target, Result res) {
        List<Integer> path = new ArrayList<>();
        int cur = target;
        while (cur != -1) {
            path.add(cur);
            cur = res.parent[cur];
        }
        Collections.reverse(path);
        return path;
    }

    public static int findCriticalTarget(Result res) {
        long best = Long.MIN_VALUE;
        int bestNode = -1;
        for (int i = 0; i < res.dist.length; i++) {
            if (res.dist[i] > best) {
                best = res.dist[i];
                bestNode = i;
            }
        }
        return bestNode;
    }
}
