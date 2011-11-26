package uk.ac.ic.doc.gander.model.name_binding;

import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Namespace;
import uk.ac.ic.doc.gander.model.NamespaceName;
import uk.ac.ic.doc.gander.model.Variable;

/**
 * Represents a variable whose binding namespace (scope) has been resolved.
 * 
 * Variables have two namespaces of interest. One is the namespace of the code
 * object they appear in, the enclosing namespace. The other is the namespace
 * whose matching name they are an unqualified reference to, the binding
 * namespace. This class links those namespaces.
 * 
 * Variables can appear all over the place but, in a lexically bound language
 * like Python, each appearance of a variable binds in a single namespace. This
 * namespace, the binding namespace, will be the same throughout the code block
 * it appears in.
 */
public final class ScopedVariable {

	private final Variable variable;
	private final NamespaceName bindingLocation;

	/**
	 * Construct new resolved scope for a variable.
	 * 
	 * @param variable
	 *            the bound variable
	 * @param bindingLocation
	 *            the locations that the variable binds in
	 */
	ScopedVariable(Variable variable, NamespaceName bindingLocation) {
		if (variable == null)
			throw new NullPointerException("Variable not optional");
		if (bindingLocation == null)
			throw new NullPointerException(
					"Scope not optional for a scoped variable");
		if (!variable.name().equals(bindingLocation.name()))
			throw new IllegalArgumentException(
					"A variable can only bind in a namespace location "
							+ "with the same name");

		this.variable = variable;
		this.bindingLocation = bindingLocation;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		assert variable.name().equals(bindingLocation.name());
		return variable.name();
	}

	public NamespaceName bindingLocation() {
		return bindingLocation;
	}

	public Variable variable() {
		return variable;
	}

	/**
	 * @return the runtime model
	 */
	@Deprecated
	public Model getModel() {
		return variable.model();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((bindingLocation == null) ? 0 : bindingLocation.hashCode());
		result = prime * result
				+ ((variable == null) ? 0 : variable.hashCode());
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
		ScopedVariable other = (ScopedVariable) obj;
		if (bindingLocation == null) {
			if (other.bindingLocation != null)
				return false;
		} else if (!bindingLocation.equals(other.bindingLocation))
			return false;
		if (variable == null) {
			if (other.variable != null)
				return false;
		} else if (!variable.equals(other.variable))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ScopedVariable [bindingLocation=" + bindingLocation
				+ ", variable=" + variable + "]";
	}

}
