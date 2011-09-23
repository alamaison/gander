package uk.ac.ic.doc.gander.flowinference.types;

import uk.ac.ic.doc.gander.model.Class;

public class TObject implements Type {

	private final Class metaclass;

	public TObject(Class classInstance) {
		assert classInstance != null;
		this.metaclass = classInstance;
	}

	public Class getClassInstance() {
		return metaclass;
	}

	public String getName() {
		return "Instance<" + getClassInstance().getFullName() + ">";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((metaclass == null) ? 0 : metaclass.hashCode());
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
		TObject other = (TObject) obj;
		if (metaclass == null) {
			if (other.metaclass != null)
				return false;
		} else if (!metaclass.equals(other.metaclass))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getName();
	}

}
