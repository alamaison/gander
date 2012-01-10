package uk.ac.ic.doc.gander.importing;

import uk.ac.ic.doc.gander.importing.ImportSimulator.Binder;

final class StandardImportBindingScheme<O, C, M> implements BindingScheme<M> {

	private final ImportSimulator.Binder<O, C, M> bindingHandler;
	private final Import<C, M> importInstance;

	StandardImportBindingScheme(Import<C, M> importInstance,
			Binder<O, C, M> bindingHandler) {
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
			bindingHandler.bindModuleToLocalName(module, name, importInstance
					.container());
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