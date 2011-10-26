package uk.ac.ic.doc.gander.flowinference.modelgoals;

import org.python.pydev.parser.jython.SimpleNode;

import uk.ac.ic.doc.gander.model.Namespace;

public class ModelSite<T extends SimpleNode> {
	private final T node;
	private final Namespace enclosingScope;

	ModelSite(T node, Namespace enclosingScope) {
		this.node = node;
		this.enclosingScope = enclosingScope;
	}

	public Namespace getEnclosingScope() {
		return enclosingScope;
	}

	public T getNode() {
		return node;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((enclosingScope == null) ? 0 : enclosingScope.hashCode());
		result = prime * result + ((node == null) ? 0 : node.hashCode());
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
		ModelSite<?> other = (ModelSite<?>) obj;
		if (enclosingScope == null) {
			if (other.enclosingScope != null)
				return false;
		} else if (!enclosingScope.equals(other.enclosingScope))
			return false;
		if (node == null) {
			if (other.node != null)
				return false;
		} else if (!node.equals(other.node))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ModelSite [enclosingScope=" + enclosingScope + ", node=" + node
				+ "]";
	}

}
