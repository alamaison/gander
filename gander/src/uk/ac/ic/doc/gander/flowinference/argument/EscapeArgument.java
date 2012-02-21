package uk.ac.ic.doc.gander.flowinference.argument;

import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.TopFp;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;

/**
 * A special argument that really signifies we have no idea where a passed value
 * ends up.
 */
final class EscapeArgument implements Argument {

	@Override
	public ArgumentDestination passArgumentAtCall(InvokableCodeObject receiver) {

		return new ArgumentDestination() {

			@Override
			public Result<FlowPosition> nextFlowPositions() {
				return TopFp.INSTANCE;
			}
		};

	}
}