package uk.ac.ic.doc.gander.importing;

final class ImportAsScheme<O, C, M> implements ModuleBindingScheme<M> {

	private final String asName;
	private final ImportSimulator.Binder<O, C, M> bindingHandler;
	private final Import<C, M> importInstance;

	ImportAsScheme(Import<C, M> importInstance, String asName,
			ImportSimulator.Binder<O, C, M> bindingHandler) {
		assert importInstance != null;
		assert !asName.isEmpty();
		assert bindingHandler != null;

		this.importInstance = importInstance;
		this.asName = asName;
		this.bindingHandler = bindingHandler;
	}

	public void bindSolitaryToken(M module, String name) {
		if (module != null) {
			bindingHandler.bindModuleToLocalName(module, asName, importInstance
					.container());
		} else {
			bindingHandler.onUnresolvedImport(importInstance, name);
		}
	}

	public void bindFirstToken(M module, String name) {
	}

	public void bindIntermediateToken(M module, String name,
			M previouslyLoadedModule) {
		if (module != null) {
			bindingHandler.bindModuleToName(module, name,
					previouslyLoadedModule);
		} else {
			bindingHandler.onUnresolvedImport(importInstance, name);
		}
	}

	public void bindFinalToken(M module, String name, M previouslyLoadedModule) {
		if (module != null) {
			bindingHandler.bindModuleToName(module, name,
					previouslyLoadedModule);
			bindingHandler.bindModuleToLocalName(module, asName, importInstance
					.container());
		} else {
			bindingHandler.onUnresolvedImport(importInstance, name);
		}
	}

}