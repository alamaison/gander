package uk.ac.ic.doc.gander.flowinference.flowgoals.expressionflow;

import java.util.Set;

import uk.ac.ic.doc.gander.flowinference.argument.ArgumentPassage;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.TopFp;
import uk.ac.ic.doc.gander.flowinference.result.RedundancyEliminator;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Result.Transformer;

/**
 * Turns formal parameters into flow positions.
 */
public final class ReceivingParameterPositioner implements
		Transformer<ArgumentPassage, Result<FlowPosition>> {

	@Override
	public Result<FlowPosition> transformFiniteResult(
			Set<ArgumentPassage> receivingParameters) {

		RedundancyEliminator<FlowPosition> parameterPositions = new RedundancyEliminator<FlowPosition>();

		for (ArgumentPassage parameter : receivingParameters) {
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