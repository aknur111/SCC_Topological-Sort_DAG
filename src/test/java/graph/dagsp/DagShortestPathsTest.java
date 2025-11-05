package graph.dagsp;

import graph.common.Graph;
import graph.common.Metrics;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DagShortestPathsTest {

    @Test
    public void testShortestPathsSimpleDAG() {
        Graph g = new Graph(5, true);
        g.addEdge(0, 1, 2);
        g.addEdge(0, 2, 5);
        g.addEdge(1, 2, 1);
        g.addEdge(1, 3, 2);
        g.addEdge(2, 3, 1);
        g.addEdge(3, 4, 3);

        Metrics metrics = new Metrics();
        DagShortestPaths.Result res = DagShortestPaths.shortestPaths(g, 0, metrics);

        long[] dist = res.dist;

        assertEquals(0L, dist[0]);
        assertEquals(2L, dist[1]);
        assertEquals(3L, dist[2]);
        assertEquals(4L, dist[3]);
        assertEquals(7L, dist[4]);

        List<Integer> pathTo4 = DagShortestPaths.reconstructPath(4, res);
        assertEquals(List.of(0, 1, 3, 4), pathTo4);
    }

    @Test
    public void testLongestPathsCriticalPath() {
        Graph g = new Graph(5, true);
        g.addEdge(0, 1, 2);
        g.addEdge(0, 2, 5);
        g.addEdge(1, 2, 1);
        g.addEdge(1, 3, 2);
        g.addEdge(2, 3, 1);
        g.addEdge(3, 4, 3);

        Metrics metrics = new Metrics();
        DagShortestPaths.Result res = DagShortestPaths.longestPaths(g, 0, metrics);

        int target = DagShortestPaths.findCriticalTarget(res);
        assertEquals(4, target);

        long[] dist = res.dist;
        assertEquals(9L, dist[4]);

        List<Integer> criticalPath = DagShortestPaths.reconstructPath(4, res);
        assertEquals(List.of(0, 2, 3, 4), criticalPath);
    }
}
