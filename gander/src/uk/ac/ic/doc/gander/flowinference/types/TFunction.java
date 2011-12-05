package uk.ac.ic.doc.gander.flowinference.types;

import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Namespace;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;
import uk.ac.ic.doc.gander.model.codeobject.FunctionCO;

public class TFunction implements TNamespace, TCodeObject {

	private final FunctionCO functionInstance;

	public TFunction(FunctionCO functionInstance) {
		if (functionInstance == null) {
			throw new NullPointerException("Code object required");
		}
		
		this.functionInstance = functionInstance;
	}

	public CodeObject codeObject() {
		return functionInstance;
	}

	@Deprecated
	public TFunction(Function functionInstance) {
		this(functionInstance.codeObject());
	}

	@Deprecated
	public Function getFunctionInstance() {
		return functionInstance.oldStyleConflatedNamespace();
	}

	@Deprecated
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
		return "TFunction [" + getName() + "]";
	}

}
