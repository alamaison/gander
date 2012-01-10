package uk.ac.ic.doc.gander.importing;

import uk.ac.ic.doc.gander.importing.ImportSimulator.Binder;

final class StandardImportAsScheme<O, C, M> implements ModuleBindingScheme<M> {

	private final Import<C, M> importInstance;
	private final ImportSimulator.Binder<O, C, M> bindingHandler;

	StandardImportAsScheme(StandardImportAs<C, M> importInstance,
			Binder<O, C, M> bindingHandler) {
		assert importInstance != null;
		assert bindingHandler != null;

		this.importInstance = importInstance;
		this.bindingHandler = bindingHandler;
	}

	public void bindSolitaryToken(M module, String name) {
		if (module != null) {
			bindingHandler.bindModuleToLocalName(module, importInstance
					.specification().bindingName(), importInstance.container());
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
			bindingHandler.bindModuleToLocalName(module, importInstance
					.specification().bindingName(), importInstance.container());
		} else {
			bindingHandler.onUnresolvedImport(importInstance, name);
		}
	}

}