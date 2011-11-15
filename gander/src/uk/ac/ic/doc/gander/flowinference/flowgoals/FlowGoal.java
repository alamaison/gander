package uk.ac.ic.doc.gander.flowinference.flowgoals;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.dda.Goal;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.model.ModelSite;

/**
 * This is the root of all flow queries.
 * 
 * This class is responsible to building the transitive closure of the one-step
 * flow goal solutions. All other work is delegated to those subgoals.
 */
public final class FlowGoal implements Goal<Set<ModelSite<? extends exprType>>> {

	private final FlowPosition position;

	public FlowGoal(FlowPosition position) {
		this.position = position;
	}

	public Set<ModelSite<? extends exprType>> initialSolution() {
		return Collections.emptySet();
	}

	public Set<ModelSite<? extends exprType>> recalculateSolution(
			SubgoalManager goalManager) {
		Set<FlowPosition> closure = new HashSet<FlowPosition>();

		Set<FlowPosition> positions = Collections.singleton(position);

		/*
		 * Flow-search continues until no new positions are found.
		 */
		do {
			// null indicates Top, all positions, and trumps everything
			if (positions == null) {
				return null;
			}

			closure.addAll(positions);
			positions = findNextFlowPositions(positions, goalManager);
		} while (!isSuperset(closure, positions));

		/*
		 * Only expression positions make sense for an external caller. All
		 * others are intermediate steps and can be discarded because they have
		 * no source-code equivalent.
		 */
		return filterPositions(closure);
	}

	private static Set<ModelSite<? extends exprType>> filterPositions(
			Set<FlowPosition> closure) {
		Set<ModelSite<? extends exprType>> expressions = new HashSet<ModelSite<? extends exprType>>();

		for (FlowPosition flowPosition : closure) {
			if (flowPosition instanceof ExpressionPosition<?>) {
				expressions.add(((ExpressionPosition<?>) flowPosition)
						.getSite());
			}
		}
		return expressions;
	}

	private Set<FlowPosition> findNextFlowPositions(
			Set<FlowPosition> previousPositions, SubgoalManager goalManager) {
		HashSet<FlowPosition> nextPositions = new HashSet<FlowPosition>();

		for (FlowPosition fPos : previousPositions) {
			Set<FlowPosition> positions = goalManager.registerSubgoal(fPos
					.nextStepGoal());

			// null indicates Top, all positions, and trumps everything
			if (positions == null) {
				return null;
			}

			nextPositions.addAll(positions);
		}

		return nextPositions;
	}

	/**
	 * Is a set a superset of another.
	 * 
	 * {@code null} is taken to mean the set of everything so is the superset of
	 * everything.
	 * 
	 * @param positions
	 *            the candidate superset
	 * @param nextStepPositions
	 *            the comparison set
	 * @return
	 */
	private static boolean isSuperset(Set<FlowPosition> positions,
			Set<FlowPosition> nextStepPositions) {
		if (positions == null) {
			return true;
		} else if (nextStepPositions == null) {
			return false;
		}

		Set<FlowPosition> rhs = new HashSet<FlowPosition>(nextStepPositions);
		rhs.removeAll(positions);
		return rhs.isEmpty();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((position == null) ? 0 : position.hashCode());
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
		FlowGoal other = (FlowGoal) obj;
		if (position == null) {
			if (other.position != null)
				return false;
		} else if (!position.equals(other.position))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FlowGoal [position=" + position + "]";
	}

}
