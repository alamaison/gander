package uk.ac.ic.doc.gander.model;

public abstract class ModelWalker {

	public final void walk(Model model) {
		walkThroughPackage(model.getTopLevel());
	}

	// We have a special case here just for the top-level package because
	// the Model isn't a namespace and can't be processed by
	// walkThroughNamespace
	private void walkThroughPackage(Module topLevel) {
		visitModule(topLevel);
		walkThroughNamespace(topLevel);
	}

	private void walkThroughNamespace(Namespace namespace) {
		for (Module module : namespace.getModules().values()) {
			visitModule(module);
			walkThroughNamespace(module);
		}
		for (Class klass : namespace.getClasses().values()) {
			visitClass(klass);
			walkThroughNamespace(klass);
		}
		for (Function function : namespace.getFunctions().values()) {
			visitFunction(function);
			walkThroughNamespace(function);
		}
	}

	protected void visitModule(Module module) {
	}

	protected void visitClass(Class klass) {
	}

	protected void visitFunction(Function function) {
	}
}
