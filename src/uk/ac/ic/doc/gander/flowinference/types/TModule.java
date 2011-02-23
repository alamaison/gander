package uk.ac.ic.doc.gander.flowinference.types;

import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Scope;

public class TModule implements ScopeType {
	private Module moduleInstance;

	public TModule(Module module) {
		moduleInstance = module;
	}
	
	public Module getModuleInstance() {
		return moduleInstance;
	}

	public Scope getScopeInstance() {
		return getModuleInstance();
	}
}
