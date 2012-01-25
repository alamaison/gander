package uk.ac.ic.doc.gander.flowinference.dda;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Stack;

import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;

/**
 * Find the solution to a single goal.
 * 
 * An instance of this class is only valid for one goal as it must maintain
 * state.
 * 
 * @param <T>
 */
public final class GoalSolver<T> {

	private final WorkList workList = new DequeBasedWorkList();
	private final KnowledgeBase knowledgebase;
	private final Goal<T> rootGoal;
	private File debugTrigger;

	public static <R> GoalSolver<R> newInstance(Goal<R> goal,
			KnowledgeBase knowledgebase) {
		return new GoalSolver<R>(goal, knowledgebase);
	}

	@Deprecated
	public GoalSolver(Goal<T> goal, KnowledgeBase knowledgebase) {
		rootGoal = goal;
		this.knowledgebase = knowledgebase;
		workList.add(goal);
		this.knowledgebase.addGoal(goal);

		debugTrigger = new File("dodebug");
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
				if (knowledgebase.addGoal(newSubgoal, goal)) {
					workList.add(newSubgoal);
				}

				return (R) currentSolutionOfGoal(newSubgoal);
			}
		});

		if (debugTrigger.exists()) {
			System.out.println("-" + goal + ":");

			if (newSolution instanceof FiniteResult<?>) {
				if (((FiniteResult<?>) newSolution).isEmpty()) {
					System.out.println("\t\tBOTTOM");
				} else {
					for (Object result : ((FiniteResult<?>) newSolution)) {
						System.out.println("\t\t" + result);
					}
				}
			} else {

				System.out.println("\t\t" + newSolution);
			}
		}

		knowledgebase.updateSolution(goal, newSolution);

		if (newSolution == null) {
			return oldSolution != null;
		} else {
			return !newSolution.equals(oldSolution);
		}
	}
}

/**
 * Abstraction around the storage container for the worklist.
 * 
 * The semantics require that adding equal goals repeatedly is idempotent. In
 * other words, this should act rather like a set.
 */
interface WorkList {

	boolean isEmpty();

	void add(Goal<?> goal);

	void addAll(Collection<? extends Goal<?>> goals);

	Goal<?> nextWorkItem();
}

/**
 * Worklist implementation based on a stack.
 * 
 * The implications are that if a goal with no dependencies adds a new subgoal,
 * that goal will be chosen for the next iteration. However, if the goal already
 * has dependencies and it's new solution has changed, those dependencies are
 * added instead.
 */
final class StackBasedWorkList implements WorkList {
	Stack<Goal<?>> workList = new Stack<Goal<?>>();

	public boolean isEmpty() {
		return workList.isEmpty();
	}

	public void add(Goal<?> goal) {
		if (!workList.contains(goal))
			workList.push(goal);
	}

	public void addAll(Collection<? extends Goal<?>> goals) {
		for (Goal<?> goal : goals) {
			add(goal);
		}
	}

	public Goal<?> nextWorkItem() {
		return workList.pop();
	}
}

/**
 * Worklist implementation based on a deque.
 */
final class DequeBasedWorkList implements WorkList {
	Deque<Goal<?>> workList = new ArrayDeque<Goal<?>>();

	public boolean isEmpty() {
		return workList.isEmpty();
	}

	public void add(Goal<?> goal) {
		if (!workList.contains(goal))
			workList.push(goal);
	}

	public void addAll(Collection<? extends Goal<?>> goals) {
		for (Goal<?> goal : goals) {
			workList.addFirst(goal);
		}
	}

	public Goal<?> nextWorkItem() {
		return workList.pop();
	}
}
