package uk.ac.ic.doc.gander.model;

import uk.ac.ic.doc.gander.model.name_binding.BindingLocation;
import uk.ac.ic.doc.gander.model.name_binding.Variable;

/**
 * Represents a name in a namespace.
 * 
 * This differs from {@link Variable} as it doesn't represent a variable name,
 * just a named entry in the namespace. Each variable can be resolved to an
 * instance of this class.
 */
public final class NamespaceName {

	private final String name;
	private final Namespace namespace;

	/**
	 * Constructs new representation of a name in a namespace.
	 * 
	 * @param name
	 *            the name acting as a key to map to objects
	 * @param namespace
	 *            the namespace in which the name acts as a key to map to
	 *            objects
	 */
	public NamespaceName(String name, Namespace namespace) {
		if (name == null)
			throw new NullPointerException(
					"Names in a namespace must actually exits");
		if (name.isEmpty())
			throw new IllegalArgumentException("Names must have characters");
		if (namespace == null)
			throw new NullPointerException(
					"Namespace names need a namespace to hold them");

		this.name = name;
		this.namespace = namespace;
	}

	/**
	 * Converts a binding location into the namespace name model used for
	 * flow-based type inference.
	 * 
	 * @param bindingLocation
	 *            the location to convert
	 */
	public NamespaceName(BindingLocation bindingLocation) {
		this(bindingLocation.name(), bindingLocation.codeObject().unqualifiedNamespace());
	}

	public String name() {
		return name;
	}

	public Namespace namespace() {
		return namespace;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((namespace == null) ? 0 : namespace.hashCode());
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
		NamespaceName other = (NamespaceName) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (namespace == null) {
			if (other.namespace != null)
				return false;
		} else if (!namespace.equals(other.namespace))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "NamespaceName[name=" + name + ", namespace=" + namespace + "]";
	}

}
