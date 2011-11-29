package uk.ac.ic.doc.gander.flowinference.flowgoals;

import uk.ac.ic.doc.gander.flowinference.result.Top;

/**
 * Result representing all flow positions.
 * 
 * Otherwise known as Top, this is used when the flow has 'escaped', in other
 * words when our analysis lost track of it.
 */
public final class TopFp extends Top<FlowPosition> {

	public static final TopFp INSTANCE = new TopFp();

	@Override
	public String toString() {
		return "⊤fp";
	}

	private TopFp() {
	}

}
