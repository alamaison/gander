package uk.ac.ic.doc.gander.flowinference.types;

import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Namespace;
import uk.ac.ic.doc.gander.model.codeobject.ClassCO;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;

public class TClass implements TNamespace, TCodeObject {

	private final ClassCO classInstance;

	public TClass(ClassCO classInstance) {
		if (classInstance == null) {
			throw new NullPointerException("Code object required");
		}

		this.classInstance = classInstance;
	}

	public CodeObject codeObject() {
		return classInstance;
	}

	@Deprecated
	public TClass(Class classInstance) {
		this(classInstance.codeObject());
	}

	@Deprecated
	public Class getClassInstance() {
		return classInstance.oldStyleConflatedNamespace();
	}

	public Namespace getNamespaceInstance() {
		return getClassInstance();
	}

	public String getName() {
		return getNamespaceInstance().getFullName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((classInstance == null) ? 0 : classInstance.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TClass other = (TClass) obj;
		if (classInstance == null) {
			if (other.classInstance != null)
				return false;
		} else if (!classInstance.equals(other.classInstance))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TClass [" + getName() + "]";
	}

}
