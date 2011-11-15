package uk.ac.ic.doc.gander.model;

/**
 * Like {@link NamespaceWalker} but treats all model elements uniformly.
 */
public abstract class CodeObjectWalker {

	public final void walk(Namespace root) {
		visitCodeObject(root);
		walkThroughNamespace(root);
	}

	private void walkThroughNamespace(Namespace namespace) {
		for (Module module : namespace.getModules().values()) {
			walk(module);
		}
		for (Class klass : namespace.getClasses().values()) {
			walk(klass);
		}
		for (Function function : namespace.getFunctions().values()) {
			walk(function);
		}
	}

	/**
	 * Triggered on encountering a model code-block element.
	 * 
	 * TODO: Namespace should eventually become CodeObject.
	 */
	protected abstract void visitCodeObject(Namespace codeObject);
}
