/**
 * 
 */
package uk.ac.ic.doc.gander.model.build;

import org.python.pydev.parser.jython.SimpleNode;

import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Package;

class ModuleBuilderVisitor extends ScopedModuleBuilder {

	private Module module;

	public ModuleBuilderVisitor(String moduleName, Package parent) {
		super(moduleName, parent);
	}

	public Module getModule() {
		return module;
	}

	@Override
	protected void createdClass(Class scope) {
		getScope().addClass(scope);
	}

	@Override
	protected void createdFunction(Function scope) {
		getScope().addFunction(scope);
	}

	@Override
	protected void createdModule(Module scope) {
		module = scope;
	}

	@Override
	public void traverse(SimpleNode node) throws Exception {
		node.traverse(this);
	}

	@Override
	protected Object unhandled_node(SimpleNode node) throws Exception {
		return null;
	}

}