package uk.ac.ic.doc.gander.importing;

final class ImportScheme<O, C, M> implements BindingScheme<M, M> {

	private final C outerImportReceiver;
	private final ImportSimulator.Binder<O, C, M> bindingHandler;

	ImportScheme(C outerImportReceiver,
			ImportSimulator.Binder<O, C, M> bindingHandler) {
		assert outerImportReceiver != null;
		assert bindingHandler != null;
		this.outerImportReceiver = outerImportReceiver;
		this.bindingHandler = bindingHandler;
	}

	public void bindSolitaryToken(M module, String name) {
		bindFirstToken(module, name);
	}

	public void bindFirstToken(M module, String name) {
		bindingHandler.bindModuleToLocalName(module, name, outerImportReceiver);
	}

	public void bindFinalToken(M module, String name, M previouslyLoadedModule) {
		bindingHandler.bindModuleToName(module, name, previouslyLoadedModule);
	}

	public void bindIntermediateToken(M module, String name,
			M previouslyLoadedModule) {
		bindingHandler.bindModuleToName(module, name, previouslyLoadedModule);
	}

}