package uk.ac.ic.doc.gander.flowinference.flowgoals.expressionflow;

import java.util.Set;

import uk.ac.ic.doc.gander.flowinference.argument.ArgumentDestination;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.TopFp;
import uk.ac.ic.doc.gander.flowinference.result.RedundancyEliminator;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Result.Transformer;

/**
 * Turns formal parameters into flow positions.
 */
public final class ReceivingParameterPositioner implements
		Transformer<ArgumentDestination, Result<FlowPosition>> {

	@Override
	public Result<FlowPosition> transformFiniteResult(
			Set<ArgumentDestination> receivingParameters) {

		RedundancyEliminator<FlowPosition> parameterPositions = new RedundancyEliminator<FlowPosition>();

		for (ArgumentDestination parameter : receivingParameters) {
			parameterPositions.add(parameter.nextFlowPositions());
			if (parameterPositions.isFinished()) {
				break;
			}
		}

		return parameterPositions.result();
	}

	@Override
	public Result<FlowPosition> transformInfiniteResult() {
		return TopFp.INSTANCE;
	}
}