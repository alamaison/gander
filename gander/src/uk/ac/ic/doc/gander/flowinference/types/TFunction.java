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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((functionInstance == null) ? 0 : functionInstance.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof TFunction))
			return false;
		TFunction other = (TFunction) obj;
		if (functionInstance == null) {
			if (other.functionInstance != null)
				return false;
		} else if (!functionInstance.equals(other.functionInstance))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TFunction [getName()=" + getName() + "]";
	}

}
