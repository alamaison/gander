package uk.ac.ic.doc.gander.flowinference.types;

import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Namespace;

public class TFunction implements TNamespace {

	private Function functionInstance;

	public TFunction(Function functionInstance) {
		assert functionInstance != null;
		this.functionInstance = functionInstance;
	}

	public Function getFunctionInstance() {
		return functionInstance;
	}

	public Namespace getNamespaceInstance() {
		return getFunctionInstance();
	}

	public String getName() {
		return getNamespaceInstance().getFullName();
	}

}
