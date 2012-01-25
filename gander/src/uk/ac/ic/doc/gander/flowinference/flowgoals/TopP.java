package uk.ac.ic.doc.gander.flowinference.flowgoals;

import uk.ac.ic.doc.gander.flowinference.result.Top;
import uk.ac.ic.doc.gander.model.codeobject.FormalParameter;

/**
 * Result representing all parameters of every callable.
 * 
 * Otherwise known as Top, this is used when our analysis lost track of where an
 * argument is received.
 */
public final class TopP extends Top<FormalParameter> {

	public static final TopP INSTANCE = new TopP();

	@Override
	public String toString() {
		return "⊤p";
	}

	private TopP() {
	}

}
