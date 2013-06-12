package uk.ac.ic.doc.gander.flowinference.argument;

import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.model.parameters.FormalParameter;

/**
 * Models the destination of an argument passed to an invokable.
 * 
 * This would be a {@link FormalParameter} were it not for the fact that
 * Python's calling posibilities include an argument ending up as <em>part</em>
 * of a paramter (for example a tuple element) rather than as the the parameter
 * itself.
 */
public interface ArgumentDestination {

	public Result<FlowPosition> nextFlowPositions();
}
