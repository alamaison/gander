package uk.ac.ic.doc.gander.concretetype;

import uk.ac.ic.doc.gander.flowinference.result.Top;
import uk.ac.ic.doc.gander.implementation.Implementation;

/**
 * Result representing all concrete types.
 * 
 * Otherwise known as Top, this is used when insufficient information was
 * available to infer the actual implementation(s) and is used as a conservative
 * estimate in those cases.
 */
public final class TopC extends Top<Implementation> implements ConcreteType {

	public static final TopC INSTANCE = new TopC();

	@Override
	public String toString() {
		return "\u22A4c";
	}

	private TopC() {
	}

}
