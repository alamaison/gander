package uk.ac.ic.doc.gander.flowinference.dda;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.Test;

public final class GoalSolverTest {

	/**
	 * A very basic goal that sums a list. Importantly, the goals are never
	 * circularly-dependent.
	 */
	private static final class SumGoal implements Goal {

		private final Integer headValue;

		private List<Integer> tail = new ArrayList<Integer>();
		private SumGoal tailGoal = null;

		public SumGoal(Iterable<Integer> values) {
			Iterator<Integer> it = values.iterator();
			if (it.hasNext()) {
				headValue = it.next();

				while (it.hasNext()) {
					tail.add(it.next());
				}

			} else {
				headValue = 0;
			}
		}

		public Object initialSolution() {
			return new Integer(0);
		}

		public Object recalculateSolution(SubgoalManager engine) {
			if (!tail.isEmpty() && tailGoal == null) {
				tailGoal = new SumGoal(tail);
				engine.registerSubgoal(tailGoal);
			}

			if (tailGoal != null) {
				return headValue
						+ (Integer) engine.currentSolutionOfGoal(tailGoal);
			} else {
				return headValue;
			}
		}
	}

	@Test
	public void demandDrivenSumming() {
		List<Integer> nums = new ArrayList<Integer>();
		nums.add(1);
		nums.add(5);
		nums.add(6000);

		GoalSolver solver = new GoalSolver(new SumGoal(nums));
		Object solution = solver.solve();
		assertEquals(new Integer(6006), solution);
	}

	@Test
	public void demandDrivenSummingNothingToDo() {
		List<Integer> nums = new ArrayList<Integer>();

		GoalSolver solver = new GoalSolver(new SumGoal(nums));
		Object solution = solver.solve();
		assertEquals(new Integer(0), solution);
	}

	@Test
	public void demandDrivenSummingNothingToSum() {
		List<Integer> nums = new ArrayList<Integer>();
		nums.add(6000);

		GoalSolver solver = new GoalSolver(new SumGoal(nums));
		Object solution = solver.solve();
		assertEquals(new Integer(6000), solution);
	}

	private static class Vertex {
		private String name;

		public Vertex(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return "Vertex [name=" + name + "]";
		}
	}

	private static final class DependencyGoal implements Goal {

		private DirectedGraph<Vertex, DefaultEdge> graph;
		private Map<Vertex, DependencyGoal> incoming = new HashMap<Vertex, DependencyGoal>();
		private Vertex vertex;

		public DependencyGoal(DirectedGraph<Vertex, DefaultEdge> graph,
				Vertex vertex) {
			this.vertex = vertex;
			this.graph = graph;

			for (DefaultEdge edge : graph.incomingEdgesOf(vertex)) {
				incoming.put(graph.getEdgeSource(edge), null);
			}
		}

		public Object initialSolution() {
			return Collections.unmodifiableSet(incoming.keySet());
		}

		public Object recalculateSolution(SubgoalManager engine) {
			Set<Vertex> incomingTransitive = new HashSet<Vertex>(incoming
					.keySet());

			for (Vertex incomingVertex : incoming.keySet()) {

				DependencyGoal g = incoming.get(incomingVertex);
				if (g == null) {
					g = new DependencyGoal(graph, incomingVertex);
					engine.registerSubgoal(g);
					incoming.put(incomingVertex, g);
				}

				incomingTransitive.addAll((Set<Vertex>) engine
						.currentSolutionOfGoal(g));
			}

			return incomingTransitive;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((vertex == null) ? 0 : vertex.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DependencyGoal other = (DependencyGoal) obj;
			if (vertex == null) {
				if (other.vertex != null)
					return false;
			} else if (!vertex.equals(other.vertex))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "DependencyGoal [vertex=" + vertex + "]";
		}
	}

