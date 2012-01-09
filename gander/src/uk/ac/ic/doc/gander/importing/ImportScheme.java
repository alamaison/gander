package uk.ac.ic.doc.gander.importing;

final class ImportScheme<O, C, M> implements ModuleBindingScheme<M> {

	private final C outerImportReceiver;
	private final ImportSimulator.Binder<O, C, M> bindingHandler;
	private final ImportInfo importInfo;
	private final M relativeTo;

	ImportScheme(M relativeTo, C outerImportReceiver,
			ImportSimulator.Binder<O, C, M> bindingHandler,
			ImportInfo importInfo) {
		assert outerImportReceiver != null;
		assert bindingHandler != null;
		assert importInfo != null;
		this.relativeTo = relativeTo;
		this.outerImportReceiver = outerImportReceiver;
		this.bindingHandler = bindingHandler;
		this.importInfo = importInfo;
	}

	public void bindSolitaryToken(M module, String name) {
		bindFirstToken(module, name);
	}

	public void bindFirstToken(M module, String name) {
		if (module != null) {
			bindingHandler.bindModuleToLocalName(module, name,
					outerImportReceiver);
		} else {
			bindingHandler.onUnresolvedLocalImport(importInfo, relativeTo,
					name, outerImportReceiver);
		}
	}

	public void bindFinalToken(M module, String name, M previouslyLoadedModule) {
		if (module != null) {
			bindingHandler.bindModuleToName(module, name,
					previouslyLoadedModule);
		} else {
			bindingHandler.onUnresolvedImport(importInfo, relativeTo, name,
					previouslyLoadedModule);
		}
	}

	public void bindIntermediateToken(M module, String name,
			M previouslyLoadedModule) {
		if (module != null) {
			bindingHandler.bindModuleToName(module, name,
					previouslyLoadedModule);
		} else {
			bindingHandler.onUnresolvedImport(importInfo, relativeTo, name,
					previouslyLoadedModule);
		}
	}

}