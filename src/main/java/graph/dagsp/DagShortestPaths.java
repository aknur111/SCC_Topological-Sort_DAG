package graph.dagsp;

import graph.common.Graph;
import graph.common.Metrics;
import graph.topo.TopologicalSort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DagShortestPaths {

    public static class Result {
        public final long[] dist;
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
