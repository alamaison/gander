package uk.ac.ic.doc.gander.importing;

final class ImportScheme<O, C, M> implements ModuleBindingScheme<M> {

	private final ImportSimulator.Binder<O, C, M> bindingHandler;
	private final Import<C, M> importInstance;

	ImportScheme(Import<C, M> importInstance,
			ImportSimulator.Binder<O, C, M> bindingHandler) {
		assert importInstance != null;
		assert bindingHandler != null;
		this.importInstance = importInstance;
		this.bindingHandler = bindingHandler;
	}

	public void bindSolitaryToken(M module, String name) {
		bindFirstToken(module, name);
	}

	public void bindFirstToken(M module, String name) {
		if (module != null) {
			bindingHandler.bindModuleToLocalName(module, name,
					importInstance.container());
		} else {
			bindingHandler.onUnresolvedImport(importInstance, name);
		}
	}

	public void bindFinalToken(M module, String name, M previouslyLoadedModule) {
		if (module != null) {
			bindingHandler.bindModuleToName(module, name,
					previouslyLoadedModule);
		} else {
			bindingHandler.onUnresolvedImport(importInstance, name);
		}
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

}