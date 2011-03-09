package uk.ac.ic.doc.gander.flowinference.types;

import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Scope;

public class TModule implements TImportable, ScopeType {
	private Module moduleInstance;

	public TModule(Module module) {
		assert module != null;
		moduleInstance = module;
	}

	public Module getModuleInstance() {
		return moduleInstance;
	}

	public Scope getScopeInstance() {
		return getModuleInstance();
	}
}
