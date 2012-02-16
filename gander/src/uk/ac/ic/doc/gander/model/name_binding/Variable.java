package uk.ac.ic.doc.gander.model.name_binding;

import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;

/**
 * Represents a name appearing in a code block.
 * 
 * This class represents a name and the code block it appears in. The code block
 * and namespace may correspond or they may not. Only a binding lookup can
 * decide.
 * 
 * To look up the binding namespace, call {@link bindingLocation}.
 */
public class Variable {

	private final String name;
	private final CodeObject codeObject;

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

	public CodeObject codeObject() {
		return codeObject;
	}

	/**
	 * Return the location that is accessed or modified by this variable.
	 * 
	 * Variables have two locations of interest. One is the code object they
	 * appear in. The other is the code object whose execution namespace they
	 * are an unqualified reference to, the binding namespace.
	 * 
	 * Variables can appear all over the place but, in a lexically bound
	 * language like Python, each appearance of a variable binds in a single
	 * location. This binding location will be the same throughout the code
	 * block it appears in. This method finds that location.
	 */
	public BindingLocation bindingLocation() {
		return Binder.resolveBindingLocation(this);
	}

	@Deprecated
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
		return "Variable [name=" + name + ", codeObject=" + codeObject + "]";
	}

}
