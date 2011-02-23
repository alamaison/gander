package uk.ac.ic.doc.gander.flowinference.types;

import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Scope;

public class TFunction implements ScopeType {

	private Function functionInstance;

	public TFunction(Function functionInstance) {
		this.functionInstance = functionInstance;
	}

	public Function getFunctionInstance() {
		return functionInstance;
	}

	public Scope getScopeInstance() {
		return getFunctionInstance();
	}

}
