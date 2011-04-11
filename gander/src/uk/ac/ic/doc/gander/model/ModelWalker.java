package uk.ac.ic.doc.gander.model;

import java.util.Map.Entry;

public abstract class ModelWalker {

	public final void walk(Model model) {
		walkThroughPackage(model.getTopLevelPackage());

	}

	// We have a special case here just for the top-level package because
	// the Model isn't a namespace and can't be processed by
	// walkThroughNamespace
	private void walkThroughPackage(Package pkg) {
		visitPackage(pkg);
		walkThroughNamespace(pkg);
	}

	private void walkThroughNamespace(Namespace namespace) {
		for (Entry<String, Package> pkg : namespace.getPackages().entrySet()) {
			visitPackage(pkg.getValue());
			walkThroughNamespace(pkg.getValue());
		}
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

	protected void visitPackage(Package pkg) {
	}

	protected void visitModule(Module value) {
	}

	protected void visitClass(Class value) {
	}

	protected void visitFunction(Function value) {
	}
}
