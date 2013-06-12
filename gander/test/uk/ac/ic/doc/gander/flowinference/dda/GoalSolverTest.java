package uk.ac.ic.doc.gander.flowinference.dda;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public final class GoalSolverTest {

	/**
	 * A very basic goal that sums a list. Importantly, the goals are never
	 * circularly-dependent.
	 */
	private static final class SumGoal implements Goal<Integer> {

		private final Integer headValue;

		private List<Integer> tail = new ArrayList<Integer>();

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

		public Integer initialSolution() {
			return new Integer(0);
		}

		public Integer recalculateSolution(SubgoalManager engine) {
			if (!tail.isEmpty()) {
				return headValue + engine.registerSubgoal(new SumGoal(tail));

			} else {
				return headValue;
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((headValue == null) ? 0 : headValue.hashCode());
			result = prime * result + ((tail == null) ? 0 : tail.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof SumGoal))
				return false;
			SumGoal other = (SumGoal) obj;
			if (headValue == null) {
				if (other.headValue != null)
					return false;
			} else if (!headValue.equals(other.headValue))
				return false;
			if (tail == null) {
				if (other.tail != null)
					return false;
			} else if (!tail.equals(other.tail))
				return false;
			return true;
		}

	}

	private GoalSolver<Integer> newSumSolver(List<Integer> nums) {
		return GoalSolver.newInstance(new SumGoal(nums), new KnowledgeBase());
	}

	@Test
	public void demandDrivenSumming() {
		List<Integer> nums = new ArrayList<Integer>();
		nums.add(1);
		nums.add(5);
		nums.add(6000);

		GoalSolver<Integer> solver = newSumSolver(nums);
		Object solution = solver.solve();
		assertEquals(new Integer(6006), solution);
	}

	@Test
	public void demandDrivenSummingNothingToDo() {
		List<Integer> nums = new ArrayList<Integer>();

		GoalSolver<Integer> solver = newSumSolver(nums);
		Object solution = solver.solve();
		assertEquals(new Integer(0), solution);
	}

	@Test
	public void demandDrivenSummingNothingToSum() {
		List<Integer> nums = new ArrayList<Integer>();
		nums.add(6000);

		GoalSolver<Integer> solver = newSumSolver(nums);
		Object solution = solver.solve();
		assertEquals(new Integer(6000), solution);
	}

	@Test
	public void reusingSummingKnowledgeBase() {
		List<Integer> nums = new ArrayList<Integer>();
		nums.add(6000);

		KnowledgeBase blackboard = new KnowledgeBase();

		GoalSolver<Integer> solver = GoalSolver.newInstance(new SumGoal(nums),
				blackboard);
		Integer solution = solver.solve();
		assertEquals(new Integer(6000), solution);

		solver = GoalSolver.newInstance(new SumGoal(nums), blackboard);
		solution = solver.solve();
		assertEquals(new Integer(6000), solution);

		solver = GoalSolver.newInstance(new SumGoal(new ArrayList<Integer>()),
				blackboard);
		solution = solver.solve();
		assertEquals(new Integer(0), solution);

		nums.add(1);
		nums.add(5);

		solver = GoalSolver.newInstance(new SumGoal(nums), blackboard);
		solution = solver.solve();
		assertEquals(new Integer(6006), solution);
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

	private static class Graph {

		private final HashMap<Vertex, Set<Vertex>> graph = new HashMap<Vertex, Set<Vertex>>();

		public Collection<Vertex> verticesIncomingTowards(Vertex vertex) {

			Set<Vertex> c = graph.get(vertex);
			if (c == null) {
				return Collections.emptySet();
			} else {
				return Collections.unmodifiableCollection(graph.get(vertex));
			}
		}

		public void addEdge(Vertex from, Vertex to) {
			Set<Vertex> dependents = graph.get(to);
			if (dependents == null) {
				dependents = new HashSet<Vertex>();
				graph.put(to, dependents);
			}

			dependents.add(from);
		}
	}

	private static final class DependencyGoal implements Goal<Set<Vertex>> {

		private Graph graph;
		private Set<Vertex> incoming = new HashSet<Vertex>();
		private Vertex vertex;

		public DependencyGoal(Graph graph, Vertex vertex) {
			this.vertex = vertex;
			this.graph = graph;

			incoming.addAll(graph.verticesIncomingTowards(vertex));
		}

		public Set<Vertex> initialSolution() {
			return Collections.unmodifiableSet(incoming);
		}

		public Set<Vertex> recalculateSolution(SubgoalManager engine) {
			Set<Vertex> incomingTransitive = new HashSet<Vertex>(incoming);

			for (Vertex incomingVertex : incoming) {

				incomingTransitive.addAll(engine
						.registerSubgoal(new DependencyGoal(graph,
								incomingVertex)));
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
		final Graph graph = new Graph();

		Vertex a = new Vertex("a");
		Vertex b = new Vertex("b");
		Vertex c = new Vertex("c");
		Vertex d = new Vertex("d");
		Vertex e = new Vertex("e");
		Vertex f = new Vertex("f");
		graph.addEdge(a, b);
		graph.addEdge(b, c);
		graph.addEdge(b, d);
		graph.addEdge(c, e);
		graph.addEdge(d, e);
		graph.addEdge(e, b);
		graph.addEdge(e, f);

		GoalSolver<Set<Vertex>> solver = GoalSolver.newInstance(
				new DependencyGoal(graph, c), new KnowledgeBase());
		Set<Vertex> dependencies = solver.solve();

		Set<Vertex> expectedDependencies = new HashSet<Vertex>();
		expectedDependencies.add(e);
		expectedDependencies.add(c);
		expectedDependencies.add(d);
		expectedDependencies.add(b);
		expectedDependencies.add(a);

		assertEquals(expectedDependencies, dependencies);
	}

	private static final class DependencyGoalNullInitialSolution implements
			Goal<Set<Vertex>> {

		private Graph graph;
		private Set<Vertex> incoming = new HashSet<Vertex>();
		private Vertex vertex;

		public DependencyGoalNullInitialSolution(Graph graph, Vertex vertex) {
			this.vertex = vertex;
			this.graph = graph;

			incoming.addAll(graph.verticesIncomingTowards(vertex));
		}

		public Set<Vertex> initialSolution() {
			return null;
		}

		public Set<Vertex> recalculateSolution(SubgoalManager engine) {
			Set<Vertex> incomingTransitive = new HashSet<Vertex>(incoming);

			for (Vertex incomingVertex : incoming) {
				Set<Vertex> s = engine
						.registerSubgoal(new DependencyGoalNullInitialSolution(
								graph, incomingVertex));
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
		final Graph graph = new Graph();

		Vertex a = new Vertex("a");
		Vertex b = new Vertex("b");
		Vertex c = new Vertex("c");
		Vertex d = new Vertex("d");
		Vertex e = new Vertex("e");
		Vertex f = new Vertex("f");
		graph.addEdge(a, b);
		graph.addEdge(b, c);
		graph.addEdge(b, d);
		graph.addEdge(c, e);
		graph.addEdge(d, e);
		graph.addEdge(e, b);
		graph.addEdge(e, f);

		GoalSolver<Set<Vertex>> solver = GoalSolver.newInstance(
				new DependencyGoalNullInitialSolution(graph, c),
				new KnowledgeBase());
		Set<Vertex> dependencies = solver.solve();

		Set<Vertex> expectedDependencies = new HashSet<Vertex>();
		expectedDependencies.add(e);
		expectedDependencies.add(c);
		expectedDependencies.add(d);
		expectedDependencies.add(b);
		expectedDependencies.add(a);

		assertEquals(expectedDependencies, dependencies);
	}
}
