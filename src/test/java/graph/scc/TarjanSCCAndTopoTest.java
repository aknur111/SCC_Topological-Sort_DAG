package graph.scc;

import graph.common.Graph;
import graph.common.Metrics;
import graph.topo.TopologicalSort;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TarjanSCCAndTopoTest {

    @Test
    public void testSingleCycleOneSCC() {
        Graph g = new Graph(3, true);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        g.addEdge(2, 0, 1);

        Metrics metrics = new Metrics();
        TarjanSCC tarjan = new TarjanSCC(g, metrics);
        TarjanSCC.Result res = tarjan.run();

        assertEquals(1, res.components.size(), "Expected exactly one SCC");

        List<Integer> comp = res.components.get(0);
        assertEquals(3, comp.size(), "SCC size should be 3");
        assertTrue(comp.contains(0));
        assertTrue(comp.contains(1));
        assertTrue(comp.contains(2));

        int c0 = res.compId[0];
        assertEquals(c0, res.compId[1]);
        assertEquals(c0, res.compId[2]);
    }

    @Test
    public void testPureDAGManySCCs() {
        Graph g = new Graph(4, true);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        g.addEdge(2, 3, 1);

        Metrics sccMetrics = new Metrics();
        TarjanSCC tarjan = new TarjanSCC(g, sccMetrics);
        TarjanSCC.Result res = tarjan.run();

        assertEquals(4, res.components.size(), "Expected 4 SCCs");

        for (List<Integer> comp : res.components) {
            assertEquals(1, comp.size(), "Each SCC should have size 1");
        }

        Metrics topoMetrics = new Metrics();
        List<Integer> order = TopologicalSort.kahn(g, topoMetrics);

        assertEquals(4, order.size(), "Topological order should contain all 4 vertices");

        int[] pos = new int[4];
        for (int i = 0; i < order.size(); i++) {
            pos[order.get(i)] = i;
        }

        assertTrue(pos[0] < pos[1], "0 should come before 1");
        assertTrue(pos[1] < pos[2], "1 should come before 2");
        assertTrue(pos[2] < pos[3], "2 should come before 3");
    }

    @Test
    public void testMultipleSCCsWithCondensation() {
        Graph g = new Graph(6, true);

        g.addEdge(0, 1, 1);
        g.addEdge(1, 0, 1);

        g.addEdge(2, 3, 1);
        g.addEdge(3, 2, 1);

        g.addEdge(1, 2, 1);
        g.addEdge(3, 4, 1);
        g.addEdge(4, 5, 1);

        Metrics sccMetrics = new Metrics();
        TarjanSCC tarjan = new TarjanSCC(g, sccMetrics);
        TarjanSCC.Result res = tarjan.run();

        int compCount = res.components.size();
        assertEquals(4, compCount, "Expected 4 SCCs");

        int cntSize2 = 0;
        int cntSize1 = 0;
        for (List<Integer> comp : res.components) {
            if (comp.size() == 2) cntSize2++;
            if (comp.size() == 1) cntSize1++;
        }
        assertEquals(2, cntSize2, "There should be two components of size 2");
        assertEquals(2, cntSize1, "There should be two components of size 1");

        int c0 = res.compId[0];
        int c1 = res.compId[1];
        int c2 = res.compId[2];
        int c3 = res.compId[3];
        int c4 = res.compId[4];
        int c5 = res.compId[5];

        assertEquals(c0, c1, "0 and 1 should be in the same SCC");
        assertEquals(c2, c3, "2 and 3 should be in the same SCC");

        assertNotEquals(c0, c2, "SCC(0,1) and SCC(2,3) should be different");
        assertNotEquals(c0, c4, "SCC(0,1) and vertex 4 should be different");
        assertNotEquals(c2, c4, "SCC(2,3) and vertex 4 should be different");
        assertNotEquals(c4, c5, "4 and 5 should be in different SCCs");

        Metrics condMetrics = new Metrics();
        Graph dag = CondensationGraphBuilder.buildCondensation(g, res.compId, compCount, condMetrics);

        Metrics topoMetrics = new Metrics();
        List<Integer> topo = TopologicalSort.kahn(dag, topoMetrics);

        assertEquals(compCount, topo.size(), "Topological order of condensation graph should contain all components");
    }
}
