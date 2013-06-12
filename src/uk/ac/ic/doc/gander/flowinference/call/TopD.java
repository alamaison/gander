package uk.ac.ic.doc.gander.flowinference.call;

import uk.ac.ic.doc.gander.flowinference.result.Top;

/**
 * Result representing the infinite set of dispatched calls.
 * 
 * Otherwise known as Top, this is used when our analysis is unable to determine
 * how a code object is invoked.
 */
public final class TopD extends Top<CallDispatch> {

	public final static TopD INSTANCE = new TopD();

	private TopD() {
	}

	@Override
	public String toString() {
		return "⊤i";
	}

}
