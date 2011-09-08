package uk.ac.ic.doc.gander.flowinference.types;

import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Namespace;

public class TModule implements TImportable, TNamespace {
	private Module moduleInstance;

	public TModule(Module loaded) {
		assert loaded != null;
		moduleInstance = loaded;
	}

	public Module getModuleInstance() {
		return moduleInstance;
	}

	public Namespace getNamespaceInstance() {
		return getModuleInstance();
	}

	public String getName() {
		return getNamespaceInstance().getFullName();
	}
}
