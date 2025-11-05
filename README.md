# ADS Assignment 4 – SCC, Topological Ordering and DAG Shortest Paths

## 1. Goal and Scenario

This project combines two core topics of the course in a single practical case:

1. **Strongly Connected Components (SCC) & Topological Ordering**
2. **Shortest Paths in DAGs**

The scenario is a simplified model of a **Smart City / Smart Campus Scheduling** problem:

- Vertices represent **tasks**: street cleaning, road repair, sensor/camera maintenance, analytics jobs, etc.
- Directed edges represent **dependencies** (“task A must be completed before task B”).
- Some dependencies form **cycles** (mutually dependent subtasks). These must be detected and compressed into single units.
- The compressed structure is a **DAG of components**, on which we can run dynamic programming patterns such as shortest paths and critical path analysis.

The goal is to:

- Detect and compress strongly connected components.
- Build and topologically sort the condensation DAG.
- Run shortest-path and longest-path algorithms on the DAG to analyse schedule quality and bottlenecks.
- Evaluate performance on several datasets and discuss the effect of graph size and structure.

---

## 2. Algorithms and Design

### 2.1 Graph Representation (`graph.common`)

All algorithms share a common graph representation:

- **`Graph`**
    - Directed or undirected graph (we use directed graphs in this assignment).
    - Vertices are indexed `0 .. n-1`.
    - Edges are stored in adjacency lists: `List<List<Edge>>`.
    - Each `Edge` has:
        - `int to` – target vertex,
        - `int weight` – non-negative integer weight.

This representation is used for:

- the original task graph,
- the condensation DAG,
- all SCC, topological sort and DAG-SP computations.

### 2.2 Weight Model

We use **edge weights** as the cost model:

- Each directed edge `(u, v)` has a weight `w ≥ 0`.
- In the original graph this can model:
    - task duration,
    - cost, or
    - travel time between locations.
- When building the condensation DAG, each component becomes one vertex; edges between components inherit the original edge weights.

This is reflected in all JSON files via:

```json
"weight_model": "edge"
```

## 2.3 Strongly Connected Components – Tarjan’s Algorithm (graph.scc.TarjanSCC)

### We implement Tarjan’s algorithm to find SCCs in a directed graph:

**Time complexity:**  O(V + E)

**Space:** O(V) for stacks and arrays.

### Key ideas:

**Maintain arrays:** 

-disc[v] – discovery time of vertex v in DFS,

-low[v] – lowest discovery index reachable from v via DFS tree + back edges,

-onStack[v] – whether v is currently on the DFS stack.


Every time we find a root of an SCC (low[v] == disc[v]), we pop vertices from the stack until we reach v.
All popped vertices form one strongly connected component.

**The implementation returns:**

public static class Result {
public final List<List<Integer>> components; // vertices grouped by SCC
public final int[] compId;                   // compId[v] = component index
}

**Output** 

For each dataset the program prints, for example:

````
=== Strongly Connected Components (SCC) ===
Component 0 (size=2): [4, 3]
Component 1 (size=3): [2, 1, 0]
Total SCCs = 2
Tarjan: dfsVisits=5, dfsEdges=6, time=0 ms

````

**Where:**

-dfsVisits – number of vertices visited in Tarjan DFS,

-dfsEdges – number of DFS edges processed,

-time – elapsed milliseconds measured via System.nanoTime() inside Metrics.

## 2.4 Condensation DAG (graph.scc.CondensationGraphBuilder)

Given:
 
***TarjanSCC.Result res with compId[v] and components.size(),*** we build the condensation graph:

- Each SCC becomes a single vertex (component index).

- For every original edge u → v:

-If compId[u] != compId[v], we add an edge compId[u] → compId[v] to the condensation graph.

-Edge weight is inherited from the original edge.

- Multiple parallel edges between the same pair of components can be either:

- merged (e.g. keep the lightest or heaviest), or stored separately (simple and still correct for DAG-SP).

### Properties:

- The condensation graph is always a DAG (no cycles between SCCs).

- Its size is at most the size of the original graph.
````
Example output:

=== Condensation DAG ===
C0 ->
C1 -> C0(w=4)
````

**This means:**

-Component 1 has an edge to component 0 with weight 4.

-Component 0 has no outgoing edges.

## 2.5 Topological Sorting – Kahn’s Algorithm (graph.topo.TopologicalSort)

### On the condensation DAG we run Kahn’s algorithm:

- Compute in-degree for each vertex.

- Push all vertices with in-degree 0 into a queue.

***Repeatedly:***

pop a vertex v from the queue,

append it to the topological order,

decrease in-degrees of all neighbors;

if some neighbor’s in-degree becomes 0, push it into the queue.

***Metrics:***

topoPushes – number of times a vertex is pushed into the queue,

topoPops – number of pops,

