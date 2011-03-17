/**
 * 
 */
package uk.ac.ic.doc.gander.model.build;

import org.python.pydev.parser.jython.SimpleNode;

import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Module;

public final class ModuleBuilder extends ImportAwareBuilder {

	private Module module;

	public ModuleBuilder(String moduleName, Model model, BuildablePackage parent) {
		super(moduleName, model, parent);
	}

	public Module getModule() {
		return module;
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
	protected void onCreatedModule(Module scope) {
		BuildableNamespace parent = (BuildableNamespace) getScope();
		if (parent != null)
			parent.addModule(scope);
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