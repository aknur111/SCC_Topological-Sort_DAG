package graph.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Directed or undirected weighted graph represented with adjacency lists.
 * This class is used as the common graph representation across all tasks.
 */
public class Graph {

    public static class Edge {
        public final int to;
        public final int weight;

        public Edge(int to, int weight) {
            this.to = to;
            this.weight = weight;
        }

        @Override
        public String toString() {
            return "->" + to + "(w=" + weight + ")";
        }
    }

    private final int n;
    private final boolean directed;
    private final List<List<Edge>> adj;

    /**
     * Creates a new graph.
     *
     * @param n        number of vertices (0..n-1)
     * @param directed true if the graph is directed, false for undirected
     */
    public Graph(int n, boolean directed) {
        this.n = n;
        this.directed = directed;
        this.adj = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }
    }

    public int n() {
        return n;
    }

    public boolean isDirected() {
        return directed;
    }
    /**
     * Adds a directed edge (or undirected, depending on the graph mode).
     *
     * @param u source vertex
     * @param v target vertex
     * @param w edge weight
     */
    public void addEdge(int u, int v, int w) {
        adj.get(u).add(new Edge(v, w));
        if (!directed) {
            adj.get(v).add(new Edge(u, w));
        }
    }

    public List<Edge> neighbors(int u) {
        return adj.get(u);
    }

    /**
     * Returns the whole adjacency structure of the graph.
     *
     * @return adjacency lists for all vertices
     */
    public List<List<Edge>> adj() {
        return adj;
    }
}
