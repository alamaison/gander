package uk.ac.ic.doc.gander.flowinference.flowgoals;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.dda.Goal;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.expressionflow.ExpressionPosition;
import uk.ac.ic.doc.gander.flowinference.result.Concentrator;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.RedundancyEliminator;
import uk.ac.ic.doc.gander.flowinference.result.Concentrator.DatumProcessor;
import uk.ac.ic.doc.gander.flowinference.result.Result.Processor;
import uk.ac.ic.doc.gander.model.ModelSite;

/**
 * This is the root of all flow queries.
 * 
 * This class is responsible to building the transitive closure of the one-step
 * flow goal solutions. All other work is delegated to those subgoals.
 */
public final class FlowGoal implements Goal<Result<ModelSite<exprType>>> {

	private final FlowPosition position;

	public FlowGoal(FlowPosition position) {
		this.position = position;
	}

	public Result<ModelSite<exprType>> initialSolution() {
		return FiniteResult.bottom();
	}

	public Result<ModelSite<exprType>> recalculateSolution(
			SubgoalManager goalManager) {

		return new FlowGoalSolver(position, goalManager).solution();
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

final class FlowGoalSolver {

	private final SubgoalManager goalManager;
	private final Result<ModelSite<exprType>> solution;

	FlowGoalSolver(FlowPosition position, SubgoalManager goalManager) {
		this.goalManager = goalManager;

		RedundancyEliminator<FlowPosition> closure = new RedundancyEliminator<FlowPosition>();

		Result<FlowPosition> positions = new FiniteResult<FlowPosition>(
				Collections.singleton(position));
		/*
		 * Flow-search continues until no new positions are found.
		 */
		do {
			closure.add(positions);
			positions = findNextFlowPositions(positions);

		} while (!closure.isFinished()
				&& !isSuperset(closure.result(), positions));

		/*
		 * Only expression positions make sense for an external caller. All
		 * others are intermediate steps and can be discarded because they have
		 * no source-code equivalent.
		 */
		solution = filterPositions(closure.result());
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
	private boolean isSuperset(Result<FlowPosition> positions,
			Result<FlowPosition> nextStepPositions) {
		return new SuperSetChecker(positions, nextStepPositions).isSuperset();
	}

	private final class SuperSetChecker {

		private Set<FlowPosition> positionsSet = null;
		private Set<FlowPosition> nextStepPositionsSet = null;

		SuperSetChecker(Result<FlowPosition> positions,
				Result<FlowPosition> nextStepPositions) {

			positions.actOnResult(new Processor<FlowPosition>() {

				public void processInfiniteResult() {
					// null means top here - one of the few places
				}

				public void processFiniteResult(Set<FlowPosition> result) {
					positionsSet = result;
				}
			});

			nextStepPositions.actOnResult(new Processor<FlowPosition>() {

				public void processInfiniteResult() {
					// null means top here - one of the few places
				}

				public void processFiniteResult(Set<FlowPosition> result) {
					nextStepPositionsSet = result;
				}
			});

		}

		boolean isSuperset() {

			if (positionsSet == null) {
				return true;
			} else if (nextStepPositionsSet == null) {
				return false;
			} else {
				Set<FlowPosition> rhs = new HashSet<FlowPosition>(
						nextStepPositionsSet);
				rhs.removeAll(positionsSet);
				return rhs.isEmpty();
			}
		}

	}

	private Result<ModelSite<exprType>> filterPositions(
			Result<FlowPosition> positions) {
		return new PositionFilter(positions).filteredPositions;
	}

	public Result<ModelSite<exprType>> solution() {
		return solution;
	}

	private Result<FlowPosition> findNextFlowPositions(
			Result<FlowPosition> previousPositions) {

		Concentrator<FlowPosition, FlowPosition> finder = Concentrator
				.newInstance(new DatumProcessor<FlowPosition, FlowPosition>() {

					public Result<FlowPosition> process(FlowPosition fPos) {
						return goalManager.registerSubgoal(fPos.nextStepGoal());
					}
				}, TopFp.INSTANCE);

		previousPositions.actOnResult(finder);
		return finder.result();
	}

	private final class PositionFilter {

		private Result<ModelSite<exprType>> filteredPositions;

		private final Processor<FlowPosition> processor = new Processor<FlowPosition>() {

			public void processInfiniteResult() {
				filteredPositions = TopF.INSTANCE;
			}

			public void processFiniteResult(Set<FlowPosition> result) {

				Set<ModelSite<exprType>> expressions = new HashSet<ModelSite<exprType>>();

				for (FlowPosition flowPosition : result) {
					if (flowPosition instanceof ExpressionPosition) {
						ModelSite<? extends exprType> position = ((ExpressionPosition) flowPosition)
								.getSite();

						// TODO: better way to lose wildcard
						expressions.add(new ModelSite<exprType>(position
								.astNode(), position.codeObject()));
					}
				}

				filteredPositions = new FiniteResult<ModelSite<exprType>>(
						expressions);
			}
		};

		PositionFilter(Result<FlowPosition> positions) {

			positions.actOnResult(processor);
		}
	}

}
