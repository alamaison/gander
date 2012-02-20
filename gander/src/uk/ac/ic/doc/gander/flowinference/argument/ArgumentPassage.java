package uk.ac.ic.doc.gander.flowinference.argument;

import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.result.Result;

public interface ArgumentPassage {

	public Result<FlowPosition> nextFlowPositions();
}
