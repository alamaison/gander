package uk.ac.ic.doc.gander.model;

import uk.ac.ic.doc.gander.model.codeobject.CodeObject;
import uk.ac.ic.doc.gander.model.name_binding.NamespaceKey;

/**
 * Represents a name appearing in a code block.
 * 
 * This is notably different from {@link NamespaceKey} which represents a name
 * and the namespace it binds in. This class represents a name and the code
 * block it appears in. The code block and namespace may correspond or they may
 * not. Only a binding lookup can decide.
 */
public final class Variable {

	private final String name;
	private final Namespace codeBlock;

	public Variable(String name, Namespace codeBlock) {
		if (name == null)
			throw new NullPointerException("A variable without a "
					+ "name doesn't make sense");
		if (name.isEmpty())
			throw new IllegalArgumentException("A variable without a "
					+ "name doesn't make sense");
		if (codeBlock == null)
			throw new NullPointerException(
					"Variables can only appear in a code block");

		this.name = name;
		this.codeBlock = codeBlock;
	}

	public String name() {
		return name;
	}

	@Deprecated
	public Namespace codeBlock() {
		return codeBlock;
	}
	
	public CodeObject codeObject() {
		return codeBlock.codeObject();
	}

	public Model model() {
		return codeBlock.model();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((codeBlock == null) ? 0 : codeBlock.hashCode());
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
		if (codeBlock == null) {
			if (other.codeBlock != null)
				return false;
		} else if (!codeBlock.equals(other.codeBlock))
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
		return "Variable [codeBlock=" + codeBlock + ", name=" + name + "]";
	}

}
