package uk.ac.ic.doc.gander.flowinference.dda;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

/**
 * Find the solution to a single goal.
 * 
 * An instance of this class is only valid for one goal as it must maintain
 * state.
 * @param <T>
 */
public final class GoalSolver<T> {

	private final WorkList workList = new WorkList();
	private final KnowledgeBase knowledgebase;
	private final Goal<T> rootGoal;

	public GoalSolver(Goal<T> goal) {
		rootGoal = goal;
		workList.add(goal);
		knowledgebase = new KnowledgeBase(goal);
	}

	public T solve() {
		while (!workList.isEmpty()) {
			iterate();
		}

		return (T) knowledgebase.getLastSolution(rootGoal);
	}

	private void iterate() {
		Goal<?> workItem = workList.nextWorkItem();
		boolean changed = update(workItem);
		if (changed) {
			workList.addAll(goalsDependentOnThisOne(workItem));
		}
	}

	private Collection<? extends Goal<?>> goalsDependentOnThisOne(Goal<?> goal) {
		return knowledgebase.dependents(goal);
	}

	private boolean update(final Goal<?> goal) {
		Object oldSolution = knowledgebase.getLastSolution(goal);
		Object newSolution = goal.recalculateSolution(new SubgoalManager() {

			private Object currentSolutionOfGoal(Goal<?> subgoal) {
				return knowledgebase.getLastSolution(subgoal);
			}

			public <R> R registerSubgoal(Goal<R> newSubgoal) {
				/*
				 * Only add the subgoal to the worklist if it hasn't been seen
				 * before. If it already existed, the parent already had access
				 * to the most up-to-date answer for the goal and adding it to
				 * the worklist will cause the subgoal to be recalculated even
				 * when the solution has stabilised preventing the process from
				 * terminating.
				 */
				if (knowledgebase.addGoal(goal, newSubgoal)) {
					workList.add(newSubgoal);
				}
				
				return (R) currentSolutionOfGoal(newSubgoal);
			}
		});

		knowledgebase.updateSolution(goal, newSolution);

		if (newSolution == null) {
			return oldSolution != null;
		} else {
			return !newSolution.equals(oldSolution);
		}
	}
}

/**
 * Representation of dependencies between goals.
 * 
 * The constructor doesn't take an initial goal because this doesn't belong in a
 * <em>dependency</em> graph. The initial goal, be definition, has no
 * dependencies so insisting on being created with such a goal puts unnecessary
 * requirements on the caller.
 */
final class DependencyGraph {
	private final DirectedGraph<Goal<?>, DefaultEdge> graph = new DefaultDirectedGraph<Goal<?>, DefaultEdge>(
			DefaultEdge.class);

	Collection<? extends Goal<?>> dependents(Goal<?> goal) {
		Set<Goal<?>> deps = new HashSet<Goal<?>>();
		if (graph.containsVertex(goal)) {
			for (DefaultEdge edge : graph.incomingEdgesOf(goal)) {
				deps.add(graph.getEdgeSource(edge));
			}
		}
		return deps;
	}

	void addDependency(Goal<?> parent, Goal<?> subgoal) {
		graph.addVertex(parent);
		graph.addVertex(subgoal);
		graph.addEdge(parent, subgoal);
	}

}

final class KnowledgeBase {
	private final Map<Goal<?>, Object> solutionStore = new HashMap<Goal<?>, Object>();
	private final DependencyGraph dependencies = new DependencyGraph();

	KnowledgeBase(Goal<?> goal) {
		solutionStore.put(goal, goal.initialSolution());
	}

	Collection<? extends Goal<?>> dependents(Goal<?> goal) {
		return dependencies.dependents(goal);
	}

	boolean addGoal(Goal<?> parent, Goal<?> subgoal) {
		dependencies.addDependency(parent, subgoal);

		if (!solutionStore.containsKey(subgoal)) {
			solutionStore.put(subgoal, subgoal.initialSolution());
			return true;
		} else {
			return false;
		}
	}

	Object getLastSolution(Goal<?> goal) {
		return solutionStore.get(goal);
	}

	void updateSolution(Goal<?> goal, Object newSolution) {
		assert solutionStore.containsKey(goal);
		solutionStore.put(goal, newSolution);
	}
}

/**
 * Abstraction around the storage container for the worklist.
 * 
 * The semantics require that adding equal goals repeatedly is idempotent. In
 * other words, this should act rather like a set.
 */
final class WorkList {
	Stack<Goal<?>> workList = new Stack<Goal<?>>();

	boolean isEmpty() {
		return workList.isEmpty();
	}

	void add(Goal<?> goal) {
		if (!workList.contains(goal))
			workList.push(goal);
	}

	void addAll(Collection<? extends Goal<?>> goals) {
		for (Goal<?> goal : goals) {
			add(goal);
		}
	}

	Goal<?> nextWorkItem() {
		return workList.pop();
	}
}