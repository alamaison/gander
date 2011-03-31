/**
 * 
 */
package uk.ac.ic.doc.gander.model.build;

import org.python.pydev.parser.jython.SimpleNode;

import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Package;


/**
 * A module builder that doesn't follow imports.
 */
public final class DumbModuleBuilder extends ModuleNamespaceBuilder {

	private Module module;
	
	public DumbModuleBuilder(String moduleName, Package parent) {
		super(moduleName, parent);
	}

	public Module getModule() {
		return module;
	}

	@Override
	protected void onCreatedModule(Module module) {
		this.module = module;
	}

	@Override
	protected void onCreatedClass(Class scope) {
		BuildableNamespace parent = (BuildableNamespace) getScope();
		if (parent != null)
			parent.addClass(scope);
	}

	@Override
	protected void onCreatedFunction(Function scope) {
		BuildableNamespace parent = (BuildableNamespace) getScope();
		if (parent != null)
			parent.addFunction(scope);
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