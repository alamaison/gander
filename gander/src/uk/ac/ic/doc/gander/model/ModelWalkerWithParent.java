package uk.ac.ic.doc.gander.model;

import java.util.Stack;
import java.util.Map.Entry;

public abstract class ModelWalkerWithParent {

	private Stack<Module> ancestors = new Stack<Module>();

	public final void walk(Model model) {
		walkThroughPackage(model.getTopLevel());
	}

	protected final Module getEnclosingModule() {
		return ancestors.peek();
	}

	protected void visitPackage(Module pkg) {
	}

	protected void visitModule(Module module) {
	}

	protected void visitClass(Class klass) {
	}

	protected void visitFunction(Function function) {
	}

	// We have a special case here just for the top-level package because
	// the Model isn't a namespace and can't be processed by
	// walkThroughNamespace
	private void walkThroughPackage(Module pkg) {
		visitPackage(pkg);
		ancestors.push(pkg);
		walkThroughNamespace(pkg);
		ancestors.pop();
	}

	private void walkThroughNamespace(OldNamespace namespace) {
		for (Entry<String, Module> module : namespace.getModules().entrySet()) {
			visitModule(module.getValue());
			ancestors.push(module.getValue());
			walkThroughNamespace(module.getValue());
			ancestors.pop();
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

}
