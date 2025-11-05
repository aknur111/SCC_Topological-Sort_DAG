package graph.common;

/**
 * Instrumentation helper for algorithms.
 * Counts operations (DFS visits, relaxations, etc.)
 * and measures elapsed time in milliseconds with double precision.
 */
public class Metrics {

    public long dfsVisits;
    public long dfsEdges;

    public long topoPushes;
    public long topoPops;

    public long relaxations;

    private long startTimeNs;
    private double elapsedMillis;

    /**
     * Starts the timer for this metrics instance.
     * Uses System.nanoTime() under the hood.
     */
    public void startTimer() {
        startTimeNs = System.nanoTime();
    }

    /**
     * Stops the timer and stores elapsed time in milliseconds as double.
     */
    public void stopTimer() {
        if (startTimeNs != 0L) {
            long end = System.nanoTime();
            long diffNs = end - startTimeNs;
            this.elapsedMillis = diffNs / 1_000_000.0;
            startTimeNs = 0L;
        }
    }

    /**
     * Returns the last measured elapsed time in milliseconds.
     */
    public double getElapsedMillis() {
        return elapsedMillis;
    }

    /**
     * Resets all counters and timing.
     */
    public void reset() {
        dfsVisits = 0;
        dfsEdges = 0;
        topoPushes = 0;
        topoPops = 0;
        relaxations = 0;
        startTimeNs = 0L;
        elapsedMillis = 0.0;
    }
}
