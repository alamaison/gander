package uk.ac.ic.doc.gander.importing;

final class ImportAsScheme<O, C, M> implements ModuleBindingScheme<M> {

	private final M relativeTo;
	private final C outerImportReceiver;
	private final String asName;
	private final ImportSimulator.Binder<O, C, M> bindingHandler;
	private final ImportInfo importSpec;

	ImportAsScheme(M relativeTo, C outerImportReceiver, String asName,
			ImportSimulator.Binder<O, C, M> bindingHandler,
			ImportInfo importSpec) {
		assert outerImportReceiver != null;
		assert !asName.isEmpty();
		assert bindingHandler != null;
		this.relativeTo = relativeTo;
		this.outerImportReceiver = outerImportReceiver;
		this.asName = asName;
		this.bindingHandler = bindingHandler;
		this.importSpec = importSpec;
	}

	public void bindSolitaryToken(M module, String name) {
		if (module != null) {
			bindingHandler.bindModuleToLocalName(module, asName,
					outerImportReceiver);
		} else {
			bindingHandler.onUnresolvedLocalImport(importSpec, relativeTo,
					name, outerImportReceiver);
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
			bindingHandler.onUnresolvedImport(importSpec, relativeTo, name,
					previouslyLoadedModule);
		}
	}

	public void bindFinalToken(M module, String name, M previouslyLoadedModule) {
		if (module != null) {
			bindingHandler.bindModuleToName(module, name,
					previouslyLoadedModule);
			bindingHandler.bindModuleToLocalName(module, asName,
					outerImportReceiver);
		} else {
			bindingHandler.onUnresolvedLocalImport(importSpec, relativeTo,
					name, outerImportReceiver);
		}
	}

}