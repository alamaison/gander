package uk.ac.ic.doc.gander.model.name_binding;

import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Namespace;

/**
 * Represents a name binding.
 * 
 * In other words, a name bound in a particular namespace. Names can appear all
 * over the place but, in a lexically bound language like Python, each
 * appearance of the name binds in a single namespace. Instance of this class
 * represents such bindings.
 */
public final class NamespaceKey {
	private final String name;
	private final Namespace namespace;
	private final Model model;

	/**
	 * Construct new name binding.
	 * 
	 * @param name
	 *            the bound name
	 * @param namespace
	 *            the namespace the name is bound in
	 * @param model
	 *            runtime model to which the namespace belongs
	 */
	NamespaceKey(String name, Namespace namespace, Model model) {
		if (name == null)
			throw new NullPointerException("A name binding without a "
					+ "name doesn't make sense");
		if (name.isEmpty())
			throw new IllegalArgumentException("A name binding without a "
					+ "name doesn't make sense");
		if (namespace == null)
			throw new NullPointerException("A name binding without a "
					+ "namespace doesn't make sense");
		if (model == null)
			throw new NullPointerException("A binding only exists "
					+ "in the context of particular model");

		this.name = name;
		this.namespace = namespace;
		this.model = model;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the namespace the name is bound in
	 */
	public Namespace getNamespace() {
		return namespace;
	}

	/**
	 * @return the runtime model
	 */
	public Model getModel() {
		return model;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((model == null) ? 0 : model.hashCode());
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
		NamespaceKey other = (NamespaceKey) obj;
		if (model == null) {
			if (other.model != null)
				return false;
		} else if (!model.equals(other.model))
			return false;
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
		return "NamespaceKey [name=" + name + ", namespace=" + namespace + "]";
	}
	
}
