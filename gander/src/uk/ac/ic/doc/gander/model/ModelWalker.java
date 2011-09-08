package uk.ac.ic.doc.gander.model;

import java.util.Map.Entry;

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
		for (Entry<String, Module> module : namespace.getModules().entrySet()) {
			visitModule(module.getValue());
			walkThroughNamespace(module.getValue());
		}
		for (Entry<String, Class> klass : namespace.getClasses().entrySet()) {
			visitClass(klass.getValue());
			walkThroughNamespace(klass.getValue());
		}
		for (Entry<String, Function> function : namespace.getFunctions()
				.entrySet()) {
			visitFunction(function.getValue());
			walkThroughNamespace(function.getValue());
		}
	}

	protected void visitModule(Module module) {
	}

	protected void visitClass(Class klass) {
	}

	protected void visitFunction(Function function) {
	}
}
