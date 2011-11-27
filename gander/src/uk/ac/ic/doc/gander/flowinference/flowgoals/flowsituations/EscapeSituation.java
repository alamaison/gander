package uk.ac.ic.doc.gander.flowinference.flowgoals.flowsituations;

import java.util.Set;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;

final class EscapeSituation implements FlowSituation {

	public Set<FlowPosition> nextFlowPositions(SubgoalManager goalManager) {
		/*
		 * null means Top, the set of all flow positions.
		 */
		return null;
	}

}
