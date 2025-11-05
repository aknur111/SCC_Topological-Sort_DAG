package graph.scc;

import graph.common.Graph;
import graph.common.Metrics;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;


/**
 * Implementation of Tarjan's algorithm for finding strongly connected
 * components (SCCs) in a directed graph.
 * <p>
 * The algorithm runs in O(V + E) time. It reports components as a list
 * of vertex lists and also stores component ids for each vertex.
 */
public class TarjanSCC {


    /**
     * Result of Tarjan's algorithm: list of SCCs and an array of component ids.
     */
    public static class Result {

        /** components[k] is the list of vertices in k-th SCC. */
        public final List<List<Integer>> components;

        /** compId[v] is the index of the SCC containing vertex v. */
        public final int[] compId;

        public Result(List<List<Integer>> components, int[] compId) {
            this.components = components;
            this.compId = compId;
        }
    }

    private final Graph g;
    private final Metrics metrics;

    private int index;
    private int[] indices;
    private int[] lowlink;
    private boolean[] onStack;
    private Deque<Integer> stack;
    private List<List<Integer>> components;


    /**
     * Creates a new Tarjan SCC solver.
     *
     * @param g       directed graph on which SCCs will be computed
     * @param metrics metrics object used to count DFS operations and time
     */
    public TarjanSCC(Graph g, Metrics metrics) {
        this.g = g;
        this.metrics = metrics;
    }


    /**
     * Runs Tarjan's algorithm on the given graph.
     *
     * @return SCC result containing all components and component ids
     */
    public Result run() {
        int n = g.n();
        index = 0;
        indices = new int[n];
        lowlink = new int[n];
        onStack = new boolean[n];
        Arrays.fill(indices, -1);
        stack = new ArrayDeque<>();
        components = new ArrayList<>();

        metrics.startTimer();
        for (int v = 0; v < n; v++) {
            if (indices[v] == -1) {
                strongConnect(v);
            }
        }
        metrics.stopTimer();

        int[] compId = new int[n];
        for (int cid = 0; cid < components.size(); cid++) {
            for (int v : components.get(cid)) {
                compId[v] = cid;
            }
        }

        return new Result(components, compId);
    }

    private void strongConnect(int v) {
        indices[v] = index;
        lowlink[v] = index;
        index++;

        stack.push(v);
        onStack[v] = true;
        metrics.dfsVisits++;

        for (Graph.Edge e : g.neighbors(v)) {
            metrics.dfsEdges++;
            int w = e.to;
            if (indices[w] == -1) {
                strongConnect(w);
                lowlink[v] = Math.min(lowlink[v], lowlink[w]);
            } else if (onStack[w]) {
                lowlink[v] = Math.min(lowlink[v], indices[w]);
            }
        }

        if (lowlink[v] == indices[v]) {
            List<Integer> comp = new ArrayList<>();
            int w;
            do {
                w = stack.pop();
                onStack[w] = false;
                comp.add(w);
            } while (w != v);
            components.add(comp);
        }
    }
}
