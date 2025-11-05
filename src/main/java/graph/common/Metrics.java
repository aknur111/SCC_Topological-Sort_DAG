package graph.common;

public class Metrics {

    public long dfsVisits;
    public long dfsEdges;

    public long topoPushes;
    public long topoPops;

    public long relaxations;

    public long startTimeNs;
    public long endTimeNs;

    public void startTimer() {
        startTimeNs = System.nanoTime();
    }

    public void stopTimer() {
        endTimeNs = System.nanoTime();
    }

    public long elapsedMillis() {
        return (endTimeNs - startTimeNs) / 1_000_000;
    }

    public void reset() {
        dfsVisits = dfsEdges = topoPushes = topoPops = relaxations = 0L;
        startTimeNs = endTimeNs = 0L;
    }
}
