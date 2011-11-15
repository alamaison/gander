package uk.ac.ic.doc.gander.model;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.VisitorBase;

import uk.ac.ic.doc.gander.model.codeblock.CodeBlock;

public class ModelSite<T extends SimpleNode> {
	private final T node;
	private final Namespace codeObject;
	private final Model model;

	public ModelSite(T node, Namespace codeObject, Model model) {
		this.node = node;
		this.codeObject = codeObject;
		this.model = model;
	}

	public Model model() {
		return model;
	}

	public Namespace codeObject() {
		return codeObject;
	}

	public T astNode() {
		/*
		 * TODO: Move this to the constructor once we separate Namespaces and
		 * CodeBlocks. It's here for the moment because asCodeBlock calls the
		 * constructor and the constructor would call asCodeBlock leading to an
		 * infinite loop.
		 */
		assert codeBlockContainsNode(codeObject.asCodeBlock(), node);
		return node;
	}

	private boolean codeBlockContainsNode(CodeBlock codeBlock,
			final T subjectNode) {
		final boolean foundNode[] = { false };

		try {
			codeBlock.accept(new VisitorBase() {

				@Override
				protected Object unhandled_node(SimpleNode node)
						throws Exception {
					if (node.equals(subjectNode))
						foundNode[0] = true;
					return null;
				}

				@Override
				public void traverse(SimpleNode node) throws Exception {
					node.traverse(this);
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return foundNode[0];
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((codeObject == null) ? 0 : codeObject.hashCode());
		result = prime * result + ((model == null) ? 0 : model.hashCode());
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
		if (codeObject == null) {
			if (other.codeObject != null)
				return false;
		} else if (!codeObject.equals(other.codeObject))
			return false;
		if (model == null) {
			if (other.model != null)
				return false;
		} else if (!model.equals(other.model))
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
		return "ModelSite [codeObject=" + codeObject + ", node=" + node + "]";
	}

}
