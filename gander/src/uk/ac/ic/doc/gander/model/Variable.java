package uk.ac.ic.doc.gander.model;

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
	private final Model model;

	public Variable(String name, Namespace codeBlock, Model model) {
		this.name = name;
		this.codeBlock = codeBlock;
		this.model = model;
	}

	public String name() {
		return name;
	}

	public Namespace codeBlock() {
		return codeBlock;
	}

	public Model model() {
		return model;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((codeBlock == null) ? 0 : codeBlock.hashCode());
		result = prime * result + ((model == null) ? 0 : model.hashCode());
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
		return true;
	}

	@Override
	public String toString() {
		return "Variable [codeBlock=" + codeBlock + ", name=" + name + "]";
	}

}
