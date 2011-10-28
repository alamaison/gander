package uk.ac.ic.doc.gander.flowinference.types;

import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Namespace;

public class TClass implements TNamespace {

	private final Class classInstance;

	public TClass(Class classInstance) {
		assert classInstance != null;
		this.classInstance = classInstance;
	}

	public Class getClassInstance() {
		return classInstance;
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