	@Test
	public void demandDrivenDependencyAnalysis() {
		final DirectedGraph<Vertex, DefaultEdge> graph = new DefaultDirectedGraph<Vertex, DefaultEdge>(
				DefaultEdge.class);

		Vertex a = new Vertex("a");
		Vertex b = new Vertex("b");
		Vertex c = new Vertex("c");
		Vertex d = new Vertex("d");
		Vertex e = new Vertex("e");
		Vertex f = new Vertex("f");
		graph.addVertex(a);
		graph.addVertex(b);
		graph.addVertex(c);
		graph.addVertex(d);
		graph.addVertex(e);
		graph.addVertex(f);
		graph.addEdge(a, b);
		graph.addEdge(b, c);
		graph.addEdge(b, d);
		graph.addEdge(c, e);
		graph.addEdge(d, e);
		graph.addEdge(e, b);
		graph.addEdge(e, f);

		GoalSolver solver = new GoalSolver(new DependencyGoal(graph, c));
		Set<Vertex> dependencies = (Set<Vertex>) solver.solve();

		Set<Vertex> expectedDependencies = new HashSet<Vertex>();
		expectedDependencies.add(e);
		expectedDependencies.add(c);
		expectedDependencies.add(d);
		expectedDependencies.add(b);
		expectedDependencies.add(a);

		assertEquals(expectedDependencies, dependencies);
	}

	private static final class DependencyGoalNullInitialSolution implements
			Goal {

		private DirectedGraph<Vertex, DefaultEdge> graph;
		private Map<Vertex, DependencyGoalNullInitialSolution> incoming = new HashMap<Vertex, DependencyGoalNullInitialSolution>();
		private Vertex vertex;

		public DependencyGoalNullInitialSolution(
				DirectedGraph<Vertex, DefaultEdge> graph, Vertex vertex) {
			this.vertex = vertex;
			this.graph = graph;

			for (DefaultEdge edge : graph.incomingEdgesOf(vertex)) {
				incoming.put(graph.getEdgeSource(edge), null);
			}
		}

		public Object initialSolution() {
			return null;
		}

		public Object recalculateSolution(SubgoalManager engine) {
			Set<Vertex> incomingTransitive = new HashSet<Vertex>(incoming
					.keySet());

			for (Vertex incomingVertex : incoming.keySet()) {

				DependencyGoalNullInitialSolution g = incoming
						.get(incomingVertex);
				if (g == null) {
					g = new DependencyGoalNullInitialSolution(graph,
							incomingVertex);
					engine.registerSubgoal(g);
					incoming.put(incomingVertex, g);
				}
				Set<Vertex> s = (Set<Vertex>) engine.currentSolutionOfGoal(g);
				if (s != null)
					incomingTransitive.addAll(s);
			}

			return incomingTransitive;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((vertex == null) ? 0 : vertex.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DependencyGoalNullInitialSolution other = (DependencyGoalNullInitialSolution) obj;
			if (vertex == null) {
				if (other.vertex != null)
					return false;
			} else if (!vertex.equals(other.vertex))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "DependencyGoal [vertex=" + vertex + "]";
		}
	}

	@Test
	public void demandDrivenDependencyAnalysisNullInitial() {
		final DirectedGraph<Vertex, DefaultEdge> graph = new DefaultDirectedGraph<Vertex, DefaultEdge>(
				DefaultEdge.class);

		Vertex a = new Vertex("a");
		Vertex b = new Vertex("b");
		Vertex c = new Vertex("c");
		Vertex d = new Vertex("d");
		Vertex e = new Vertex("e");
		Vertex f = new Vertex("f");
		graph.addVertex(a);
		graph.addVertex(b);
		graph.addVertex(c);
		graph.addVertex(d);
		graph.addVertex(e);
		graph.addVertex(f);
		graph.addEdge(a, b);
		graph.addEdge(b, c);
		graph.addEdge(b, d);
		graph.addEdge(c, e);
		graph.addEdge(d, e);
		graph.addEdge(e, b);
		graph.addEdge(e, f);

		GoalSolver solver = new GoalSolver(
				new DependencyGoalNullInitialSolution(graph, c));
		Set<Vertex> dependencies = (Set<Vertex>) solver.solve();

		Set<Vertex> expectedDependencies = new HashSet<Vertex>();
		expectedDependencies.add(e);
		expectedDependencies.add(c);
		expectedDependencies.add(d);
		expectedDependencies.add(b);
		expectedDependencies.add(a);

		assertEquals(expectedDependencies, dependencies);
	}

}
