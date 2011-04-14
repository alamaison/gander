/**
 * 
 */
package uk.ac.ic.doc.gander.model.build;

import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Package;

public final class PackageModuleBuilder extends ImportAwareBuilder {

	private Module module;

	public PackageModuleBuilder(String moduleName, Model model, Package parent) {
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
		// The package module shouldn't automatically be added to the parent
		// package as we will just copy the stuff out of it later
		module = scope;
	}
}