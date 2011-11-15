package uk.ac.ic.doc.gander.flowinference.flowgoals.flowsituations;

import java.util.Set;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;

/**
 * Represent the situation that an expression finds itself in that could lead to
 * the expression's value flowing elsewhere.
 * 
 * Examples include being on the RHS of an assignment statement or being a
 * parameter to a function.
 */
public interface FlowSituation {

	/**
	 * Returns the flow positions that the expression can flow to in a single
	 * execution step.
	 */
	Set<FlowPosition> nextFlowPositions(SubgoalManager goalManager);

}
