package uk.ac.ic.doc.gander.model;

import uk.ac.ic.doc.gander.model.codeobject.CodeObject;
import uk.ac.ic.doc.gander.model.name_binding.ScopedVariable;

/**
 * Represents a name appearing in a code block.
 * 
 * This is notably different from {@link ScopedVariable} which represents a name
 * and the namespace it binds in. This class represents a name and the code
 * block it appears in. The code block and namespace may correspond or they may
 * not. Only a binding lookup can decide.
 */
public final class Variable {

	private final String name;
	private final CodeObject codeObject;

	@Deprecated
	public Variable(String name, Namespace codeBlock) {
		if (name == null)
			throw new NullPointerException("A variable without a "
					+ "name doesn't make sense");
		if (name.isEmpty())
			throw new IllegalArgumentException("A variable without a "
					+ "name doesn't make sense");
		if (codeBlock.codeObject() == null)
			throw new NullPointerException(
					"Variables can only appear in a code block");

		this.name = name;
		this.codeObject = codeBlock.codeObject();
	}

	public Variable(String name, CodeObject codeObject) {
		if (name == null)
			throw new NullPointerException("A variable without a "
					+ "name doesn't make sense");
		if (name.isEmpty())
			throw new IllegalArgumentException("A variable without a "
					+ "name doesn't make sense");
		if (codeObject == null)
			throw new NullPointerException(
					"Variables can only appear in a code object's block");

		this.name = name;
		this.codeObject = codeObject;
	}

	public String name() {
		return name;
	}

	@Deprecated
	public Namespace codeBlock() {
		return codeObject.model().intrinsicNamespace(codeObject);
	}

	public CodeObject codeObject() {
		return codeObject;
	}

	public Model model() {
		return codeObject.model();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((codeObject == null) ? 0 : codeObject.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Variable other = (Variable) obj;
		if (codeObject == null) {
			if (other.codeObject != null)
				return false;
		} else if (!codeObject.equals(other.codeObject))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Variable [codeObject=" + codeObject + ", name=" + name + "]";
	}

}
