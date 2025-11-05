package graph.common;


/**
 * Simple metrics collector used to instrument algorithms.
 * <p>
 * It stores operation counters and elapsed time in milliseconds.
 * SCC, topological sort and DAG shortest paths all share this class.
 */
public class Metrics {


    /** Number of DFS vertex visits (for SCC). */
    public long dfsVisits;

    /** Number of DFS edges processed (for SCC). */
    public long dfsEdges;

    /** Number of pushes to the queue (for Kahn topological sort). */
    public long topoPushes;

    /** Number of pops from the queue (for Kahn topological sort). */
    public long topoPops;

    /** Number of relaxations in DAG shortest/longest paths. */
    public long relaxations;

    public long startTimeNs;
    public long endTimeNs;

    /**
     * Starts the internal timer using System.nanoTime().
     */
    public void startTimer() {
        startTimeNs = System.nanoTime();
    }

    /**
     * Stops the timer and stores elapsed time in milliseconds.
     */
    public void stopTimer() {
        endTimeNs = System.nanoTime();
    }

    /**
     * Returns elapsed time in milliseconds between startTimer() and stopTimer().
     *
     * @return elapsed time in milliseconds
     */
    public long elapsedMillis() {
        return (endTimeNs - startTimeNs) / 1_000_000;
    }

    /**
     * Resets all counters and timing to zero.
     */
    public void reset() {
        dfsVisits = dfsEdges = topoPushes = topoPops = relaxations = 0L;
        startTimeNs = endTimeNs = 0L;
    }
}