optional timing.

Example output:
````
=== Topological order of components ===
[1, 0]
Derived order of original tasks:
Component 1 -> [2, 1, 0]
Component 0 -> [4, 3]
Kahn: pushes=2, pops=2
````

We also show a derived order of original tasks by printing the vertices inside each component in topological component order.

## 2.6 Single-Source Shortest Paths in a DAG (graph.dagsp.DagShortestPaths)

### For a DAG, shortest paths can be computed faster and simpler than general Dijkstra:

Compute a topological order topo.

***Initialize:***

dist[source] = 0,

dist[v] = +∞ for all other vertices.

Traverse vertices in topo order:

For each edge v → to with weight w:

if dist[v] + w < dist[to], relax:

dist[to] = dist[v] + w,

parent[to] = v,

increment metrics.relaxations.

Longest paths (critical paths) are computed similarly, but:

dist is initialized to -∞ except dist[source] = 0,

relax if dist[v] + w > dist[to].

**Complexity:** O(V + E) on the DAG.

***Result container:***
````
public static class Result {
public final long[] dist;   // distances from the source
public final int[] parent;  // parent[v] – previous vertex on some optimal path
}
````

**Additional helper methods:**

- reconstructPath(int target, Result res) – reconstructs one optimal path using parent[].

- findCriticalTarget(Result res) – finds a vertex with the maximum distance (end of the critical path).

Example output:
````
=== Shortest paths on condensation DAG ===
Source task = 0, component = 1
dist[0] = 4
dist[1] = 0
Relaxations (shortest) = 1, time=0 ms
Shortest path to component 0: [1, 0]
Shortest path to component 1: [1]

=== Longest paths (critical path) on condensation DAG ===
Critical path (components): [1, 0]
Critical path length = 4
Relaxations (longest) = 1, time=0 ms
````

## 3. Project Structure

The project is a standard Maven Java project.
````
assignment4/
├─ pom.xml
├─ src/
│  ├─ main/java/
│  │  ├─ app/
│  │  │  └─ Main.java
│  │  └─ graph/
│  │     ├─ common/
│  │     │  ├─ Graph.java
│  │     │  └─ Metrics.java
│  │     ├─ scc/
│  │     │  ├─ TarjanSCC.java
│  │     │  └─ CondensationGraphBuilder.java
│  │     ├─ topo/
│  │     │  └─ TopologicalSort.java
│  │     └─ dagsp/
│  │        └─ DagShortestPaths.java
│  └─ test/java/
│     └─ graph/
│        ├─ scc/
│        │  └─ TarjanSCCAndTopoTest.java
│        └─ dagsp/
│           └─ DagShortestPathsTest.java
├─ data/
│  ├─ tasks.json
│  ├─ tasks_small_1.json
│  ├─ tasks_small_2.json
│  ├─ tasks_small_3.json
│  ├─ tasks_medium_1.json
│  ├─ tasks_medium_2.json
│  ├─ tasks_medium_3.json
│  ├─ tasks_large_1.json
│  ├─ tasks_large_2.json
│  └─ tasks_large_3.json
└─ README.md
````

## 3.1 Dependencies

Declared in pom.xml:

- Gson – JSON parsing:

- com.google.code.gson:gson:2.10.1

- JUnit 5 (Jupiter) – unit testing:

- org.junit.jupiter:junit-jupiter:5.10.0

- Maven Surefire Plugin – test runner.

- Exec Maven Plugin – run app.Main with dependencies.

## 4. Input Data Format

All datasets are JSON files in the data/ directory with the following structure:
````
{
"directed": true,
"n": 12,
"edges": [
{ "u": 0, "v": 1, "w": 2 },
{ "u": 1, "v": 2, "w": 3 }
],
"source": 0,
"weight_model": "edge"
}
````

***Fields:***

- directed: true for directed graphs (used in all datasets).

- n: number of vertices, numbered 0 .. n-1.

- edges: array of objects with:

- u: source vertex,

- v: target vertex,

- w: non-negative integer weight.

- source: source vertex for single-source shortest paths on the condensation DAG (mapped via component id).

- weight_model: string, set to "edge" for this assignment.

***The program:***

1. Parses the JSON using Gson.

2. Builds a Graph instance from n, directed and edges.

3. Runs SCC, condensation, topological sort and DAG shortest/longest paths.

## 5. Build and Run Instructions
   **5.1 Requirements**

- Java 17 or newer

- Maven 3.x

**5.2 Build**

From the project root:

- mvn package


**This command:**

- compiles all sources,

- runs all JUnit tests,

- builds target/assignment4-1.0-SNAPSHOT.jar.

**5.3 Run the Main Program**

We use the exec-maven-plugin to run app.Main with all dependencies.

