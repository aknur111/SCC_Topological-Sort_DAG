package graph.common;

import java.util.ArrayList;
import java.util.List;

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

    public void addEdge(int u, int v, int w) {
        adj.get(u).add(new Edge(v, w));
        if (!directed) {
            adj.get(v).add(new Edge(u, w));
        }
    }

    public List<Edge> neighbors(int u) {
        return adj.get(u);
    }

    public List<List<Edge>> adj() {
        return adj;
    }
}