General pattern:
````
mvn exec:java -Dexec.mainClass=app.Main -Dexec.args="data/<dataset>.json"
````

Examples:
## Small datasets
````

mvn exec:java -Dexec.mainClass=app.Main -Dexec.args="data/tasks_small_1.json"
mvn exec:java -Dexec.mainClass=app.Main -Dexec.args="data/tasks_small_2.json"
mvn exec:java -Dexec.mainClass=app.Main -Dexec.args="data/tasks_small_3.json"
````

## Medium datasets
````
mvn exec:java -Dexec.mainClass=app.Main -Dexec.args="data/tasks_medium_1.json"
mvn exec:java -Dexec.mainClass=app.Main -Dexec.args="data/tasks_medium_2.json"
mvn exec:java -Dexec.mainClass=app.Main -Dexec.args="data/tasks_medium_3.json"
````

## Large datasets
````
mvn exec:java -Dexec.mainClass=app.Main -Dexec.args="data/tasks_large_1.json"
mvn exec:java -Dexec.mainClass=app.Main -Dexec.args="data/tasks_large_2.json"
mvn exec:java -Dexec.mainClass=app.Main -Dexec.args="data/tasks_large_3.json"

````

**The program prints:**

- number and sizes of SCCs,

- structure of the condensation DAG,

- topological order of components and derived order of tasks,

- shortest distances from the chosen source component,

- critical path (longest path) and its length,

- instrumentation metrics for each algorithm.

## 5.4 Run Unit Tests
mvn test


***Unit tests include:***

- graph.scc.TarjanSCCAndTopoTest

- single directed cycle (exactly one SCC),

- pure DAG with no cycles (each vertex is its own SCC),

- graph with multiple SCCs, condensation DAG and topological order.

- graph.dagsp.DagShortestPathsTest

- small DAG for shortest paths,

- small DAG for longest (critical) path.

These tests verify correctness of SCC decomposition, condensation, topological sort and DAG shortest/longest paths.

## 6. Datasets

Each student is required to provide 9 datasets with different sizes and structures.

### 6.1 Summary Table

Note: numbers for #edges and #SCC below correspond to the current version of datasets.
They can be updated easily from the program output if the datasets are modified.

````
Small (6–10 vertices)
Dataset	n (vertices)	#edges	Type	#SCC	Max SCC size	Notes
tasks_small_1.json	6	6	cyclic	2	3	two non-trivial SCCs (size 3 and 2) + isolated
tasks_small_2.json	6	4	pure DAG	6	1	simple chain 0→1→2→3→4 + isolated vertex 5
tasks_small_3.json	6	7	mixed/cyclic	3	2	two 2-node SCCs, one trivial SCC + isolated
Medium (10–20 vertices)
Dataset	n	#edges	Type	#SCC	Max SCC size	Notes
tasks_medium_1.json	12	14	mixed	8	3	chain of SCCs plus singleton vertices
tasks_medium_2.json	15	15	mixed	13	3	mostly chain, one non-trivial SCC of size 3
tasks_medium_3.json	15	19	mixed	9	3	several SCCs of size 2–3, some singletons
Large (20–50 vertices) – performance / timing
Dataset	n	#edges	Type	#SCC	Max SCC size	Notes
tasks_large_1.json	30	38	mixed	24	4	long chain of components with a few larger SCCs
tasks_large_2.json	40	118	dense mixed	34	4	relatively dense DAG-like graph plus SCCs
tasks_large_3.json	50	54	mixed	30	5	five SCCs of size 5 + long tail of singleton SCCs
````
***These datasets provide:***

-both cyclic and acyclic structures,

-varying density (from simple chains to denser graphs),

-multiple SCCs in at least one dataset per size category.

## 7. Metrics and Experimental Results
   ### 7.1 Instrumentation – Metrics

Instrumentation is centralized in graph.common.Metrics. It stores:

- long dfsVisits – number of DFS node visits (for Tarjan SCC),

- long dfsEdges – number of DFS edges processed,

- long topoPushes – queue pushes in Kahn’s algorithm,

- long topoPops – queue pops in Kahn’s algorithm,

- long relaxations – number of relaxations in DAG shortest/longest paths.

***Timing is measured via:***

- startTimer() – records System.nanoTime() at the beginning,

- stopTimer() – computes elapsed time in milliseconds,

- elapsedMillis() – returns recorded time.

***Each algorithm uses Metrics as follows:***

**TarjanSCC:**

-increments dfsVisits when visiting a vertex,

-increments dfsEdges for each DFS edge,

-wraps the DFS in startTimer() / stopTimer().

**TopologicalSort.kahn:**

-increments topoPushes and topoPops for each queue operation.

**DagShortestPaths:**

-increments relaxations every time an edge is relaxed in both shortest and longest path computations,
-wraps the DP loops in startTimer() / stopTimer().


